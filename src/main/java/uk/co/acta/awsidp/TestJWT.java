package uk.co.acta.awsidp;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.services.kms.model.GenerateRandomRequest;
import software.amazon.awssdk.services.kms.model.GenerateRandomResponse;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class TestJWT {
    public static void main(String[] args) throws Exception {

/*
        for (Provider p : Security.getProviders()) {
            System.out.println(p.getName() + " " + p.getInfo());
            for (Provider.Service service : p.getServices()) {
                System.out.println(service.getAlgorithm());
                System.out.println(service.getAttribute("SupportedCurves"));
            }
        }
        Provider provider = Security.getProvider("SunEC");
        Provider.Service service = provider.getService("AlgorithmParameters", "EC");
        System.out.println(service.getAttribute("SupportedCurves"));

*/




/*
        GenerateRandomRequest request = GenerateRandomRequest.builder().numberOfBytes(512).build();
        KmsClient client = KmsClient.builder().region(Region.EU_WEST_2).build();
        GenerateRandomResponse response = client.generateRandom(request);


        byte[] seed = response.plaintext().asByteArray();

        */
        byte[] seed = "aadsasdasdasdasasdasd".getBytes(StandardCharsets.UTF_8);
        SecureRandom secureRandom = new SecureRandom(seed);


        KeyPairGenerator g = KeyPairGenerator.getInstance("EC","SunEC");
        ECGenParameterSpec ecsp = new ECGenParameterSpec("secp256r1");
        g.initialize(ecsp, secureRandom);

        KeyPair kp = g.genKeyPair();

        PrivateKey privKey = kp.getPrivate();
        PublicKey pubKey = kp.getPublic();



        sign(kp);




    }

    private static void sign(KeyPair key) {

        String publicKey = Base64.getEncoder().encodeToString(key.getPublic().getEncoded());


        System.out.println("-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----\n");


        String encodedString = "-----BEGIN PRIVATE KEY-----\n";
        encodedString = encodedString+Base64.getEncoder().encodeToString(key.getPrivate().getEncoded())+"\n";
        encodedString = encodedString+"-----END PRIVATE KEY-----\n";

        System.out.println(encodedString);


        Date now = new Date();

        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusMinutes(5);


            String jwtToken = Jwts.builder()
                    .claim("name", "Jane Doe")
                    .claim("email", "jane@example.com")
                    .setSubject("jane")
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(now)
                    .setExpiration(dateTime.toDate())
                    .signWith(key.getPrivate(), SignatureAlgorithm.ES256)
                    .compact();

            System.out.println("jwtToken\n" + jwtToken);

    }

}


