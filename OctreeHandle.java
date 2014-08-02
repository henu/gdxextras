package fi.henu.gdxextras;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class OctreeHandle extends Transformhandle
{

	public OctreeHandle()
	{
		octree = null;
		region = null;
		update_required = false;
	}

	// Invalidates OctreeHandle and clears its reference to any Octree. This
	// imitates destructor of C++.
	public void close()
	{
		setOctree(null);
		update();
	}

	public void getTransform(Matrix4 result)
	{
		result.set(transf);
	}

	public boolean getFlag(int flag_id)
	{
		if (region == null) return false;
		return region.getFlag(flag_id);
	}

	public void setOctree(Octree octree)
	{
		if (this.octree != octree) {
			this.octree = octree;
			update_required = true;
		}
	}

	public void clearBoundingbox()
	{
		this.bb.clr();
		update_required = true;
	}

	public void setBoundingbox(BoundingBox bb)
	{
		this.bb.set(bb);
		update_required = true;
	}

	public void setInfiniteBoundingbox()
	{
		this.bb.inf();
		update_required = true;
	}

	public void addBoundingbox(BoundingBox bb)
	{
		this.bb.ext(bb);
		update_required = true;
	}

	public void resetTransform()
	{
		transf.idt();
		// Mark to need update
		update_required = true;
	}

	public void setTransform(Matrix4 transf)
	{
		this.transf.set(transf);
		// Mark to need update
		update_required = true;
	}

	public void translate(float x, float y, float z)
	{
		// Do transform change
		tmp_m.setToTranslation(x, y, z);
		Matrix4.mul(tmp_m.val, transf.val);
		// TODO: Optimize!
		transf.set(tmp_m);
		// Mark to need update
		update_required = true;
	}
	public void translate(Vector3 v)
	{
		translate(v.x, v.y, v.z);
	}

	public void rotate(float axis_x, float axis_y, float axis_z, float angle)
	{
		// Do transform change
		tmp_m.setToRotation(axis_x, axis_y, axis_z, angle);
		Matrix4.mul(tmp_m.val, transf.val);
		transf.set(tmp_m);
		// Mark to need update
		update_required = true;
	}

	public void rotate(Vector3 axis, float angle)
	{
		rotate(axis, angle);
	}

	public void addAnotherTransform(Matrix4 transf)
	{
		// Do transform change
		tmp_m.set(transf);
		Matrix4.mul(tmp_m.val, this.transf.val);
		this.transf.set(tmp_m);
		// Mark to need update
		update_required = true;
	}

	// Uses given boundingbox and transform to place handle to given Octree.
	// Octree may be null. In this case, handle is removed from Octree.
	public void update()
	{
		// If no update is required, then do nothing
		if (!update_required) {
			return;
		}

		// Find new region from possible new octree
		OctreeRegion new_region;
		if (octree != null) {
			new_region = octree.findRegion(bb, transf);
		} else {
			new_region = null;
		}

		// If region is same as the old one, then do nothing
		if (region == new_region) {
			return;
		}

		// First add handle to the new region. This needs to be done before
		// removing, because otherwise we might accidentally remove the found
		// new region, if the new one is empty, and the old one is the last user
		// in the children of new one.
		if (octree != null) {
			new_region.registerHandle();
		}

		// Remove handle from its old region
		if (region != null) {
			region.unregisterHandle();
		}

		// Mark using of this new region
		if (octree != null) {
			region = new_region;
		} else {
			region = null;
		}

		update_required = false;
	}

	private Octree octree;
	private OctreeRegion region;

	private Matrix4 transf = new Matrix4();
	private BoundingBox bb = new BoundingBox();

	private boolean update_required;

	private final Matrix4 tmp_m = new Matrix4();

}
