package com.indivaragroup.woi.mobile.awsgateway.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.woi.mobile.awsgateway.apipath.BiSnapApi;
import com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.constant.Header;
import com.indivaragroup.woi.mobile.awsgateway.dto.ApplicationTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.dto.AuthorizationResponse;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import com.indivaragroup.woi.mobile.awsgateway.outbound.BiSnapRestClient;
import com.indivaragroup.woi.mobile.awsgateway.outbound.CardlessRestClient;
import com.indivaragroup.woi.mobile.awsgateway.outbound.IdpRestClient;
import com.indivaragroup.woi.mobile.awsgateway.outbound.QrisRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi.*;
import static com.indivaragroup.woi.mobile.awsgateway.apipath.RintisApi.QRIS_PAYMENT_CREDIT;
import static com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant.SUCCESS;
import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.*;
import static com.indivaragroup.woi.mobile.awsgateway.constant.RintisErrorConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ValidatorServiceImplTest {
    @Mock
    private IdpRestClient idpRestClient;
    @Mock
    private CardlessRestClient cardlessRestClient;
    @Mock
    private QrisRestClient qrisRestClient;
    @Mock
    private BiSnapRestClient biSnapRestClient;

    @InjectMocks
    private ValidatorServiceImpl validatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testValidateResponseToken_Success() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(X_APPLICATION_TOKEN, "Bearer mockToken");
        requestEvent.setHeaders(headers);

        AuthorizationResponse mockResponse = new AuthorizationResponse();
        mockResponse.setId(UUID.randomUUID());
        mockResponse.setUsername("mockUsername");
        when(idpRestClient.authorizeRequest(anyString())).thenReturn(mockResponse);

        APIGatewayProxyResponseEvent result = validatorService.validateResponseToken(requestEvent);

        assertTrue(result.getHeaders().containsKey(X_MEMBER_ID));
        assertTrue(result.getHeaders().containsKey(X_USERNAME));
    }

    @Test
    void testValidateResponseToken_InvalidToken() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(X_APPLICATION_TOKEN, "Bearer mockToken");
        requestEvent.setHeaders(headers);

        APIGatewayProxyResponseEvent result = validatorService.validateResponseToken(requestEvent);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result.getBody());
        JsonNode fieldValue = jsonNode.path("status").path("code");

        assertEquals(ErrorConstant.INVALID_TOKEN.getCode(), fieldValue.asText());
    }

    @Test
    void testValidateSignature_Success() throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put("orderId", "123");
        param.put("channelId", "456");
        param.put("amount", "100");
        param.put("signature", "generatedSignature");

        String path = "/virtualaccount/inquiry";

        APIGatewayProxyResponseEvent result = validatorService.validateSignature(param, path);

        assertNotNull(result);
    }

    @Test
    void testValidateSignature_InvalidSignature() throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put("orderId", "123");
        param.put("channelId", "456");
        param.put("amount", "100");
        param.put("signature", "invalidSignature");

        String path = "/virtualaccount/inquiry";

        APIGatewayProxyResponseEvent result = validatorService.validateSignature(param, path);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result.getBody());
        JsonNode fieldValue = jsonNode.path("status").path("code");

        assertEquals(ErrorConstant.SIGNATURE_INVALID.getCode(), fieldValue.asText());
    }

    @Test
    void testValidateAlgoCardlessHeader_ApiAccessToken_Success()
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            SignatureException, InvalidKeyException {
        // Arrange
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(VALIDATE_TOKEN_B2B.getEndPoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-03-05T14:58:46+07:00");
        header.put(Header.X_CLIENT_KEY, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        when(cardlessRestClient.authorizeRequest(anyString())).thenReturn(new SnapAbstractResponse(SUCCESS.getCode(), SUCCESS.getMessage()));

        // Act
        APIGatewayProxyResponseEvent responseEvent = validatorService.validateAlgoCardlessHeader(mockEvent);

        // Assert
        assertNotNull(responseEvent);
    }

    @Test
    void testValidateAlgoCardlessHeader_InvalidTimestamp_Error()
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            SignatureException, InvalidKeyException {
        // Arrange
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(VALIDATE_TOKEN_B2B.getEndPoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "invalid_timestamp");
        header.put(Header.X_CLIENT_KEY, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        // Act
        APIGatewayProxyResponseEvent responseEvent = validatorService.validateAlgoCardlessHeader(mockEvent);

        // Assert
        assertNotNull(responseEvent);
    }

    @Test
    void testValidateAlgoCardlessHeader_InvalidClientId_Error()
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            SignatureException, InvalidKeyException {
        // Arrange
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(VALIDATE_TOKEN_B2B.getEndPoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-03-05T14:58:46+07:00");
        header.put(Header.X_CLIENT_KEY, "invalid_clientId");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        // Act
        APIGatewayProxyResponseEvent responseEvent = validatorService.validateAlgoCardlessHeader(mockEvent);

        // Assert
        assertNotNull(responseEvent);
    }

    @Test
    void testValidateAlgoCardlessHeader_NonApiAccessToken_Success()
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            SignatureException, InvalidKeyException {
        // Arrange
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(CANCEL_PAYMENT.getEndPoint());

        Map<String, String> header = new HashMap<>();
        header.put(AUTHORIZATION, "2024-03-05T14:58:46+07:00");
        header.put(X_CLIENT_KEY, "invalid_clientId");
        header.put(X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        // Act
        APIGatewayProxyResponseEvent responseEvent = validatorService.validateAlgoCardlessHeader(mockEvent);

        // Assert
        assertNotNull(responseEvent);
    }

    @Test
    void testValidateRintisHeader_SuccessfulValidation() throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, IOException {
        // Arrange
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(QRIS_PAYMENT_CREDIT.getEndPoint());
        mockEvent.setHeaders(Map.of(
                Header.AUTHORIZATION, "validToken",
                Header.RTS_TIMESTAMP, "validTimestamp",
                Header.RTS_SIGNATURE, "validSignature",
                Header.X_CLIENT_KEY, "validClientId"
        ));
        mockEvent.setBody("{\"key\":\"value\"}");

        SnapAbstractResponse mockResponse = new SnapAbstractResponse();
        mockResponse.setResponseCode(SUCCESS.getCode());

        when(qrisRestClient.validateTokenSignature(anyString(), anyString(), anyString(),  any(), any())).thenReturn(mockResponse);

        // Act
        APIGatewayProxyResponseEvent response = validatorService.validateRintisHeader(mockEvent);

        // Assert
        assertNotNull(response);
//        assertEquals("{\"key\":\"value\"}", response.getBody());
//        assertEquals(mockEvent.getHeaders(), response.getHeaders());
//        verify(qrisRestClient, times(1)).validateTokenSignature(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testValidateRintisHeader_MissingAuthorization() throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, IOException {
        // Arrange
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setPath(QRIS_PAYMENT_CREDIT.getEndPoint());
        event.setHeaders(Map.of(
                Header.RTS_TIMESTAMP, "validTimestamp",
                Header.RTS_SIGNATURE, "validSignature"
        ));

        // Set a valid JSON body
        event.setBody("{\"key\":\"value\"}");

        // Act
        APIGatewayProxyResponseEvent response = validatorService.validateRintisHeader(event);

        // Assert
        assertNotNull(response);
        assertTrue(response.getBody().contains(UNAUTHENTICATED.getMessage()));
    }

    @Test
    void testValidateRintisHeader_MissingTimestamp() throws NoSuchAlgorithmException, SignatureException,
            InvalidKeySpecException, InvalidKeyException, IOException {
        // Arrange
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setPath(QRIS_PAYMENT_CREDIT.getEndPoint());
        event.setHeaders(Map.of(
                Header.AUTHORIZATION, "validToken",
                Header.RTS_SIGNATURE, "validSignature"
        ));

        // Set a valid JSON body
        event.setBody("{\"key\":\"value\"}");

        // Act
        APIGatewayProxyResponseEvent response = validatorService.validateRintisHeader(event);

        // Assert
        assertNotNull(response);
        assertTrue(response.getBody().contains(UNAUTHENTICATED.getMessage()));
    }

    @Test
    void testValidateRintisHeader_MissingSignature() throws NoSuchAlgorithmException, SignatureException,
            InvalidKeySpecException, InvalidKeyException, IOException {
        // Arrange
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setPath(QRIS_PAYMENT_CREDIT.getEndPoint());
        event.setHeaders(Map.of(
                Header.AUTHORIZATION, "validToken",
                Header.RTS_TIMESTAMP, "validTimestamp"
        ));

        // Set a valid JSON body
        event.setBody("{\"key\":\"value\"}");

        // Act
        APIGatewayProxyResponseEvent response = validatorService.validateRintisHeader(event);

        // Assert
        assertNotNull(response);
        assertTrue(response.getBody().contains(UNAUTHENTICATED.getMessage()));
    }

//    @Test
//    void testValidateBiSnapHeader_ApiAccessToken_Success() {
//        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
//        mockEvent.setPath(BiSnapApi.ACCESS_TOKEN_B2B.getEndpoint());
//
//        Map<String, String> header = new HashMap<>();
//        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
//        header.put(Header.X_CLIENT_KEY, "16341b80-7e3a-4861-bcb7-0c196062432e");
//        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
//        mockEvent.setHeaders(header);
//
//        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);
//        String responseCode = MessageFormat.format(
//                BiSnapResponseConstant.SUCCESSFUL.getCode(), BiSnapApi.ACCESS_TOKEN_B2B.getServiceCode()
//        );
//
//        assertNotNull(responseEvent);
//    }

//    @Test
//    void testValidateBiSnapHeader_B2b2cSuccess() {
//        // Arrange
//        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
//        mockEvent.setPath(BiSnapApi.VALIDATE_TOKEN_B2B2C.getEndpoint());
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
//        headers.put(Header.X_CLIENT_KEY, "clientKey");
//        headers.put(Header.X_PARTNER_ID, "partnerId");
//        headers.put(Header.X_EXTERNAL_ID, "externalId");
//        headers.put(Header.CHANNEL_ID, "channelId");
//        headers.put(Header.X_SIGNATURE, "signature");
//        headers.put(Header.AUTHORIZATION, "Bearer accessToken");
//        headers.put(Header.AUTHORIZATON_CUSTOMER, "Bearer accessTokenB2b2c");
//        mockEvent.setHeaders(headers);
//
//        // Mock responses for external API calls
//        SnapAbstractResponse mockValidateTokenResponse = new SnapAbstractResponse();
//        mockValidateTokenResponse.setResponseCode("00");  // Success response code
//        when(biSnapRestClient.validateToken(any())).thenReturn(mockValidateTokenResponse);
//
//        SnapAbstractResponse mockValidateB2b2cResponse = new SnapAbstractResponse();
//        mockValidateB2b2cResponse.setResponseCode("00");  // Success response code
//
//        // Ensure the mock returns a non-null response
//        when(biSnapRestClient.validateToken("Bearer accessToken")).thenReturn(mockValidateTokenResponse);
//        when(biSnapRestClient.validateTokenb2b2c("Bearer accessTokenB2b2c")).thenReturn(mockValidateB2b2cResponse);
//
//        // Spy on the validator service
//        ValidatorServiceImpl spyValidatorService = Mockito.spy(validatorService);
//
//        // Act
//        APIGatewayProxyResponseEvent responseEvent = spyValidatorService.validateBiSnapHeader(mockEvent);
//
//        // Assert
//        assertNotNull(responseEvent);
//        verify(biSnapRestClient).validateToken("Bearer accessToken");
//    }

    @Test
    void testValidateBiSnapHeader_B2b2cUnauthorized() throws IOException {
        // Given
        APIGatewayProxyRequestEvent mockEvent = mock(APIGatewayProxyRequestEvent.class);
        when(mockEvent.getPath()).thenReturn(BiSnapApi.VALIDATE_TOKEN_B2B2C.getEndpoint()); // Set path to B2B2C endpoint
        when(mockEvent.getHeaders()).thenReturn(Map.of(
                Header.X_TIMESTAMP, "valid-timestamp",
                Header.AUTHORIZATION, "invalid-token",
                Header.X_SIGNATURE, "valid-signature",
                Header.X_PARTNER_ID, "valid-partner-id",
                Header.X_CLIENT_KEY, "valid-client-key",
                Header.X_EXTERNAL_ID, "valid-external-id",
                Header.CHANNEL_ID, "valid-channel-id"
        ));

        SnapAbstractResponse mockResponse = new SnapAbstractResponse();
        mockResponse.setResponseCode("400");
        when(biSnapRestClient.validateTokenb2b2c(any(), any(ApplicationTokenRequest.class))).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = validatorService.validateBiSnapHeader(mockEvent);

        // Then
        assertNotNull(response);
        assertEquals(mockResponse.getResponseCode(), response.getStatusCode().toString());
    }

    @Test
    void testValidateBiSnapHeader_ApiAccessToken_InvalidTimestamp() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.ACCESS_TOKEN_B2B.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "InvalidTimestamp");
        header.put(Header.X_CLIENT_KEY, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

    @Test
    void testValidateBiSnapHeader_ApiAccessToken_MissingXClientKey() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.ACCESS_TOKEN_B2B.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_CLIENT_KEY, "");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

    @Test
    void testValidateBiSnapHeader_ApiAccessToken_MissingSignature() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.ACCESS_TOKEN_B2B.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_CLIENT_KEY, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_SIGNATURE, "");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

//    @Test
//    void testValidateBiSnapHeader_ApiTransaction_Success() {
//        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
//        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());
//
//        Map<String, String> header = new HashMap<>();
//        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
//        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
//        header.put(Header.X_EXTERNAL_ID, "1234567890");
//        header.put(Header.CHANNEL_ID, "001231");
//        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
//        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
//        mockEvent.setHeaders(header);
//
//        when(biSnapRestClient.validateToken(anyString()))
//                .thenReturn(new SnapAbstractResponse(SUCCESS.getCode(), SUCCESS.getMessage()));
//
//        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);
//
//        assertNotNull(responseEvent);
//    }

//    @Test
//    void testValidateBiSnapHeader_ApiTransaction_MissingAccessToken() {
//        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
//        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());
//
//        Map<String, String> header = new HashMap<>();
//        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
//        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
//        header.put(Header.X_EXTERNAL_ID, "1234567890");
//        header.put(Header.CHANNEL_ID, "001231");
//        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
//        header.put(Header.AUTHORIZATION, "");
//        mockEvent.setHeaders(header);
//
//        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);
//
//        assertNotNull(responseEvent);
//    }

//    @Test
//    void testValidateBiSnapHeader_ApiTransaction_InvalidAccessToken() {
//        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
//        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());
//
//        Map<String, String> header = new HashMap<>();
//        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
//        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
//        header.put(Header.X_EXTERNAL_ID, "1234567890");
//        header.put(Header.CHANNEL_ID, "001231");
//        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
//        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
//        mockEvent.setHeaders(header);
//
//        when(biSnapRestClient.validateToken(anyString()))
//                .thenReturn(new SnapAbstractResponse(INVALID_TOKEN.getCode(), INVALID_TOKEN.getMessage()));
//
//        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);
//
//        assertNotNull(responseEvent);
//    }

    @Test
    void testValidateBiSnapHeader_ApiTransaction_MissingPartnerId() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_PARTNER_ID, "");
        header.put(Header.X_EXTERNAL_ID, "1234567890");
        header.put(Header.CHANNEL_ID, "001231");
        header.put(Header.X_SIGNATURE, "Bws8ndRPcRtn+VmF5vJ5yKRGzCe9bbiDnLWB+ETjqvWW93wFSfZbNvJcsucI8gXxbJl3iGgrddyqg+ccDicu5qmxMrCglSjncAhp+7AcHneTOvfds/xvWWw0o5TpB4NMQT89X2hvBQTVZEEdfJ/HFgEcTuN973xprxVsuWCBHc1nt+iJYLXFu7Dp5875O+yQ1xF1PYv7WVWXGsJeTCFE/6YkIQczuNb7LOSPthiLVte0oIKhhKonJ1gCDTktSwyogzbWbP9Zwdn0aphXGIM/9Mig7q7ZIArRT5apXqtDyg2kjXOBlHlY5u7pLe+sdjOsJNxCbe6BAqGq43rNkTNg1w==");
        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

    @Test
    void testValidateBiSnapHeader_ApiTransaction_MissingSignature() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_EXTERNAL_ID, "1234567890");
        header.put(Header.CHANNEL_ID, "001231");
        header.put(Header.X_SIGNATURE, "");
        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

    @Test
    void testValidateBiSnapHeader_ApiTransaction_MissingExternalId() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_EXTERNAL_ID, "");
        header.put(Header.CHANNEL_ID, "001231");
        header.put(Header.X_SIGNATURE, "Signature");
        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }

    @Test
    void testValidateBiSnapHeader_ApiTransaction_MissingChannelId() throws IOException {
        APIGatewayProxyRequestEvent mockEvent = new APIGatewayProxyRequestEvent();
        mockEvent.setPath(BiSnapApi.QR_MPM_GENERATE.getEndpoint());

        Map<String, String> header = new HashMap<>();
        header.put(Header.X_TIMESTAMP, "2024-09-17T14:58:46+07:00");
        header.put(Header.X_PARTNER_ID, "16341b80-7e3a-4861-bcb7-0c196062432e");
        header.put(Header.X_EXTERNAL_ID, "1234567890");
        header.put(Header.CHANNEL_ID, "");
        header.put(Header.X_SIGNATURE, "Signature");
        header.put(Header.AUTHORIZATION, "Bearer Access-Token");
        mockEvent.setHeaders(header);

        APIGatewayProxyResponseEvent responseEvent = validatorService.validateBiSnapHeader(mockEvent);

        assertNotNull(responseEvent);
    }
}