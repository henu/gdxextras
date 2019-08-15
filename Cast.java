package fi.henu.gdxextras;

public class Cast
{
	public static byte[] hexToBytes(String str)
	{
		if (str.length() % 2 != 0) {
			throw new RuntimeException("Hex string must have even length!");
		}
		int bytes_len = str.length() / 2;
		byte[] bytes = new byte[bytes_len];
		for (int i = 0; i < bytes_len; ++ i) {
			char c1 = str.charAt(i * 2 + 1);
			char c2 = str.charAt(i * 2);
			byte b = 0;
			if (c1 >= '0' && c1 <= '9') {
				b += c1 - '0';
			} else if (c1 >= 'a' && c1 <= 'f') {
				b += 10 + c1 - 'a';
			} else if (c1 >= 'A' && c1 <= 'F') {
				b += 10 + c1 - 'A';
			} else {
				throw new RuntimeException("Invalid characters \"" + c1 + "\" in hex string!");
			}
			if (c2 >= '0' && c2 <= '9') {
				b += (c2 - '0') * 16;
			} else if (c2 >= 'a' && c2 <= 'f') {
				b += (10 + c2 - 'a') * 16;
			} else if (c2 >= 'A' && c2 <= 'F') {
				b += (10 + c2 - 'A') * 16;
			} else {
				throw new RuntimeException("Invalid characters \"" + c1 + "\" in hex string!");
			}
			bytes[i] = b;
		}
		return bytes;
	}

	public static String bytesToHex(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
