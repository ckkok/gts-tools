package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandlerApiGatewayLambdaProxyTest {
  @Test
  @DisplayName("Given a valid number in body, return isValid as true and statusCode 200")
  void givenValidNumberInBodyReturnIsValidTrue() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    request.setBody("{\"number\":\"91234567\"}");
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals(200, response.getStatusCode());
    var responseBody = new JSONObject(response.getBody());
    assertTrue(responseBody.getBoolean("isValid"));
  }

  @Test
  @DisplayName("Given a valid number in query parameters, return isValid as true and statusCode 200")
  void givenValidNumberInQueryParamsReturnIsValidTrue() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    var queryParams = Map.of("number", "91234567");
    request.setQueryStringParameters(queryParams);
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals(200, response.getStatusCode());
    var responseBody = new JSONObject(response.getBody());
    assertTrue(responseBody.getBoolean("isValid"));
  }

  @Test
  @DisplayName("Given a blank number, return isValid as false and statusCode 200")
  void givenBlankNumberReturnIsValidTrue() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    request.setBody("{\"number\":\"\"}");
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals(200, response.getStatusCode());
    var responseBody = new JSONObject(response.getBody());
    assertFalse(responseBody.getBoolean("isValid"));
  }

  @Test
  @DisplayName("Given a number without 8 characters post-trim, return isValid as false and statusCode 200")
  void givenNumberNotOfExpectedLengthPostTrimReturnIsValidTrue() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    request.setBody("{\"number\":\" 1 2 3 4 \"}");
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals(200, response.getStatusCode());
    var responseBody = new JSONObject(response.getBody());
    assertFalse(responseBody.getBoolean("isValid"));
  }

  @Test
  @DisplayName("Given no input, return isValid as false and statusCode 200")
  void givenNullInputReturnIsValidFalse() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals(200, response.getStatusCode());
    var responseBody = new JSONObject(response.getBody());
    assertFalse(responseBody.getBoolean("isValid"));
  }

  @Test
  @SetEnvironmentVariable(key="ACCESS_CONTROL_ALLOW_ORIGIN", value="*")
  @DisplayName("Given ACCESS_CONTROL_ALLOW_ORIGIN environment variable, set its value in the response headers")
  void givenAccessControlAllowOriginEnvVarSetCORSResponseHeader() {
    var mockContext = mock(Context.class);
    var mockLogger = mock(LambdaLogger.class);
    when(mockContext.getLogger()).thenReturn(mockLogger);
    var request = new APIGatewayProxyRequestEvent();
    request.setBody("{\"number\":\"91234567\"}");
    var app = new HandlerApiGatewayLambdaProxy();
    var response = app.handleRequest(request, mockContext);
    assertEquals("*", response.getHeaders().get("Access-Control-Allow-Origin"));
  }
}
