package com.pharmacy.MediNova.Model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchasedMedicine {
    private long medicineId;
    private String medicineName;
    private float rate;
    private int quantity;
    private float totalAmt;
}
