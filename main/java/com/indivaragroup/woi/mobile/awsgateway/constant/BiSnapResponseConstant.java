package com.indivaragroup.woi.mobile.awsgateway.constant;

import lombok.Getter;

@Getter
public enum BiSnapResponseConstant {

    SUCCESSFUL("200{0}00", "Successful"),
    IN_PROGRESS("202{0}00", "Successful"),
    BAD_REQUEST("400{0}00", "Bad Request"),
    INVALID_X_TIMESTAMP_FORMAT("400{0}01", "Invalid Field Format X-TIMESTAMP"),
    INVALID_X_EXTERNAL_ID_FORMAT("400{0}01", "Invalid Field Format X-EXTERNAL-ID"),
    INVALID_CHANNEL_ID_FORMAT("400{0}01", "Invalid Field Format CHANNEL-ID"),
    INVALID_X_CLIENT_KEY("400{0}01", "Invalid Field Format X-CLIENT-KEY"),
    INVALID_X_PARTNER_ID("400{0}01", "Invalid Field Format X-PARTNER-ID"),
    MISSING_X_TIMESTAMP("400{0}02", "Missing Or Invalid Field Format X-TIMESTAMP"),
    MISSING_X_EXTERNAL_ID("400{0}02", "Missing Or Invalid Field Format X-EXTERNAL-ID"),
    MISSING_X_PARTNER_ID("400{0}02", "Missing Or Invalid Field Format X-PARTNER-ID"),
    MISSING_CHANNEL_ID("400{0}02", "Missing Or Invalid Field Format CHANNEL-ID"),
    MISSING_X_CLIENT_KEY("400{0}02", "Missing Or Invalid Field Format X-CLIENT-KEY"),
    UNAUTHORIZED("401{0}00", "Unauthorized. Invalid Client ID or Client Secret or Signature"),
    UNAUTHORIZED_AUTH_CODE("401{0}00", "Unauthorized. Invalid Auth Code or Refresh Token"),
    INVALID_TOKEN("401{0}01", "Invalid Token (B2B)"),
    INVALID_CUSTOMER_TOKEN("401{0}02", "Invalid Customer Token"),
    TOKEN_NOT_FOUND("401{0}03", "Token Not Found (B2B)"),
    GENERAL_ERROR("500{0}00", "General Error"),
    CONFLICT("409{0}00", "Conflict"),
    TRANSACTION_NOT_FOUND("404{0}01", "Transaction Not Found"),
    INSUFFICIENT_FUNDS("403{0}14", "Insufficient Funds"),
    DUPLICATE_PARTNER_REFERENCE_NO("409{0}01", "Duplicate partnerReferenceNo"),
    INVALID_PROCESSING_CODE("400{0}01", "Invalid Field Processing Code"),
    SUSPEND_TRANSACTION("403{0}16", "Suspend Transaction"),
    INTERNAL_SERVER_ERROR("500{0}02", "Internal Server Error"),
    EXTERNAL_SERVER_ERROR("500{0}02", "External Server Error");

    private final String code;
    private final String message;

    BiSnapResponseConstant(String code, String description) {
        this.code = code;
        this.message = description;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
