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

        //KeyPairGenerator g = KeyPairGenerator.getInstance("EC","SunEC");
        KeyPairGenerator g = KeyPairGenerator.getInstance(curveName,"SunEC");
        System.out.println(g.getProvider());
        System.out.println(g.getAlgorithm());
        ECGenParameterSpec ecsp = new ECGenParameterSpec(curveName);
        g.initialize(ecsp, secureRandom);


        KeyPairGenerator kpg = KeyPairGenerator.getInstance(curveName);
        kpg.initialize(new ECGenParameterSpec(curveName));
        KeyPair kp = kpg.generateKeyPair();

        EdECPublicKey publicKey = (EdECPublicKey) kp.getPublic();
        EdECPrivateKey privateKey = (EdECPrivateKey) kp.getPrivate();
        System.out.println("Encoding format: " + publicKey.getFormat());
        byte[] derEncoded = publicKey.getEncoded();
// base64 encoded is what you get for PEM, between the header and footer lines
        String base64DEREncoded = Base64.getEncoder().encodeToString(derEncoded);
        System.out.println("Base64 SubjectPublicKeyInfo: " + base64DEREncoded);


        System.out.println("Generated key pair " + kp.getClass());

        System.out.println("kp.getPublic().getAlgorithm() = " + kp.getPublic().getAlgorithm());
        System.out.println("got public key");

/*
	public ECKey(final Curve crv, final ECPublicKey pub, final PrivateKey priv,
		     final KeyUse use, final Set<KeyOperation> ops, final Algorithm alg, final String kid,
		     final URI x5u, final Base64URL x5t, final Base64URL x5t256, final List<Base64> x5c,
		     final Date exp, final Date nbf, final Date iat,
		     final KeyStore ks) {

 */
        Set<KeyOperation> ops = new HashSet<>(Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
        String kid = "1111";



  /*      // Generate EC key pair with P-256 curve
      KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();
*/
// Convert to JWK format

//        ECGenParameterSpec ecsp = new ECGenParameterSpec(curveName);
  //      g.initialize(ecsp, secureRandom);



        OctetKeyPair jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                .secureRandom(secureRandom)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key (optional)
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID (optional)
                .issueTime(new Date()) // issued-at timestamp (optional)
                .generate();


        System.out.println("got jwk");
        System.out.println(jwk.getKeyID());



        //ECKey ecKey = new ECKey(Curve.Ed25519, publicKey, kp.getPrivate(), KeyUse.SIGNATURE, ops, Algorithm.parse(publicKey.getAlgorithm()), kid);

        //ECKey jwk = new ECKey.Builder(ecKey).build();



        Key key = new Key();
        key.setUse("sig");
        key.setAlg(kp.getPublic().getAlgorithm());
        key.setX(Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));

        key.setCrv(curveName);
        key.setKty("OKP");

        keys.addKey(key);
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
