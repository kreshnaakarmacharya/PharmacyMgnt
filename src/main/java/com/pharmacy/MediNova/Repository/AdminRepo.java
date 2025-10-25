package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin,Integer> {
    Admin findByUsernameAndPassword(String username,String password);
}
