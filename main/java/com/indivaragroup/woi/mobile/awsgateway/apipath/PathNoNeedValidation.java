package com.indivaragroup.woi.mobile.awsgateway.apipath;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathNoNeedValidation {

    //It follows BPI, needs follow-up, is still used, or not?
    public static final String MEMBER_REGISTER_VYBE_PRO = "idp/v1/bpi/register/{code}";
    public static final String MEMBER_SET_PIN_VYBE_PRO = "idp/v1/bpi/set-pin";

    //============================
    public static final String MEMBER_REGISTER = "idp/api/v1/member/register";
    public static final String MEMBER_SET_MPIN = "idp/api/v1/member/setMpin";
    public static final String CHECK_MEMBER = "idp/api/v1/member/check";
    public static final String MEMBER_FORGOT_PASSWORD = "idp/api/v1/member/forgotpassword";
    public static final String MEMBER_FORGOT_PASSWORD_OTP = "idp/api/v1/member/forgotpassword-otp";
    public static final String NOTIF_EMAIL = "notification/api/v1/notification/mail";
    public static final String AUTH_LOGIN = "idp/api/v1/auth/login";
    public static final String AUTHORIZATION = "idp/api/v1/auth/authorization";
    public static final String IDP_NOTIF_SEND_OTP = "idp/api/v1/notification/otp/send";
    public static final String IDP_NOTIF_VERIFY_OTP = "idp/api/v1/notification/otp/verify";
    public static final String NOTIF_OTP_SEND = "notification/api/v1/otp/send";
    public static final String NOTIF_VERIFY = "notification/api/v1/otp/verify";
    public static final String NOTIF_SEND_OTP ="notification/api/v1/send-otp";
    public static final String VALIDATE_PHONE_NUMBER ="idp/api/v1/member/validate-phone-number";
    public static final String VALIDATE_EMAIL ="idp/api/v1/member/validate-email";
    public static final String CREATE_MEMBER ="idp/api/v1/member";

    public static final String OAUTH_TOKEN ="transaction-qris/api/v1/api/oauth/token";


}
