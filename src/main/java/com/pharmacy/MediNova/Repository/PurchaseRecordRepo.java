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

    @Query("SELECT p FROM PurchaseRecord p WHERE DATE(p.purchase_date_time) = CURRENT_DATE")
    List<PurchaseRecord> findTodayPurchases();

    @Query(
            value = "SELECT COALESCE(SUM(total_amt), 0) AS daily_total_sales\n" +
                    "FROM purchase_record\n" +
                    "WHERE DATE(purchase_date_time) = CURDATE();",
            nativeQuery = true
    )
    Double getTodaySales();

    @Query("SELECT p FROM PurchaseRecord p WHERE p.id = :id")
    PurchaseRecord findMedicinesByPurchaseRecordId(@Param("id") Long id);


}
