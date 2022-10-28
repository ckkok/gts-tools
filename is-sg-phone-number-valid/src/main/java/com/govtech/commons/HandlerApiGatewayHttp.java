package com.govtech.commons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class HandlerApiGatewayHttp implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
    System.out.println(request);
    var number = Util.getNumberFromRequest(request.getQueryStringParameters(), request.getBody());
    var validationResult = Util.validateNumber(number, context.getLogger());
    var response = new APIGatewayV2HTTPResponse();
    response.setStatusCode(200);
    response.setBody(validationResult.toString());
    return response;
  }
}