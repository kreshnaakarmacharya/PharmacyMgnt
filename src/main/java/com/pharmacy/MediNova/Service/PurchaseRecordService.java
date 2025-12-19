package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import com.pharmacy.MediNova.Repository.MedicineRepository;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PurchaseRecordService {
    @Autowired
    private PurchaseRecordRepo purchaseRecordRepo;
    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer getCurrentCustomer(){
        CustomCustomerDetails customerDetails= (CustomCustomerDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerDetails.getUser();
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePurchase(List<CartItem> cartItems,Long shippingAddressId) throws Exception{
        Customer customer = getCurrentCustomer();

        boolean isPrescriptionRequired = false;
        List<PurchasedMedicine> purchasedMedicineList = new ArrayList<>();

        PurchasedMedicine purchasedMedicine;

        for(CartItem item : cartItems){
            Medicine medicine = (Medicine) item.getMedicine();

            if(medicine.isRequiredPrescription()){
                isPrescriptionRequired = true;
            }
            // Check stock before decreasing
            if (medicine.getQuantityInStock() < item.getQuantity()) {
                throw new Exception("Not enough stock for medicine: " + medicine.getName());
            }

            // Decrease stock
            medicine.setQuantityInStock(medicine.getQuantityInStock() - item.getQuantity());
            medicineRepository.save(medicine);

            //prepare purchase medicine list
            purchasedMedicine = new PurchasedMedicine();
            purchasedMedicine.setMedicineId(medicine.getId());
            purchasedMedicine.setMedicineName(medicine.getName());
            purchasedMedicine.setQuantity(item.getQuantity());

            purchasedMedicineList.add(purchasedMedicine);
        }

        if(isPrescriptionRequired){
            throw new Exception("Prescription required");
        }
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotalPrice();
        }

        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setCustomerId(customer.getId());
        purchaseRecord.setShippingAddressId(shippingAddressId);
        purchaseRecord.setMedicines(purchasedMedicineList);
        purchaseRecord.setRequiredPrescription(isPrescriptionRequired);
        purchaseRecord.setTotalAmt(totalAmount);
        purchaseRecord.setPurchaseDateTime(LocalDateTime.now());
        this.purchaseRecordRepo.save(purchaseRecord);
    }

    public List<PurchaseRecord> getAllPurchaseRecords(){
        return this.purchaseRecordRepo.findTodayPurchases();
    }

    public String getCustomerNameById(Long customerId){
        return customerRepository.findNameById(customerId);
    }

    public Double getTodaySales() {
        return purchaseRecordRepo.getTodaySales();
    }

    public List<PurchasedMedicine> getMedicinesByPurchaseId(Long purchaseId) {
        PurchaseRecord record = purchaseRecordRepo.findMedicinesByPurchaseRecordId(purchaseId);
        return record.getMedicines();
    }

    public List<PurchaseRecord> getSales(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(23, 59, 59);

        return purchaseRecordRepo.findSalesBetween(from, to);
    }

    public Double getTotalSales(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(23, 59, 59);

        return purchaseRecordRepo.findTotalSalesBetween(from, to);
    }

}
