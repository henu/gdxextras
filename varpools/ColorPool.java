package fi.henu.gdxextras.varpools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

public class ColorPool extends Pool<Color>
{
	public ColorPool(int initial_capacity, int max)
	{
		super(initial_capacity, max);
	}

	public Color obtain(float r, float g, float b)
	{
		Color color = obtain();
		color.set(r, g, b, 1);
		return color;
	}

	public Color obtain(float r, float g, float b, float a)
	{
		Color color = obtain();
		color.set(r, g, b, a);
		return color;
	}

	public Color obtain(Color color)
	{
		Color new_color = obtain();
		if (color != null) {
			new_color.set(color);
		} else {
			new_color.set(Color.WHITE);
		}
		return new_color;
	}

	@Override
	protected Color newObject()
	{
		return new Color();
	}
}
