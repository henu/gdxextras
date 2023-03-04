package fi.henu.gdxextras.game;

import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.Mathutils;
import fi.henu.gdxextras.collisions.Collision;
import fi.henu.gdxextras.collisions.Rectangle2DShape;
import fi.henu.gdxextras.collisions.Shape;
import fi.henu.gdxextras.collisions.Sphere2DShape;
import fi.henu.gdxextras.collisions.SphereShape;

public class PlaneShape implements Shape
{
	public PlaneShape(float pos_along_normal, float normal_x, float normal_y, float normal_z)
	{
		this.pos_along_normal = pos_along_normal;
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		this.normal_z = normal_z;
		assert Math.abs(Math.sqrt(normal_x * normal_x + normal_y * normal_y + normal_z * normal_z) - 1) < 0.001;
	}

	@Override
	public boolean findCollisionsTo(Array<Collision> result, Shape shape, float pos_x, float pos_y, float pos_z, float extra_margin, boolean flip_normals)
	{
		// Plane collides Sphere
		if (shape instanceof SphereShape) {
			SphereShape sphere = (SphereShape)shape;
			float distance_to_plane = Mathutils.distanceToPlane(pos_x, pos_y, pos_z, normal_x, normal_y, normal_z) - pos_along_normal;
			float depth = sphere.getRadius() - distance_to_plane;
			if (depth > -extra_margin) {
				result.add(new Collision(-normal_x, -normal_y, -normal_z, depth, flip_normals));
				return true;
			}
			return false;
		}

		// Plane collides Sphere2D
		if (shape instanceof Sphere2DShape) {
			if (Math.abs(normal_y) > 0.0001) {
				throw new RuntimeException("Unable to get Plane/Sphere2D collision if plane normal Y is not zero!");
			}
			Sphere2DShape sphere = (Sphere2DShape)shape;
			float distance_to_plane = Mathutils.distanceToPlane(pos_x, pos_y, pos_z, normal_x, normal_y, normal_z) - pos_along_normal;
			float depth = sphere.getRadius() - distance_to_plane;
			if (depth > -extra_margin) {
				result.add(new Collision(-normal_x, -normal_y, -normal_z, depth, flip_normals));
				return true;
			}
			return false;
		}

		// Plane collides Rectangle2D
		if (shape instanceof Rectangle2DShape) {
			if (Math.abs(normal_y) > 0.0001) {
				throw new RuntimeException("Unable to get Plane/Rectangle2D collision if plane normal Y is not zero!");
			}
//			throw new NotImplementedException();
// TODO: Code this!
return false;
		}

		// Unknown shape. Ask it to do the collision check instead
		return shape.findCollisionsTo(result, this, -pos_x, -pos_y, -pos_z, extra_margin, !flip_normals);
	}

	private final float pos_along_normal;
	private final float normal_x, normal_y, normal_z;
}
