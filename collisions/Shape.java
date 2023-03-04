package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.utils.Array;

public interface Shape
{
	// Returns true if at least one collision was found
	boolean findCollisionsTo(Array<Collision> result, Shape shape, float pos_x, float pos_y, float pos_z, float extra_margin, boolean flip_normals);
}
