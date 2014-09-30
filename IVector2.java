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
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

}
