package fi.henu.gdxextras.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

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

	// World dimensions mean how big the tiles are when calculating physics, and render
	// dimensions mean what is the total size of a single tile rhombus in pixels.
	public void setIsometricCamera(float tile_world_width, float tile_world_height, float tile_render_width, float tile_render_height, float tile_render_heightstep)
	{
		if (tile_world_width <= 0 || tile_world_height <= 0) {
			throw new RuntimeException("Tile world width and height must be greater than zero!");
		}
		camera_type = CT_ISOMETRIC;
		isometric_tile_world_width = tile_world_width;
		isometric_tile_world_height = tile_world_height;
		isometric_tile_render_width = tile_render_width;
		isometric_tile_render_height = tile_render_height;
		isometric_tile_render_heightstep = tile_render_heightstep;
		isometric_obj_comp = new IsometricObjectComparator();
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

	public boolean isIsometricCamera()
	{
		return camera_type == CT_ISOMETRIC;
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
		if (Gdx.graphics.getWidth() != projection_matrix_window_width || Gdx.graphics.getHeight() != projection_matrix_window_height) {
			recalculateProjectionMatrix();
		}
		return projection_matrix;
	}

	public float getIsometricDrawX(Vector3 pos)
	{
		assert(camera_type == CT_ISOMETRIC);
		float pos_tiles_x = pos.x / isometric_tile_world_width;
		float pos_tiles_y = pos.y / isometric_tile_world_height;
		float pos_tiles_z = pos.z / isometric_tile_world_width;
		return (pos_tiles_x + pos_tiles_z) * isometric_tile_render_width / 2 + pos_tiles_y * isometric_tile_render_heightstep;
	}

	public float getIsometricDrawY(Vector3 pos)
	{
		assert(camera_type == CT_ISOMETRIC);
		float pos_tiles_x = pos.x / isometric_tile_world_width;
		float pos_tiles_y = pos.y / isometric_tile_world_height;
		float pos_tiles_z = pos.z / isometric_tile_world_width;
		return (-pos_tiles_x + pos_tiles_z) * isometric_tile_render_height / 2 + pos_tiles_y * isometric_tile_render_heightstep;
	}

	public void sortObjectsBeforeRendering(Array<GameObject> objs)
	{
		if (camera_type == CT_ISOMETRIC) {
			objs.sort(isometric_obj_comp);
		}
	}

	private class IsometricObjectComparator implements Comparator<GameObject>
	{
		@Override
		public int compare(GameObject obj1, GameObject obj2)
		{
			float obj1_depth = obj1.getPosition().z - obj1.getPosition().x;
			float obj2_depth = obj2.getPosition().z - obj2.getPosition().x;
			if (obj1_depth < obj2_depth) return 1;
			if (obj1_depth > obj2_depth) return -1;
			return 0;
		}
	}

	private static final short CT_NOTHING = 0;
	private static final short CT_SIDE_CAMERA = 1;
	private static final short CT_ISOMETRIC = 2;

	// Camera type. This is set usually only once
	private short camera_type;

	// Properties for isometric camera. These are changed only when camera type is set.
	private float isometric_tile_world_width;
	private float isometric_tile_world_height;
	private float isometric_tile_render_width;
	private float isometric_tile_render_height;
	private float isometric_tile_render_heightstep;
	private IsometricObjectComparator isometric_obj_comp;

	// Camera scaling and scrolling. These are changed often
	private float scaling;
	private final Vector2 scroll;

	// This is a result of scaling, scrolling and window size
	private final Matrix4 projection_matrix;

	// These keep track at what window size the projection matrix was recalculated at
	private int projection_matrix_window_width;
	private int projection_matrix_window_height;

	private void recalculateProjectionMatrix()
	{
		if (camera_type == CT_SIDE_CAMERA || camera_type == CT_ISOMETRIC) {
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

			// Mark at what window size the projection matrix was recalculated
			projection_matrix_window_width = Gdx.graphics.getWidth();
			projection_matrix_window_height = Gdx.graphics.getWidth();

			return;
		}

		throw new RuntimeException("Unable to calculate projection matrix because camera has unsupported type!");
	}
}
