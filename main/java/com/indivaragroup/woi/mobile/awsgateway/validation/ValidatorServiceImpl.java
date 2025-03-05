package com.indivaragroup.woi.mobile.awsgateway.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi;
import com.indivaragroup.woi.mobile.awsgateway.apipath.ApiEndpoint;
import com.indivaragroup.woi.mobile.awsgateway.apipath.RintisApi;
import com.indivaragroup.woi.mobile.awsgateway.apipath.BiSnapApi;
import com.indivaragroup.woi.mobile.awsgateway.constant.BiSnapResponseConstant;
import com.indivaragroup.woi.mobile.awsgateway.constant.RintisErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.dto.ApplicationTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.dto.AuthorizationResponse;
import com.indivaragroup.woi.mobile.awsgateway.dto.UpdateStatusRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import com.indivaragroup.woi.mobile.awsgateway.outbound.BiSnapRestClient;
import com.indivaragroup.woi.mobile.awsgateway.outbound.CardlessRestClient;
import com.indivaragroup.woi.mobile.awsgateway.outbound.IdpRestClient;
import com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.constant.Header;
import com.indivaragroup.woi.mobile.awsgateway.outbound.QrisRestClient;
import com.indivaragroup.woi.mobile.awsgateway.util.FormatterUtil;
import com.indivaragroup.woi.mobile.awsgateway.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi.ACCESS_TOKEN_B2B;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi.getByEndPoint;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.BiSnapApi.*;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.GeneralApiPath.TOPUP_INQUIRY;
import static com.indivaragroup.woi.mobile.awsgateway.constant.BiSnapResponseConstant.*;
import static com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant.SIGNATURE_INVALID;
import static com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant.SUCCESS;
import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.BEARER;
import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.CONTENT_TYPE;
import static com.indivaragroup.woi.mobile.awsgateway.constant.RintisErrorConstant.*;
import static com.indivaragroup.woi.mobile.awsgateway.error.ErrorUtils.*;
import static com.indivaragroup.woi.mobile.awsgateway.util.PasswordUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorServiceImpl implements ValidatorService {
    private final IdpRestClient idpRestClient;
    private final CardlessRestClient cardlessRestClient;
    private final QrisRestClient qrisRestClient;
    private final BiSnapRestClient biSnapRestClient;

    @Value("${co.code}")
    private String coCode;

    @Value("${bayarGw.merchantId}")
    private String merchantId;
    @Value("${bayarGw.transactionKey}")
    private String transactionKey;
    @Value("${algoCardlessWithdrawal.clientId}")
    private String algoClientId;
    @Value("${algoCardlessWithdrawal.partnerId}")
    private String algoPartnerId;
    @Value("${algoCardlessWithdrawal.secretId}")
    private String algoSecretId;
    @Value("${algoCardlessWithdrawal.encodedPublicKey}")
    private String algoPublicKey;
    @Value("${biller.sourceId}")
    private String sourceId;
    @Value("${biller.sourceUser}")
    private String sourceUser;
    @Value("${biller.sourcePass}")
    private String sourcePass;
    @Value("${rintis.partnerId}")
    private String rintisPartnerId;


    public APIGatewayProxyResponseEvent validateResponseToken(APIGatewayProxyRequestEvent event) throws JsonProcessingException {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        String token = event.getHeaders().get(Header.X_APPLICATION_TOKEN);

        if (token == null || token.isEmpty()) {
            token = event.getHeaders().get(Header.X_APPLICATION_TOKEN_LOWERCASE);
        }

        if (!StringUtils.hasText(token)) {
            return createErrorResponse(403, ErrorConstant.INVALID_TOKEN.getCode(), ErrorConstant.INVALID_TOKEN.getMessage());
        }

        if (!token.contains(Header.BEARER)) {
            return createErrorResponse(403, ErrorConstant.INVALID_TOKEN.getCode(), ErrorConstant.INVALID_TOKEN.getMessage());
        }

        AuthorizationResponse responseAuth = validateToken(token);

        if (responseAuth != null && responseAuth.getId() != null) {
            HashMap<String, String> headers = new HashMap<>();
            String contentType = event.getHeaders().get(CONTENT_TYPE);
            headers.put(CONTENT_TYPE, contentType != null ? contentType : "application/json");
            headers.put(Header.X_MEMBER_ID, String.valueOf(responseAuth.getId()));
            headers.put(Header.X_USERNAME, responseAuth.getUsername());
            responseEvent.setHeaders(headers);
            responseEvent.setStatusCode(200);
        } else {
            return createErrorResponse(403, ErrorConstant.INVALID_TOKEN.getCode(), ErrorConstant.INVALID_TOKEN.getMessage());
        }

        return responseEvent;
    }
    public APIGatewayProxyResponseEvent validateSignature(Map<String, String> param, String path) throws JsonProcessingException {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        log.info("GENERATE SIGNATURE FROM TRX ID, ACCOUNT, ISSUER, AND AMOUNT FOR P2P");
        String orderId = param.get("orderID");
        String channelId= param.get("channelID");
        String amount = param.get("amount");
        String signature;

        signature = merchantId + "##" + channelId + "##" + transactionKey + "##" + orderId + "##" + amount;
        log.info("signatureGenerated : {}", signature);

        String signatureGenerated = PasswordUtil.sha1(1, signature);
        log.info("GENERATED SIGNATURE : {}", signatureGenerated);

        log.info("VALIDATION GENERATED SIGNATURE WITH SIGNATURE FROM REQUEST PARAMETER");
        String signatureFromRequest = param.get("signature");
        log.info("signatureFromRequest : {}", signatureFromRequest);

        if (signatureFromRequest.equals(signatureGenerated)) {
            log.info("SIGNATURE CORRECT");
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            responseEvent.setHeaders(headers);
            responseEvent.setStatusCode(200);
        } else {
            return createErrorResponse(403, SIGNATURE_INVALID.getCode(), SIGNATURE_INVALID.getMessage());
        }

        return responseEvent;
    }

    public APIGatewayProxyResponseEvent validateSecurityKey(String body) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        log.info("REQUEST BODY : {}", body);
        UpdateStatusRequest updateStatusRequest;
        try{
            updateStatusRequest = new ObjectMapper().readValue(body, UpdateStatusRequest.class);
        } catch (JsonProcessingException jpe){
            return createErrorResponse(500, "500", jpe.getMessage());
        }


        log.info("Generate Security Key For Biller Switch");
        String transactionNumber = updateStatusRequest.getTransactionNumber();
        log.info("Transaction Number : {}", transactionNumber);
        String formatSecurityKey = sourceId + "##" + sourceUser + "##" + sourcePass + "##" + transactionNumber;
        log.info("Source Id : {}, Source User : {}, Source Pass : {}", sourceId, sourceUser, sourcePass);

        String generatedSecurityKey = PasswordUtil.sha1(1, formatSecurityKey);
        log.info("Generated Security Key : {}", generatedSecurityKey);

        log.info("Validate Security Key");
        String securityKeyFromRequest = updateStatusRequest.getSecurityKey();
        log.info("Security Key from Request : {}", securityKeyFromRequest);

        if (securityKeyFromRequest.equalsIgnoreCase(generatedSecurityKey)) {
            log.info("Security is Valid");
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            responseEvent.setHeaders(headers);
            responseEvent.setStatusCode(200);
        } else {
            return createErrorResponse(403, SIGNATURE_INVALID.getCode(), SIGNATURE_INVALID.getMessage());
        }

        return responseEvent;
    }

    @Override
    public APIGatewayProxyResponseEvent validateAlgoCardlessHeader(APIGatewayProxyRequestEvent eventFromGateway) throws NoSuchAlgorithmException,
                                                                                                                SignatureException,
                                                                                                                InvalidKeySpecException,
                                                                                                                InvalidKeyException,
                                                                                                                IOException {

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        AlgoCardlessApi apiConstant = getByEndPoint(eventFromGateway.getPath());

        log.info("apiConstant : {}", apiConstant.getEndPoint());

        String timeStamp = eventFromGateway.getHeaders().get(Header.X_TIMESTAMP);
        String clientId = eventFromGateway.getHeaders().get(Header.X_CLIENT_KEY);
        String signature = eventFromGateway.getHeaders().get(Header.X_SIGNATURE);

        log.info("VALIDATION TIMESTAMP : {}", timeStamp);
        if (isTimeStampInvalid(timeStamp)) {
            return createAlgoErrorResponse(MISSING_X_TIMESTAMP, apiConstant, null);
        }


        log.info("VALIDATION ClientId : {}", clientId);
        if (isClientIdInvalid(clientId)) {
            return createAlgoErrorResponse(MISSING_X_CLIENT_KEY, apiConstant, null);
        }

        if (!clientId.equals(algoClientId)) {
            return createAlgoErrorResponse(INVALID_X_CLIENT_KEY, apiConstant, null);
        }

        if (isApiAccessToken(apiConstant)) {
            if (!validateAsymmetricSignature(clientId, timeStamp, signature)) {
                return createAlgoErrorResponse(UNAUTHORIZED, apiConstant, null);
            }
        } else {
            if (!validateApiRequest(eventFromGateway, apiConstant, signature)) {
                return createAlgoErrorResponse(UNAUTHORIZED, apiConstant, null);
            }
        }

        responseEvent.setStatusCode(200);
        return responseEvent;
    }

    @Override
    public APIGatewayProxyResponseEvent validateRintisHeader(APIGatewayProxyRequestEvent eventFromGateway) throws IOException {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        RintisApi endpoint = RintisApi.getByEndPoint(eventFromGateway.getPath());

        log.info("apiConstant : {}", endpoint.getEndPoint());

        String token = eventFromGateway.getHeaders().get(Header.AUTHORIZATION);
        String timestamp = eventFromGateway.getHeaders().get(Header.RTS_TIMESTAMP);
        String signature = eventFromGateway.getHeaders().get(Header.RTS_SIGNATURE);
        String partnerId = eventFromGateway.getHeaders().get(Header.RTS_PARTNER_ID);

        String bodyString = eventFromGateway.getBody();

        String[] headers = {token, timestamp, signature, partnerId};
        for (String header : headers) {
            if (isHeaderRintisExist(header)) {
                return createRintisErrorResponse(RintisErrorConstant.UNAUTHENTICATED);
            }
        }

        if (!isPartnerIdRintisValid(partnerId)) {
            return createRintisErrorResponse(RintisErrorConstant.UNAUTHENTICATED_MESSAGE);
        }

        String requestBodyString = PasswordUtil.minifyBody(bodyString);

        try {
            log.info("Start validating Rintis authentication");

            String authToken = token.replace(BEARER, "");
            String requestUri = "/co" + eventFromGateway.getPath();
            SnapAbstractResponse response = qrisRestClient.validateTokenSignature(authToken, timestamp, signature, requestUri, requestBodyString);

            if (response != null && response.getResponseCode().equals(SUCCESS.getCode())) {

                log.info("Rintis authentication success");

                responseEvent.setStatusCode(200);
                responseEvent.setBody(eventFromGateway.getBody());
                responseEvent.setHeaders(eventFromGateway.getHeaders());
            } else {
                return createRintisErrorResponse(RintisErrorConstant.UNAUTHENTICATED_MESSAGE);
            }

        }catch (Exception e){
            log.error("Error Validate token Rintis : {}", e.getMessage());
            return createRintisErrorResponse(RintisErrorConstant.UNKNOWN_ERROR);
        }

        return responseEvent;
    }

    private boolean isHeaderRintisExist(String header) {
        return header == null || header.isEmpty();
    }

    private boolean isPartnerIdRintisValid(String header) {
        return header != null && header.equals(rintisPartnerId);
    }

    private boolean validateAsymmetricSignatureBiSnap(String signature, String timeStamp, String clientKey) {
         SnapAbstractResponse response = biSnapRestClient.validateAsymmetricSignature(signature, timeStamp, clientKey);
         return response.getResponseCode().equals(MessageFormat.format(BiSnapResponseConstant.SUCCESSFUL.getCode(), "00"));
    }

    private boolean validateSymmetricSignatureBiSnap(String signature, String timeStamp, String clientKey,
                                                     String externalId, String requestBody, String urlEndpoint) {
        SnapAbstractResponse response = biSnapRestClient.validateSymmetricSignature(signature, timeStamp,
                clientKey, externalId, requestBody, urlEndpoint);
        return response.getResponseCode().equals(MessageFormat.format(BiSnapResponseConstant.SUCCESSFUL.getCode(), "00"));
    }


    @Override
    public APIGatewayProxyResponseEvent validateBiSnapHeader(APIGatewayProxyRequestEvent eventFromGateway) throws IOException {
        log.info("Validation BI SNAP API : {}", eventFromGateway.getPath());
        String accessToken = eventFromGateway.getHeaders().get(Header.AUTHORIZATION);
        String accessTokenB2b2c = eventFromGateway.getHeaders().get(Header.AUTHORIZATON_CUSTOMER);
        String partnerId = eventFromGateway.getHeaders().get(Header.X_PARTNER_ID);
        String clientKey = eventFromGateway.getHeaders().get(Header.X_CLIENT_KEY);
        String externalId = eventFromGateway.getHeaders().get(Header.X_EXTERNAL_ID);
        String timestamp = eventFromGateway.getHeaders().get(Header.X_TIMESTAMP);
        String signature = eventFromGateway.getHeaders().get(Header.X_SIGNATURE);
        String channelId = eventFromGateway.getHeaders().get(Header.CHANNEL_ID);
        String bodyString = eventFromGateway.getBody();

        BiSnapApi endpoint = BiSnapApi.getByEndpoint(eventFromGateway.getPath());
        log.info("endpoint BI SNAP API : {}", endpoint);

        if (isTimeStampInvalid(timestamp)) {
            return createBiSnapErrorResponse(INVALID_X_TIMESTAMP_FORMAT, endpoint);
        }

        if (isSignatureInvalid(signature)) {
            return createBiSnapErrorResponse(UNAUTHORIZED, endpoint);
        }

        if (isApiAccessTokenBisnap(endpoint)) {
            log.info("Validation access token");

            if (isClientIdInvalid(clientKey)) {
                return createBiSnapErrorResponse(MISSING_X_CLIENT_KEY, endpoint);
            }

            if (!validateAsymmetricSignatureBiSnap(signature, timestamp, clientKey)) {
                return createBiSnapErrorResponse(UNAUTHORIZED, endpoint);
            }

        } else {
            log.info("Validation API");

            if (isPartnerIdInvalid(partnerId)) {
                return createBiSnapErrorResponse(MISSING_X_PARTNER_ID, endpoint);
            }

            if (isExternalIdInvalid(externalId)) {
                return createBiSnapErrorResponse(MISSING_X_EXTERNAL_ID, endpoint);
            }

            if (isChannelIdInvalid(channelId)) {
                return createBiSnapErrorResponse(MISSING_CHANNEL_ID, endpoint);
            }

            if (!isApiGetAuthToken(endpoint)) {
                String requestBodyString = PasswordUtil.minifyBody(bodyString);

                if (!validateSymmetricSignatureBiSnap(signature, timestamp, partnerId, externalId, requestBodyString, endpoint.getEndpoint())) {
                    return createBiSnapErrorResponse(UNAUTHORIZED, endpoint);
                }
            }

            if (!validateRequestBiSnapApi(partnerId, accessToken, endpoint, accessTokenB2b2c)) {
                return createBiSnapErrorResponse(UNAUTHORIZED, endpoint);
            }
        }

        log.info("Validation success");
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(200);
        return responseEvent;
    }

    private boolean validateRequestBiSnapApi(String partnerId, String accessToken, BiSnapApi endpoint, String accessTokenB2b2c) {
        log.info("Validating API access token");
        if (isAccessTokenInvalid(accessToken)) {
            return false;
        }

        String tokenB2b = accessToken.replace(BEARER, "");

        SnapAbstractResponse validateB2b = biSnapRestClient.validateToken(partnerId, new ApplicationTokenRequest(tokenB2b));

        if (!validateB2b.getResponseCode().equals(SUCCESS.getCode())) {
            return false;
        }

        if (isApiNeedB2b2c(endpoint)) {
            log.info("Validating access token B2B2C");

            if (isAccessTokenB2b2cInvalid(accessTokenB2b2c)) {
                return false;
            }

            String tokenB2b2c = accessTokenB2b2c.replace(BEARER, "");

            SnapAbstractResponse validateB2b2C = biSnapRestClient.validateTokenb2b2c(partnerId, new ApplicationTokenRequest(tokenB2b2c));
            return validateB2b2C.getResponseCode().equals(SUCCESS.getCode());
        }

        return true;
    }

    private boolean validateApiRequest(APIGatewayProxyRequestEvent eventFromGateway, ApiEndpoint apiConstant, String signature) throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        log.info("VALIDATION API : {}", eventFromGateway.getPath());
        String accessToken = eventFromGateway.getHeaders().get(Header.AUTHORIZATION);
        String bodyString = eventFromGateway.getBody();
        String partnerId = eventFromGateway.getHeaders().get(Header.X_PARTNER_ID);
        String externalId = eventFromGateway.getHeaders().get(Header.X_EXTERNAL_ID);

        log.info("VALIDATION partnerId : {}", partnerId);
        if (isPartnerIdInvalid(partnerId)) {
            return false;
        }

        if (!partnerId.equals(algoPartnerId)) {
            return false;
        }

        log.info("VALIDATION externalId : {}", externalId);
        if (isExternalIdInvalid(externalId)) {
            return false;
        }

        log.info("VALIDATION accessToken : {}", accessToken);
        if (isAccessTokenInvalid(accessToken)) {
            return false;
        }

        if (accessToken.startsWith(Header.BEARER)) {
            accessToken = accessToken.split(" ")[1].trim();
        }

        SnapAbstractResponse response = cardlessRestClient.authorizeRequest(accessToken);

        if (!response.getResponseCode().equals(SUCCESS.getCode())) {
            return false;
        }

        log.info("VALIDATION simmetricSignature : {}", signature);
        if (isSignatureInvalid(signature)) {
            return false;
        }

        String generatedSymmetricSignature = generateSymmetricSignature(eventFromGateway.getHttpMethod(),
                apiConstant.getEndPoint(),
                accessToken, eventFromGateway.getHeaders().get(Header.X_TIMESTAMP),
                bodyString, algoSecretId);

        return signature.equals(generatedSymmetricSignature);
    }

    private boolean isSignatureInvalid(String signature) {
        return signature == null || signature.isEmpty();
    }

    private boolean isAccessTokenInvalid(String accessToken) {
        return accessToken == null || accessToken.isEmpty() || !accessToken.contains(Header.BEARER);
    }

    private boolean isAccessTokenB2b2cInvalid(String accessTokenB2b2c) {
        return accessTokenB2b2c == null || accessTokenB2b2c.isEmpty() || !accessTokenB2b2c.contains(Header.BEARER);
    }

    private boolean isExternalIdInvalid(String externalId) {
        return externalId == null || externalId.isEmpty();
    }

    private boolean isPartnerIdInvalid(String partnerId) {
        return partnerId == null || partnerId.isEmpty();
    }

    private boolean validateAsymmetricSignature(String clientId, String timeStamp,
                                                String signature) throws NoSuchAlgorithmException, 
                                                                        InvalidKeySpecException, 
                                                                        SignatureException, 
                                                                        InvalidKeyException {
        log.info("VALIDATION API ACCESS TOKEN Asymmetric signature");
        String stringToSign = String.join("|", clientId, timeStamp);
        log.info("stringToSign : {}", stringToSign);

        String publicKey = new String(Base64.getDecoder().decode(algoPublicKey));
        Boolean isVerify = verifyAsymmetricSignature(stringToSign, signature, publicKey);
        log.info("isVerify : {}", isVerify);

        return isVerify;
    }

    private boolean isApiAccessToken(ApiEndpoint apiConstant) {
        return apiConstant.getEndPoint().contains(ACCESS_TOKEN_B2B.getEndPoint());
    }

    private boolean isApiAccessTokenBisnap(BiSnapApi apiConstant) {
        return apiConstant.getEndpoint().contains(ACCESS_TOKEN_B2B.getEndPoint())
                || apiConstant.getEndpoint().contains(ACCESS_TOKEN_B2B2C.getEndpoint());
    }

    private boolean isApiNeedB2b2c(BiSnapApi apiConstant) {
        return apiConstant.getEndpoint().contains(DIRECT_DEBIT_PAYMENT.getEndpoint())
                || apiConstant.getEndpoint().contains(DIRECT_DEBIT_CANCEL.getEndpoint())
                || apiConstant.getEndpoint().contains(DIRECT_DEBIT_STATUS.getEndpoint())
                || apiConstant.getEndpoint().contains(DIRECT_DEBIT_REFUND.getEndpoint())
                || apiConstant.getEndpoint().contains(BALANCE_INQUIRY.getEndpoint());
    }

    private boolean isApiGetAuthToken(BiSnapApi apiConstant) {
        return apiConstant.getEndpoint().contains(GET_AUTH_CODE.getEndpoint());
    }

    private boolean isClientIdInvalid(String clientId) {
        return clientId == null || clientId.isEmpty();
    }

    private boolean isChannelIdInvalid(String channelId) {
        return channelId == null || channelId.isEmpty();
    }

    private boolean isTimeStampInvalid(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty()) {
            return true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(FormatterUtil.SDF_TIMESTAMP_ISO);
        try {
            sdf.parse(timeStamp);
        } catch (ParseException e) {
            return true;
        }

        return false;
    }

    private AuthorizationResponse validateToken(String token) {
        return idpRestClient.authorizeRequest(token);
    }
}
