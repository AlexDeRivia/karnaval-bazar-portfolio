package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderStatus;

public interface OnlineOrderRepository extends JpaRepository<OnlineOrder, String> {
    List<OnlineOrder> findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            OnlineOrderStatus status, Instant before);

    List<OnlineOrder> findTop100ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
            List<OnlineOrderStatus> statuses, Instant before);

    @Query("select count(o) from OnlineOrder o join o.lines l where l.productId = :productId "
            + "and o.status = :status and o.stockReserved = true")
    long countReservedForProduct(@Param("productId") Long productId,
            @Param("status") OnlineOrderStatus status);
}
