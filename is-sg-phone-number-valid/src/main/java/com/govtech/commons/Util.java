package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

public class Util {

  private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

  private static final int EXPECTED_PHONE_NUMBER_LENGTH = 8;

  private static final String KEY_PHONE_NUMBER = "number";
  private static final String REGION = "SG";

  private static final RSAPrivateKey PRIVATE_KEY;

  public static RSAPrivateKey getPrivateKey(String fileName) {
    if (fileName == null || fileName.length() == 0) {
      return null;
    }
    try (var is = Util.class.getClassLoader().getResourceAsStream(fileName)) {
      if (is == null) {
        return null;
      }
      var fileContents = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      var pem = fileContents.replace("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll("(\\r\\n|\\r|\\n)", "")
        .replace("-----END PRIVATE KEY-----", "");
      var encoded = Base64.getDecoder().decode(pem);
      var keyFactory = KeyFactory.getInstance("RSA");
      var keySpec = new PKCS8EncodedKeySpec(encoded);
      return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    } catch (IOException e) {
      System.out.println("Unable to read file " + fileName);
      return null;
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Invalid algorithm");
      return null;
    } catch (InvalidKeySpecException e) {
      System.out.println("Invalid key");
      return null;
    }
  }

  static {
    var fileName = System.getenv("KEY_FILE");
    if (fileName == null || fileName.length() == 0) {
      PRIVATE_KEY = null;
    } else {
      PRIVATE_KEY = getPrivateKey(fileName);
    }
  }

  public static String decrypt(String input, RSAPrivateKey privateKey) {
    try {
      var cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      var decoded = Base64.getDecoder().decode(input);
      return new String(cipher.doFinal(decoded));
    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
      System.out.println("Invalid padding / algorithm specified");
      return null;
    } catch (InvalidKeyException e) {
      System.out.println("Invalid private key");
      return null;
    } catch (IllegalBlockSizeException e) {
      System.out.println("Invalid block size in encrypted input");
      return null;
    } catch (BadPaddingException e) {
      System.out.println("Bad padding in encrypted input");
      return null;
    }
  }

  public static JSONObject validateNumber(String number, LambdaLogger logger) {
    if (number == null || number.length() == 0) {
      logger.log("No number received" + System.lineSeparator());
      return createValidationResult(number, false);
    }
    var sanitizedNumber = number.trim().replaceAll("\\s", "");
    var maskedNumber = maskNumber(sanitizedNumber, 4);
    logger.log("Validating number received: " + maskedNumber + System.lineSeparator());
    if (sanitizedNumber.length() != EXPECTED_PHONE_NUMBER_LENGTH) {
      return createValidationResult(number, false);
    }
    try {
      var phoneNumber = PHONE_NUMBER_UTIL.parse(number, REGION);;
      boolean isValidNumber = PHONE_NUMBER_UTIL.isValidNumber(phoneNumber);
      return createValidationResult(number, isValidNumber);
    } catch (Exception e) {
      logger.log("Unable to parse number: " + maskedNumber + System.lineSeparator() + e + System.lineSeparator());
      return createValidationResult(number, false);
    }
  }

  public static String getNumberFromRequest(Map<String, String> queryParams, String body) {
    if (queryParams == null || queryParams.isEmpty()) {
      if (body == null || body.length() == 0) {
        return null;
      }
      try {
        var bodyObj = new JSONObject(body);
        var number = bodyObj.getString(KEY_PHONE_NUMBER);
        if (PRIVATE_KEY != null) {
          return decrypt(number, PRIVATE_KEY);
        }
        return number;
      } catch (Exception e) {
        return null;
      }
    }
    var number = queryParams.get(KEY_PHONE_NUMBER);
    if (PRIVATE_KEY != null) {
      return decrypt(number, PRIVATE_KEY);
    }
    return number;
  }

  public static JSONObject createValidationResult(String number, boolean isValid) {
    var result = new JSONObject();
    result.put("number", number == null ? JSONObject.NULL : number);
    result.put("isValid", isValid);
    return result;
  }

  public static String maskNumber(String number, int numCharsToKeepAtEnd) {
    if (number == null || number.length() <= numCharsToKeepAtEnd) {
      return number;
    }
    char[] tokens = number.toCharArray();
    int startIndex = tokens.length - numCharsToKeepAtEnd;
    number.getChars(startIndex, tokens.length, tokens, startIndex);
    Arrays.fill(tokens, 0, startIndex, '*');
    return new String(tokens);
  }
}
