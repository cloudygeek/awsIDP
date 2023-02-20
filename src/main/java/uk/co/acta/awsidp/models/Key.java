package uk.co.acta.awsidp.models;

import lombok.Data;

@Data
public class Key {
    String kid;
    String crv;
    String kty;
    String alg;
    String use;
    String n;
    String e;
    String x;

}
