package com.example.bookstore.service;

import com.example.bookstore.common.OrderStatus;
import com.example.bookstore.common.PaymentMethod;
import com.example.bookstore.dto.OrderDTO;
import com.example.bookstore.dto.OrderItemDTO;
import com.example.bookstore.entity.*;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    // 用户端允许的订单状态流转；PAID 仅用于模拟支付，其余状态由管理员维护
    private static final Map<OrderStatus, Set<OrderStatus>> USER_ALLOWED_STATUS_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.COMPLETED)
    );

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final UserBalanceService userBalanceService;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository,
                       CartItemRepository cartItemRepository, UserRepository userRepository,
                       UserBalanceService userBalanceService, AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.userBalanceService = userBalanceService;
        this.addressRepository = addressRepository;
    }

    /** 购物车下单：明细来自购物车，创建后清空购物车。 */
    @Transactional
    public OrderDTO createOrder(Long userId, String shippingAddress, String phone) {
        User user = requireUser(userId);
        List<OrderItemDTO> items = loadCartAsItems(userId);
        Order savedOrder = createOrderEntity(user, items, shippingAddress, phone);
        cartItemRepository.deleteByUserId(userId);
        return convertToDTO(savedOrder);
    }

    /** 兼容旧接口：与 createOrder 行为一致（创建 PENDING 订单并清空购物车）。 */
    @Transactional
    public OrderDTO createPendingOrder(Long userId, String shippingAddress, String phone) {
        return createOrder(userId, shippingAddress, phone);
    }

    /** 按给定明细创建待支付订单（不清空购物车）。 */
    @Transactional
    public OrderDTO createPendingOrderWithItems(Long userId, String shippingAddress, String phone,
                                                List<OrderItemDTO> items) {
        User user = requireUser(userId);
        Order savedOrder = createOrderEntity(user, items, shippingAddress, phone);
        return convertToDTO(savedOrder);
    }

    /** 直接下单：余额支付当场完成（PAID），其他渠道保持 PENDING 等待支付确认。 */
    @Transactional
    public OrderDTO createOrderWithBalance(Long userId, Long addressId, List<OrderItemDTO> items,
                                           String paymentMethod) {
        User user = requireUser(userId);

        String shippingAddress = null;
        String phone = null;
        if (addressId != null) {
            Address address = addressRepository.findByIdAndUserId(addressId, userId)
                    .orElseThrow(() -> new CustomException("地址不存在", HttpStatus.NOT_FOUND.value()));
            shippingAddress = address.getProvince() + address.getCity() +
                    (address.getDistrict() != null ? address.getDistrict() : "") + address.getDetail();
            phone = address.getPhone();
        }

        Order savedOrder = createOrderEntity(user, items, shippingAddress, phone);

        PaymentMethod method = PaymentMethod.from(paymentMethod);
        if (method == PaymentMethod.BALANCE) {
            boolean success = userBalanceService.deduct(userId, savedOrder.getTotalPrice());
            if (!success) {
                throw new CustomException("余额不足", HttpStatus.BAD_REQUEST.value());
            }
            savedOrder.setStatus(OrderStatus.PAID.name());
            savedOrder = orderRepository.save(savedOrder);
        }

        if (OrderStatus.PAID.name().equals(savedOrder.getStatus())) {
            adjustSalesCount(savedOrder, 1);
        }

        cartItemRepository.deleteByUserId(userId);
        return convertToDTO(savedOrder);
    }

    // ========== 状态流转 ==========

    public List<OrderDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = requireOrder(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权访问此订单", HttpStatus.FORBIDDEN.value());
        }
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Long userId, String status) {
        Order order = requireOrder(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权修改此订单", HttpStatus.FORBIDDEN.value());
        }

        OrderStatus target = parseStatus(status);
        OrderStatus previous = parseStatus(order.getStatus());
        if (target != previous) {
            Set<OrderStatus> allowed = USER_ALLOWED_STATUS_TRANSITIONS.getOrDefault(previous, Set.of());
            if (!allowed.contains(target)) {
                throw new CustomException("非法的订单状态变更", HttpStatus.BAD_REQUEST.value());
            }
        }

        order.setStatus(target.name());
        if (target == OrderStatus.CANCELLED && previous != OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        if (target == OrderStatus.PAID && previous != OrderStatus.PAID) {
            adjustSalesCount(order, 1);
        }
        return convertToDTO(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        Order order = requireOrder(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权删除此订单", HttpStatus.FORBIDDEN.value());
        }

        OrderStatus status = parseStatus(order.getStatus());
        if (status != OrderStatus.PENDING && status != OrderStatus.CANCELLED) {
            throw new CustomException("只能删除待付款或已取消的订单", HttpStatus.BAD_REQUEST.value());
        }
        if (status == OrderStatus.PENDING) {
            restoreStock(order);
        }
        orderRepository.delete(order);
    }

    // ========== 内部实现 ==========

    /** 统一的下单核心：校验数量与库存、按用户折扣计算成交价、扣减库存、落库。 */
    private Order createOrderEntity(User user, List<OrderItemDTO> items, String shippingAddress, String phone) {
        if (items == null || items.isEmpty()) {
            throw new CustomException("订单明细不能为空", HttpStatus.BAD_REQUEST.value());
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setStatus(OrderStatus.PENDING.name());

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : items) {
            validateQuantity(itemDTO.getQuantity());
            Book book = bookRepository.findById(itemDTO.getBookId())
                    .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

            if (book.getStock() < itemDTO.getQuantity()) {
                throw new CustomException("书籍 " + book.getName() + " 库存不足", HttpStatus.BAD_REQUEST.value());
            }

            // 后端根据书籍原价和用户折扣计算成交单价，不信任前端传入的价格
            BigDecimal unitPrice = book.getPrice().multiply(user.getDiscount());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(unitPrice);
            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            book.setStock(book.getStock() - itemDTO.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }

    private List<OrderItemDTO> loadCartAsItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new CustomException("购物车为空", HttpStatus.BAD_REQUEST.value());
        }
        return cartItems.stream()
                .map(cartItem -> new OrderItemDTO(
                        cartItem.getBook().getId(),
                        "",
                        "",
                        cartItem.getQuantity(),
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));
    }

    private Order requireOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new CustomException("订单状态不能为空", HttpStatus.BAD_REQUEST.value());
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException("非法的订单状态", HttpStatus.BAD_REQUEST.value());
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStock(book.getStock() + item.getQuantity());
            bookRepository.save(book);
        }
    }

    private void adjustSalesCount(Order order, int direction) {
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            int current = book.getSalesCount() != null ? book.getSalesCount() : 0;
            int updated = current + direction * item.getQuantity();
            book.setSalesCount(Math.max(0, updated));
            bookRepository.save(book);
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CustomException("商品数量必须大于0", HttpStatus.BAD_REQUEST.value());
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getBook().getId(),
                        item.getBook().getName(),
                        item.getBook().getAuthor(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }
}