package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PurchaseRecordService {
    @Autowired
    private PurchaseRecordRepo purchaseRecordRepo;

    private Customer getCurrentCustomer(){
        CustomCustomerDetails customerDetails= (CustomCustomerDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerDetails.getUser();
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePurchase(List<CartItem> cartItems) throws Exception{
        Customer customer = getCurrentCustomer();

        boolean isPrescriptionRequired = false;
        List<PurchasedMedicine> purchasedMedicineList = new ArrayList<>();

        PurchasedMedicine purchasedMedicine;

        for(CartItem item : cartItems){
            Medicine medicine = (Medicine) item.getMedicine();

            if(medicine.isRequiredPrescription()){
                isPrescriptionRequired = true;
            }

            purchasedMedicine = new PurchasedMedicine();
            purchasedMedicine.setMedicineId(medicine.getId());
            purchasedMedicine.setMedicineName(medicine.getName());
            purchasedMedicine.setQuantity(item.getQuantity());

            purchasedMedicineList.add(purchasedMedicine);
        }

        if(isPrescriptionRequired){
            throw new Exception("Prescription required");
        }

        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setCustomer_id(customer.getId());
        purchaseRecord.setMedicines(purchasedMedicineList);
        purchaseRecord.setRequired_prescription(isPrescriptionRequired);

        this.purchaseRecordRepo.save(purchaseRecord);
    }
}
