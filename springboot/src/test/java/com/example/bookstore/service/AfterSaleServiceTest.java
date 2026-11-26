package com.example.bookstore.service;

import com.example.bookstore.dto.AfterSaleRequest;
import com.example.bookstore.entity.AfterSale;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.AfterSaleRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 售后工单业务规则测试：可申请状态、重复提交、审核状态机。
 */
class AfterSaleServiceTest {

    private AfterSaleRepository afterSaleRepository;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private MessageService messageService;
    private AfterSaleService service;

    private static final long USER_ID = 21L;
    private static final long ORDER_ID = 100L;

    @BeforeEach
    void setUp() {
        afterSaleRepository = mock(AfterSaleRepository.class);
        orderRepository = mock(OrderRepository.class);
        userRepository = mock(UserRepository.class);
        messageService = mock(MessageService.class);
        service = new AfterSaleService(afterSaleRepository, orderRepository, userRepository, messageService);

        when(afterSaleRepository.save(any(AfterSale.class))).thenAnswer(inv -> {
            AfterSale a = inv.getArgument(0);
            if (a.getId() == null) a.setId(1L);
            return a;
        });
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(orderRepository.findWithItemsById(anyLong())).thenReturn(Optional.empty());
    }

    private Order orderWithStatus(long id, long userId, String status) {
        User user = new User();
        user.setId(userId);
        user.setUsername("林晚");

        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("98.00"));
        return order;
    }

    @Test
    @DisplayName("已付款订单可以申请售后，并写入待审核状态与退款金额")
    void shouldAllowApplyForPaidOrder() {
        when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(orderWithStatus(ORDER_ID, USER_ID, "PAID")));
        when(afterSaleRepository.findByOrderIdAndStatus(ORDER_ID, "PENDING")).thenReturn(List.of());

        var dto = service.apply(USER_ID, new AfterSaleRequest(ORDER_ID, "return", "封面破损", "书脊有压痕"));

        assertThat(dto.getStatus()).isEqualTo("PENDING");
        assertThat(dto.getType()).isEqualTo("RETURN");
        assertThat(dto.getTypeText()).isEqualTo("退货退款");
        assertThat(dto.getRefundAmount()).isEqualByComparingTo("98.00");
        verify(messageService).sendMessage(eq(USER_ID), anyString(), anyString(), eq("ORDER"));
    }

    @Test
    @DisplayName("待付款/已取消订单不能申请售后")
    void shouldRejectNotEligibleOrderStatus() {
        when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(orderWithStatus(ORDER_ID, USER_ID, "PENDING")));

        assertThatThrownBy(() -> service.apply(USER_ID, new AfterSaleRequest(ORDER_ID, "REFUND", "", "")))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("不支持申请售后");
    }

    @Test
    @DisplayName("同一订单已有待处理售后时不能重复提交")
    void shouldRejectDuplicatePending() {
        when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(orderWithStatus(ORDER_ID, USER_ID, "SHIPPED")));
        AfterSale existing = new AfterSale();
        existing.setId(9L);
        when(afterSaleRepository.findByOrderIdAndStatus(ORDER_ID, "PENDING")).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.apply(USER_ID, new AfterSaleRequest(ORDER_ID, "RETURN", "", "")))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("已有待处理");
    }

    @Test
    @DisplayName("非法售后类型被拒绝")
    void shouldRejectUnknownType() {
        when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(orderWithStatus(ORDER_ID, USER_ID, "PAID")));

        assertThatThrownBy(() -> service.apply(USER_ID, new AfterSaleRequest(ORDER_ID, "INVALID", "", "")))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("不支持的售后类型");
    }

    @Test
    @DisplayName("审核状态机：PENDING→APPROVED→COMPLETED 合法，PENDING→COMPLETED 非法")
    void shouldEnforceHandleStateMachine() {
        AfterSale pending = new AfterSale();
        pending.setId(5L);
        pending.setUserId(USER_ID);
        pending.setOrderId(ORDER_ID);
        pending.setStatus("PENDING");
        pending.setType("RETURN");
        when(afterSaleRepository.findById(5L)).thenReturn(Optional.of(pending));

        // 直接完成：非法
        assertThatThrownBy(() -> service.handle(5L, "COMPLETED", ""))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("不允许此操作");

        // 先通过再完成：合法
        var approved = service.handle(5L, "APPROVED", "同意退货");
        assertThat(approved.getStatus()).isEqualTo("APPROVED");
        assertThat(approved.getAdminRemark()).isEqualTo("同意退货");
        verify(messageService, atLeastOnce()).sendMessage(eq(USER_ID), anyString(), anyString(), eq("ORDER"));

        var completed = service.handle(5L, "COMPLETED", "已收到退货");
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("管理员处理不存在的售后单返回业务异常")
    void shouldThrowWhenAfterSaleMissing() {
        when(afterSaleRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handle(404L, "APPROVED", ""))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("售后单不存在");
    }
}