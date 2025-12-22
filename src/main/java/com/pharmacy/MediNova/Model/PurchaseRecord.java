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

    public enum OrderStatus{
        PENDING,
        DELIVERED,
    }
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

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private OrderStatus status;

    @Column( name = "seen_By_Admin", nullable = false)
    private boolean seenByAdmin = false;

    @PrePersist
     void onCreate() {
        this.purchaseDateTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
}
