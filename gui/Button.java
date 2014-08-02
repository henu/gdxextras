package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import fi.henu.gdxextras.Font;

// TODO: Make toggling on/off available!
public class Button extends Widget
{

	// This is used to set default background and selected graphics for Button
	public static void setDefaultGraphics(AtlasRegion region_left, AtlasRegion region_right, Texture tex_center, AtlasRegion region_hilight_left, AtlasRegion region_hilight_right, Texture tex_hilight_center, float end_width, float height)
	{
		default_region_left = region_left;
		default_region_right = region_right;
		default_tex_center = tex_center;
		default_region_hilight_left = region_hilight_left;
		default_region_hilight_right = region_hilight_right;
		default_tex_hilight_center = tex_hilight_center;
		default_end_width = end_width;
		default_height = height;
	}

	public Button(float height, Color bg_color)
	{
		super();
		// Set appearance options
		this.height = height;
		this.bg_color = bg_color;
		this.bg_color_disabled = getDisabledColor(bg_color);
		// Reset icon and label
		icon = null;
		icon_color = null;
		icon_color_disabled = null;
		label = null;
		label_color = null;
		label_color_disabled = null;
		label_pixelheight = 0;
		label_font = null;
		// Reset state
		hilight = false;
		pressed = false;
		enabled = true;
	}

	public void setLabel(String label, float pixelheight, Color color, Font font)
	{
		this.label = label;
		label_pixelheight = pixelheight;
		label_color = color;
		this.label_color_disabled = getDisabledColor(label_color);
		label_font = font;
		markToNeedReposition();
	}
	
	public void setIcon(AtlasRegion icon, Color icon_color)
	{
		this.icon = icon;
		this.icon_color = icon_color;
		this.icon_color_disabled = getDisabledColor(icon_color);
	}

	// Hilight can be used to display constant selection of button.
	public void setHilight(boolean enabled)
	{
		hilight = enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			clearKeyboardListener();
			pressed = true;
			return true;
		}
		return false;
	}

	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			pressed = pointerOver(0);
		}
	}

	public void pointerUp(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			if (pointerOver(0) && enabled) {
				fireEvent();
			}
			pressed = false;
			unregisterPointerListener(pointer_id);
		}
	}

	// Returns true if position is over Widget. Position is relative to Widget
	// and over the rectangle of Widget, so this function is for Widgets that
	// are not rectangle shaped.
	public boolean isOver(float x, float y)
	{
		return true;
	}

	protected void doRendering(SpriteBatch batch)
	{
		if (!enabled) {
			batch.setColor(bg_color_disabled);
		} else {
			batch.setColor(bg_color);
		}
		float scale = height / default_height;
		// Render background
		renderHorizontalBar(batch, default_region_left, default_end_width, default_region_right, default_end_width, default_tex_center, default_height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		// Render possible hilights
		if (pressed && enabled) {
			batch.setColor(1, 1, 1, 1);
			renderHorizontalBar(batch, default_region_hilight_left, default_end_width, default_region_hilight_right, default_end_width, default_tex_hilight_center, default_height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		}
		if (hilight) {
			batch.setColor(1, 1, 1, 1);
			renderHorizontalBar(batch, default_region_hilight_left, default_end_width, default_region_hilight_right, default_end_width, default_tex_hilight_center, default_height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		}
		// Render possible icon
		if (icon != null) {
			if (!enabled) {
				batch.setColor(icon_color_disabled);
			} else {
				batch.setColor(icon_color);
			}			
			renderFromCenter(batch, icon, getPositionX() + default_end_width * scale, getCenterY(), scale);
		}
		// Render possible label
		if (label != null) {
			float draw_x = getPositionX() + default_end_width * scale;
			if (icon != null) {
				draw_x += default_end_width * scale;
			}
			if (!enabled) {
				label_font.renderString(batch, label, draw_x, getPositionY() + (default_height / 2) * scale + label_pixelheight / 2, label_pixelheight, label_color_disabled);
			} else {
				label_font.renderString(batch, label, draw_x, getPositionY() + (default_height / 2) * scale + label_pixelheight / 2, label_pixelheight, label_color);
			}			
		}
		batch.setColor(1, 1, 1, 1);
	}

	protected float doGetMinWidth()
	{
		float scale = height / default_height;
		float min_width = default_end_width * 2f * scale;
		if (label != null) {
			min_width += label_font.getStringWidth(label, label_pixelheight);
			if (icon != null) {
				min_width += default_end_width * scale;
			}
		}
		return min_width;
	}

	protected float doGetMinHeight(float width)
	{
		return height;
	}

	// Default graphics for all buttons
	private static AtlasRegion default_region_left = null;
	private static AtlasRegion default_region_right = null;
	private static Texture default_tex_center = null;
	private static AtlasRegion default_region_hilight_left = null;
	private static AtlasRegion default_region_hilight_right = null;
	private static Texture default_tex_hilight_center = null;
	private static float default_end_width = 0;
	private static float default_height = 0;

	// Options
	private float height;
	private Color bg_color;
	private Color bg_color_disabled;
	private AtlasRegion icon;
	private Color icon_color;
	private Color icon_color_disabled;
	private String label;
	private Color label_color;
	private Color label_color_disabled;
	private float label_pixelheight;
	private Font label_font;

	// State
	private boolean hilight;
	private boolean pressed;
	private boolean enabled;

}
