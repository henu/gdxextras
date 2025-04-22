package fi.henu.gdxextras.utils;

public class NumberUtils
{
	public static String extractNumberFromString(String num)
	{
		if (num == null) {
			return null;
		}

		// Get rid of characters that are always problematic
		num = num.replace(" ", "");
		num = num.replace("'", "");

		// If there are multiple commas, then get rid of those
		int commas = StringUtils.countSubstrings(num, ",");
		if (commas > 1) {
			num = num.replace(",", "");
			commas = 0;
		}

		// Count dots, and check if we have problems
		int dots = StringUtils.countSubstrings(num, ".");
		// If there are both comma and dot
		if (dots == 1 && commas == 1) {
			int comma_pos = num.indexOf(',');
			int dot_pos = num.indexOf('.');
			if (comma_pos < dot_pos) {
				num = num.replace(",", "");
			} else {
				num = num.replace(".", "");
				num = num.replace(",", ".");
			}
		}
		// If there are only comma
		else if (dots == 0 && commas == 1) {
			num = num.replace(",", ".");
		}
		// If there is something really weird going on, then just give up
		else if (dots > 1) {
			return null;
		}

		// Remove all but number
		num = num.replaceAll("[^0-9.]", "");

		// If there is nothing sensible left
		if (num.isEmpty() || ".".equals(num)) {
			return null;
		}

		return num;
	}
}
