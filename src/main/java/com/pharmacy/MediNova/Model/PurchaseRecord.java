package com.pharmacy.MediNova.Model;

import com.pharmacy.MediNova.Converter.MedicineListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="purchase_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchaseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name="id")
    private long id;

    @Column(name="customer_id")
    private long customerId;

    @Column(name = "shipping_address_id", nullable = false)
    private long shippingAddressId;

    @Convert(converter = MedicineListConverter.class)
    @Column(name = "purchased_medicine", columnDefinition = "JSON")
    private List<PurchasedMedicine> medicines;

    @Column(name="required_prescription")
    private boolean requiredPrescription;

    @Column(name="prescription_img")
    private String prescriptionImg;

    @Column(name="purchase_date_time")
    private LocalDateTime purchaseDateTime;

    @Column(name="total_amt")
    private Double totalAmt;

    @PrePersist
    void createdAt() {
        this.purchaseDateTime = LocalDateTime.now();
    }
}
