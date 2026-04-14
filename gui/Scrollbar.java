package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

// TODO: Support vertical scrollbars!
public class Scrollbar extends Widget
{
	// This is used to set default background and selected graphics for Button
	public static void setDefaultStyle(ScrollbarStyle style)
	{
		default_style = style;
	}

	public static ScrollbarStyle getDefaultStyle()
	{
		return default_style;
	}

	public Scrollbar(float value)
	{
		this.value = value;
		setHorizontalExpanding(1);
	}

	public float getValue()
	{
		return value;
	}

	public void setStyle(ScrollbarStyle style)
	{
		this.style = style;
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			clearKeyboardListener();
			setValueFromPointer(pos);
			return true;
		}
		return false;
	}

	@Override
	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			setValueFromPointer(pos);
		}
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			setValueFromPointer(pos);
			unregisterPointerListener(pointer_id);
		}
	}

	@Override
	public void pointerCancelled(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			unregisterPointerListener(pointer_id);
		}
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		ScrollbarStyle style = getStyle();
		int pixel_height = Math.max(Math.max(Math.max(style.region_left.getRegionHeight(), style.region_right.getRegionHeight()), style.region_handle.getRegionHeight()), style.tex_center.getHeight());

		batch.setColor(style.bg_color);

		// Render background
		renderHorizontalBar(batch, style.region_left, style.region_right, style.tex_center, pixel_height, getPositionX(), getPositionY(), getWidth(), style.scaling);

		batch.setColor(style.handle_color);
		render(batch, style.region_handle, getPositionX() + getValue() * (getWidth() - style.region_handle.getRegionWidth() * style.scaling), getPositionY(), style.scaling);

		batch.setColor(1, 1, 1, 1);
	}

	@Override
	protected float doGetMinWidth()
	{
		ScrollbarStyle style = getStyle();
		int left_region_width = style.region_left.getRegionWidth();
		int right_region_width = style.region_left.getRegionWidth();
		return (left_region_width + right_region_width) * style.scaling;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		ScrollbarStyle style = getStyle();
		int pixel_height = Math.max(Math.max(Math.max(style.region_left.getRegionHeight(), style.region_right.getRegionHeight()), style.region_handle.getRegionHeight()), style.tex_center.getHeight());
		return pixel_height * style.scaling;
	}

	private static ScrollbarStyle default_style;

	private ScrollbarStyle style;

	private float value;

	private void setValueFromPointer(Vector2 pos)
	{
		ScrollbarStyle style = getStyle();
		float handle_width = style.region_handle.getRegionWidth() * style.scaling;
		value = (pos.x - handle_width / 2 - getPositionX()) / (getWidth() - handle_width);
		value = MathUtils.clamp(value, 0, 1);
		fireEvent();
	}

	private ScrollbarStyle getStyle()
	{
		if (style != null) {
			return style;
		}
		return default_style;
	}
}
