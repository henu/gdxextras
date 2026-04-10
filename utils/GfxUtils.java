package fi.henu.gdxextras.utils;

import com.badlogic.gdx.graphics.Color;
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

	public static Color combineColors(Color result, Color color1, Color color2)
	{
		float r, g, b, a;
		if (color1 != null && color2 != null) {
			r = color1.r * color2.r;
			g = color1.g * color2.g;
			b = color1.b * color2.b;
			a = color1.a * color2.a;
		} else if (color1 != null) {
			r = color1.r;
			g = color1.g;
			b = color1.b;
			a = color1.a;
		} else if (color2 != null) {
			r = color2.r;
			g = color2.g;
			b = color2.b;
			a = color2.a;
		} else {
			r = 1;
			g = 1;
			b = 1;
			a = 1;
		}
		result.set(r, g, b, a);
		return result;
	}

	public static Color combineColors(Color result, Color color1, Color color2, Color color3)
	{
		float r, g, b, a;
		if (color1 != null && color2 != null && color3 != null) {
			r = color1.r * color2.r * color3.r;
			g = color1.g * color2.g * color3.g;
			b = color1.b * color2.b * color3.b;
			a = color1.a * color2.a * color3.a;
		} else if (color1 != null && color2 != null) {
			r = color1.r * color2.r;
			g = color1.g * color2.g;
			b = color1.b * color2.b;
			a = color1.a * color2.a;
		} else if (color2 != null && color3 != null) {
			r = color2.r * color3.r;
			g = color2.g * color3.g;
			b = color2.b * color3.b;
			a = color2.a * color3.a;
		} else if (color1 != null && color3 != null) {
			r = color1.r * color3.r;
			g = color1.g * color3.g;
			b = color1.b * color3.b;
			a = color1.a * color3.a;
		} else if (color1 != null) {
			r = color1.r;
			g = color1.g;
			b = color1.b;
			a = color1.a;
		} else if (color2 != null) {
			r = color2.r;
			g = color2.g;
			b = color2.b;
			a = color2.a;
		} else if (color3 != null) {
			r = color3.r;
			g = color3.g;
			b = color3.b;
			a = color3.a;
		} else {
			r = 1;
			g = 1;
			b = 1;
			a = 1;
		}
		result.set(r, g, b, a);
		return result;
	}
}
