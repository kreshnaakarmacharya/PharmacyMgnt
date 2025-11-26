package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="shipping_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name="customer_id", nullable = false)
    private long customerId;

    @Column(name="recipient_name", nullable = false)
    private String recipientName;

    @Column(name="recipient_phone_number", nullable = false)
    private String phoneNumber;

    @Column(name="address", nullable = false)
    private String address;
}
