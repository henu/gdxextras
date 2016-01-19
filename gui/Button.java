package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

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
		// Reset state
		hilight = false;
		pressed = false;
		enabled = true;
	}

	public void setStyle(ButtonStyle style)
	{
		this.style = style;
	}

	public void setLabel(String label)
	{
		this.label = label;
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

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			clearKeyboardListener();
			pressed = true;
			return true;
		}
		return false;
	}

	@Override
	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			pressed = pointerOver(0);
		}
	}

	@Override
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

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		ButtonStyle style = getStyle();
		int pixel_height = Math.max(Math.max(style.region_left.getRegionHeight(), style.region_right.getRegionHeight()), style.tex_center.getHeight());

		if (!enabled) {
			batch.setColor(style.bg_color_disabled);
		} else {
			batch.setColor(style.bg_color);
		}
		// Render background
		renderHorizontalBar(batch, style.region_left, style.side_padding, style.region_right, style.side_padding, style.tex_center, pixel_height, getPositionX(), getPositionY(), getWidth(), getHeight(), style.bg_scaling);
		// Render possible hilights
		if (pressed && enabled) {
			batch.setColor(style.bg_color_pressed);
			renderHorizontalBar(batch, style.region_hilight_left, style.side_padding, style.region_hilight_right, style.side_padding, style.tex_hilight_center, pixel_height, getPositionX(), getPositionY(), getWidth(), getHeight(), style.bg_scaling);
		}
		if (hilight) {
			batch.setColor(1, 1, 1, 1);
			renderHorizontalBar(batch, style.region_hilight_left, style.side_padding, style.region_hilight_right, style.side_padding, style.tex_hilight_center, pixel_height, getPositionX(), getPositionY(), getWidth(), getHeight(), style.bg_scaling);
		}
		// Render possible icon
		if (icon != null) {
			if (!enabled) {
				batch.setColor(style.icon_color_disabled);
			} else {
				batch.setColor(style.icon_color);
			}
			renderFromCenter(batch, icon, getPositionX() + style.side_padding * style.bg_scaling, getCenterY(), style.bg_scaling);
		}
		// Render possible label
		if (label != null) {
			BitmapFont font = style.font;
			font.setScale(style.label_scaling);
			float draw_x = getPositionX() + style.side_padding * style.bg_scaling;
			if (icon != null) {
				draw_x += style.side_padding * style.bg_scaling;
			}
			if (!enabled) {
				font.setColor(style.label_color_disabled);
			} else if (pressed) {
				font.setColor(style.label_color_pressed);
			} else {
				font.setColor(style.label_color);
			}
			font.draw(batch, label, draw_x, getPositionY() + pixel_height * style.bg_scaling - (pixel_height * style.bg_scaling - font.getLineHeight()) / 2);
		}
		batch.setColor(1, 1, 1, 1);
	}

	@Override
	protected float doGetMinWidth()
	{
		ButtonStyle style = getStyle();
		float min_width = style.side_padding * 2f * style.bg_scaling;
		if (label != null) {
			BitmapFont font = style.font;
			font.setScale(style.label_scaling);
			min_width += font.getBounds(label).width;
			if (icon != null) {
				min_width += style.side_padding * style.bg_scaling;
			}
		}
		return min_width;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		ButtonStyle style = getStyle();
		int pixel_height = Math.max(Math.max(style.region_left.getRegionHeight(), style.region_right.getRegionHeight()), style.tex_center.getHeight());
		return pixel_height * style.bg_scaling;
	}

	private static ButtonStyle default_style;

	// Options
	private AtlasRegion icon;
	private String label;

	// State
	private boolean hilight;
	private boolean pressed;
	private boolean enabled;

	private ButtonStyle style;

	private ButtonStyle getStyle()
	{
		if (style != null) {
			return style;
		}
		return default_style;
	}
}
