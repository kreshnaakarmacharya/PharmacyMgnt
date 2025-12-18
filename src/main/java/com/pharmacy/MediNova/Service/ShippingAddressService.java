package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.ShippingDetails;
import com.pharmacy.MediNova.Repository.ShippingDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

@Service
public class ShippingAddressService {
    @Autowired
    private ShippingDetailsRepo shippingDetailsRepo;
    public void addShippingAddress(ShippingDetails shippingDetails){
        shippingDetailsRepo.save(shippingDetails);
    }

    public ShippingDetails findShippingAddressById(Long id){
        return shippingDetailsRepo.findShippingDetailsById(id);
    }

    public void updateShippingAddress(ShippingDetails shippingDetails){
        shippingDetailsRepo.save(shippingDetails);
    }

    public void deleteShippingAddress(Long id){
        shippingDetailsRepo.deleteById(id);
    }


}
