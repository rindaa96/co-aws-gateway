package com.indivaragroup.woi.mobile.awsgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStatusRequest {
    private String transactionNumber;
    private String status;
    private String securityKey;
    private String dataSwitching;
}
