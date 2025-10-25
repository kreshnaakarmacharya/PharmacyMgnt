package com.pharmacy.MediNova.Repository;

import com.pharmacy.MediNova.Model.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs,Long> {
}
