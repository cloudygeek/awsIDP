package uk.co.acta.awsidp.models;

import lombok.Data;

@Data
public class LambdaResponse {
    int statusCode;
    String message;
}
