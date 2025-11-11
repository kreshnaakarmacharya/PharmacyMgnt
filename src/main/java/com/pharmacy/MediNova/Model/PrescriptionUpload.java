package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Collate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "prescriptionUpload")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PrescriptionUpload {
    public enum OrderStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private long id;

    @Column(name="customer_id")
    private long customerId;

    @Column(name = "medicine_id")
    private long medicineId;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate=LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private OrderStatus status;

    @PrePersist
    public void setDefaultStatus(){
        if(status==null){
            status=OrderStatus.PENDING;
        }
    }
}
