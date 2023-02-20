package uk.co.acta.awsidp.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Discovery {

    String issuer;
    String authorization_endpoint;
    String token_endpoint;
    String userinfo_endpoint;
    String jwks_uri;
    List<String> scopes_supported = new ArrayList<>();
    List<String> response_types_supported = new ArrayList<>();
    List<String> token_endpoint_auth_methods_supported = new ArrayList<>();

    public void addScope(String scope) {scopes_supported.add(scope);}
    public void addResponseType(String rt) {response_types_supported.add(rt);}
    public void addAuthMethod(String auth) {token_endpoint_auth_methods_supported.add(auth);}
}
