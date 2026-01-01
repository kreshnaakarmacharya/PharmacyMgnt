package com.pharmacy.MediNova.Model.pojos;

import com.pharmacy.MediNova.Model.EpayStatus;
import com.pharmacy.MediNova.Model.PurchaseRecord;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class EpaySuccessObject {
    private EpayStatus epayStatus;
    private PurchaseRecord purchaseRecord;
}

