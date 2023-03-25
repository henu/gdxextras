package fi.henu.gdxextras.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class SpriteRenderer implements Renderer
{
	public SpriteRenderer(TextureRegion region, float scale)
	{
		this.region = region;
		scale_x = scale;
		scale_y = scale;
		region_x_offset = 0;
		region_y_offset = 0;
	}

	public SpriteRenderer(TextureRegion region, float scale_x, float scale_y)
	{
		this.region = region;
		this.scale_x = scale_x;
		this.scale_y = scale_y;
		region_x_offset = 0;
		region_y_offset = 0;
	}

	public SpriteRenderer(TextureRegion region, float scale_x, float scale_y, float region_x_offset, float region_y_offset)
	{
		this.region = region;
		this.scale_x = scale_x;
		this.scale_y = scale_y;
		this.region_x_offset = region_x_offset;
		this.region_y_offset = region_y_offset;
	}

	@Override
	public void render(SpriteBatch batch, Vector3 pos, Camera camera)
	{
		if (camera.isSideCamera()) {
			float draw_w = region.getRegionWidth() * scale_x;
			float draw_h = region.getRegionHeight() * scale_y;
			batch.draw(region, pos.x - draw_w / 2f - region_x_offset, pos.z - draw_h / 2f - region_y_offset, draw_w, draw_h);
			return;
		}

		if (camera.isIsometricCamera()) {
			float draw_w = region.getRegionWidth() * scale_x;
			float draw_h = region.getRegionHeight() * scale_y;

			float draw_x = camera.getIsometricDrawX(pos) /* - draw_w / 2f*/ - region_x_offset;
			float draw_y = camera.getIsometricDrawY(pos) /*  - draw_h / 2f */ - region_y_offset;

			batch.draw(region, draw_x, draw_y, draw_w, draw_h);
			return;
		}

		throw new RuntimeException("Unsupported camera type!");
	}

	@Override
	public float getBoundsTop(Vector3 pos, Camera camera)
	{
		if (camera.isSideCamera()) {
			float draw_h = region.getRegionHeight() * scale_y;
			return pos.z + draw_h / 2f;
		}
		throw new RuntimeException("Unsupported camera type!");
	}

	@Override
	public float getBoundsRight(Vector3 pos, Camera camera)
	{
		if (camera.isSideCamera()) {
			float draw_w = region.getRegionWidth() * scale_x;
			return pos.x + draw_w / 2f;
		}
		throw new RuntimeException("Unsupported camera type!");
	}

	@Override
	public float getBoundsBottom(Vector3 pos, Camera camera)
	{
		if (camera.isSideCamera()) {
			float draw_h = region.getRegionHeight() * scale_y;
			return pos.z - draw_h / 2f;
		}
		throw new RuntimeException("Unsupported camera type!");
	}

	@Override
	public float getBoundsLeft(Vector3 pos, Camera camera)
	{
		if (camera.isSideCamera()) {
			float draw_w = region.getRegionWidth() * scale_x;
			return pos.x - draw_w / 2f;
		}
		throw new RuntimeException("Unsupported camera type!");
	}

	private final TextureRegion region;
	private final float scale_x, scale_y;

	private final float region_x_offset, region_y_offset;
}
