package uk.co.acta.awsidp.models;

import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class Key {
    String kid;
    String crv;
    String kty;
    String alg;
    String use;
    String n;
    String e;
    String x;
    long iat;
    long exp;

    public Key(OctetKeyPair jwk) {
        this.setKty(jwk.getKeyType().getValue());
        this.setUse(jwk.getKeyUse().getValue());
        this.setCrv(jwk.getCurve().getName());
        this.setKid(jwk.getKeyID());
        this.setX(jwk.getX().toString());
        this.setIat(jwk.getIssueTime().getTime()/1000);
        this.setExp(jwk.getExpirationTime().getTime()/1000);
    }
}
