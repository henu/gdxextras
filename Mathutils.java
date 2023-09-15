package fi.henu.gdxextras;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Mathutils
{

	// Result and pos can be the same
	public static void posToPlane(Vector3 result, Vector3 pos, Vector3 normal)
	{
		float dp_n_n = normal.dot(normal);
		if (dp_n_n == 0) {
			throw new IllegalArgumentException("Normal is too small!");
		}
		float dp_p_n = pos.dot(normal);
		float pos_x = pos.x;
		float pos_y = pos.y;
		float pos_z = pos.z;
		result.set(normal);
		result.scl(-dp_p_n);
		result.scl(1f / dp_n_n);
		result.add(pos_x, pos_y, pos_z);
	}

	// If point is at the back side of plane, then distance is negative.
	// Note, that distance is measured in length of plane_normal, so if you
	// want it to be measured in basic units, then normalize plane_normal!
	public static float distanceToPlane(float point_x, float point_y, float point_z, float normal_x, float normal_y, float normal_z)
	{
		float dp_nn = dotProduct(normal_x, normal_y, normal_z, normal_x, normal_y, normal_z);
		assert Math.abs(dp_nn) > 0.0001;
		return (dotProduct(normal_x, normal_y, normal_z, point_x, point_y, point_z)) / dp_nn;
	}

	// If point is at the back side of plane, then distance is negative.
	// Note, that distance is measured in length of plane_normal, so if you
	// want it to be measured in basic units, then normalize plane_normal!
	public static float distanceToPlane(float point_x, float point_y, float normal_x, float normal_y)
	{
		float dp_nn = dotProduct(normal_x, normal_y, normal_x, normal_y);
		assert Math.abs(dp_nn) > 0.0001;
		return (dotProduct(normal_x, normal_y, point_x, point_y)) / dp_nn;
	}

	public static float distance(float x1, float y1, float x2, float y2)
	{
		float x_diff = x2 - x1;
		float y_diff = y2 - y1;
		return (float)Math.sqrt(x_diff * x_diff + y_diff * y_diff);
	}

	// Returns angle between two vectors. The result is thus [0 - 180]
	public static float angleBetweenVectors(Vector3 v1, Vector3 v2)
	{
		float v1_len = v1.len();
		if (v1_len == 0) return 0;
		float v2_len = v2.len();
		if (v2_len == 0) return 0;

		v3tmp1.set(v1);
		v3tmp2.set(v2);
		v3tmp1.scl(1f / v1_len);
		v3tmp2.scl(1f / v2_len);

		float dp_v1n_v2n = v3tmp1.dot(v3tmp2);

		float radians = (float)Math.acos(dp_v1n_v2n);
		if (Float.isNaN(radians)) {
			v3tmp1.add(v3tmp2);
			if (v3tmp1.len2() > 1) {
				return 0;
			} else {
				return 180;
			}
		}
		return MathUtils.radiansToDegrees * radians;
	}

	public static float angleBetweenVectors(float v1_x, float v1_y, float v2_x, float v2_y)
	{
		float v1_len = (float) Math.sqrt(v1_x * v1_x + v1_y * v1_y);
		if (v1_len == 0) return 0;
		float v2_len = (float) Math.sqrt(v2_x * v2_x + v2_y * v2_y);
		if (v2_len == 0) return 0;

		v1_x /= v1_len;
		v1_y /= v1_len;
		v2_x /= v2_len;
		v2_y /= v2_len;

		float dp_v1n_v2n = v1_x * v2_x + v1_y * v2_y;

		float radians = (float)Math.acos(dp_v1n_v2n);
		if (Float.isNaN(radians)) {
			v1_x += v2_x;
			v1_y += v2_y;
			if (v1_x * v1_x + v1_y * v1_y > 1.0) {
				return 0;
			}
			return 180;
		}
		return MathUtils.radiansToDegrees * radians;
	}

	// Returns the amount of right hand rotation that needs to be added
	// to vector #1 so it would become vector #2 when the rotation is
	// done at given plane. The result is between [-180 - 180]. Note! Positions
	// do not need to be at the given plane, the result is always their real
	// angle in 3D space. The plane is used only to determine if the angle
	// should be negative or positive.
	public static float angleAtPlane(Vector3 v1, Vector3 v2, Vector3 normal)
	{
		// Get rotation between vectors in case where there is no plane
		float angle = angleBetweenVectors(v1, v2);
		// Check which side the rotation is
		v3tmp1.set(v1);
		v3tmp1.crs(v2);
		float dp = normal.dot(v3tmp1);
		if (dp < 0) {
			angle = -angle;
		}
		return angle;
	}

	// At (0, 1) angle is zero, at (-1, 0) it is 90, etc.
	public static float getAngle(float x, float y)
	{
		return MathUtils.atan2(y, x) * MathUtils.radiansToDegrees - 90;
	}

	// Calculates average of angles and returns it in range [-180°, 180°]. If
	// no range can be calculated, then returns value that is bigger than 360°.
	public static float calculateAverageAngle(float[] angles)
	{
		int size = angles.length;
		assert size > 0;

		float angle;

		// Find minimum and maximum angles
		angle = fixAngle(angles[0]);
		float min_angle = angle;
		float max_angle = angle;
		for (int angle_id = 1; angle_id < size; angle_id++) {
			angle = fixAngle(angles[angle_id]);
			if (angle < min_angle) min_angle = angle;
			if (angle > max_angle) max_angle = angle;
		}

		// Check if all values should be turned 180°.
		boolean turn180 = max_angle - min_angle > 180;

		// Now calculate average angle, and turn 180° if necessary. Also
		// calculate new minimum and maximum. If again bigger than 180°
		// difference is got, then it is not possible to calculate result.
		if (turn180) {
			angle = fixAngle(angles[0] + 180);
		} else {
			angle = fixAngle(angles[0]);
		}
		min_angle = angle;
		max_angle = angle;
		float average_angle = angle;
		for (int angle_id = 1; angle_id < size; angle_id++) {
			if (turn180) {
				angle = fixAngle(angles[angle_id] + 180);
			} else {
				angle = fixAngle(angles[angle_id]);
			}
			if (angle < min_angle) min_angle = angle;
			if (angle > max_angle) max_angle = angle;
			average_angle += angle;
		}
		if (max_angle - min_angle > 180) {
			return 9999f;
		}

		average_angle /= size;

		if (turn180) {
			average_angle = fixAngle(average_angle + 180);
		}

		return average_angle;
	}

	// Makes angle to be at the range [-180°, 180°]
	public static float fixAngle(float angle)
	{
		if (angle > 180) {
			angle = ((angle + 180) % 360) - 180;
		} else if (angle < -180) {
			angle = ((angle - 180) % 360) + 180;
		}
		assert angle >= -180 && angle <= 180;
		return angle;
	}

	// Converts Field Of View from y to x. FOV is measure from top plane to
	// bottom plane, so max FOV is less than 180°.
	public static float fovYtoFovX(float fov_y, float screen_width, float screen_height)
	{
		float aspect_ratio = screen_width / screen_height;
		float viewplane_height = (float)(Math.tan(Math.toRadians(fov_y / 2)));
		float viewplane_width = viewplane_height * aspect_ratio;
		return (float)Math.toDegrees(Math.atan(viewplane_width)) * 2;
	}

	// Returns vector that is perpendicular to v.
	public static void getPerpendicular(Vector3 result, Vector3 v)
	{
		if (Math.abs(v.x) < Math.abs(v.y)) {
			result.set(0, v.z, -v.y);
		} else {
			result.set(-v.z, 0, v.x);
		}
	}

	public static double root(double num, double root)
	{
		return Math.pow(Math.E, Math.log(num) / root);
	}

	public static float dotProduct(float v1_x, float v1_y, float v2_x, float v2_y)
	{
		return v1_x * v2_x + v1_y * v2_y;
	}

	public static float dotProduct(float v1_x, float v1_y, float v1_z, float v2_x, float v2_y, float v2_z)
	{
		return v1_x * v2_x + v1_y * v2_y + v1_z * v2_z;
	}

	public static float crossProductX(float v1_x, float v1_y, float v1_z, float v2_x, float v2_y, float v2_z)
	{
		return v1_y * v2_z - v1_z * v2_y;
	}

	public static float crossProductY(float v1_x, float v1_y, float v1_z, float v2_x, float v2_y, float v2_z)
	{
		return v1_z * v2_x - v1_x * v2_z;
	}

	public static float crossProductZ(float v1_x, float v1_y, float v1_z, float v2_x, float v2_y, float v2_z)
	{
		return v1_x * v2_y - v1_y * v2_x;
	}

	public static boolean linesCollide(float line1_x1, float line1_y1, float line1_x2, float line1_y2, float line2_x1, float line2_y1, float line2_x2, float line2_y2)
	{
		float line1_xdiff = line1_x2 - line1_x1;
		float line1_ydiff = line1_y2 - line1_y1;
		float line2_xdiff = line2_x2 - line2_x1;
		float line2_ydiff = line2_y2 - line2_y1;
		float lines_x1diff = line2_x1 - line1_x1;
		float lines_y1diff = line2_y1 - line1_y1;

		float denom = line1_xdiff * line2_ydiff - line1_ydiff * line2_xdiff;
		float num1 = lines_x1diff * line2_ydiff - lines_y1diff * line2_xdiff;
		float num2 = lines_x1diff * line1_ydiff - lines_y1diff * line1_xdiff;

		if (Math.abs(denom) < MathUtils.FLOAT_ROUNDING_ERROR) {
			return Math.abs(num1) < MathUtils.FLOAT_ROUNDING_ERROR && Math.abs(num2) < MathUtils.FLOAT_ROUNDING_ERROR;
		}

		float line1_rel_pos = num1 / denom;
		float line2_rel_pos = num2 / denom;

		return line1_rel_pos >= 0 && line1_rel_pos <= 1 && line2_rel_pos >= 0 && line2_rel_pos <= 1;
	}

	// Temporary variables. These are used by multiple methods, so
	// do not expect that value is kept if you do internal call!
	// TODO: Get rid of these so things can be done in multiple threads!
	private static final Vector3 v3tmp1 = new Vector3();
	private static final Vector3 v3tmp2 = new Vector3();
}
