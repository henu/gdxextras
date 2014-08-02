package fi.henu.gdxextras;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Scenenode {

	public Scenenode() {
		parent = null;
		children = new ArrayList<Scenenode>();
		transf = new Matrix4();
		transf_abs = new Matrix4();
		transf_abs_uptodate = UpToDateStatus.NO;
		totalbb = new BoundingBox(new Vector3(0, 0, 0), new Vector3(0, 0, 0));
		totalbb_uptodate = false;
		visible = true;
		bb = new BoundingBox();
		assert !bb.isValid();
	}

	// Removes scenenode from scenegraph and cleans as much
	// as possible from it. After calling this, the scenenode
	// is unusable. This mimics destroying of object in C++
	// or other languages where is no garbage collection.
	public void close() {
		parent = null;
		Iterator<Scenenode> children_it = children.iterator();
		while (children_it.hasNext()) {
			Scenenode child = children_it.next();
			child.parentHasClosed();
		}
		children = null;
		transf_abs = null;
		totalbb = null;
		bb = null;
	}

	public void setParent(Scenenode parent) {
		// Remove possible old parent
		if (this.parent != null) {
			this.parent.unregisterChild(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.registerChild(this);
		}
		// Make a little hack, and pretend that transform was fine. If this is
		// not set, then the next function will do nothing.
		transf_abs_uptodate = UpToDateStatus.YES;
		transformChanged();
	}

	public void reset() {
		transf.idt();
		transformChanged();
	}

	// Sets boundingbox to new one
	public void setBoundingbox(BoundingBox bbox) {
		bb.set(bbox);
		markTotalBoundingsphereToNeedRecalculation();
	}

	// Adds boundingbox to existing one
	public void addBoundingbox(BoundingBox bbox) {
		if (!bb.isValid()) {
			bb.set(bbox);
		} else {
			bb.ext(bbox);
		}
		markTotalBoundingsphereToNeedRecalculation();
	}

	Matrix4 result = new Matrix4();

	public void translate(float x, float y, float z) {
		result.setToTranslation(x, y, z);
		result.mul(transf);
		transf.set(result.val);
		transformChanged();
	}

	public void rotate(float axis_x, float axis_y, float axis_z, float angle) {

		result.setToRotation(axis_x, axis_y, axis_z, angle);
		result.mul(transf);
		transf.set(result.val);
		transformChanged();
	}

	public void scale(float x, float y, float z) {
		result.setToScaling(x, y, z);
		result.mul(transf);
		transf.set(result.val);
		transformChanged();
	}

	public void translate(Vector3 v) {
		translate(v.x, v.y, v.z);
	}

	public void scale(Vector3 v) {
		scale(v.x, v.y, v.z);
	}

	public void rotate(Vector3 v, float angle) {
		rotate(v.x, v.y, v.z, angle);
	}

	// Updates absolute transform. This may be only called if this
	// Scenenode is a root of scenegraph. This also updates absolute
	// transforms of all children that need updating.
	public void updateAbsoluteTransform() {
		if (parent != null) {
			throw new RuntimeException(
					"Only root of scenegraph can be updated!");
		}

		// Check if this call is useless
		if (transf_abs_uptodate == UpToDateStatus.YES) {
			return;
		}

		// Update using identity matrix as root.
		Matrix4 transf = new Matrix4();
		updateAbsoluteTransform(transf);
	}

	public final Matrix4 getAbsoluteTransform() {
		if (transf_abs_uptodate != UpToDateStatus.YES) {
			throw new RuntimeException(
					"Unable to get absolute transform because it is not up to date!");
		}
		return transf_abs;
	}

	private enum UpToDateStatus {
		YES, NO, NO_FOR_CHILDREN
	}

	private Scenenode parent;
	private ArrayList<Scenenode> children;

	// Transform of this Scenenode, relative to possible parent.
	Matrix4 transf;

	// Absolute transform and state of it
	Matrix4 transf_abs;
	UpToDateStatus transf_abs_uptodate;

	// Total boundingbox. This contains combined boundingbox
	// from this Scenenode and all of its children. Total
	// boundingbox is relative to this Scenenode.
	BoundingBox totalbb;
	boolean totalbb_uptodate;

	// Is Scenenode visible or not. If its not visible, then it wont appear
	// in recursive boundingbox and its transform will not be updated.
	boolean visible;

	// Boundingbox of just this scenenode. Relative to this Scenenode.
	BoundingBox bb;

	private void registerChild(Scenenode child) {
		assert child != null;
		assert !children.contains(child);
		// Ensure this will not make loop of parents
		Scenenode check_parent = this;
		do {
			if (child == check_parent)
				throw new RuntimeException(
						"Unable to add child to scenenode, because that would result to loop in scenegraph!");
			check_parent = check_parent.parent;
		} while (check_parent != null);
		children.add(child);
		// If hidden, then do nothing more
		if (!visible) {
			return;
		}
		markTotalBoundingsphereToNeedRecalculation();
	}

	private void unregisterChild(Scenenode child) {
		assert child != null;
		assert children.contains(child);
		children.remove(child);
		// If hidden, then do nothing more
		if (!visible) {
			return;
		}
		// If may be possible, that this child was the only
		// one that has absolute transform out of date. To
		// be sure about this, check other children.
		if (transf_abs_uptodate == UpToDateStatus.NO_FOR_CHILDREN) {
			this.parent.checkIfAllChildrenHasAbsoluteTransformsUpToDate();
		}
		markTotalBoundingsphereToNeedRecalculation();
	}

	// Marks that this Scenenode has no parent. Also marks
	// that absolute transform needs recalculation.
	private void parentHasClosed() {
		parent = null;
		transformChangedRaw();
	}

	// Marks transform changed if Scenenode is visible and not already out
	// of date. Transform is marked out of date for children too, and parent
	// is informed about this. Also boundingbox is set out of date.
	private void transformChanged() {
		// If hidden, then do nothing
		if (!visible)
			return;

		// If already obsolete, then do nothing
		if (transf_abs_uptodate == UpToDateStatus.NO)
			return;

		// Mark all children to need recalculation too

		for (int i = 0, size = children.size(); i < size; i++) {
			children.get(i).transformChangedRaw();
		}

		transf_abs_uptodate = UpToDateStatus.NO;

		// Inform parent (and grandparents) also
		Scenenode infoparent = parent;
		while (infoparent != null
				&& infoparent.transf_abs_uptodate == UpToDateStatus.YES) {
			infoparent.transf_abs_uptodate = UpToDateStatus.NO_FOR_CHILDREN;
			infoparent = infoparent.parent;
		}

		markTotalBoundingsphereToNeedRecalculation();
	}

	// Marks absolute transform out of date but does not inform parent
	// about this. This method does not take care of boundingbox either.
	private void transformChangedRaw() {
		// If absolute transform has already been marked out
		// of date or if Scenenode is hidden, then do nothing.
		if (transf_abs_uptodate == UpToDateStatus.NO)
			return;
		if (!visible)
			return;

		for (int i = 0, size = children.size(); i < size; i++) {
			children.get(i).transformChangedRaw();
		}
		transf_abs_uptodate = UpToDateStatus.NO;
	}

	private void markTotalBoundingsphereToNeedRecalculation() {
		Scenenode totalbb_owner = this;
		while (totalbb_owner.totalbb_uptodate) {
			totalbb_owner.totalbb_uptodate = false;
			totalbb_owner = totalbb_owner.parent;
			if (totalbb_owner == null) {
				break;
			}
		}
	}

	private void updateAbsoluteTransform(final Matrix4 parent_transf_abs) {
		// If hidden, then do nothing
		if (!visible) {
			return;
		}

		assert parent != null
				|| parent.transf_abs_uptodate != UpToDateStatus.NO;
		assert transf_abs_uptodate != UpToDateStatus.YES;

		// Update absolute transform if it's really needed
		if (transf_abs_uptodate == UpToDateStatus.NO) {
			transf_abs.set(parent_transf_abs);
			transf_abs.mul(transf);
		}

		// Mark transform updated
		transf_abs_uptodate = UpToDateStatus.YES;

		// Go children through and make required updates
		for (int i = 0, size = children.size(); i < size; i++) {
			Scenenode child = children.get(i);
			if (child.transf_abs_uptodate != UpToDateStatus.YES) {
				child.updateAbsoluteTransform(transf_abs);
			}
		}
	}

	private void checkIfAllChildrenHasAbsoluteTransformsUpToDate() {
		assert transf_abs_uptodate == UpToDateStatus.NO_FOR_CHILDREN;
		boolean all_up_to_date = true;
		for (int i = 0, size = children.size(); i < size; i++) {
			Scenenode child = children.get(i);
			// Skip children that are hidden
			if (!child.visible) {
				continue;
			}
			if (child.transf_abs_uptodate != UpToDateStatus.YES) {
				all_up_to_date = false;
				break;
			}
		}
		if (!all_up_to_date) {
			return;
		}
		transf_abs_uptodate = UpToDateStatus.YES;
		if (parent != null) {
			parent.checkIfAllChildrenHasAbsoluteTransformsUpToDate();
		}
	}

}
