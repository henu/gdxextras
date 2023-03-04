package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.math.Vector3;

public class Collision
{
	public Collision(float normal_x, float normal_y, float normal_z, float depth)
	{
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		this.normal_z = normal_z;
		assert Math.abs(1 - Math.sqrt(normal_x * normal_x + normal_y * normal_y + normal_z * normal_z)) < 0.00001;
		this.depth = depth;
	}

	public Collision(float normal_x, float normal_y, float normal_z, float depth, boolean flip_normal)
	{
		if (flip_normal) {
			this.normal_x = -normal_x;
			this.normal_y = -normal_y;
			this.normal_z = -normal_z;
		} else {
			this.normal_x = normal_x;
			this.normal_y = normal_y;
			this.normal_z = normal_z;
		}
		assert Math.abs(1 - Math.sqrt(normal_x * normal_x + normal_y * normal_y + normal_z * normal_z)) < 0.00001;
		this.depth = depth;
	}

	// Warning, this creates new Vector3 every time. It is only used for debug purposes.
	public Vector3 getNormal()
	{
		return new Vector3(normal_x, normal_y, normal_z);
	}

	public float getNormalX()
	{
		return normal_x;
	}

	public float getNormalY()
	{
		return normal_y;
	}

	public float getNormalZ()
	{
		return normal_z;
	}

	public float getDepth()
	{
		return depth;
	}

	public void setDepth(float depth)
	{
		this.depth = depth;
	}

	public void setCollider(Object collider)
	{
		this.collider = collider;
	}

	public Object getCollider()
	{
		return collider;
	}

	public void setTarget(Object target)
	{
		this.target = target;
	}

	public Object getTarget()
	{
		return target;
	}

	private float normal_x, normal_y, normal_z;
	private float depth;

	// Optional collider and target
	private Object collider, target;
}
