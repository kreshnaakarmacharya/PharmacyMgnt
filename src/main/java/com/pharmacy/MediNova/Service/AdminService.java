package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Admin;
import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Repository.AdminRepo;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    @Autowired
    private AdminRepo adminRepo;
    public Admin getAdmAdminByUsernameAndPassword(String username, String password){
        return adminRepo.findByUsernameAndPassword(username,password);
    }
}
