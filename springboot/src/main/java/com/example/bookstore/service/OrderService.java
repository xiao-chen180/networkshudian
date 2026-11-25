package com.example.bookstore.service;

import com.example.bookstore.dto.CartItemDTO;
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
import java.util.stream.Collectors;

@Service
public class OrderService {

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

    @Transactional
    public OrderDTO createOrder(Long userId, String shippingAddress, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new CustomException("购物车为空", HttpStatus.BAD_REQUEST.value());
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setStatus("PENDING");

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.getStock() < cartItem.getQuantity()) {
                throw new CustomException("书籍 " + book.getName() + " 库存不足", HttpStatus.BAD_REQUEST.value());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(book.getPrice().multiply(user.getDiscount()));
            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(orderItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            book.setStock(book.getStock() - cartItem.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // 下单后清空购物车
        cartItemRepository.deleteByUserId(userId);

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO createPendingOrder(Long userId, String shippingAddress, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new CustomException("购物车为空", HttpStatus.BAD_REQUEST.value());
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setStatus("PENDING");

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.getStock() < cartItem.getQuantity()) {
                throw new CustomException("书籍 " + book.getName() + " 库存不足", HttpStatus.BAD_REQUEST.value());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(book.getPrice().multiply(user.getDiscount()));
            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(orderItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            book.setStock(book.getStock() - cartItem.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // 下单后清空购物车
        cartItemRepository.deleteByUserId(userId);

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO createPendingOrderWithItems(Long userId, String shippingAddress, String phone,
                                                 List<OrderItemDTO> items, BigDecimal totalPrice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setStatus("PENDING");

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : items) {
            Book book = bookRepository.findById(itemDTO.getBookId())
                    .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

            if (book.getStock() < itemDTO.getQuantity()) {
                throw new CustomException("书籍 " + book.getName() + " 库存不足", HttpStatus.BAD_REQUEST.value());
            }

            // 后端根据书籍原价和用户折扣计算实际价格
            BigDecimal unitPrice = book.getPrice().multiply(user.getDiscount());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(unitPrice);
            order.getItems().add(orderItem);

            calculatedTotal = calculatedTotal.add(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            book.setStock(book.getStock() - itemDTO.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(calculatedTotal);
        Order savedOrder = orderRepository.save(order);

        return convertToDTO(savedOrder);
    }

    public List<OrderDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));

        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权访问此订单", HttpStatus.FORBIDDEN.value());
        }

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO createOrderWithBalance(Long userId, Long addressId, List<OrderItemDTO> items, BigDecimal totalPrice, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PAID");

        if (addressId != null) {
            Address address = addressRepository.findByIdAndUserId(addressId, userId)
                    .orElseThrow(() -> new CustomException("地址不存在", HttpStatus.NOT_FOUND.value()));
            String shippingAddress = address.getProvince() + address.getCity() +
                    (address.getDistrict() != null ? address.getDistrict() : "") + address.getDetail();
            order.setShippingAddress(shippingAddress);
            order.setPhone(address.getPhone());
        }

        // 后端计算实际总价
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : items) {
            Book book = bookRepository.findById(itemDTO.getBookId())
                    .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

            if (book.getStock() < itemDTO.getQuantity()) {
                throw new CustomException("书籍 " + book.getName() + " 库存不足", HttpStatus.BAD_REQUEST.value());
            }

            // 后端根据书籍原价和用户折扣计算实际价格
            BigDecimal unitPrice = book.getPrice().multiply(user.getDiscount());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(unitPrice);
            order.getItems().add(orderItem);

            calculatedTotal = calculatedTotal.add(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            book.setStock(book.getStock() - itemDTO.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(calculatedTotal);

        if ("balance".equals(paymentMethod)) {
            boolean success = userBalanceService.deduct(userId, calculatedTotal);
            if (!success) {
                throw new CustomException("余额不足", HttpStatus.BAD_REQUEST.value());
            }
        }

        Order savedOrder = orderRepository.save(order);

        // 下单成功后清空购物车
        cartItemRepository.deleteByUserId(userId);

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Long userId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));

        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权修改此订单", HttpStatus.FORBIDDEN.value());
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));

        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权删除此订单", HttpStatus.FORBIDDEN.value());
        }

        orderRepository.delete(order);
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