package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "epay_status")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class EpayStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "purchase_record_id")
    private long purchaseRecordId;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_uuid")
    private String transactionUuid;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "ref_id")
    private String refId;

    @Column(name = "total_amount")
    private float totalAmount;
}
