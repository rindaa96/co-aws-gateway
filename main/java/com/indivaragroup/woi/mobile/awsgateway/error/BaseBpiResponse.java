package com.indivaragroup.woi.mobile.awsgateway.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseBpiResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 3293414188037181384L;

  @JsonProperty("failure")
  private boolean failure;
  private Status status;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Status implements Serializable{

    @Serial
    private static final long serialVersionUID = 175716376610751711L;
    private String code;
    private String text;
  }
}
