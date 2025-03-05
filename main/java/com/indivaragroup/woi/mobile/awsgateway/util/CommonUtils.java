package com.indivaragroup.woi.mobile.awsgateway.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {
  public static String JsontoString(Object object) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return mapper.writeValueAsString(object);
    } catch (NullPointerException | IOException e) {
      log.error("Error Converting to object {} to json", object, e);
    }

    return null;
  }

  public static <T> T jsonToObject(String jsonString, Class<T> clazz) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(jsonString, clazz);
  }

  public static String extractVariable(String jsonString, String variable) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(jsonString);
      JsonNode issuerNode = rootNode.get(variable);

      if (issuerNode != null) {
        return issuerNode.asText();
      } else {
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
