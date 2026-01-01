package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.EpayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpayStatusRepo extends JpaRepository<EpayStatus,Integer> {
    EpayStatus findById(long id);
    EpayStatus findByPurchaseRecordId(long purchaseRecordId);
}
