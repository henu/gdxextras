package fi.henu.gdxextras.utils;

public class ArrayUtils
{
	public static boolean contains(Object[] haystack, Object needle)
	{
		for (Object item : haystack) {
			if (needle == null && item == null) return true;
			if (needle != null && needle.equals(item)) return true;
		}
		return false;
	}
}
