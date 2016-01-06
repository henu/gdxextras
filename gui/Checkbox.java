package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Checkbox extends Widget
{
	// This is used to set default background and selected graphics for Button
	public static void setDefaultStyle(CheckboxStyle style)
	{
		default_style = style;
	}

	public Checkbox()
	{
	}

	public Checkbox(boolean checked)
	{
		this.checked = checked;
	}

	public boolean getChecked()
	{
		return checked;
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		checked = !checked;
		fireEvent();
		return false;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		CheckboxStyle style = getStyle();
		if (checked) {
			render(batch, style.checked, getPositionX(), getPositionY(), style.scaling);
		} else {
			render(batch, style.not_checked, getPositionX(), getPositionY(), style.scaling);
		}
	}

	@Override
	protected float doGetMinWidth()
	{
		CheckboxStyle style = getStyle();
		if (checked) {
			return style.checked.originalWidth * style.scaling;
		} else {
			return style.not_checked.originalWidth * style.scaling;
		}
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		CheckboxStyle style = getStyle();
		if (checked) {
			return style.checked.originalHeight * style.scaling;
		} else {
			return style.not_checked.originalHeight * style.scaling;
		}
	}

	private boolean checked;

	private static CheckboxStyle default_style;

	private CheckboxStyle getStyle()
	{
		return default_style;
	}
}
