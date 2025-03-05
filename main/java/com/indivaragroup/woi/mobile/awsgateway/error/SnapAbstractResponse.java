package com.indivaragroup.woi.mobile.awsgateway.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SnapAbstractResponse {
    private String responseCode;
    private String responseMessage;
}
