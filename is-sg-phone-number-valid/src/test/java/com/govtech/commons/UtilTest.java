package com.govtech.commons;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UtilTest {

  @Test
  @DisplayName("Given a file name for a private key, uses it to read a private key for decryption")
  void GivenFileNameForPrivateKeyUseGivenNameToLoadPrivateKey() {
    var encrypted = encrypt("91234567");
    var decrypted = Util.decrypt(encrypted, Util.getPrivateKey("another_private.key"));
    assertEquals("91234567", decrypted);
  }

  private String encrypt(String input) {
    try (var is = this.getClass().getClassLoader().getResourceAsStream("public.key")) {
      assertNotNull(is);
      var contents = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      var key = contents
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replaceAll("(\\r\\n|\\r|\\n)", "")
        .replace("-----END PUBLIC KEY-----", "");
      var encoded = Base64.getDecoder().decode(key);
      var keyFactory = KeyFactory.getInstance("RSA");
      var keySpec = new X509EncodedKeySpec(encoded);
      var publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
      var cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      return new String(Base64.getEncoder().encode(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8))));
    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
             InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }
}
