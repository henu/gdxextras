package fi.henu.gdxextras.types;

import java.util.Objects;

public final class OrderedStringPair implements Comparable<OrderedStringPair>
{
	public OrderedStringPair(String first, String second)
	{
		if (first == null || second == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		if (first.compareTo(second) <= 0) {
			this.first = first;
			this.second = second;
		} else {
			this.first = second;
			this.second = first;
		}
	}

	public String getFirst()
	{
		return first;
	}

	public String getSecond()
	{
		return second;
	}

	@Override
	public int compareTo(OrderedStringPair other)
	{
		int cmp = this.first.compareTo(other.first);
		if (cmp != 0) return cmp;
		return this.second.compareTo(other.second);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof OrderedStringPair)) return false;
		OrderedStringPair that = (OrderedStringPair)o;
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

	private final String first;
	private final String second;
}
