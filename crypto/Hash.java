package fi.henu.gdxextras.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash
{
	public static byte[] SHA256(byte[] data)
	{
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize SHA256!");
		}
		return digest.digest(data);
	}
}
