package fi.henu.gdxextras.collisions;

public class Collision2D
{
	public Collision2D(float normal_x, float normal_y, float depth)
	{
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		assert Math.abs(1 - Math.sqrt(normal_x * normal_x + normal_y * normal_y)) < 0.00001;
		this.depth = depth;
	}

	public float getNormalX()
	{
		return normal_x;
	}

	public float getNormalY()
	{
		return normal_y;
	}

	public float getDepth()
	{
		return depth;
	}

	public void setDepth(float depth)
	{
		this.depth = depth;
	}

	private float normal_x, normal_y;
	private float depth;
}