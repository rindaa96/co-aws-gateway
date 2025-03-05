package com.indivaragroup.woi.mobile.awsgateway.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Header {

  public static final String X_APPLICATION_TOKEN = "X-Application-Token";
  public static final String X_APPLICATION_TOKEN_LOWERCASE = "x-application-token";
  public static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String X_MEMBER_ID = "X-Member-Id";
  public static final String X_USERNAME = "X-UserName";
  public static final String BEARER = "Bearer ";

  //algo header for cardless transaction
  public static final String X_TIMESTAMP = "X-TIMESTAMP";
  public static final String X_CLIENT_KEY = "X-CLIENT-KEY";
  public static final String X_SIGNATURE = "X-SIGNATURE";
  public static final String AUTHORIZATION = "Authorization";
  public static final String X_EXTERNAL_ID = "X-EXTERNAL-ID";
  public static final String X_PARTNER_ID = "X-PARTNER-ID";
  public static final String CHANNEL_ID = "CHANNEL-ID";
  //rintis header for qris transaction
  public static final String RTS_TIMESTAMP = "RTS-TIMESTAMP";
  public static final String RTS_SIGNATURE = "RTS-SIGNATURE";
  public static final String RTS_PARTNER_ID = "RTS-PARTNER-ID";

  public static final String REQUEST_URI = "REQUEST-URI";

  public static final String AUTHORIZATON_CUSTOMER = "Authorization-Customer";
}
