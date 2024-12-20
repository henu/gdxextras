package fi.henu.gdxextras.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class StringUtils
{
	public static String random(int length)
	{
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Random rand = new Random();

		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; ++ i) {
			sb.append(chars.charAt(rand.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public static String filled(String substr, int length)
	{
		String result = "";
		while (result.length() < length) {
			result += substr;
		}
		if (result.length() > length) {
			result = result.substring(0, length);
		}
		return result;
	}

	public static String leftPad(String str, String substr, int length)
	{
		if (str == null) {
			return filled(substr, length);
		}
		return filled(substr, length - str.length()) + str;
	}

	public static Array<String> split(String str, String delimiter)
	{
		Array<String> splitted = new Array<>();
		if (str != null) {
			String substr = "";
			int i = 0;
			while (i + delimiter.length() <= str.length()) {
				// If match
				if (str.substring(i, i + delimiter.length()).equals(delimiter)) {
					splitted.add(substr);
					substr = "";
					i += delimiter.length();
				}
				// If no match
				else {
					substr += str.charAt(i ++);
				}
			}
			substr += str.substring(i);
			splitted.add(substr);
		}
		return splitted;
	}

	public static Array<String> split(String str, String delimiter, boolean include_empty)
	{
		Array<String> splitted = split(str, delimiter);
		if (!include_empty) {
			for (int i = 0; i < splitted.size; ) {
				if (splitted.get(i).isEmpty()) {
					splitted.removeIndex(i);
				} else {
					++ i;
				}
			}
		}
		return splitted;
	}

	public static String capitalize(String str)
	{
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
