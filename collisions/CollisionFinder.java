package fi.henu.gdxextras.collisions;

import com.badlogic.gdx.utils.Array;

public class CollisionFinder
{
	// Collision is added from the perspective of rectangle, ie. the normal will point towards it
	public static boolean rectangleSphereCollision(Array<Collision> result, float rect_width, float rect_height, float sphere_x, float sphere_z, float sphere_radius, float extra_margin, boolean flip_normals)
	{
		float half_w = rect_width / 2;
		float half_h = rect_height / 2;

		boolean collision_found = false;
		float best_depth = 0;
		float normal_x = 0;
		float normal_z = 0;

		// Check sides of box
		if (sphere_x >= -half_w && sphere_x <= half_w) {
			if (sphere_z >= 0) {
				collision_found = true;
				best_depth = half_h - sphere_z + sphere_radius;
				normal_z = -1;
			} else {
				collision_found = true;
				best_depth = half_h + sphere_z + sphere_radius;
				normal_z = 1;
			}
		}
		if (sphere_z >= -half_h && sphere_z <= half_h) {
			if (sphere_x >= 0) {
				float right_depth = half_w - sphere_x + sphere_radius;
				if (!collision_found || right_depth < best_depth) {
					collision_found = true;
					best_depth = right_depth;
					normal_x = -1;
					normal_z = 0;
				}
			} else {
				float left_depth = half_w + sphere_x + sphere_radius;
				if (!collision_found || left_depth < best_depth) {
					collision_found = true;
					best_depth = left_depth;
					normal_x = 1;
					normal_z = 0;
				}
			}
		}
		if (collision_found) {
			if (best_depth > -extra_margin) {
				result.add(new Collision(normal_x, 0, normal_z, best_depth, flip_normals));
				return true;
			}
			return false;
		}

		for (char corner_i = 0; corner_i < 4; ++ corner_i) {
			float corner_x;
			float corner_z;
			if (corner_i == 0) {
				corner_x = -half_w;
				corner_z = -half_h;
			} else if (corner_i == 1) {
				corner_x = half_w;
				corner_z = -half_h;
			} else if (corner_i == 2) {
				corner_x = -half_w;
				corner_z = half_h;
			} else {
				corner_x = half_w;
				corner_z = half_h;
			}
			float diff_x = corner_x - sphere_x;
			float diff_z = corner_z - sphere_z;
			float diff_len = (float)Math.sqrt(diff_x * diff_x + diff_z * diff_z);
			float depth = sphere_radius - diff_len;
			if (depth > -extra_margin && diff_len > 0.0001) {
				if (!collision_found || depth > best_depth) {
					collision_found = true;
					best_depth = depth;
					normal_x = diff_x / diff_len;
					normal_z = diff_z / diff_len;
				}
			}
		}

		if (collision_found) {
			result.add(new Collision(normal_x, 0, normal_z, best_depth, flip_normals));
			return true;
		}

		return false;
	}

	// Collision is added from the perspective of box, ie. the normal will point towards it
	public static boolean boxSphereCollision(Array<Collision2D> colls, float box_width, float box_height, float sphere_x, float sphere_y, float sphere_radius, float extra_margin)
	{
		float half_w = box_width / 2;
		float half_h = box_height / 2;

		boolean collision_found = false;
		float best_depth = 0;
		float normal_x = 0;
		float normal_y = 0;

		// Check sides of box
		if (sphere_x >= -half_w && sphere_x <= half_w) {
			if (sphere_y >= 0) {
				collision_found = true;
				best_depth = half_h - sphere_y + sphere_radius;
				normal_y = -1;
			} else {
				collision_found = true;
				best_depth = half_h + sphere_y + sphere_radius;
				normal_y = 1;
			}
		}
		if (sphere_y >= -half_h && sphere_y <= half_h) {
			if (sphere_x >= 0) {
				float right_depth = half_w - sphere_x + sphere_radius;
				if (!collision_found || right_depth < best_depth) {
					collision_found = true;
					best_depth = right_depth;
					normal_x = -1;
					normal_y = 0;
				}
			} else {
				float left_depth = half_w + sphere_x + sphere_radius;
				if (!collision_found || left_depth < best_depth) {
					collision_found = true;
					best_depth = left_depth;
					normal_x = 1;
					normal_y = 0;
				}
			}
		}
		if (collision_found) {
			if (best_depth > -extra_margin) {
				colls.add(new Collision2D(normal_x, normal_y, best_depth));
				return true;
			}
			return false;
		}

		for (char corner_i = 0; corner_i < 4; ++ corner_i) {
			float corner_x;
			float corner_y;
			if (corner_i == 0) {
				corner_x = -half_w;
				corner_y = -half_h;
			} else if (corner_i == 1) {
				corner_x = half_w;
				corner_y = -half_h;
			} else if (corner_i == 2) {
				corner_x = -half_w;
				corner_y = half_h;
			} else {
				corner_x = half_w;
				corner_y = half_h;
			}
			float diff_x = corner_x - sphere_x;
			float diff_y = corner_y - sphere_y;
			float diff_len = (float)Math.sqrt(diff_x * diff_x + diff_y * diff_y);
			float depth = sphere_radius - diff_len;
			if (depth > -extra_margin && diff_len > 0.0001) {
				if (!collision_found || depth > best_depth) {
					collision_found = true;
					best_depth = depth;
					normal_x = diff_x / diff_len;
					normal_y = diff_y / diff_len;
				}
			}
		}

		if (collision_found) {
			colls.add(new Collision2D(normal_x, normal_y, best_depth));
			return true;
		}

		return false;
	}

	// Collision is added from the perspective of sphere #1, ie. the normal will point towards it
	public static boolean sphereSphereCollision(Array<Collision> colls, float sphere1_radius, float sphere2_x, float sphere2_y, float sphere2_z, float sphere2_radius, float extra_margin, boolean flip_normals)
	{
		float distance = (float)Math.sqrt(sphere2_x * sphere2_x + sphere2_y * sphere2_y + sphere2_z * sphere2_z);
		if (distance > 0.00001) {
			float depth = sphere1_radius + sphere2_radius - distance;
			if (depth > -extra_margin) {
				float normal_x = -sphere2_x / distance;
				float normal_y = -sphere2_y / distance;
				float normal_z = -sphere2_z / distance;
				colls.add(new Collision(normal_x, normal_y, normal_z, depth, flip_normals));
				return true;
			}
		}
		return false;
	}

	// Collision is added from the perspective of sphere #1,
	// ie. the normal will point towards Sphere #2
	public static boolean sphereSphereCollision(Array<Collision> colls, float sphere1_radius, float sphere2_x, float sphere2_z, float sphere2_radius, float extra_margin, boolean flip_normals)
	{
		float distance = (float)Math.sqrt(sphere2_x * sphere2_x + sphere2_z * sphere2_z);
		if (distance > 0.00001) {
			float depth = sphere1_radius + sphere2_radius - distance;
			if (depth > -extra_margin) {
				float normal_x = -sphere2_x / distance;
				float normal_z = -sphere2_z / distance;
				colls.add(new Collision(normal_x, 0, normal_z, depth, flip_normals));
				return true;
			}
		}
		return false;
	}
}
