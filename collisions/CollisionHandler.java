package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import fi.henu.gdxextras.Mathutils;

public class CollisionHandler
{
	// Calculates position delta to get object out of walls. Note, that this function invalidates
	// depth values of collisions and removes those collisions that does not really hit walls.
// TODO: Use Pools to get rid of Collisions, so they can be reused!
	public static void moveOut(Vector3 result, Array<Collision> colls)
	{
		if (colls.isEmpty()) {
			result.setZero();
			return;
		}

		Array<Collision> real_colls = new Array<>();

		// Find out what collisions is the deepest one
		int deepest = 0;
		float deepest_depth = colls.get(0).getDepth();
		for (int colls_i = 1; colls_i < colls.size; colls_i ++) {
			Collision coll = colls.get(colls_i);
			if (coll.getDepth() > deepest_depth) {
				deepest_depth = coll.getDepth();
				deepest = colls_i;
			}
		}

		Collision coll_d = colls.get(deepest);

		// First move the object out using the deepest collision
		if (deepest_depth >= 0.0) {
			result.set(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ());
			result.scl(deepest_depth);
			real_colls.add(coll_d);
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

		// Current position delta also affects to other collisions, so now go them
		// through, and check how their depth changes. Because one collision is already
		// fixed, the rest of the collisions must be fixed by along the plane of the
		// fixed collision. That is why the new depths are measured along that plane.
		int deepest2 = 0;
		float deepest2_depth = -1;
		float deepest2_nrm_p_x = 0;
		float deepest2_nrm_p_y = 0;
		float deepest2_nrm_p_z = 0;
		for (int colls_i = 0; colls_i < colls.size; ++ colls_i) {
			// Skip deepest collision
			if (colls_i == deepest) {
				continue;
			}
			Collision coll = colls.get(colls_i);
			// Calculate new depth
			float dp_n_n = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), coll.getNormalZ(), coll.getNormalX(), coll.getNormalY(), coll.getNormalZ());
			assert Math.abs(dp_n_n) > 0.00001;
			coll.setDepth(coll.getDepth() - Mathutils.dotProduct(result.x, result.y, result.z, coll.getNormalX(), coll.getNormalY(), coll.getNormalZ()) / dp_n_n);

			// If this collision has negative depth, ie is not touching
			// the other object, then remove it from container.
// TODO: What is good value here?
			if (coll.getDepth() <= 0.0005) {
				continue;
			}

			// Project normal to the plane of deepest collision. This must
			// be done in two steps. First direction at plane is
			// calculated, and then normal is projected to plane using it.
			float dp_cdnn_cdnn = Mathutils.dotProduct(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ(), coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ());
			float dp_cdnn_nn = Mathutils.dotProduct(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ(), coll.getNormalX(), coll.getNormalY(), coll.getNormalZ());
			assert Math.abs(dp_cdnn_cdnn) > 0.00001;
			float m = dp_cdnn_nn / dp_cdnn_cdnn;
			float dir_at_plane_x = coll.getNormalX() - coll_d.getNormalX() * m;
			float dir_at_plane_y = coll.getNormalY() - coll_d.getNormalY() * m;
			float dir_at_plane_z = coll.getNormalZ() - coll_d.getNormalZ() * m;
			float dir_at_plane_len = (float)Math.sqrt(dir_at_plane_x * dir_at_plane_x + dir_at_plane_y * dir_at_plane_y + dir_at_plane_z * dir_at_plane_z);
			if (dir_at_plane_len < 0.0005) {
				continue;
			}
			dir_at_plane_x /= dir_at_plane_len;
			dir_at_plane_y /= dir_at_plane_len;
			dir_at_plane_z /= dir_at_plane_len;
			// Second step
			dp_n_n = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), coll.getNormalZ(), coll.getNormalX(), coll.getNormalY(), coll.getNormalZ()) * coll.getDepth() * coll.getDepth();
			float dp_n_d = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), coll.getNormalZ(), dir_at_plane_x, dir_at_plane_y, dir_at_plane_z) * coll.getDepth();
			if (Math.abs(dp_n_d) < 0.0005) {
				continue;
			}
			m = dp_n_n / dp_n_d;
			float move_at_plane_x = dir_at_plane_x * m;
			float move_at_plane_y = dir_at_plane_y * m;
			float move_at_plane_z = dir_at_plane_z * m;

			float depth = (float)Math.sqrt(move_at_plane_x * move_at_plane_x + move_at_plane_y * move_at_plane_y + move_at_plane_z * move_at_plane_z);
			if (depth > deepest2_depth) {
				deepest2_depth = depth;
				deepest2 = colls_i;
				deepest2_nrm_p_x = move_at_plane_x;
				deepest2_nrm_p_y = move_at_plane_y;
				deepest2_nrm_p_z = move_at_plane_z;
			}
		}

		// If no touching collisions were found, then mark all except deepest as not real and leave.
		if (deepest2_depth <= 0) {
			assert real_colls.size == 1;
			colls.clear();
			colls.addAll(real_colls);
			return;
		}

		// Store the second deepest collision
		Collision coll_d2 = colls.get(deepest2);
		real_colls.add(coll_d2);

		// Modify position using the second deepest collision
		result.x += deepest2_nrm_p_x;
		result.y += deepest2_nrm_p_y;
		result.z += deepest2_nrm_p_z;

		// Go rest of collisions through and check how much they should be moved so object would
		// come out of wall. Note, that since two collisions are already fixed, all other movement
		// must be done at the planes of these collisions! This means one line in space.
		float move_v_x = Mathutils.crossProductX(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ(), coll_d2.getNormalX(), coll_d2.getNormalY(), coll_d2.getNormalZ());
		float move_v_y = Mathutils.crossProductY(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ(), coll_d2.getNormalX(), coll_d2.getNormalY(), coll_d2.getNormalZ());
		float move_v_z = Mathutils.crossProductZ(coll_d.getNormalX(), coll_d.getNormalY(), coll_d.getNormalZ(), coll_d2.getNormalX(), coll_d2.getNormalY(), coll_d2.getNormalZ());
		float move_v_len = (float)Math.sqrt(move_v_x * move_v_x + move_v_y * move_v_y + move_v_z * move_v_z);
		assert move_v_len > 0.0005;
		move_v_x /= move_v_len;
		move_v_y /= move_v_len;
		move_v_z /= move_v_len;
		float deepest3_depth = -99999;
		float deepest3_move_x = 0;
		float deepest3_move_y = 0;
		float deepest3_move_z = 0;
		for (int colls_i = 0; colls_i < colls.size; ++ colls_i) {
			// Skip deepest collisions
			if (colls_i == deepest || colls_i == deepest2) {
				continue;
			}
			Collision coll = colls.get(colls_i);

			// Since object has moved again, depth of other collisions
			// have changed. Recalculate depth now.
			float dp_n_n = Mathutils.dotProduct(coll.getNormalX(), coll.getNormalY(), coll.getNormalZ(), coll.getNormalX(), coll.getNormalY(), coll.getNormalZ());
			assert Math.abs(dp_n_n) >= 0.0005;
			float dp_cd2n_n = Mathutils.dotProduct(coll_d2.getNormalX(), coll_d2.getNormalY(), coll_d2.getNormalZ(), coll.getNormalX(), coll.getNormalY(), coll.getNormalZ()) * coll_d2.getDepth();
			float depthmod = dp_cd2n_n / dp_n_n;
			coll.setDepth(coll.getDepth() - depthmod);

			// Skip collisions that are not touching
			if (coll.getDepth() <= 0.0005) {
				continue;
			}
			real_colls.add(coll);

			// Project normal to the move vector. If vectors are in 90° against
			// each others, then this collision must be abandoned, because we
			// could never move along move_v to undo this collision.
			float coll_real_x = coll.getNormalX() * coll.getDepth();
			float coll_real_y = coll.getNormalY() * coll.getDepth();
			float coll_real_z = coll.getNormalZ() * coll.getDepth();
			float dp_c_mv = Mathutils.dotProduct(coll_real_x, coll_real_y, coll_real_z, move_v_x, move_v_y, move_v_z);
			if (Math.abs(dp_c_mv) > 0.0005) {
				float dp_c_c = Mathutils.dotProduct(coll_real_x, coll_real_y, coll_real_z, coll_real_x, coll_real_y, coll_real_z);
				float m = dp_c_c / dp_c_mv;
				float projected_x = move_v_x * m;
				float projected_y = move_v_y * m;
				float projected_z = move_v_z * m;
				float projected_len = (float)Math.sqrt(projected_x * projected_x + projected_y * projected_y + projected_z * projected_z);
				if (projected_len > deepest3_depth) {
					deepest3_depth = projected_len;
					deepest3_move_x = projected_x;
					deepest3_move_y = projected_y;
					deepest3_move_z = projected_z;
				}
			}
		}

		// Now move position for final time
		if (deepest3_depth > 0.0) {
			result.x += deepest3_move_x;
			result.y += deepest3_move_y;
			result.z += deepest3_move_z;
		}

		colls.clear();
		colls.addAll(real_colls);
	}

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
			Pools.freeAll(colls);
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
				Pools.free(colls.removeIndex(colls_i));
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
	public static void bounce(Vector3 vel, Array<Collision> colls)
	{
		for (int colls_i = 0; colls_i < colls.size; ++ colls_i) {
			Collision coll = colls.get(colls_i);
			// This is a doubled "distance" from velocity vector to the "plane"
			float d = 2 * Mathutils.distanceToPlane(-vel.x, -vel.y, -vel.z, coll.getNormalX(), coll.getNormalY(), coll.getNormalZ());
			if (d > 0) {
				vel.add(coll.getNormalX() * d, coll.getNormalY() * d, coll.getNormalZ() * d);
			}
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
