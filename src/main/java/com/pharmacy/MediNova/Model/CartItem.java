package com.pharmacy.MediNova.Model;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CartItem {

    private Medicine medicine;
    private int quantity;

    public CartItem(Medicine medicine) {
        this.medicine = medicine;
        this.quantity = 1; // default
    }
    //helper method
    public double getTotalPrice() {
        return medicine.getPrice() * quantity;
    }

}
