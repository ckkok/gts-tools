package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

public class Util {

  private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

  private static final int EXPECTED_PHONE_NUMBER_LENGTH = 8;

  private static final String KEY_PHONE_NUMBER = "number";
  private static final String REGION = "SG";

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
        return bodyObj.getString(KEY_PHONE_NUMBER);
      } catch (Exception e) {
        return null;
      }
    }
    return queryParams.get(KEY_PHONE_NUMBER);
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
