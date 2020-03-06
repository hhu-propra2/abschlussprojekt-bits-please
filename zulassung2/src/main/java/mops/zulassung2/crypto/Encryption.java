package mops.zulassung2.crypto;

import mops.Zulassung2Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.security.*;

class Encryption implements EncryptionInterface{

  @Value("${dev_private_key}")
  private static PrivateKey privKey;
  @Value("${dev_public_key}")
  private static PublicKey publKey;

  private static final Logger logger = LoggerFactory.getLogger(Zulassung2Application.class);

  public byte[] encrypt(String toEncrypt) {

    try {
      SecureRandom secureRandom = new SecureRandom();
      Signature signature = Signature.getInstance("SHA256WithDSA");
      signature.initSign(privKey, secureRandom);
      byte[] data = toEncrypt.getBytes("UTF-8");
      signature.update(data);
      return signature.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
      logger.error(e.getMessage());
      return null;
    }

  }

  public boolean decrypt(String toDecrypt, String toVerify) {
    try {
      Signature signature = Signature.getInstance("SHA256WithDSA");
      signature.initVerify(publKey);

      byte[] data = toDecrypt.getBytes("UTF-8");
      signature.update(data);

      byte[] digitalSignature = toVerify.getBytes("UTF-8");

      return signature.verify(digitalSignature);
    } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
      logger.error(e.getMessage());
      return false;
    }
  }

}