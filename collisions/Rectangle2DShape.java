package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.utils.Array;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Rectangle2DShape implements Shape
{
	public Rectangle2DShape(float width, float height)
	{
		this.width = width;
		this.height = height;
	}

	private final float width, height;

	@Override
	public boolean findCollisionsTo(Array<Collision> result, Shape shape, float pos_x, float pos_y, float pos_z, float extra_margin, boolean flip_normals)
	{
		// Rectangle2D collides Rectangle2D
		if (shape instanceof Rectangle2DShape) {
			throw new NotImplementedException();
		}

		// Rectangle2D collides Sphere2D
		if (shape instanceof Sphere2DShape) {
			Sphere2DShape sphere = (Sphere2DShape)shape;
			return CollisionFinder.rectangleSphereCollision(result, width, height, pos_x, pos_z, sphere.getRadius(), extra_margin, flip_normals);
		}

		// Rectangle2D collides Sphere
		if (shape instanceof SphereShape) {
			SphereShape sphere = (SphereShape)shape;
			return CollisionFinder.rectangleSphereCollision(result, width, height, pos_x, pos_z, sphere.getRadius(), extra_margin, flip_normals);
		}

		// Unknown shape. Ask it to do the collision check instead
		return shape.findCollisionsTo(result, this, -pos_x, -pos_y, -pos_z, extra_margin, !flip_normals);
	}
}
