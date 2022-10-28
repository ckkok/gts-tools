package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.json.JSONObject;

import java.util.Arrays;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String KEY_PHONE_NUMBER = "number";
  private static final String REGION = "SG";

  private final PhoneNumberUtil phoneNumberUtil;
  private LambdaLogger logger;

  public Main() {
    this.phoneNumberUtil = PhoneNumberUtil.getInstance();
  }

  public static void main(String[] args) {
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
    var response = new APIGatewayProxyResponseEvent();
    logger = context.getLogger();
    var number = getNumberFromRequest(request);
    var validationResult = validateNumber(number);
    response.setStatusCode(200);
    response.setBody(validationResult.toString());
    return response;
  }

  private String getNumberFromRequest(APIGatewayProxyRequestEvent request) {
    var queryParams = request.getQueryStringParameters();
    if (queryParams == null) {
      var body = request.getBody();
      if (body == null) {
        return null;
      }
      try {
        var bodyObj = new JSONObject(body);
        return bodyObj.getString("number");
      } catch (Exception e) {
        return null;
      }
    }
    return queryParams.get(KEY_PHONE_NUMBER);
  }

  private JSONObject validateNumber(String number) {
    if (number == null || number.length() == 0) {
      logger.log("No number received" + System.lineSeparator());
      return createValidationResult(number, false);
    }
    var sanitizedNumber = number.trim().replaceAll("\\s", "");
    var maskedNumber = maskNumber(sanitizedNumber, 4);
    logger.log("Validating number received: " + maskedNumber + System.lineSeparator());
    if (sanitizedNumber.length() != 8) {
      return createValidationResult(number, false);
    }
    try {
      var phoneNumber = phoneNumberUtil.parse(number, REGION);;
      boolean isValidNumber = phoneNumberUtil.isValidNumber(phoneNumber);
      return createValidationResult(number, isValidNumber);
    } catch (Exception e) {
      logger.log("Unable to parse number: " + maskedNumber + System.lineSeparator() + e + System.lineSeparator());
      return createValidationResult(number, false);
    }
  }

  private JSONObject createValidationResult(String number, boolean isValid) {
    var result = new JSONObject();
    result.put("phoneNumber", number == null ? JSONObject.NULL : number);
    result.put("isValid", isValid);
    return result;
  }

  private static String maskNumber(String number, int numCharsToKeepAtEnd) {
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