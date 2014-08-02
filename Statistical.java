package fi.henu.gdxextras;

import com.badlogic.gdx.math.Vector3;

public class Statistical
{

	// Does Principal Component Analysis to 3D data. Results are given in three
	// vectors so that biggest is stored to result1, medium to result2 and
	// smallest to result3. Data needs to be in centered form, i.e. the average
	// position of all datapoints should be zero.
	public static void doPca(Vector3 result1, Vector3 result2, Vector3 result3, Vector3[] data)
	{
		assert isDataCentered(data);

		final int ITERATIONS = 100;

		// Prepare some variables
		Vector3 temp = new Vector3();
		Vector3 datapoint = new Vector3();
		int data_size = data.length;

		// Form first result
		result1.set((float)Math.random() * 2 - 1, (float)Math.random() * 2 - 1, (float)Math.random() * 2 - 1);
		for (int iter = 0; iter < ITERATIONS; iter++) {
			temp.set(0, 0, 0);
			for (int data_ofs = 0; data_ofs < data_size; data_ofs ++) {
				datapoint.set(data[data_ofs]);
				float dot_result_datapoint = result1.dot(datapoint);
				datapoint.mul(dot_result_datapoint);
				temp.add(datapoint);
			}
			result1.set(temp);
			result1.nor();
		}

		// Create new array of data that has everything shrinked to zero using
		// result1. This means moving them along result1 so they will all be at
		// one plane, where result1 is normal.
		Vector3[] data2 = new Vector3[data_size];
		for (int data_ofs = 0; data_ofs < data_size; data_ofs++) {
			// Project this datapoint to the plane
			// that has result1 as its normal.
			Vector3 new_data2point = new Vector3();
			Mathutils.posToPlane(new_data2point, data[data_ofs], result1);
			data2[data_ofs] = new_data2point;
		}

		// Form second result
		result2.set((float)Math.random() * 2 - 1, (float)Math.random() * 2 - 1, (float)Math.random() * 2 - 1);
		for (int iter = 0; iter < ITERATIONS; iter++) {
			temp.set(0, 0, 0);
			for (int data_ofs = 0; data_ofs < data_size; data_ofs++) {
				datapoint.set(data2[data_ofs]);
				float dot_result_datapoint = result2.dot(datapoint);
				datapoint.mul(dot_result_datapoint);
				temp.add(datapoint);
			}
			result2.set(temp);
			result2.nor();
		}

		// Its time to finalize results. First ensure result1
		// and result2 are perpendicular to each others.
		Mathutils.posToPlane(datapoint, result2, result1);
		result2.set(datapoint);
		result2.nor();

		// Now generate result3 from crossproduct of these two.
		result3.set(result1);
		result3.crs(result2);

		// Fix sizes so biggest is really biggest
		// TODO: Do this so that it reflects the size of real distribution
		result1.mul(3);
		result2.mul(2);
	}

	public static boolean isDataCentered(Vector3[] data)
	{
		float LEN_TO_2_THRESHOLD = 0.00001f;
		int data_size = data.length;
		float center_x = 0;
		float center_y = 0;
		float center_z = 0;
		for (int data_ofs = 0; data_ofs < data_size; data_ofs++) {
			Vector3 datapoint = data[data_ofs];
			center_x += datapoint.x;
			center_y += datapoint.y;
			center_z += datapoint.z;
		}
		return center_x * center_x + center_y * center_y + center_z * center_z < LEN_TO_2_THRESHOLD;
	}

}
