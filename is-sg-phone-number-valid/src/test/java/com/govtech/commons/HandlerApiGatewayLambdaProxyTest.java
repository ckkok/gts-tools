package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandlerApiGatewayLambdaProxyTest {
  @Test
  @DisplayName("Given a valid number, return isValid as true and statusCode 200")
  void givenValidNumberReturnIsValidTrue() {
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
}
