package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Admin;
import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Repository.AdminRepo;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminService {
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private PurchaseRecordRepo purchaseRecordRepo;

    public Admin getAdmAdminByUsernameAndPassword(String username, String password){
        return adminRepo.findByUsernameAndPassword(username,password);
    }

    private String getCorrespondingMonth(int intMonth){
        Map<Integer, String> intToStringMonth = new HashMap<>();
        intToStringMonth.put(1, "JAN");
        intToStringMonth.put(2, "FEB");
        intToStringMonth.put(3, "MAR");
        intToStringMonth.put(4, "APR");
        intToStringMonth.put(5, "MAY");
        intToStringMonth.put(6, "JUN");
        intToStringMonth.put(7, "JUL");
        intToStringMonth.put(8, "AUG");
        intToStringMonth.put(9, "SEP");
        intToStringMonth.put(10, "OCT");
        intToStringMonth.put(11, "NOV");
        intToStringMonth.put(12, "DEC");

        return intToStringMonth.get(intMonth);
    }

    public Map<String, Object> getMonthlySalesCount(){
        Map<String, Object> resultMap = new HashMap<>();
        LocalDateTime startOfYear = LocalDateTime.now()
                .withDayOfYear(1)
                .with(LocalTime.MIN);

        LocalDateTime endOfYear = LocalDateTime.now()
                .withMonth(12)
                .withDayOfMonth(31)
                .with(LocalTime.MAX);


        List<Object[]> result = this.purchaseRecordRepo.findMonthlySalesRecordCount(startOfYear,endOfYear);

        List<String> months = new ArrayList<>();
        List<Long> records = new ArrayList<>();

        for(Object[] obj : result){
            months.add(this.getCorrespondingMonth((Integer)obj[0]));
            records.add((Long)obj[1]);
        }

        resultMap.put("months", months);
        resultMap.put("records", records);
        return resultMap;
    }
}
