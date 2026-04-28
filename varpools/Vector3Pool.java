package fi.henu.gdxextras.varpools;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class Vector3Pool extends Pool<Vector3>
{
    public Vector3Pool(int initial_capacity, int max)
    {
        super(initial_capacity, max);
    }

    public Vector3 obtain(float x, float y, float z)
    {
        Vector3 new_v3 = obtain();
        new_v3.set(x, y, z);
        return new_v3;
    }

    public Vector3 obtain(Vector3 v3)
    {
        Vector3 new_v3 = obtain();
        new_v3.set(v3);
        return new_v3;
    }

    @Override
    protected Vector3 newObject()
    {
        return new Vector3();
    }
}
