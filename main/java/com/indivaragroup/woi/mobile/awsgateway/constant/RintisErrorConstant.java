package com.indivaragroup.woi.mobile.awsgateway.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RintisErrorConstant {

    UNAUTHENTICATED(401, "", ""),
    UNAUTHENTICATED_MESSAGE(400, "E006", "Unauthenticated message"),
    INVALID_PROCESSING_CODE(400, "E003","Invalid Field Processing Code"),
    INVALID_MERCHANT(404, "E009","Invalid Merchant"),
    TRANSACTION_NOT_FOUND(404,"E009", "Transaction Not Found"),
    TRANSACTION_ROUTE_NOT_FOUND(500, "E009", "Transaction route not found"),
    UNKNOWN_ERROR(500, "E001", "Unknown error");

    private final int httpStatus;
    private final String code;
    private final String message;
}
