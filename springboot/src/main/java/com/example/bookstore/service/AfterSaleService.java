package com.example.bookstore.service;

import com.example.bookstore.dto.AfterSaleDTO;
import com.example.bookstore.dto.AfterSaleRequest;
import com.example.bookstore.entity.AfterSale;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.AfterSaleRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AfterSaleService {

    /** 只有已付款之后的订单才能申请售后 */
    private static final Set<String> APPLICABLE_STATUS = Set.of("PAID", "SHIPPED", "COMPLETED");

    private static final Map<String, String> TYPE_TEXT = Map.of(
            "REFUND", "仅退款",
            "RETURN", "退货退款",
            "EXCHANGE", "换货"
    );

    private static final Map<String, String> STATUS_TEXT = Map.of(
            "PENDING", "待审核",
            "APPROVED", "已通过",
            "REJECTED", "已驳回",
            "COMPLETED", "已完成"
    );

    private final AfterSaleRepository afterSaleRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public AfterSaleService(AfterSaleRepository afterSaleRepository, OrderRepository orderRepository,
                            UserRepository userRepository, MessageService messageService) {
        this.afterSaleRepository = afterSaleRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    @Transactional
    public AfterSaleDTO apply(Long userId, AfterSaleRequest req) {
        String type = req.type() == null ? "" : req.type().trim().toUpperCase();
        if (!TYPE_TEXT.containsKey(type)) {
            throw new CustomException("不支持的售后类型", HttpStatus.BAD_REQUEST.value());
        }

        Order order = orderRepository.findWithItemsById(req.orderId())
                .orElseThrow(() -> new CustomException("订单不存在", HttpStatus.NOT_FOUND.value()));
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException("无权操作此订单", HttpStatus.FORBIDDEN.value());
        }
        if (!APPLICABLE_STATUS.contains(order.getStatus())) {
            throw new CustomException("当前订单状态不支持申请售后", HttpStatus.BAD_REQUEST.value());
        }
        if (!afterSaleRepository.findByOrderIdAndStatus(order.getId(), "PENDING").isEmpty()) {
            throw new CustomException("该订单已有待处理的售后申请", HttpStatus.BAD_REQUEST.value());
        }

        AfterSale afterSale = new AfterSale();
        afterSale.setUserId(userId);
        afterSale.setOrderId(order.getId());
        afterSale.setType(type);
        afterSale.setReason(req.reason());
        afterSale.setDescription(req.description());
        afterSale.setStatus("PENDING");
        afterSale.setRefundAmount(order.getTotalPrice());
        afterSaleRepository.save(afterSale);

        messageService.sendMessage(userId, "售后申请已提交",
                "订单 " + orderNo(order.getId()) + " 的「" + TYPE_TEXT.get(type)
                        + "」申请已提交，客服会在 24 小时内处理。", "ORDER");

        return toDTO(afterSale);
    }

    public List<AfterSaleDTO> getMyAfterSales(Long userId) {
        return afterSaleRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public AfterSaleDTO getById(Long id, Long userId) {
        AfterSale afterSale = afterSaleRepository.findById(id)
                .orElseThrow(() -> new CustomException("售后单不存在", HttpStatus.NOT_FOUND.value()));
        if (!afterSale.getUserId().equals(userId)) {
            throw new CustomException("无权查看此售后单", HttpStatus.FORBIDDEN.value());
        }
        return toDTO(afterSale);
    }

    public List<AfterSaleDTO> getAll() {
        return afterSaleRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public long countPending() {
        return afterSaleRepository.countByStatus("PENDING");
    }

    /** 管理员审核：APPROVED / REJECTED，已通过的工单可再次置为 COMPLETED */
    @Transactional
    public AfterSaleDTO handle(Long afterSaleId, String status, String adminRemark) {
        AfterSale afterSale = afterSaleRepository.findById(afterSaleId)
                .orElseThrow(() -> new CustomException("售后单不存在", HttpStatus.NOT_FOUND.value()));

        String target = status == null ? "" : status.trim().toUpperCase();
        if (!Set.of("APPROVED", "REJECTED", "COMPLETED").contains(target)) {
            throw new CustomException("非法的处理状态", HttpStatus.BAD_REQUEST.value());
        }
        String current = afterSale.getStatus();
        boolean allowed = ("PENDING".equals(current) && ("APPROVED".equals(target) || "REJECTED".equals(target)))
                || ("APPROVED".equals(current) && "COMPLETED".equals(target));
        if (!allowed) {
            throw new CustomException("当前售后单状态不允许此操作", HttpStatus.BAD_REQUEST.value());
        }

        afterSale.setStatus(target);
        afterSale.setAdminRemark(adminRemark);
        afterSale.setHandledAt(java.time.LocalDateTime.now());
        afterSaleRepository.save(afterSale);

        String title = switch (target) {
            case "APPROVED" -> "售后申请已通过";
            case "REJECTED" -> "售后申请被驳回";
            default -> "售后已完成";
        };
        String content = "订单 " + orderNo(afterSale.getOrderId()) + " 的售后申请" + STATUS_TEXT.get(target)
                + (adminRemark != null && !adminRemark.isBlank() ? "，说明：" + adminRemark : "。");
        messageService.sendMessage(afterSale.getUserId(), title, content, "ORDER");

        return toDTO(afterSale);
    }

    private String orderNo(Long orderId) {
        return "SH" + String.format("%08d", orderId);
    }

    private AfterSaleDTO toDTO(AfterSale afterSale) {
        AfterSaleDTO dto = new AfterSaleDTO();
        dto.setId(afterSale.getId());
        dto.setOrderId(afterSale.getOrderId());
        dto.setOrderNo(orderNo(afterSale.getOrderId()));
        dto.setType(afterSale.getType());
        dto.setTypeText(TYPE_TEXT.getOrDefault(afterSale.getType(), afterSale.getType()));
        dto.setReason(afterSale.getReason());
        dto.setDescription(afterSale.getDescription());
        dto.setStatus(afterSale.getStatus());
        dto.setStatusText(STATUS_TEXT.getOrDefault(afterSale.getStatus(), afterSale.getStatus()));
        dto.setAdminRemark(afterSale.getAdminRemark());
        dto.setRefundAmount(afterSale.getRefundAmount());
        dto.setCreatedAt(afterSale.getCreatedAt());
        dto.setHandledAt(afterSale.getHandledAt());

        userRepository.findById(afterSale.getUserId())
                .map(User::getUsername)
                .ifPresent(dto::setUsername);

        orderRepository.findWithItemsById(afterSale.getOrderId()).ifPresent(order -> {
            List<OrderItem> items = order.getItems();
            if (!items.isEmpty()) {
                dto.setBookName(items.get(0).getBook().getName());
                dto.setQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
            }
        });

        return dto;
    }
}