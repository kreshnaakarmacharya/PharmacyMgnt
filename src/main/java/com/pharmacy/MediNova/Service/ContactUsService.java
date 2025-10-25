package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.ContactUs;
import com.pharmacy.MediNova.Repository.ContactUsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactUsService {
    @Autowired
    private ContactUsRepository contactUsRepository;

    public void addContactUs(ContactUs contactUs){
         contactUsRepository.save(contactUs);
    }

    public List<ContactUs> getContactUs(){
        return contactUsRepository.findAll();
    }
}
