package fi.henu.gdxextras;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class OctreeRegion
{

	public OctreeRegion(Vector3 min, Vector3 max, Octree octree, OctreeRegion parent, char my_id)
	{
		this.octree = octree;
		this.parent = parent;
		this.my_id = my_id;
		bb = new BoundingBox(min, max);
		bb_center = new Vector3();
		bb.getCenter(bb_center);
		// Reset children
		children = new OctreeRegion[8];
		for (int child_id = 0; child_id < 8; child_id++) {
			children[child_id] = null;
		}
	}

	// Try to find region to fit boundingbox. It is assumed, that the
	// boundingbox fits to this region, so if it does not fit to any of its
	// children, then it returns itself
	private final Vector3 find_childmin = new Vector3();
	private final Vector3 find_childmax = new Vector3();

	public OctreeRegion find(BoundingBox test_bb, int depth_left)
	{
		assert my_id != 8;
		// If there is no depth left, then the
		// only change is to result this region.
		if (depth_left == 0) return this;

		// Find out what is the correct side to fit the boundingbox to. This is
		// done by checking on which side the boundingbox is compared to the
		// center of this region. If it overlaps even one of center axes, then
		// it means, that it cannot be fit to any of the children. This means,
		// that region must return itself.
		char child_id = 0;
		if (test_bb.min.x >= bb_center.x) child_id += 1;
		else if (test_bb.max.x <= bb_center.x) child_id += 0;
		else return this;
		if (test_bb.min.y >= bb_center.y) child_id += 2;
		else if (test_bb.max.y <= bb_center.y) child_id += 0;
		else return this;
		if (test_bb.min.z >= bb_center.z) child_id += 4;
		else if (test_bb.max.z <= bb_center.z) child_id += 0;
		else return this;

		// If child does not exist, then create it now
		if (children[child_id] == null) {
			switch (child_id) {
			case 0:
				find_childmin.set(bb.min.x, bb.min.y, bb.min.z);
				find_childmax.set(bb_center.x, bb_center.y, bb_center.z);
				break;
			case 1:
				find_childmin.set(bb_center.x, bb.min.y, bb.min.z);
				find_childmax.set(bb.max.x, bb_center.y, bb_center.z);
				break;
			case 2:
				find_childmin.set(bb.min.x, bb_center.y, bb.min.z);
				find_childmax.set(bb_center.x, bb.max.y, bb_center.z);
				break;
			case 3:
				find_childmin.set(bb_center.x, bb_center.y, bb.min.z);
				find_childmax.set(bb.max.x, bb.max.y, bb_center.z);
				break;
			case 4:
				find_childmin.set(bb.min.x, bb.min.y, bb_center.z);
				find_childmax.set(bb_center.x, bb_center.y, bb.max.z);
				break;
			case 5:
				find_childmin.set(bb_center.x, bb.min.y, bb_center.z);
				find_childmax.set(bb.max.x, bb_center.y, bb.max.z);
				break;
			case 6:
				find_childmin.set(bb.min.x, bb_center.y, bb_center.z);
				find_childmax.set(bb_center.x, bb.max.y, bb.max.z);
				break;
			case 7:
				find_childmin.set(bb_center.x, bb_center.y, bb_center.z);
				find_childmax.set(bb.max.x, bb.max.y, bb.max.z);
				break;
			}
			OctreeRegion new_child = new OctreeRegion(find_childmin, find_childmax, octree, this, child_id);
			children[child_id] = new_child;
		}

		OctreeRegion child = children[child_id];
		return child.find(test_bb, depth_left - 1);
	}

	public boolean getFlag(int flag_id)
	{
		assert my_id != 8;
		return ((flags >> flag_id) & 0x01) == 1;
	}

	// TODO: Is it possible to technically prevent calls other than
	// OctreeHandle?
	// Registers OctreeHandle. This may only be called by OctreeHandle!!!
	public void registerHandle()
	{
		assert my_id != 8;
		handles_using_this++;
	}

	// Unregisters OctreeHandle. If it is last user, then cleaning of Octree is
	// performed. This may only be called by OctreeHandle!!!
	public void unregisterHandle()
	{
		assert my_id != 8;
		assert handles_using_this > 0;
		handles_using_this--;
		if (handles_using_this == 0) {
			tryToClean();
		}
	}

	// Goes regions through recursively and toggles specific flag on if region
	// is visible. If region is hidden, then flag is turned off. May only be
	// called from Octree!!!
	public void toggleFlagFromVisibles(Frustum frustum, int flag_id)
	{
		assert my_id != 8;
		// Check if this region is 100% inside frustum
		Vector3 tempv = new Vector3();
		if (frustum.pointInFrustum(bb.getCorner000(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner001(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner010(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner011(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner100(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner101(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner110(tempv)) &&
		    frustum.pointInFrustum(bb.getCorner111(tempv))) {
			toggleFlag(flag_id, true);
		}
		// Check if this region is partly inside frustum
		else if (frustum.boundsInFrustum(bb)) {
			// Toggle flag on
			flags |= (1 << flag_id);
			// Check children
			for (int child_id = 0; child_id < 8; child_id++) {
				OctreeRegion child = children[child_id];
				if (child != null) {
					child.toggleFlagFromVisibles(frustum, flag_id);
				}
			}
		}
		// If all tests have failed, then this BoundingBox is not even partly
		// inside frustum. This means that the flags are toggled false.
		else {
			toggleFlag(flag_id, false);
		}
	}

	// Toggles specific flag recursively. This may be called only internally or
	// from Octree!!!
	public void toggleFlag(int flag_id, boolean value)
	{
		assert my_id != 8;
		if (value) {
			flags |= (1 << flag_id);
		} else {
			flags &= ~(1 << flag_id);
		}
		for (int child_id = 0; child_id < 8; child_id++) {
			OctreeRegion child = children[child_id];
			if (child != null) {
				child.toggleFlag(flag_id, value);
			}
		}
	}

	// Checks if there is any children on region. If not, then it means that
	// this region can be removed, because it is assumed that there is no more
	// handles using this region.
	private void tryToClean()
	{
		assert my_id != 8;
		assert handles_using_this == 0;

		// If this is root region, then do not clean it
		if (parent == null) return;
		assert my_id != 9;

		// Ensure there are not any children left
		for (OctreeRegion child : children) {
			if (child != null) {
				// Child was found, so no cleaning can be done
				return;
			}
		}

		// Ask parent to clean me
		parent.cleanChild(my_id);
		my_id = 8;
	}

	private void cleanChild(char child_id)
	{
		assert my_id != 8;
		assert children[child_id] != null;
		assert children[child_id].handles_using_this == 0;
		children[child_id] = null;
		// If there is no more users, then try to clean this too
		if (handles_using_this == 0) {
			tryToClean();
		}
	}

	private final Octree octree;
	private final OctreeRegion parent;

	// Child id of this region to its parent. If this is 8, then it means that
	// region has been cleaned and cannot be used anymore. If it is 9, then it
	// means that this region is root.
	private char my_id;

	private final BoundingBox bb;
	private final Vector3 bb_center;

	private final OctreeRegion[] children;

	private int flags = 0;

	private int handles_using_this = 0;

}
