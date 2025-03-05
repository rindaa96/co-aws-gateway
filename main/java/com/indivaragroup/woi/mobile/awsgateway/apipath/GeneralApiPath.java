package com.indivaragroup.woi.mobile.awsgateway.apipath;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralApiPath {
    public static final String TOPUP_INQUIRY = "bayar-gw/api/virtualaccount/inquiry";
    public static final String TOPUP_PAYMENT_NOTIFICATION = "bayar-gw/api/paymentNotif";
    public static final String UPDATE_STATUS = "updateStatus";
}
