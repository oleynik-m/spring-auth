package com.example.dlg.repository;

import com.example.dlg.domain.Orders;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Orders entity.
 */
@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    default Optional<Orders> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Orders> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Orders> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct orders from Orders orders left join fetch orders.products left join fetch orders.people",
        countQuery = "select count(distinct orders) from Orders orders"
    )
    Page<Orders> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct orders from Orders orders left join fetch orders.products left join fetch orders.people")
    List<Orders> findAllWithToOneRelationships();

    @Query("select orders from Orders orders left join fetch orders.products left join fetch orders.people where orders.id =:id")
    Optional<Orders> findOneWithToOneRelationships(@Param("id") Long id);
}
