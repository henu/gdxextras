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

	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	
	public boolean equals(IVector2 v)
	{
		return v.x == x && v.y == y;
	}

	public IVector2 cpy()
	{
		return new IVector2(x, y);
	}

}
