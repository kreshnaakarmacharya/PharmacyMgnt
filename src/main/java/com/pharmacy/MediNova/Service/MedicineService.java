package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {
    @Autowired
    private MedicineRepository medicineRepository;

    public void addMedicine(Medicine medicine){

        medicineRepository.save(medicine);
    }
    public Medicine findMedicineById(Long id){

        return medicineRepository.findMedicineById(id);
    }
    public List<Medicine> getAllMedicine(){

        return medicineRepository.findAll();
    }
    public List<Medicine> getAllMedicineAlphabetically() {
        return medicineRepository.findAll(Sort.by("name").ascending());
    }


    public void updateMedicine(Medicine medicine)
    {
        medicineRepository.save(medicine);
    }
    public void deleteMedicineById(Long id){

        medicineRepository.deleteById(id);
    }

    public List<Medicine> getMedicineByName(String medicineName){
       return medicineRepository.findMedicineByName(medicineName);
    }

    public List<Medicine> getMedicineByCategory(String category){
         return medicineRepository.findByCategoryNameIgnoreCase(category);
    }

}
