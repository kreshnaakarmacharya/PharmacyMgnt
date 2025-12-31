package com.pharmacy.MediNova.Model.pojos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class EsewaEpayResponse {
    private String transaction_code;
    private String product_code;
    private String transaction_uuid;
    private float total_amount;
    private String status;
    private String signed_field_names;
    private String signature;
}
