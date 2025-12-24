package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/medicine")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;

    @GetMapping("/addMedicine")
    public String getAddMedicine(Model model){
        return "Admin/MedicineForm";
    }

    @PostMapping("/addMedicineForm")
    @ResponseBody
    public Map<String,String> addMedicine(@ModelAttribute Medicine medicine,
                                          @RequestParam("image") MultipartFile image,
                                          Model model ) {

        Map<String,String> response = new HashMap<>();

        try {
            if (!image.isEmpty()) {
                long sizeInKB = image.getSize() / 1024;

                if (sizeInKB > 200) {
                    response.put("status", "error");
                    response.put("message", "Error: Image size must be less than 200KB");
                    return response;
                }
                String fileName = image.getOriginalFilename();
                Files.copy(image.getInputStream(), Path.of("src\\main\\resources\\static\\images\\" + image.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);

                // Save relative URL in DB
                medicine.setImageUrl("/images/" + fileName);
            }

            // Save medicine record in DB
            medicineService.addMedicine(medicine);

            response.put("status", "success");
            response.put("message", "Medicine added successfully");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Failed to add medicine: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/view/{id}")
    public String viewMedicineById(@PathVariable("id") Long id, Model model) {
        Medicine med = medicineService.findMedicineById(id);
        model.addAttribute("medicine", med);
        return "Admin/ViewMedicineDetails";
    }

    @GetMapping("/backToList")
    public String backToMedicineList(){
        return "redirect:/medicineDashboard";
    }

    @GetMapping("/editMedicine/{id}")
    public String editMedicine(@PathVariable("id") Long id,Model model){
        Medicine med = medicineService.findMedicineById(id);
        model.addAttribute("medicine", med);
        return "Admin/EditMedicine";
    }

    @PostMapping("/updateMedicine")
    @ResponseBody
    public Map<String,String > updateMedicine(@ModelAttribute Medicine updateMedicine,
                                                @RequestParam("image") MultipartFile image ){
        Medicine updatemed=medicineService.findMedicineById(updateMedicine.getId());
        updatemed.setName(updateMedicine.getName());
        updatemed.setBrandName(updateMedicine.getBrandName());
        updatemed.setCategoryName(updateMedicine.getCategoryName());
        updatemed.setBatchNumber(updateMedicine.getBatchNumber());
        updatemed.setDosageForm(updateMedicine.getDosageForm());
        updatemed.setStrength(updateMedicine.getStrength());
        updatemed.setManufacturer(updateMedicine.getManufacturer());
        updatemed.setManufactureDate(updateMedicine.getManufactureDate());
        updatemed.setExpiryDate(updateMedicine.getExpiryDate());
        updatemed.setPrice(updateMedicine.getPrice());
        updatemed.setQuantityInStock(updateMedicine.getQuantityInStock());
        updatemed.setDescription(updateMedicine.getDescription());
        if (image != null && !image.isEmpty()) {
            try {
                // Delete old image if exists
                if (updatemed.getImageUrl() != null) {
                    Path oldImagePath = Path.of("src\\main\\resources\\static\\images\\"+ updatemed.getImageUrl());
                    Files.deleteIfExists(oldImagePath);
                }

                // Save new image
                String filename = image.getOriginalFilename();
                Path newImagePath = Path.of("src/main/resources/static/images/" + filename);
                Files.copy(image.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);

                // Update DB
                updatemed.setImageUrl("/images/"+filename);

            } catch (IOException e) {
                e.printStackTrace();
                // Optionally, return error response if using AJAX
            }
        }

        medicineService.updateMedicine(updatemed);
        Map<String,String> response=new HashMap<>();
        response.put("status","success");
        response.put("message","Medicine updated sucessfully");
        return response;
    }

    @DeleteMapping("/deleteMedicine/{id}")
    @ResponseBody
    public Map<String,String> deleteMedicine(@PathVariable Long id){
        medicineService.deleteMedicineById(id);
        Map<String,String> response=new HashMap<>();
        response.put("status","success");
        response.put("message","Medicine deleted successfully");
        return response;
    }

    @GetMapping("/searchMedicine")
    public String searchMedicine(@RequestParam(value="keyword",required = false) String keyword,Model model){
        List<Medicine> medicine;
        if(keyword !=null && !keyword.isEmpty()){
            medicine=medicineService.getMedicineByName(keyword);
        }
        else{
            medicine=medicineService.getAllMedicine();
        }
        model.addAttribute("searchMedicines",medicine);
        model.addAttribute("keyword",keyword);
        return "Admin/SearchMedicinePage";
    }

    @GetMapping("/filter/{category}")
    public String filterByCategory(@PathVariable String category, Model model) {
        List<Medicine> filtered;

        if (category.equalsIgnoreCase("all")) {
            filtered = medicineService.getAllMedicine();
        } else {
            filtered = medicineService.getMedicineByCategory(category);
        }

        model.addAttribute("medList", filtered);
        return "fragments/medicine-list :: list"; // returns only the medicine list fragment
    }




}
