package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.Mathutils;

public class CollisionHandler
{
	// Calculates position delta to get object out of walls. Note, that this function invalidates
	// depth values of collisions and removes those collisions that does not really hit walls.
	public static void moveOut(Vector2 result, Array<Collision2D> colls)
	{
		if (colls.isEmpty()) {
			result.setZero();
			return;
		}

		// Find out what collisions is the deepest one
		int deepest = 0;
		float deepest_depth = colls.get(0).getDepth();
		for (int colls_i = 1; colls_i < colls.size; colls_i ++) {
			Collision2D coll = colls.get(colls_i);
			if (coll.getDepth() > deepest_depth) {
				deepest_depth = coll.getDepth();
				deepest = colls_i;
			}
		}

		Collision2D coll_d = colls.get(deepest);

		// First move the object out using the deepest collision
		if (deepest_depth >= 0.0) {
			result.set(coll_d.getNormalX(), coll_d.getNormalY());
			result.scl(deepest_depth);
		}
		// If none of the collisions were really touching anything, then clear all
		else {
			colls.clear();
			result.setZero();
			return;
		}

		// If this was the only collision, then do nothing else
		if (colls.size == 1) {
			return;
		}

		// Current position delta also affects to other collisions, so now go them through, and
		// check how their depth changes. Because one collision is already fixed, the rest of
		// collisions must be fixed by going along a vector that is perpendicular to the normal
		// of fixed collision. That is why the new depths are measured along that vector.
		float fix2v_x = -coll_d.getNormalY();
		float fix2v_y = coll_d.getNormalX();
		int deepest2 = 0;
		float deepest2_depth = -1;
		for (int colls_i = 0; colls_i < colls.size; ) {
			// Skip deepest collision
			if (colls_i == deepest) {
				++ colls_i;
				continue;
			}
			Collision2D coll = colls.get(colls_i);
			// Calculate new depth
			float dp_n_n = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), coll.getNormalX(), coll.getNormalY());
			assert Math.abs(dp_n_n) > 0.00001;
			coll.setDepth(coll.getDepth() - Mathutils.dotProduct(result.x, result.y, coll.getNormalX(), coll.getNormalY()) / dp_n_n);

			// If this collision has negative depth, ie is not touching
			// the other object, then remove it from container.
// TODO: What is good value here?
			if (coll.getDepth() <= 0.0005) {
				// Since removing will happen, make sure the
				// "deepest" still points to the correct collision.
				if (colls.ordered) {
					if (colls_i < deepest) {
						-- deepest;
					}
				} else {
					if (deepest == colls.size - 1) {
						deepest = colls_i;
					}
				}
				colls.removeIndex(colls_i);
				continue;
			}

			// Remember that it is mandatory to do the fixings of these collisions along
			// that vector(fix2v) that was perpendicular to the normal of already fixed
			// collision? Because of this, the new depth is not the one we want. From now
			// on, the depth will be measured using vector fix2v. Also, the negativity of
			// depth no longer tells if collision is really touching or not.
			float dp_n_f2v = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), fix2v_x, fix2v_y);
			// If this is zero, then it means that this collision has almost the same normal, as
			// the already fixed collision. In this case, this does not need fixing, so skip it!
			if (Math.abs(dp_n_f2v) <= 0.0001) {
				++ colls_i;
				continue;
			}
			// Calculate how much fix2v should be added, so this collision would be fixed.
			coll.setDepth((dp_n_n * coll.getDepth()) / dp_n_f2v);

			// Check if this is the "deepest" one
			float coll_depth_abs = Math.abs(coll.getDepth());
			if (coll_depth_abs > deepest2_depth) {
				deepest2_depth = coll_depth_abs;
				deepest2 = colls_i;
			}

			++ colls_i;
		}

		// If new "deepest" collision was found, then fix along fix2v
		if (deepest2_depth > 0.0) {
			result.x += fix2v_x * colls.get(deepest2).getDepth();
			result.y += fix2v_y * colls.get(deepest2).getDepth();
		}
	}

	// Uses every collision to bounce object
	public static void bounce(Vector2 vel, Array<Collision2D> colls)
	{
		for (int colls_i = 0; colls_i < colls.size; ++ colls_i) {
			Collision2D coll = colls.get(colls_i);
			// This is a doubled "distance" from velocity vector to the "plane"
			float d = 2 * Mathutils.distanceToPlane(-vel.x, -vel.y, coll.getNormalX(), coll.getNormalY());
			if (d > 0) {
				vel.add(coll.getNormalX() * d, coll.getNormalY() * d);
			}
		}
	}
}
