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

	public Collision2D()
	{
		normal_x = 1;
		normal_y = 0;
		depth = 0;
	}

	public void set(Collision2D coll)
	{
		normal_x = coll.normal_x;
		normal_y = coll.normal_y;
		depth = coll.depth;
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

	public void setNormal(float normal_x, float normal_y)
	{
		assert Math.abs(1 - Math.sqrt(normal_x * normal_x + normal_y * normal_y)) < 0.00001;
		this.normal_x = normal_x;
		this.normal_y = normal_y;
	}

	public void setDepth(float depth)
	{
		this.depth = depth;
	}

	private float normal_x, normal_y;
	private float depth;
}
