package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.ShippingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingDetailsRepo extends JpaRepository<ShippingDetails, Long> {
    List<ShippingDetails> findByCustomerId(long customerId);
    ShippingDetails findShippingDetailsById(Long id);
}
