package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.RintisValidateTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class QrisRestClientTest {

    @Mock
    private QrisEndPoint qrisEndPoint;

    @InjectMocks
    private QrisRestClient qrisRestClient;

    @Mock
    private Call<SnapAbstractResponse> mockCall;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testValidateTokenSignature_Success() throws IOException {
        // Arrange
        String token = "validToken";
        String timestamp = "validTimestamp";
        String signature = "validSignature";
        String reqUri = "/api/qris/validate/token-signature";
        String requestBody = "{\"requestBody\": \"minifiedBody\"}";

        SnapAbstractResponse mockResponse = new SnapAbstractResponse();
        mockResponse.setResponseCode("00");
        mockResponse.setResponseMessage("Success");

        Response<SnapAbstractResponse> retrofitResponse = Response.success(mockResponse);

        when(qrisEndPoint.validateTokenSignature(anyString(), anyString(), anyString(), any() ,any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(retrofitResponse);

        // Act
        SnapAbstractResponse response = qrisRestClient.validateTokenSignature(token, timestamp, signature,
                reqUri, requestBody);

        // Assert
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("Success", response.getResponseMessage());
        verify(qrisEndPoint, times(1)).validateTokenSignature(token, timestamp, signature, reqUri,requestBody);
        verify(mockCall, times(1)).execute();
    }

    @Test
    void testValidateTokenSignature_Failure() throws IOException {
        // Arrange
        String token = "validToken";
        String timestamp = "validTimestamp";
        String signature = "validSignature";
        String reqUri = "/api/qris/validate/token-signature";
        String requestBody = "{\"requestBody\": \"minifiedBody\"}";

        when(qrisEndPoint.validateTokenSignature(anyString(), anyString(), anyString(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        // Act
        SnapAbstractResponse response = qrisRestClient.validateTokenSignature(token, timestamp, signature,
                reqUri, requestBody);

        // Assert
        assertNull(response);
        verify(qrisEndPoint, times(1)).validateTokenSignature(token, timestamp, signature, reqUri,
                requestBody);
        verify(mockCall, times(1)).execute();
    }

    @Test
    void testValidateTokenSignature_NullResponse() throws IOException {
        // Arrange
        String token = "validToken";
        String timestamp = "validTimestamp";
        String signature = "validSignature";
        String reqUri = "/api/qris/validate/token-signature";
        String requestBody = "{\"requestBody\": \"minifiedBody\"}";

        when(qrisEndPoint.validateTokenSignature(anyString(), anyString(), anyString(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(null);

        // Act
        SnapAbstractResponse response = qrisRestClient.validateTokenSignature(token, timestamp, signature,
         reqUri, requestBody);

        // Assert
        assertNull(response);
        verify(qrisEndPoint, times(1)).validateTokenSignature(token, timestamp, signature, reqUri, requestBody);
        verify(mockCall, times(1)).execute();
    }

}