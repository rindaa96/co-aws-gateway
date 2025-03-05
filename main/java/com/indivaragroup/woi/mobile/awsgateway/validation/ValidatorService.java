package com.indivaragroup.woi.mobile.awsgateway.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public interface ValidatorService {
    APIGatewayProxyResponseEvent validateResponseToken(APIGatewayProxyRequestEvent eventFromGateway) throws JsonProcessingException;
    APIGatewayProxyResponseEvent validateSignature(Map<String, String> param, String path) throws JsonProcessingException;
    APIGatewayProxyResponseEvent validateSecurityKey(String body);
    APIGatewayProxyResponseEvent validateAlgoCardlessHeader(APIGatewayProxyRequestEvent eventFromGateway) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, IOException;
    APIGatewayProxyResponseEvent validateRintisHeader(APIGatewayProxyRequestEvent eventFromGateway) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException;
    APIGatewayProxyResponseEvent validateBiSnapHeader(APIGatewayProxyRequestEvent eventFromGateway) throws IOException;
}
