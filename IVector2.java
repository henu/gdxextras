package fi.henu.gdxextras;

public class IVector2
{
	
	public int x, y;
	
	public IVector2()
	{
		x = 0;
		y = 0;
	}

	public IVector2(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public IVector2(IVector2 v)
	{
		x = v.x;
		y = v.y;
	}

	public void set(IVector2 pos)
	{
		x = pos.x;
		y = pos.y;
	}

	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	
	@Override
	public final boolean equals(Object o)
	{
		if (o instanceof IVector2) {
			IVector2 v = (IVector2)o;
			return equals(v.x, v.y);
		}
		return false;
	}

	public boolean equals(int x, int y)
	{
		return x == this.x && y == this.y;
	}

	public float distanceTo(IVector2 pos)
	{
		return (float)Math.sqrt(distanceTo2(pos));
	}

	public long distanceTo2(IVector2 pos)
	{
		long xdiff = pos.x - x;
		long ydiff = pos.y - y;
		return xdiff * xdiff + ydiff * ydiff;
	}

	public int chebyshevDistanceTo(IVector2 pos)
	{
		return Math.max(Math.abs(pos.x - x), Math.abs(pos.y - y));
	}

	public IVector2 cpy()
	{
		return new IVector2(x, y);
	}

	public void scl(int i) {
		x *= i;
		y *= i;
	}

	public void sub(IVector2 pos)
	{
		sub(pos.x, pos.y);
	}

	private void sub(int x, int y) {
		this.x -= x;
		this.y -= y;
	}

	@Override
	public int hashCode()
	{
		return x * 991 + y * 997;
	}
}
