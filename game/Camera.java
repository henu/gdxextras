package fi.henu.gdxextras.game;

import com.badlogic.gdx.math.Matrix4;

public class Camera
{
	public Camera()
	{
		camera_type = CT_NOTHING;
		projection_matrix = new Matrix4();
	}

	public void setViewport(float left, float bottom, float right, float top)
	{
		viewport_left = left;
		viewport_bottom = bottom;
		viewport_right = right;
		viewport_top = top;
		recalculateProjectionMatrix();
	}

	public void setSideCamera()
	{
		camera_type = CT_SIDE_CAMERA;
		recalculateProjectionMatrix();
	}

	public float getViewportTop()
	{
		return viewport_top;
	}

	public float getViewportRight()
	{
		return viewport_right;
	}

	public float getViewportBottom()
	{
		return viewport_bottom;
	}

	public float getViewportLeft()
	{
		return viewport_left;
	}

	public Matrix4 getProjectionMatrix()
	{
		return projection_matrix;
	}

	private static final short CT_NOTHING = 0;
	private static final short CT_SIDE_CAMERA = 1;

	private short camera_type;

	private final Matrix4 projection_matrix;

	private float viewport_top;
	private float viewport_right;
	private float viewport_bottom;
	private float viewport_left;

	private void recalculateProjectionMatrix()
	{
		if (camera_type == CT_SIDE_CAMERA) {
			float viewport_width = viewport_right - viewport_left;
			float viewport_height = viewport_top - viewport_bottom;
			if (viewport_width > 0 && viewport_height > 0) {
				float[] projection_matrix_raw = {
						2f / viewport_width, 0f, 0f, 0f,
						0f, 2f / viewport_height, 0f, 0f,
						0f, 0f, -2f, 0f,
						-1f, -1f, -1f, 1f
				};
				projection_matrix.set(projection_matrix_raw);
				projection_matrix.translate(-viewport_left, -viewport_bottom, 0);
			}
			return;
		}

		throw new RuntimeException("Unable to calculate projection matrix because camera has no type!");
	}
}
