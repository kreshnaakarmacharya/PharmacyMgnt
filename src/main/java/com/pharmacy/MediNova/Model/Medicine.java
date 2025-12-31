package com.pharmacy.MediNova.Model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Entity
@Table(name = "medicinedetails")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "dosage_form")
    private String dosageForm;

    @Column(name = "strength")
    private String strength;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "batch_number")
    private String batchNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity_in_stock")
    private int quantityInStock;

    @Column(name = "price")
    private float price;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name="required_prescription")
    private boolean requiredPrescription;

    //This method run automatically just before data is saved into db
    @PrePersist
    @PreUpdate
    public void setDefaultPrescriptionRequirement(){
        if("ka".equalsIgnoreCase(categoryName) || "kha".equalsIgnoreCase(categoryName)){
            this.requiredPrescription = true;
        }
        else{
            this.requiredPrescription = false;
        }
    }
}
