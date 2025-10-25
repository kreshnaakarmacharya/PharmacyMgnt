package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine,Long> {
    Medicine findMedicineById(Long id);
    List<Medicine> findMedicineByName(String medicineName);
    List<Medicine> findByCategoryNameIgnoreCase(String categoryName);
}
