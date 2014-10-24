package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;

import fi.henu.gdxextras.Font;

// TODO: Make toggling on/off available!
public class Button extends Widget
{

	// This is used to set default background and selected graphics for Button
	public static void setDefaultStyle(ButtonStyle style)
	{
		default_style = style;
	}

	public Button()
	{
		super();
		// Reset icon and label
		icon = null;
		label = null;
		label_pixelheight = 0;
		label_font = null;
		// Reset state
		hilight = false;
		pressed = false;
		enabled = true;
	}

	public void setLabel(String label, float pixelheight, Font font)
	{
		this.label = label;
		label_pixelheight = pixelheight;
		label_font = font;
		markToNeedReposition();
	}
	
	public void setIcon(AtlasRegion icon)
	{
		this.icon = icon;
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
			batch.setColor(getStyle().bg_color_disabled);
		} else {
			batch.setColor(getStyle().bg_color);
		}
		float scale = getStyle().scaling;
		// Render background
		renderHorizontalBar(batch, getStyle().region_left, getStyle().side_padding, getStyle().region_right, getStyle().side_padding, getStyle().tex_center, getStyle().height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		// Render possible hilights
		if (pressed && enabled) {
			batch.setColor(1, 1, 1, 1);
			renderHorizontalBar(batch, getStyle().region_hilight_left, getStyle().side_padding, getStyle().region_hilight_right, getStyle().side_padding, getStyle().tex_hilight_center, getStyle().height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		}
		if (hilight) {
			batch.setColor(1, 1, 1, 1);
			renderHorizontalBar(batch, getStyle().region_hilight_left, getStyle().side_padding, getStyle().region_hilight_right, getStyle().side_padding, getStyle().tex_hilight_center, getStyle().height, getPositionX(), getPositionY(), getWidth(), getHeight(), scale);
		}
		// Render possible icon
		if (icon != null) {
			if (!enabled) {
				batch.setColor(getStyle().icon_color_disabled);
			} else {
				batch.setColor(getStyle().icon_color);
			}			
			renderFromCenter(batch, icon, getPositionX() + getStyle().side_padding * scale, getCenterY(), scale);
		}
		// Render possible label
		if (label != null) {
			float draw_x = getPositionX() + getStyle().side_padding * scale;
			if (icon != null) {
				draw_x += getStyle().side_padding * scale;
			}
			if (!enabled) {
				label_font.renderString(batch, label, draw_x, getPositionY() + (getStyle().height / 2) * scale + label_pixelheight / 2, label_pixelheight, getStyle().label_color_disabled);
			} else {
				label_font.renderString(batch, label, draw_x, getPositionY() + (getStyle().height / 2) * scale + label_pixelheight / 2, label_pixelheight, getStyle().label_color);
			}			
		}
		batch.setColor(1, 1, 1, 1);
	}

	protected float doGetMinWidth()
	{
		float min_width = getStyle().side_padding * 2f * getStyle().scaling;
		if (label != null) {
			min_width += label_font.getStringWidth(label, label_pixelheight);
			if (icon != null) {
				min_width += getStyle().side_padding * getStyle().scaling;
			}
		}
		return min_width;
	}

	protected float doGetMinHeight(float width)
	{
		return getStyle().height * getStyle().scaling;
	}

	private static ButtonStyle default_style;

	// Options
	private AtlasRegion icon;
	private String label;
	private float label_pixelheight;
	private Font label_font;

	// State
	private boolean hilight;
	private boolean pressed;
	private boolean enabled;

	private ButtonStyle getStyle()
	{
		return default_style;
	}
}
