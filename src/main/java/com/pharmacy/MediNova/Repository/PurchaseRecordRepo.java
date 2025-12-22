package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Model.PurchasedMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRecordRepo extends JpaRepository<PurchaseRecord,Long>
{
    PurchaseRecord findById(long id);

    //show the list of medicine purchased today only in admin
    @Query("SELECT p FROM PurchaseRecord p WHERE DATE(p.purchaseDateTime) = CURRENT_DATE")
    List<PurchaseRecord> findTodayPurchases();

    //get total amount of sales and show in admin
    @Query(
            value = "SELECT COALESCE(SUM(total_amt), 0) AS daily_total_sales\n" +
                    "FROM purchase_record\n" +
                    "WHERE DATE(purchase_date_time) = CURDATE();",
            nativeQuery = true
    )
    Double getTodaySales();

    //View medicines purchased by customer in view details in sales statement
    @Query("SELECT p FROM PurchaseRecord p WHERE p.id = :id")
    PurchaseRecord findMedicinesByPurchaseRecordId(@Param("id") Long id);

    @Query("""
        SELECT pr
        FROM PurchaseRecord pr
        WHERE pr.purchaseDateTime BETWEEN :from AND :to
    """)
    List<PurchaseRecord> findSalesBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT SUM(pr.totalAmt)
        FROM PurchaseRecord pr
        WHERE pr.purchaseDateTime BETWEEN :from AND :to
    """)
    Double findTotalSalesBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<PurchaseRecord> findOrderByCustomerId(long id);

    long countBySeenByAdminFalse();

}
