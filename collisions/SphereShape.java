package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.utils.Array;

public class SphereShape implements Shape
{
	public SphereShape(float radius)
	{
		this.radius = radius;
	}

	public float getRadius()
	{
		return radius;
	}

	@Override
	public boolean findCollisionsTo(Array<Collision> result, Shape shape, float pos_x, float pos_y, float pos_z, float extra_margin, boolean flip_normals)
	{
		// Sphere collides Sphere
		if (shape instanceof SphereShape) {
			SphereShape sphere = (SphereShape)shape;
			return CollisionFinder.sphereSphereCollision(result, radius, pos_x, pos_y, pos_z, sphere.radius, extra_margin, flip_normals);
		}

		// Unknown shape. Ask it to do the collision check instead
		return shape.findCollisionsTo(result, this, -pos_x, -pos_y, -pos_z, extra_margin, !flip_normals);
	}

	private final float radius;
}
