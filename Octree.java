package fi.henu.gdxextras;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Octree
{

	public Octree(float min_x, float min_y, float min_z, float max_x, float max_y, float max_z)
	{
		// Create root region
		root = new OctreeRegion(new Vector3(min_x, min_y, min_z), new Vector3(max_x, max_y, max_z), this, null, (char)9);
	}

	private final BoundingBox findregion_realbb = new BoundingBox();
	public OctreeRegion findRegion(BoundingBox bb, Matrix4 transf)
	{
		// Calculate real boundingbox
		findregion_realbb.set(bb);
		findregion_realbb.mul(transf);

		// Start finding regions
		int max_depth = 8;
		OctreeRegion region = root.find(findregion_realbb, max_depth);

		return region;
	}

	public void toggleFlagFromVisibles(Frustum frustum, int flag_id)
	{
		root.toggleFlagFromVisibles(frustum, flag_id);
	}

	public void toggleFlag(int flag_id, boolean value)
	{
		root.toggleFlag(flag_id, value);
	}

	private OctreeRegion root;

}
