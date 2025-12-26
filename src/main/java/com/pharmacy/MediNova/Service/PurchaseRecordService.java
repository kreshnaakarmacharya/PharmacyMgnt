package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import com.pharmacy.MediNova.Repository.MedicineRepository;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseRecordService {
    @Autowired
    private PurchaseRecordRepo purchaseRecordRepo;
    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CustomerRepository customerRepository;


    @Autowired
    private NotificationSevice notificationSevice;

    private Customer getCurrentCustomer(){
        CustomCustomerDetails customerDetails= (CustomCustomerDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerDetails.getUser();
    }

    private String savePrescriptionImage(long customerId, MultipartFile file) throws IOException {
        if(file != null && !file.isEmpty()){
            String uploadDir = "prescriptionImages"+ File.separator+customerId;

            Path path =  Paths.get(uploadDir);

            if(!Files.exists(path)){
                Files.createDirectories(path);
            }

            // Generate safe unique file name
            String originalFileName = file.getOriginalFilename();

            String fileName = UUID.randomUUID() + originalFileName;

            // Save file
            Path filePath = path.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return uploadDir+File.separator+fileName;
        } else {
            return "";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePurchase(List<CartItem> cartItems, Long shippingAddressId, MultipartFile prescription) throws Exception{
        Customer customer = getCurrentCustomer();

        boolean isPrescriptionRequired = (prescription != null && !prescription.isEmpty());
        List<PurchasedMedicine> purchasedMedicineList = new ArrayList<>();

        PurchasedMedicine purchasedMedicine;

        for(CartItem item : cartItems){
            Medicine medicine = (Medicine) item.getMedicine();

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

        String prescriptionImagePath = "";

        if(isPrescriptionRequired){
            prescriptionImagePath = this.savePrescriptionImage(customer.getId(),prescription);
        }
        purchaseRecord.setPrescriptionImg(prescriptionImagePath);
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

    public List<PurchaseRecord> findAllLatestFirstNonPrescribed() {
        return purchaseRecordRepo.findByRequiredPrescriptionFalseOrderByPurchaseDateTimeDesc();
    }
    public List<PurchaseRecord> findAllLatestFirstPrescribed() {
        return purchaseRecordRepo.findByRequiredPrescriptionTrueOrderByPurchaseDateTimeDesc();
    }

    public void markApproved(Long orderId){
        PurchaseRecord record=purchaseRecordRepo.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order not found"));
        record.setStatus(PurchaseRecord.OrderStatus.APPROVED);
        purchaseRecordRepo.save(record);

        long customerId=record.getCustomerId();
        notificationSevice.send(customerId,orderId,
                "Prescription Approved",
                "Your Prescription has been approved.you can pay total amount of money"
        ,"APPROVED");
    }
    public void markRejected(Long orderId){
        PurchaseRecord record=purchaseRecordRepo.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order not found"));
        record.setStatus(PurchaseRecord.OrderStatus.REJECTED);
        purchaseRecordRepo.save(record);

        long customerId=record.getCustomerId();
        notificationSevice.send(customerId,orderId,
                "Prescription Rejected",
                "Your Prescription has been rejected."
                ,"APPROVED");
    }


    @Transactional
    public void markDispatched(Long orderId) {

        // 1. Fetch order
        PurchaseRecord record = purchaseRecordRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Update status
        record.setStatus(PurchaseRecord.OrderStatus.DISPATCHED);
        purchaseRecordRepo.save(record);

        // 3. Send notification
        long customerId = record.getCustomerId(); // or getCustomerId() if field exists
        notificationSevice.send(
                customerId,
                record.getId(),
                "Order Dispatched",
                "Your order #" + record.getId() + " has been dispatched.",
                "ORDER"
        );
    }


    public void markDelivered(Long orderId) {
        // 1. Fetch order
        PurchaseRecord record = purchaseRecordRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Update status
        record.setStatus(PurchaseRecord.OrderStatus.DELIVERED);
        purchaseRecordRepo.save(record);

        // 3. Send notification
        long customerId = record.getCustomerId(); // or getCustomerId() if field exists
        notificationSevice.send(
                customerId,
                record.getId(),
                "Order Delivered",
                "Your order has been delivered.",
                "ORDER"
        );
    }

    public List<PurchaseRecord> getOrderCustomerById(long customerId){
        return purchaseRecordRepo.findOrderByCustomerId(customerId);
    }

    public long countUnseenOrders() {
        return purchaseRecordRepo.countBySeenByAdminFalse();
    }

    @Transactional
    public void markOrdersAsSeenByAdmin() {
        List<PurchaseRecord> unseen =
                purchaseRecordRepo.findAll()
                        .stream()
                        .filter(r -> !r.isSeenByAdmin())
                        .toList();

        unseen.forEach(r -> r.setSeenByAdmin(true));
    }


}
