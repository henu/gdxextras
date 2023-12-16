package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class DPad extends Widget
{
	public static void setDefaultStyle(DPadStyle style)
	{
		default_style = style;
	}

	public DPad()
	{
		dpad_value_curve_strength_x = 1.8f;
		dpad_value_curve_strength_y = 1.8f;

		value = new Vector2(0, 0);

		captured_pointer_id = -1;
	}

	public float getValueXClamped()
	{
		if (value.x > 0) {
			return Math.min(1f, (float)Math.pow(value.x, dpad_value_curve_strength_x));
		} else {
			return -Math.min(1f, (float)Math.pow(Math.abs(value.x), dpad_value_curve_strength_x));
		}
	}

	public float getValueYClamped()
	{
		if (value.y > 0) {
			return Math.min(1f, (float)Math.pow(value.y, dpad_value_curve_strength_y));
		} else {
			return -Math.min(1f, (float)Math.pow(Math.abs(value.y), dpad_value_curve_strength_y));
		}
	}

	@Override
	protected float doGetMinWidth()
	{
		return getStyle().width;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		return getStyle().width;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		// Render the background
		if (getStyle().region != null) {
			render(
					batch, getStyle().region,
					getPositionX(),
					getPositionY(),
					getWidth() / getStyle().region.originalWidth
			);
		}

		// Render the center button
		if (getStyle().center_button_region != null) {
			float dpad_region_size = getWidth() * getStyle().center_button_relative_width;
			float dpad_margin_size = getWidth() * (1 - getStyle().center_button_relative_width) / 2;

			float val_x_limited = value.x;
			float val_y_limited = value.y;
			float val_len = value.len();
			if (val_len > 1) {
				val_x_limited /= val_len;
				val_y_limited /= val_len;
			}

			float scale = dpad_region_size / getStyle().center_button_region.originalWidth;
			render(
					batch, getStyle().center_button_region,
					getPositionX() + dpad_margin_size + dpad_margin_size * val_x_limited,
					getPositionY() + dpad_margin_size + dpad_margin_size * val_y_limited,
					scale
			);
		}
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (captured_pointer_id < 0) {
			captured_pointer_id = pointer_id;
			setValue(pos);
			return true;
		}
		return false;
	}

	@Override
	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == captured_pointer_id) {
			setValue(pos);
		}
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		if (pointer_id == captured_pointer_id) {
			captured_pointer_id = -1;
			value.set(0, 0);
			unregisterPointerListener(pointer_id);
		}
	}

	private static DPadStyle default_style;

	private float dpad_value_curve_strength_x, dpad_value_curve_strength_y;

	private Vector2 value;

	private int captured_pointer_id;

	private void setValue(Vector2 pointer_pos)
	{
		float dpad_margin_size = getWidth() * (1 - getStyle().center_button_relative_width) / 2;

		float moved_x = pointer_pos.x - getCenterX();
		float moved_y = pointer_pos.y - getCenterY();

		value.x = moved_x / dpad_margin_size;
		value.y = moved_y / dpad_margin_size;
	}

	private DPadStyle getStyle()
	{
// TODO: Support custom styles!
		return default_style;
	}
}
