package fi.henu.gdxextras;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Deserializer
{
	
	// Assumes string is UTF-8 encoded. size_bytes tells how many
	// bytes are used to represent the number of bytes in string.
	public static String deserializeString(ByteBuffer serialized, int size_bytes)
	{
		int len;
		if (size_bytes == 1) {
			len = serialized.get();
		} else if (size_bytes == 2) {
			len = serialized.getShort();
		} else if (size_bytes == 4) {
			len = serialized.getInt();
		} else {
			throw new RuntimeException("Invalid byte count " + size_bytes + " of string length!");
		}
		
		// Read bytes
		byte[] bytes = new byte[len];
		serialized.get(bytes, 0, len);
		
		// Do conversion
		String result;
		try {
			result = new String(bytes, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 decoding is not available!");
		}
		return result;
	}

}
