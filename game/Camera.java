package fi.henu.gdxextras.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Camera
{
	public Camera()
	{
		camera_type = CT_NOTHING;
		scaling = 1;
		scroll = new Vector2(0, 0);
		projection_matrix = new Matrix4();
	}

	public void setSideCamera()
	{
		camera_type = CT_SIDE_CAMERA;
		recalculateProjectionMatrix();
	}

	public void setScaling(float scaling)
	{
		this.scaling = scaling;
		recalculateProjectionMatrix();
	}

	// This is measured in game units, not in screen pixels. So for example
	// if scaling is 2, then scrolling 100 means scrolling 200 real pixels.
	public void setScroll(float x, float y)
	{
		scroll.set(x, y);
		recalculateProjectionMatrix();
	}

	public boolean isSideCamera()
	{
		return camera_type == CT_SIDE_CAMERA;
	}

	public float getScaling()
	{
		return scaling;
	}

	public Vector2 getScroll()
	{
		return scroll;
	}

	public Matrix4 getProjectionMatrix()
	{
		return projection_matrix;
	}

	private static final short CT_NOTHING = 0;
	private static final short CT_SIDE_CAMERA = 1;

	// Camera type. This is set usually only once
	private short camera_type;

	// Camera scaling and scrolling. These are changed often
	private float scaling;
	private final Vector2 scroll;

	// This is a result of scaling, scrolling and window size
	private final Matrix4 projection_matrix;

	private void recalculateProjectionMatrix()
	{
		if (camera_type == CT_SIDE_CAMERA) {
			float viewport_width = Gdx.graphics.getWidth() / scaling;
			float viewport_height = Gdx.graphics.getHeight() / scaling;
			float[] projection_matrix_raw = {
				2f / viewport_width, 0f, 0f, 0f,
				0f, 2f / viewport_height, 0f, 0f,
				0f, 0f, -2f, 0f,
				-1f, -1f, -1f, 1f
			};
			projection_matrix.set(projection_matrix_raw);
			projection_matrix.translate(scroll.x, scroll.y, 0);
			return;
		}

		throw new RuntimeException("Unable to calculate projection matrix because camera has no type!");
	}
}
