package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class HandlerApiGatewayLambdaProxy implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
    var number = Util.getNumberFromRequest(request.getQueryStringParameters(), request.getBody());
    var validationResult = Util.validateNumber(number, context.getLogger());
    var response = new APIGatewayProxyResponseEvent();
    response.setStatusCode(200);
    response.setBody(validationResult.toString());
    var corsHeaderValue = System.getenv("ACCESS_CONTROL_ALLOW_ORIGIN");
    if (corsHeaderValue != null && corsHeaderValue.length() > 0) {
      response.setHeaders(Map.of("Access-Control-Allow-Origin", corsHeaderValue));
    }
    return response;
  }
}
