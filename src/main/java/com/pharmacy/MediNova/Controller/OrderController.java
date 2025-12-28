package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Service.PurchaseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @GetMapping("/nonPrescribeMedicineOrderDetails")
    public String nonPrescribedMedicineOrderDetails(Model model) {
        purchaseRecordService.markOrdersAsSeenByAdmin();
        List<PurchaseRecord> allRecords = purchaseRecordService.findAllLatestFirstNonPrescribed();

        // Map purchase record id -> customer name
        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord record : allRecords) {
            String name = purchaseRecordService.getCustomerNameById(record.getCustomerId());
            customerNames.put(record.getId(), name);
        }

        model.addAttribute("purchaseRecord", allRecords);
        model.addAttribute("customerNames", customerNames);
        return "Admin/NonPrescribedOrder";
    }

    @GetMapping("/prescribedMedicineOrderDetails")
    public String prescribedMedicineOrderDetails(Model model) {
        purchaseRecordService.markOrdersAsSeenByAdmin();
        List<PurchaseRecord> allRecords = purchaseRecordService.findAllLatestFirstPrescribed();

        // Map purchase record id -> customer name
        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord record : allRecords) {
            String name = purchaseRecordService.getCustomerNameById(record.getCustomerId());
            customerNames.put(record.getId(), name);
        }

        model.addAttribute("purchaseRecord", allRecords);
        model.addAttribute("customerNames", customerNames);
        return "Admin/PrescribedOrder";
    }

    @GetMapping("/prescription/view/{customerId}/{filename}")
    public ResponseEntity<Resource> viewPrescription(
            @PathVariable("customerId") long customerId,
            @PathVariable("filename") String filename) {

        try {
            Path file = Paths.get("prescriptionImages")
                    .resolve(String.valueOf(customerId))
                    .resolve(filename)
                    .normalize();

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            MediaType mediaType= MediaTypeFactory.getMediaType(file.toString()).
                    orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
