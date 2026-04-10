package fi.henu.gdxextras.varpools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Vector2Pool extends Pool<Vector2>
{
	public Vector2Pool(int initial_capacity, int max)
	{
		super(initial_capacity, max);
	}

	public Vector2 obtain(float x, float y)
	{
		Vector2 new_v2 = obtain();
		new_v2.set(x, y);
		return new_v2;
	}

	public Vector2 obtain(Vector2 v2)
	{
		Vector2 new_v2 = obtain();
		new_v2.set(v2);
		return new_v2;
	}

	@Override
	protected Vector2 newObject()
	{
		return new Vector2();
	}
}
