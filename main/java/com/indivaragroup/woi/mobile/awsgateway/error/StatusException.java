package com.indivaragroup.woi.mobile.awsgateway.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusException implements Serializable {

  @Serial
  private static final long serialVersionUID = -245983780158153386L;

  private String code;
  private String text;
}
