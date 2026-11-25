
package com.example.bookstore.repository;

import com.example.bookstore.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.book"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.book"})
    @Query("select distinct o from Order o order by o.createdAt desc")
    List<Order> findAllWithDetails();

    @EntityGraph(attributePaths = {"items", "items.book"})
    Optional<Order> findWithItemsById(Long id);

    /** 每个用户的订单数量：[userId, count] */
    @Query("select o.user.id, count(o) from Order o group by o.user.id")
    List<Object[]> countGroupedByUser();
}
