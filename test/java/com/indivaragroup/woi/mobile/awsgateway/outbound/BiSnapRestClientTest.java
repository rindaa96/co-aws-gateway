package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.constant.ErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.dto.ApplicationTokenRequest;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BiSnapRestClientTest {
    @Mock
    private BiSnapEndpoint biSnapEndpoint;

    @InjectMocks
    private BiSnapRestClient biSnapRestClient;

    @Mock
    private Call<SnapAbstractResponse> mockCall;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testValidateToken_Success() throws IOException {
        String token = "Bearer accessToken";
        String clientKey = "clientkey";

        SnapAbstractResponse mockResponse = new SnapAbstractResponse();
        mockResponse.setResponseCode(ErrorConstant.SUCCESS.getCode());
        mockResponse.setResponseMessage(ErrorConstant.SUCCESS.getMessage());

        Response<SnapAbstractResponse> retrofitResponse = Response.success(mockResponse);

        when(biSnapEndpoint.validateToken(any(), any(ApplicationTokenRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(retrofitResponse);

        SnapAbstractResponse response = biSnapRestClient.validateToken(clientKey, new ApplicationTokenRequest(token));

        assertNotNull(response);
        assertEquals(ErrorConstant.SUCCESS.getCode(), response.getResponseCode());
        assertEquals(ErrorConstant.SUCCESS.getMessage(), response.getResponseMessage());
        verify(mockCall, times(1)).execute();
    }

    @Test
    void testValidateToken_Failure() throws IOException {
        String token = "Bearer accessToken";
        String clientKey = "clientkey";

        SnapAbstractResponse mockResponse = new SnapAbstractResponse();
        mockResponse.setResponseCode(ErrorConstant.INVALID_TOKEN.getCode());
        mockResponse.setResponseMessage(ErrorConstant.INVALID_TOKEN.getMessage());

        Response<SnapAbstractResponse> retrofitResponse = Response.success(mockResponse);

        when(biSnapEndpoint.validateToken(any(), any(ApplicationTokenRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(retrofitResponse);

        SnapAbstractResponse response = biSnapRestClient.validateToken(clientKey, new ApplicationTokenRequest(token));

        assertNotNull(response);
        assertEquals(ErrorConstant.INVALID_TOKEN.getCode(), response.getResponseCode());
        assertEquals(ErrorConstant.INVALID_TOKEN.getMessage(), response.getResponseMessage());
        verify(mockCall, times(1)).execute();
    }

    @Test
    void testValidateToken_Exception() throws IOException {
        String token = "Bearer accessToken";
        String clientKey = "clientkey";

        when(biSnapEndpoint.validateToken(any(), any(ApplicationTokenRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network Error"));

        SnapAbstractResponse response = biSnapRestClient.validateToken(clientKey, new ApplicationTokenRequest(token));

        assertNull(response);
        verify(mockCall, times(1)).execute();
    }

    @Test
    void testValidateToken_NullResponse() throws IOException {
        String token = "Bearer accessToken";
        String clientKey = "clientkey";

        when(biSnapEndpoint.validateToken(any(), any(ApplicationTokenRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(null);

        SnapAbstractResponse response = biSnapRestClient.validateToken(clientKey, new ApplicationTokenRequest(token));

        assertNull(response);
        verify(mockCall, times(1)).execute();
    }
}
