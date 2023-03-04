package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.utils.Array;

public class CombinedShape implements Shape
{
	public CombinedShape()
	{
		subshapes = new Array<>();
	}

	@Override
	public boolean findCollisionsTo(Array<Collision> result, Shape shape, float pos_x, float pos_y, float pos_z, float extra_margin, boolean flip_normals)
	{
		boolean colls_found = false;
		for (int subshape_i = 0; subshape_i < subshapes.size; ++ subshape_i) {
			SubShape subshape = subshapes.get(subshape_i);
			if (flip_normals) {
				if (subshape.shape.findCollisionsTo(result, shape, pos_x - subshape.pos_x, pos_y - subshape.pos_y, pos_z - subshape.pos_z, extra_margin, flip_normals)) {
					colls_found = true;
				}
			} else {
				if (subshape.shape.findCollisionsTo(result, shape, pos_x - subshape.pos_x, pos_y - subshape.pos_y, pos_z - subshape.pos_z, extra_margin, flip_normals)) {
					colls_found = true;
				}
			}
		}
		return colls_found;
	}

	public void addSubShape(Shape shape, float x, float y, float z)
	{
		subshapes.add(new SubShape(shape, x, y, z));
	}

	public void addSubShape(Shape shape, float x, float z)
	{
		addSubShape(shape, x, 0, z);
	}

	private class SubShape
	{
		Shape shape;
		float pos_x, pos_y, pos_z;

		public SubShape(Shape shape, float x, float y, float z)
		{
			this.shape = shape;
			pos_x = x;
			pos_y = y;
			pos_z = z;
		}
	}

	private final Array<SubShape> subshapes;
}
