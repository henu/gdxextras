package fi.henu.gdxextras;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;

abstract public class Transformhandle
{

	// Invalidates Transformhandle, clearing all references. Can be called
	// multiple times.
	public abstract void close();

	// Sets bounding box to zero sized
	public abstract void clearBoundingbox();

	// Sets boundingbox to given box
	public abstract void setBoundingbox(BoundingBox bb);

	public abstract void setInfiniteBoundingbox();

	// Extends existing boundingbox with given one
	public abstract void addBoundingbox(BoundingBox bb);

	// Gets absolute transform
	public abstract void getTransform(Matrix4 result);

	// Returns value if specific flag from handle.
	public abstract boolean getFlag(int flag_id);

}
