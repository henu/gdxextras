package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class ScrollbarStyle
{
	// Textures
	public TextureAtlas.AtlasRegion region_handle;
	public TextureAtlas.AtlasRegion region_left;
	public TextureAtlas.AtlasRegion region_right;
	public Texture tex_center;

	// Colors
	public Color bg_color;
	public Color handle_color;

	// Scaling
	public float scaling = 1f;
}
