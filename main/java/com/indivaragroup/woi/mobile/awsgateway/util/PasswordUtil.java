package com.indivaragroup.woi.mobile.awsgateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtil {

	public static String sha1(int iteration, String content) {
		log.info("[START] Hashing (SHA1) the content.");
		String hashResult = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");		
			digest.reset();
			byte[] input = digest.digest(content.getBytes("UTF-8"));
			for (int i = 1; i < iteration; i++) {
				digest.reset();
				input = digest.digest(input);
			}
			hashResult = byteToHex(input);
		} catch(Exception e) {
			log.error("failed to hash (SHA1) the content.", e);
		}
		log.info("[END] Done hashing (SHA1) the content.");
		return hashResult;
	}

	public static String byteToHex (byte[] data){
		char[] charData = Hex.encodeHex(data);
		return String.valueOf(charData);
	}

	public static String generateSignatureSHA256withRSA(String privateKeyBase64, String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

		String realPK = clearPrivateKey(privateKeyBase64);
		byte[] privateKeyBytes = Base64.getDecoder().decode(realPK);

		// Convert byte[] to PrivateKey
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		// Create Signature instance
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(stringToSign.getBytes());

		// Generate signature
		byte[] signatureBytes = signature.sign();

		// Encode signature to Base64
		return Base64.getEncoder().encodeToString(signatureBytes);
	}

	private static String clearPrivateKey(String pKey) {
		return pKey.replace("-----END PRIVATE KEY-----", "")
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("\n", "");
	}

	public static boolean verifyAsymmetricSignature(String stringToSign, String signature, String publicKey)
														throws NoSuchAlgorithmException, InvalidKeySpecException,
																InvalidKeyException, SignatureException {
		String realPK = clearPublicKey(publicKey);
		log.info("publicKey : {}", realPK);

		byte[] publicKeyBytes = Base64.getDecoder().decode(realPK);
		log.info("publicKeyBytes : {}", publicKeyBytes);

		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		log.info("publicKeySpec : {}", publicKeySpec);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		log.info("keyFactory : {}", keyFactory);

		PublicKey pk = keyFactory.generatePublic(publicKeySpec);
		log.info("pk : {}", pk);

		Signature sign = Signature.getInstance("SHA256withRSA");

		sign.initVerify(pk);
		sign.update(stringToSign.getBytes(StandardCharsets.UTF_8));
		log.info("sign : {}", sign);

		byte[] s = Base64.getDecoder().decode(signature);
		log.info("byte[] s : {}", s);

		return sign.verify(s);
	}
	private static String clearPublicKey(String pKey) {
		return pKey.replace("-----END PUBLIC KEY-----", "")
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("\n", "")
				.replaceAll("\\s+", "");
	}

	public static String minifyBody(String input) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object jsonObject = mapper.readValue(input, Object.class);
		return mapper.writeValueAsString(jsonObject);
	}

	public static String generateSymmetricSignature(
			String httpMethod,
			String endPoint,
			String b2bAccessToken,
			String timestamp,
			String body,
			String clientSecret
	) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		log.info("Creating Transaction Signature...");

		String minifyBody = minifyBody(body);
		log.info("Minify Body : {}", minifyBody);

		String hexEncodeBody = sha256Hex(minifyBody);
		log.info("Hex Encode Body : {}", hexEncodeBody);

		String stringToSign = String.join(":", httpMethod, endPoint, b2bAccessToken, hexEncodeBody, timestamp);
		log.info("String to Sign : {}", stringToSign);

		SecretKeySpec secretKey = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA512");
		Mac hmac = Mac.getInstance("HmacSHA512");
		hmac.init(secretKey);

		String signature = Base64.getEncoder().encodeToString(hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
		log.info("Signature : {}", signature);

		return signature;
	}
}
