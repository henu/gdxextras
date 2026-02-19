package fi.henu.gdxextras.types;

import java.util.Objects;

public class StringPair implements Comparable<StringPair>
{
	public String first;
	public String second;

	public StringPair(String first, String second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public int compareTo(StringPair other)
	{
		int cmp = this.first.compareTo(other.first);
		if (cmp != 0) return cmp;
		return this.second.compareTo(other.second);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof StringPair)) return false;
		StringPair that = (StringPair)o;
		return first.equals(that.first) && second.equals(that.second);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(first, second);
	}

	@Override
	public String toString()
	{
		return "(" + first + ", " + second + ")";
	}
}
