package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long>
{
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
