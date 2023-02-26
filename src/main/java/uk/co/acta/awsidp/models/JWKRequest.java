package uk.co.acta.awsidp.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @AllArgsConstructor
public class JWKRequest {
    String url;
    String host;
    Map<String, List<String>> headers;


}
