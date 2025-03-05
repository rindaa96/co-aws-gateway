package com.indivaragroup.woi.mobile.awsgateway.error;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.indivaragroup.woi.mobile.awsgateway.apipath.AlgoCardlessApi;
import com.indivaragroup.woi.mobile.awsgateway.apipath.BiSnapApi;
import com.indivaragroup.woi.mobile.awsgateway.apipath.RintisApi;
import com.indivaragroup.woi.mobile.awsgateway.constant.BiSnapResponseConstant;
import com.indivaragroup.woi.mobile.awsgateway.constant.RintisErrorConstant;
import com.indivaragroup.woi.mobile.awsgateway.util.CommonUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorUtils {
    public static APIGatewayProxyResponseEvent createErrorResponse(int statusCode,
                                                                   String errorCode,
                                                                   String errorMessage) {
        APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();

        BaseBpiResponse error = BaseBpiResponse.builder().failure(true).status(BaseBpiResponse.Status.builder()
                .code(errorCode)
                .text(errorMessage).build()).build();

        errorResponse.setStatusCode(statusCode);
        errorResponse.setBody(CommonUtils.JsontoString(error));
        return errorResponse;
    }

    public static APIGatewayProxyResponseEvent createAlgoErrorResponse(BiSnapResponseConstant responseConstant,
                                                                       AlgoCardlessApi endPoint,
                                                                       String errorMessage) {
        APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();

        SnapAbstractResponse error = SnapAbstractResponse.builder()
                .responseCode(MessageFormat.format(responseConstant.getCode(), endPoint.getCode()))
                .responseMessage(errorMessage != null ? errorMessage : responseConstant.getMessage()).build();

        log.info("error : {}", MessageFormat.format(responseConstant.getCode(), endPoint.getCode()));
        errorResponse.setStatusCode(500);
        errorResponse.setBody(CommonUtils.JsontoString(error));

        log.info("errorResponse : {}", errorResponse);
        return errorResponse;
    }

    public static APIGatewayProxyResponseEvent createBiSnapErrorResponse(
            BiSnapResponseConstant responseConstant,
            BiSnapApi endpoint
    ) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        SnapAbstractResponse error = SnapAbstractResponse.builder()
                .responseCode(MessageFormat.format(responseConstant.getCode(), endpoint.getServiceCode()))
                .responseMessage(responseConstant.getMessage())
                .build();

        log.info("Error : {}", MessageFormat.format(responseConstant.getCode(), endpoint.getServiceCode()));
        response.setStatusCode(Integer.valueOf(responseConstant.getCode().substring(0, 3)));
        response.setBody(CommonUtils.JsontoString(error));

        log.info("Response : {}", response);
        return response;
    }

    public static APIGatewayProxyResponseEvent createRintisErrorResponse(RintisErrorConstant errorConstant) {
        APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();

        ErrorResponse error = new ErrorResponse();
        error.setCode(errorConstant.getCode());
        error.setMessage(errorConstant.getMessage());

        GeneralRintisResponse response = new GeneralRintisResponse();
        response.setRes(error);

        log.info("error : {}", errorConstant.getMessage());
        errorResponse.setStatusCode(errorConstant.getHttpStatus());
        errorResponse.setBody(CommonUtils.JsontoString(response));

        log.info("errorResponse : {}", errorResponse);
        return errorResponse;
    }
}
