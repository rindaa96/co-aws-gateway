package com.indivaragroup.woi.mobile.awsgateway.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi;
import com.indivaragroup.woi.mobile.awsgateway.apipath.BiSnapApi;
import com.indivaragroup.woi.mobile.awsgateway.constant.Header;
import com.indivaragroup.woi.mobile.awsgateway.constant.RintisErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.util.HttpUtils;
import com.indivaragroup.woi.mobile.awsgateway.validation.ValidatorService;
import com.indivaragroup.woi.mobile.awsgateway.constant.HttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi.*;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.GeneralApiPath.*;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.PathNoNeedValidation.*;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.RintisApi.*;
import static com.indivaragroup.woi.mobile.awsgateway.error.ErrorUtils.createAlgoErrorResponse;
import static com.indivaragroup.woi.mobile.awsgateway.error.ErrorUtils.createErrorResponse;
import static com.indivaragroup.woi.mobile.awsgateway.constant.BiSnapResponseConstant.BAD_REQUEST;
import static com.indivaragroup.woi.mobile.awsgateway.error.ErrorUtils.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AwsGatewayMain implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ValidatorService validatorService;
    @Value("${outbound.baseUrl}")
    private String baseUrl;

    @Value("${outbound.baseUrlCo}")
    private String baseUrlCo;

    @Value("${co.code}")
    private String coCode;

    @Override
    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent eventFromGateway) {
        log.info("event : {}", eventFromGateway);

        String apiUrl = baseUrl + "/"+ coCode + eventFromGateway.getPath();
        String apiUrlCo = baseUrlCo + "/"+ coCode + eventFromGateway.getPath();

        if (isPathNoNeedValidation(removePrefix(eventFromGateway.getPath()))) {
            log.info("APIs That don't need any validation");
            return handleWithoutAnyValidation(eventFromGateway, apiUrl);

        } else if (isTopupTransaction(removePrefix(eventFromGateway.getPath()))) {
            log.info("Transaction Top Up");
            return handleWithSignatureKeyValidation(eventFromGateway, apiUrl);

        } else if (isBiSnapTransaction(eventFromGateway.getPath())) {
            log.info("Transaction BI SNAP {}", eventFromGateway.getPath());
            return handleWithBiSnapValidation(eventFromGateway, apiUrlCo, apiUrl);

        } else if (isCardlessTransaction(removePrefix(eventFromGateway.getPath()))) {
            log.info("Transaction Cardless Withdrawal");
            return handleWithAlgoCardlessValidation(eventFromGateway, apiUrl);

        } else if (isIndivaraBillerSwitchTransaction(eventFromGateway.getPath())) {
            log.info("Transaction on Indivara Biller Switch");
            return handleWithSecurityKeyValidation(eventFromGateway, apiUrl);

        } else if (isRintisTransaction(removePrefix(eventFromGateway.getPath()))) {
            log.info("Transaction on Rintis");
            return handleWithRintisValidation(eventFromGateway, apiUrl);

        }
        else {
            log.info("hit endpoint to : {}", apiUrl);
            return handleWithTokenValidation(eventFromGateway, apiUrl);
        }
    }
    private APIGatewayProxyResponseEvent handleWithoutAnyValidation(APIGatewayProxyRequestEvent eventFromGateway,
                                                                    String url) {
        return hitToUrl(eventFromGateway, url);
    }
    private boolean isPathNoNeedValidation(String eventPath) {
        return  eventPath.equals(AUTH_LOGIN) || eventPath.equals(NOTIF_EMAIL) ||
                eventPath.equals(MEMBER_FORGOT_PASSWORD) || eventPath.equals(NOTIF_VERIFY) ||
                eventPath.equals(NOTIF_SEND_OTP) || eventPath.equals(NOTIF_OTP_SEND) || eventPath.equals(AUTHORIZATION) ||
                eventPath.equals(VALIDATE_PHONE_NUMBER) || eventPath.equals(VALIDATE_EMAIL) || eventPath.equals(CREATE_MEMBER) ||
                eventPath.equals(MEMBER_FORGOT_PASSWORD_OTP) || eventPath.equals(IDP_NOTIF_SEND_OTP) || eventPath.equals(IDP_NOTIF_VERIFY_OTP) ||
                eventPath.equals(MEMBER_REGISTER) || eventPath.equals(MEMBER_SET_MPIN) || eventPath.equals(MEMBER_REGISTER_VYBE_PRO) ||
                eventPath.equals(MEMBER_SET_PIN_VYBE_PRO) || eventPath.equals(CHECK_MEMBER) || eventPath.equals(OAUTH_TOKEN);
    }
    private boolean isTopupTransaction(String eventPath) {
        return eventPath.contains(TOPUP_INQUIRY) ||
                eventPath.contains(TOPUP_PAYMENT_NOTIFICATION);
    }
    private boolean isIndivaraBillerSwitchTransaction(String eventPath) {
        return eventPath.contains(UPDATE_STATUS);
    }

    private boolean isCardlessTransaction(String eventPath) {
        return eventPath.contains(ACCESS_TOKEN_B2B.getEndPoint()) ||
                eventPath.contains(CREATE_PAYMENT.getEndPoint()) ||
                eventPath.contains(CANCEL_PAYMENT.getEndPoint());
    }

    private boolean isRintisTransaction(String eventPath) {
        return eventPath.contains(QRIS_PAYMENT_CREDIT.getEndPoint()) ||
                eventPath.contains(QRIS_REFUND.getEndPoint()) ||
                eventPath.contains(QRIS_CHECK_STATUS.getEndPoint());
    }

    private boolean isBiSnapTransaction(String eventPath) {
        return eventPath.contains(BiSnapApi.ACCESS_TOKEN_B2B.getEndpoint()) ||
                eventPath.contains(BiSnapApi.ACCESS_TOKEN_B2B2C.getEndpoint()) ||
                eventPath.contains(BiSnapApi.DIRECT_DEBIT_PAYMENT.getEndpoint()) ||
                eventPath.contains(BiSnapApi.DIRECT_DEBIT_CANCEL.getEndpoint()) ||
                eventPath.contains(BiSnapApi.DIRECT_DEBIT_REFUND.getEndpoint()) ||
                eventPath.contains(BiSnapApi.REGISTRATION_ACCOUNT_BINDING.getEndpoint()) ||
                eventPath.contains(BiSnapApi.REGISTRATION_ACCOUNT_UNBINDING.getEndpoint()) ||
                eventPath.contains(BiSnapApi.DIRECT_DEBIT_STATUS.getEndpoint()) ||
                eventPath.contains(BiSnapApi.QR_MPM_GENERATE.getEndpoint()) ||
                eventPath.contains(BiSnapApi.QR_MPM_REFUND.getEndpoint()) ||
                eventPath.contains(BiSnapApi.QR_MPM_CANCEL.getEndpoint()) ||
                eventPath.contains(BiSnapApi.QR_MPM_QUERY.getEndpoint()) ||
                eventPath.contains(BiSnapApi.TRANSACTION_HISTORY_LIST.getEndpoint())||
                eventPath.contains(BiSnapApi.BALANCE_INQUIRY.getEndpoint()) ||
                eventPath.contains(BiSnapApi.GET_AUTH_CODE.getEndpoint()) ||
                eventPath.contains(BiSnapApi.BALANCE_INQUIRY.getEndpoint()) ||
                eventPath.contains(BiSnapApi.PAYMENT_CPM.getEndpoint()) ||
                eventPath.contains(BiSnapApi.REFUND_CPM.getEndpoint()) ||
                eventPath.contains(BiSnapApi.CANCEL_CPM.getEndpoint())
                ;
    }

    private APIGatewayProxyResponseEvent handleWithAlgoCardlessValidation(APIGatewayProxyRequestEvent eventFromGateway,
                                                                          String url) {
        APIGatewayProxyResponseEvent eventFromSignatureValidation;
        try {
            log.info("Signature Validation");
            eventFromSignatureValidation = validatorService.validateAlgoCardlessHeader(eventFromGateway);
        } catch (Exception e) {
            return createAlgoErrorResponse(BAD_REQUEST, AlgoCardlessApi.getByEndPoint(eventFromGateway.getPath()), e.getMessage());
        }

        if (eventFromSignatureValidation.getStatusCode() == 200) {
            eventFromSignatureValidation = hitToUrl(eventFromGateway, url);
        }
        return eventFromSignatureValidation;
    }

    private APIGatewayProxyResponseEvent handleWithRintisValidation(APIGatewayProxyRequestEvent eventFromGateway,
                                                                    String url) {
        APIGatewayProxyResponseEvent eventFromSignatureValidation;
        try {
            log.info("rintis validation");
            eventFromSignatureValidation = validatorService.validateRintisHeader(eventFromGateway);
        } catch (Exception e) {
            return createRintisErrorResponse(RintisErrorConstant.UNKNOWN_ERROR);
        }

        if (eventFromSignatureValidation.getStatusCode() == 200) {

            if (eventFromGateway.getHeaders().containsKey(Header.AUTHORIZATION)) {
                // Authorization is already valid
                eventFromGateway.getHeaders().remove(Header.AUTHORIZATION);
            }
            eventFromSignatureValidation = hitToUrl(eventFromGateway, url);
        }
        return eventFromSignatureValidation;
    }

    private APIGatewayProxyResponseEvent handleWithSignatureKeyValidation(APIGatewayProxyRequestEvent eventFromGateway,
                                                                          String url) {
        APIGatewayProxyResponseEvent eventFromSignatureValidation;
        try {
            log.info("Signature Validation");
            eventFromSignatureValidation = validatorService.validateSignature(eventFromGateway.getQueryStringParameters(), eventFromGateway.getPath());
        } catch (Exception e) {
            return createErrorResponse(500, "500", e.getMessage());
        }

        if (eventFromSignatureValidation.getStatusCode() == 200) {
            eventFromSignatureValidation = hitToUrl(eventFromGateway, url);
        }
        return eventFromSignatureValidation;
    }

    private APIGatewayProxyResponseEvent handleWithSecurityKeyValidation(APIGatewayProxyRequestEvent eventFromGateway,
                                                                          String url) {
        APIGatewayProxyResponseEvent eventFromSecurityValidation;
        try {
            log.info("Security Key Validation");
            eventFromSecurityValidation = validatorService.validateSecurityKey(eventFromGateway.getBody());
        } catch (Exception e) {
            return createErrorResponse(500, "500", e.getMessage());
        }

        if (eventFromSecurityValidation.getStatusCode() == 200) {
            eventFromSecurityValidation = hitToUrl(eventFromGateway, url);
        }
        return eventFromSecurityValidation;
    }

    private APIGatewayProxyResponseEvent handleWithBiSnapValidation(APIGatewayProxyRequestEvent eventFromGateway, String urlCo, String urlMobile) {
        APIGatewayProxyResponseEvent eventFromSignatureValidation;
        try {
            log.info("BI SNAP Header Validation");
            eventFromSignatureValidation = validatorService.validateBiSnapHeader(eventFromGateway);
        } catch (Exception e) {
            return createBiSnapErrorResponse(BAD_REQUEST, BiSnapApi.getByEndpoint(eventFromGateway.getPath()));
        }

        if (eventFromSignatureValidation.getStatusCode() == 200) {
            if (eventFromGateway.getHeaders().containsKey(Header.AUTHORIZATION)) {
                String authorizationHeader = eventFromGateway.getHeaders().get(Header.AUTHORIZATION);

                if (authorizationHeader.startsWith("Bearer ")) {
                    authorizationHeader = authorizationHeader.substring(7);
                }

                // Set the modified Authorization header
                eventFromGateway.getHeaders().put("Authorization", authorizationHeader);
            }

            if (eventFromGateway.getPath().contains("/transaction-qris/api/v1.0/qr/")){
                log.info("hit to co-mobile-trx-qris");
                eventFromSignatureValidation = hitToUrl(eventFromGateway, urlMobile);
            } else {
                log.info("hit to co-merchant");
                eventFromSignatureValidation = hitToUrl(eventFromGateway, urlCo);
            }

        }
        return eventFromSignatureValidation;
    }

    private APIGatewayProxyResponseEvent handleWithTokenValidation(APIGatewayProxyRequestEvent eventFromGateway, String url) {
        APIGatewayProxyResponseEvent eventFromService;

        try {
            log.info("Token Validation");
            eventFromService = validatorService.validateResponseToken(eventFromGateway);
            eventFromGateway.setHeaders(eventFromService.getHeaders());
        } catch (JsonProcessingException e) {
            return createErrorResponse(500, "500", e.getMessage());
        }

        log.info("Header from gateway : {}", eventFromGateway.getHeaders());
        if (eventFromService.getStatusCode() == 200) {
            eventFromService = hitToUrl(eventFromGateway, url);
        }
        return eventFromService;
    }
    private APIGatewayProxyResponseEvent hitToUrl(APIGatewayProxyRequestEvent eventFromGateway, String url) {
        log.info("parameter: {}", eventFromGateway.getQueryStringParameters());
        if (eventFromGateway.getHttpMethod().equals(HttpMethod.GET)) {
            log.info("START GET METHOD : {}", url);
            return HttpUtils.makeHttpGetRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                                eventFromGateway.getQueryStringParameters());
        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.POST)) {
            log.info("START POST METHOD : {}", url);
            if (eventFromGateway.getHeaders().containsKey("Content-Type") && eventFromGateway.getHeaders().get("Content-Type").contains("multipart/form-data")) {
                log.info("starting with image");
                return HttpUtils.makeHttpPostRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                        eventFromGateway.getBody(), eventFromGateway.getQueryStringParameters(), true);
            } else {
                return HttpUtils.makeHttpPostRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                        eventFromGateway.getBody(), eventFromGateway.getQueryStringParameters(), false);
            }

        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.PUT)) {
            log.info("START PUT METHOD : {}", url);
            return HttpUtils.makeHttpRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                             eventFromGateway.getBody(), new HttpPut(), eventFromGateway.getQueryStringParameters());
        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.OPTIONS)) {
            log.info("START OPTIONS METHOD : {}", url);
            return HttpUtils.makeHttpRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                             eventFromGateway.getBody(), new HttpOptions(), eventFromGateway.getQueryStringParameters());
        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.HEAD)) {
            log.info("START HEAD METHOD : {}", url);
            return HttpUtils.makeHttpRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                             eventFromGateway.getBody(), new HttpHead(), eventFromGateway.getQueryStringParameters());
        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.PATCH)) {
            log.info("START PATCH METHOD : {}", url);
            return HttpUtils.makeHttpRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                             eventFromGateway.getBody(), new HttpPatch(), eventFromGateway.getQueryStringParameters());
        } else if (eventFromGateway.getHttpMethod().equals(HttpMethod.DELETE)) {
            log.info("START DELETE METHOD : {}", url);
            return HttpUtils.makeHttpRequest(url, resetHeaders(eventFromGateway.getHeaders()),
                                             eventFromGateway.getBody(), new HttpDelete(), eventFromGateway.getQueryStringParameters());
        } else {
            return createErrorResponse(405, "405", "Http Method is invalid");
        }
    }
    private Map<String, String> resetHeaders(Map<String, String> headers) {
        // Create a new map to store filtered headers
        Map<String, String> filteredHeaders = new HashMap<>();
        log.info("RESET HEADER : {}", headers);
        // Create a set of headers to exclude
        String[] keysToExclude = {"Accept-Encoding", "CloudFront-Forwarded-Proto", "CloudFront-Is-Desktop-Viewer",
                                  "CloudFront-Is-Mobile-Viewer", "CloudFront-Is-SmartTV-Viewer", "CloudFront-Is-Tablet-Viewer",
                                  "CloudFront-Viewer-ASN", "CloudFront-Viewer-Country", "Host", "Postman-Token", "User-Agent",
                                  "Via", "X-Amz-Cf-Id", "X-Amzn-Trace-Id", "X-Forwarded-For", "X-Forwarded-Port",
                                  "X-Forwarded-Proto"};

        log.info("headers.entrySet() : {}", headers.entrySet());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Check if the key should be excluded
            if (!shouldExcludeKey(key, keysToExclude)) {
                filteredHeaders.put(key, value);
            }
        }

        log.info("filteredHeaders : {}", filteredHeaders);
        return filteredHeaders;
    }
    private static boolean shouldExcludeKey(String key, String[] keysToExclude) {
        for (String excludedKey : keysToExclude) {
            if (key.equals(excludedKey)) {
                return true;
            }
        }
        return false;
    }

    private static String removePrefix(String input) {
        int secondSlashIndex = indexOfNthOccurrence(input, '/', 1);

        if (secondSlashIndex != -1) {
            return input.substring(secondSlashIndex + 1);
        } else {
            // Handle the case where there are fewer than two '/' characters
            return input;
        }
    }

    private static int indexOfNthOccurrence(String str, char c, int n) {
        int index = -1;
        for (int i = 0; i < n; i++) {
            index = str.indexOf(c, index + 1);
            if (index == -1) {
                break;
            }
        }
        return index;
    }
}
