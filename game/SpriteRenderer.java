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
	}

	public SpriteRenderer(TextureRegion region, float scale_x, float scale_y)
	{
		this.region = region;
		this.scale_x = scale_x;
		this.scale_y = scale_y;
	}

	@Override
	public void render(SpriteBatch batch, Vector3 pos)
	{
// TODO: Check what projection is in use!
		float draw_w = region.getRegionWidth() * scale_x;
		float draw_h = region.getRegionHeight() * scale_y;
		batch.draw(region, pos.x - draw_w / 2f, pos.z - draw_h / 2f, draw_w, draw_h);
	}

	@Override
	public float getBoundsTop(Vector3 pos)
	{
		float draw_h = region.getRegionHeight() * scale_y;
		return pos.z + draw_h / 2f;
	}

	@Override
	public float getBoundsRight(Vector3 pos)
	{
		float draw_w = region.getRegionWidth() * scale_x;
		return pos.x + draw_w / 2f;
	}

	@Override
	public float getBoundsBottom(Vector3 pos)
	{
		float draw_h = region.getRegionHeight() * scale_y;
		return pos.z - draw_h / 2f;
	}

	@Override
	public float getBoundsLeft(Vector3 pos)
	{
		float draw_w = region.getRegionWidth() * scale_x;
		return pos.x - draw_w / 2f;
	}

	private final TextureRegion region;
	private final float scale_x, scale_y;
}
