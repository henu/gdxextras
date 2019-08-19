package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;

public class LabelStyle
{
	public LabelStyle()
	{
		parent = null;
		height = -1;
	}

	public LabelStyle(LabelStyle parent)
	{
		this.parent = parent;
		height = -1;
	}

	public void setHeight(float height)
	{
		this.height = height;
	}

	public float getHeight()
	{
		if (parent != null && height < 0) {
			return parent.getHeight();
		}
		return height;
	}

	public void setFont(BitmapFont font)
	{
		this.font = font;
	}

	public BitmapFont getFont()
	{
		if (parent != null && font == null) {
			return parent.getFont();
		}
		return font;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		if (parent != null && color == null) {
			return parent.getColor();
		}
		return color;
	}

	public void setShadow(Vector2 shadow)
	{
		this.shadow = shadow;
	}

	public Vector2 getShadow()
	{
		if (parent != null && shadow == null) {
			return parent.getShadow();
		}
		return shadow;
	}

	private final LabelStyle parent;

	private float height;
	private BitmapFont font;
	private Color color;
	private Vector2 shadow;
}
