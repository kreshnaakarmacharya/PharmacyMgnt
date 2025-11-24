package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRecordRepo extends JpaRepository<PurchaseRecord,Long>
{
    PurchaseRecord findById(long id);
}
