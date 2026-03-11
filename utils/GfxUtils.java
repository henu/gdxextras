package fi.henu.gdxextras.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class GfxUtils
{
	public static void drawRegionFromCenter(SpriteBatch batch, TextureAtlas.AtlasRegion region, float x, float y, float scale_x, float scale_y, float rotation)
	{
		float draw_x = x + (region.offsetX - region.originalWidth * 0.5f) * scale_x;
		float draw_y = y + (region.offsetY - region.originalHeight * 0.5f) * scale_y;
		batch.draw(region, draw_x, draw_y, region.getRegionWidth() * 0.5f * scale_x, region.getRegionHeight() * 0.5f * scale_y, region.packedWidth * scale_x, region.packedHeight * scale_y, 1, 1, rotation);
	}
}
