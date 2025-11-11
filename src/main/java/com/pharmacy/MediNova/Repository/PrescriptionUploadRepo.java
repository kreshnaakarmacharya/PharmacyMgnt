package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.PrescriptionUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionUploadRepo  extends JpaRepository<PrescriptionUpload,Integer> {
    // Fetch all prescriptions uploaded by a customer
    List<PrescriptionUpload> findByCustomerId(long customerId);

    // Optional: fetch all pending prescriptions for admin
    List<PrescriptionUpload> findByStatus(PrescriptionUpload.OrderStatus status);
}
