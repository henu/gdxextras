package fi.henu.gdxextras.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hash
{
	public static byte[] SHA256(byte[] data)
	{
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize SHA256!");
		}
		return digest.digest(data);
	}

	public static byte[] PDKDF2SHA256(int iterations, String salt, String data)
	{
		KeySpec spec = new PBEKeySpec(data.toCharArray(), salt.getBytes(), iterations, 256);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			return factory.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize PDKF2 SHA256!");
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("Invalid key!");
		}
	}
}
