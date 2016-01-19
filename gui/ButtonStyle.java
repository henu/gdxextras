package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class ButtonStyle
{
	// Textures
	public AtlasRegion region_left;
	public AtlasRegion region_right;
	public Texture tex_center;
	public AtlasRegion region_hilight_left;
	public AtlasRegion region_hilight_right;
	public Texture tex_hilight_center;
	// Sizes
	public float side_padding = 0f;
	public float bg_scaling = 1f;
	public float label_scaling = 1f;
	// Colors
	public Color bg_color = Color.WHITE;
	public Color bg_color_disabled = Color.GRAY;
	public Color bg_color_pressed = Color.WHITE;
	public Color label_color = Color.BLACK;
	public Color label_color_pressed = Color.BLACK;
	public Color label_color_disabled = Color.DARK_GRAY;
	public Color icon_color;
	public Color icon_color_disabled;
	// Font
	public BitmapFont font;
}
