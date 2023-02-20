package uk.co.acta.awsidp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import com.nimbusds.jose.util.Base64URL;
import org.joda.time.DateTime;
import uk.co.acta.awsidp.models.Key;
import uk.co.acta.awsidp.models.Keys;


import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

public class TestKeys implements RequestHandler<Object, String> {


    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String curveName = Curve.Ed25519.getName();//"Ed25519";

    public static void main(String[] args) throws Exception {

        TestKeys tk = new TestKeys();

        Keys keys = new Keys();


        byte[] seed = "aadsasdasdasdasasdasd".getBytes(StandardCharsets.UTF_8);
        SecureRandom secureRandom = new SecureRandom(seed);
        System.out.println("Curve.Ed25519.getName() = " + Curve.Ed25519.getName());

        DateTime issuedAt = new DateTime();
        DateTime expiryTime = new DateTime();
        expiryTime = expiryTime.plusMinutes(5);
        String kid = UUID.randomUUID().toString();
        OctetKeyPair jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                .secureRandom(secureRandom)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key (optional)
                .keyID(kid) // give the key a unique ID (optional)
                .issueTime(issuedAt.toDate()) // issued-at timestamp (optional)
                .expirationTime(expiryTime.toDate())
                .generate();

        System.out.println("got jwk");

        Base64URL x = jwk.getX();
        Base64URL d = jwk.getD();

        System.out.println("x = " + x.toJSONString());
        System.out.println("d = " + d.toJSONString());


        /*

crv – The cryptographic curve. Must not be null.
x – The public 'x' parameter. Must not be null.
d – The private 'd' parameter. Must not be null.
use – The key use, null if not specified or if the key is intended for signing as well as encryption.
ops – The key operations, null if not specified.
alg – The intended JOSE algorithm for the key, null if not specified.
kid – The key ID, null if not specified.
x5u – The X.509 certificate URL, null if not specified.
x5t – The X.509 certificate SHA-1 thumbprint, null if not specified.
x5t256 – The X.509 certificate SHA-256 thumbprint, null if not specified.
x5c – The X.509 certificate chain, null if not specified.
exp – The key expiration time, null if not specified.
nbf – The key not-before time, null if not specified.
iat – The key issued-at time, null if not specified.
 ks – Reference to the underlying key store, null if not specified.

OctetKeyPair(final Curve crv, final Base64URL x,
			    final KeyUse use, final Set<KeyOperation> ops, final Algorithm alg, final String kid,
			    final URI x5u, final Base64URL x5t, final Base64URL x5t256, final List<Base64> x5c,
			    final Date exp, final Date nbf, final Date iat,
			    final KeyStore ks)

*/
        Set<KeyOperation> ops = new HashSet<>(Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));

        OctetKeyPair jwk2 = new OctetKeyPair(Curve.Ed25519, x, d,
                KeyUse.SIGNATURE, null, null, kid,
                null, null, null, null,
                expiryTime.toDate(), null, issuedAt.toDate(), null
        );


        System.out.println("got jwk2");

        // Output the private and public OKP JWK parameters
        //System.out.println("JWK " + jwk);

        // Output the public OKP JWK parameters only
        System.out.println("Public JWK " + jwk.toPublicJWK());
        System.out.println("Public JWK2 " + jwk2.toPublicJWK());

        Key k = new Key(jwk);
        Key k2 = new Key(jwk2);

        keys.addKey(k);
        keys.addKey(k2);
        String json = tk.gson.toJson(keys);
        System.out.println(json);
    }

    @Override
    public String handleRequest(Object event, Context context) {
        LambdaLogger logger = context.getLogger();

        // log execution details
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());

        List<Key> keys = new ArrayList<>();

        String json = gson.toJson(keys);
        logger.log("JSON = " + json);


        return json;
    }
}
