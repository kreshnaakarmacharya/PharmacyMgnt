package com.pharmacy.MediNova.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.MediNova.Model.PurchasedMedicine;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
@Converter
public class MedicineListConverter implements AttributeConverter<List<PurchasedMedicine>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<PurchasedMedicine> medicineList) {
            try {
                return objectMapper.writeValueAsString(medicineList);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public List<PurchasedMedicine> convertToEntityAttribute(String json) {
        try{
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PurchasedMedicine.class)
                    );
        }
        catch (Exception e){
            throw new RuntimeException("Could not convert JSON to list", e);
        }
    }
}
