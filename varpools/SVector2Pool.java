package fi.henu.gdxextras.varpools;

import com.badlogic.gdx.utils.Pool;

import fi.henu.gdxextras.SVector2;

public class SVector2Pool extends Pool<SVector2>
{
	public SVector2Pool(int initial_capacity, int max)
	{
		super(initial_capacity, max);
	}

	public SVector2 obtain(short x, short y)
	{
		SVector2 sv2 = obtain();
		sv2.set(x, y);
		return sv2;
	}

	public SVector2 obtain(int x, int y)
	{
		SVector2 new_sv2 = obtain();
		new_sv2.set((short)x, (short)y);
		return new_sv2;
	}

	public SVector2 obtain(SVector2 sv2)
	{
		SVector2 new_sv2 = obtain();
		new_sv2.set(sv2);
		return new_sv2;
	}

	public void freeAll(Iterable<SVector2> sv2s)
	{
		for (SVector2 sv2 : sv2s) {
			free(sv2);
		}
	}

	@Override
	protected SVector2 newObject()
	{
		return new SVector2();
	}
}
