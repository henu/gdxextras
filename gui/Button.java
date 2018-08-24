package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

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
		// Reset state
		enabled = true;
	}

	public void setStyle(ButtonStyle style)
	{
		this.style = style;
		label_layout = null;
	}

	public void setLabel(String label)
	{
		this.label = label;
		label_layout = null;
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
		label_layout = null;
	}

	public boolean isHilight()
	{
		return hilight;
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
			label_layout = null;
			return true;
		}
		return false;
	}

	@Override
	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			boolean new_pressed = pointerOver(0);
			if (pressed != new_pressed) {
				label_layout = null;
			}
			pressed = new_pressed;
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
			label_layout = null;
			unregisterPointerListener(pointer_id);
		}
	}

	// Returns true if position is over Widget. Position is relative to Widget
	// and over the rectangle of Widget, so this function is for Widgets that
	// are not rectangle shaped.
	@Override
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

		batch.setColor(1, 1, 1, 1);

		// Render possible label
		if (label != null) {
			BitmapFont font = style.font;
			font.getData().setScale(style.label_scaling);

			// Decide color
			Color font_color;
			if (!enabled) {
				font_color = style.label_color_disabled;
			} else if (pressed) {
				font_color = style.label_color_pressed;
			} else {
				font_color = style.label_color;
			}

			if (label_layout == null) {
				label_layout = new GlyphLayout(style.font, label, font_color, 0, Align.left, false);
			}

			float draw_x = getPositionX() + style.side_padding * style.bg_scaling;
			if (icon != null) {
				draw_x += style.side_padding * style.bg_scaling;
			}
			font.draw(batch, label_layout, draw_x, getPositionY() + pixel_height * style.bg_scaling - (pixel_height * style.bg_scaling - font.getLineHeight()) / 2);
		}
	}

	@Override
	protected float doGetMinWidth()
	{
		ButtonStyle style = getStyle();
		float min_width = style.side_padding * 2f * style.bg_scaling;
		if (label != null) {
			BitmapFont font = style.font;
			font.getData().setScale(style.label_scaling);

			label_layout = new GlyphLayout();
			label_layout.setText(style.font, label);

			min_width += label_layout.width;
			if (icon != null) {
				min_width += style.side_padding * style.bg_scaling;
			}

			label_layout = null;
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
	private GlyphLayout label_layout;

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
