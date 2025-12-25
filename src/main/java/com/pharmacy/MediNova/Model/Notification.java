package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long orderId;

    private String title;
    private String message;
    private String type; // ORDER / PAYMENT

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

}

