package com.pharmacy.MediNova.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Model.pojos.EpaySuccessObject;
import com.pharmacy.MediNova.Model.pojos.EsewaEpayResponse;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import com.pharmacy.MediNova.Repository.EpayStatusRepo;
import com.pharmacy.MediNova.Repository.MedicineRepository;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.ThreadLocalRandom;

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
    @Autowired
    private EpayStatusRepo epayStatusRepo;

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

    public String getEsewaSignature(String productCode, float totalAmt, String transactionId) throws Exception {

        String totalAmount = "total_amount="+totalAmt;
        String transactionUuid = "transaction_uuid="+transactionId;
        String productCode2 = "product_code="+productCode;

        String message = String.join(",",
                totalAmount,
                transactionUuid,
                productCode2
        );

        String secret = "8gBm/:&EnhH.1/q";
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(),"HmacSHA256");
        sha256_HMAC.init(secret_key);
        String signature = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
        return signature;
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseRecord savePurchase(List<CartItem> cartItems, Long shippingAddressId, MultipartFile prescription) throws Exception{
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
            purchasedMedicine.setRate(item.getMedicine().getPrice());
            purchasedMedicine.setTotalAmt(item.getMedicine().getPrice() * item.getQuantity());

            purchasedMedicineList.add(purchasedMedicine);
        }

        float totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotalPrice();
        }

        float grandTotal = totalAmount;

        String transactionId = customer.getId()+"-"+ThreadLocalRandom.current().nextInt(1, 10_000_000);

        PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setCustomerId(customer.getId());
        purchaseRecord.setShippingAddressId(shippingAddressId);
        purchaseRecord.setMedicines(purchasedMedicineList);
        purchaseRecord.setRequiredPrescription(isPrescriptionRequired);
        purchaseRecord.setTotalAmt(grandTotal);
        purchaseRecord.setPurchaseDateTime(LocalDateTime.now());
        purchaseRecord.setIsPaid(false);
        purchaseRecord.setTransactionId(transactionId);

        String prescriptionImagePath = "";

        if(isPrescriptionRequired){
            prescriptionImagePath = this.savePrescriptionImage(customer.getId(),prescription);
        }
        purchaseRecord.setPrescriptionImg(prescriptionImagePath);
         return this.purchaseRecordRepo.save(purchaseRecord);
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

    public EpaySuccessObject getEpaySuccessObject(Long purchaseRecordId){
        PurchaseRecord purchaseRecord = this.purchaseRecordRepo.findById(purchaseRecordId)
                .orElseThrow(()->new RuntimeException("Order not found"));
        EpayStatus epayStatus = this.epayStatusRepo.findByPurchaseRecordId(purchaseRecordId);

        return new EpaySuccessObject(epayStatus, purchaseRecord);
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
                "Your Prescription has been approved.you can pay total " +
                        "amount of money.Please check your order status. "
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


    public long getTotalOrderCount(){
        return purchaseRecordRepo.count();
    }
    public long countOrderByRequiredPrescriptionTrue(){
        return purchaseRecordRepo.countOrderByRequiredPrescriptionTrue();
    }

    public PurchaseRecord findById(long orderId) {
        return purchaseRecordRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public EpaySuccessObject saveNewEpayStatus(String esewaResponse) throws Exception{
        byte[] decodedBytes = Base64.decodeBase64(esewaResponse);
        String response = new String(decodedBytes, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        EsewaEpayResponse esewaEpayResponse = mapper.readValue(response, EsewaEpayResponse.class);

        PurchaseRecord purchaseRecord = this.purchaseRecordRepo.findByTransactionId(esewaEpayResponse.getTransaction_uuid());
        EpayStatus esewaStatus = null;

        if(purchaseRecord!=null && purchaseRecord.getIsPaid() == true){
            esewaStatus = this.epayStatusRepo.findByPurchaseRecordId(purchaseRecord.getId());
        } else {
            if (esewaEpayResponse.getStatus().equals("COMPLETE")) {
                purchaseRecord.setIsPaid(true);
            }

            esewaStatus = new EpayStatus();
            esewaStatus.setPurchaseRecordId(purchaseRecord.getId());
            esewaStatus.setProductCode(esewaEpayResponse.getProduct_code());
            esewaStatus.setTransactionUuid(esewaEpayResponse.getTransaction_uuid());
            esewaStatus.setTotalAmount(esewaEpayResponse.getTotal_amount());
            esewaStatus.setStatus(esewaEpayResponse.getStatus());
            esewaStatus.setRefId(esewaEpayResponse.getTransaction_code());

            esewaStatus = this.epayStatusRepo.save(esewaStatus);
        }

        EpaySuccessObject esewaSuccessObject = new EpaySuccessObject();
        esewaSuccessObject.setEpayStatus(esewaStatus);
        esewaSuccessObject.setPurchaseRecord(purchaseRecord);

        return esewaSuccessObject;
    }
}
