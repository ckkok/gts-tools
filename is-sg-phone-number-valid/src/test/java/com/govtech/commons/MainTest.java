package com.govtech.commons;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class MainTest {
  @Test
  void test() {
    var result = new JSONObject();
    result.put("phoneNumber", JSONObject.NULL);
    result.put("isValid", true);
    var responseString = result.toString();
    System.out.println(responseString);
  }
}
