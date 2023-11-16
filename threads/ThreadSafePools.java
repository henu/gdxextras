package fi.henu.gdxextras.threads;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;

public class ThreadSafePools
{
	static public <T> T obtain(Class<T> type)
	{
		synchronized (pools) {
			Pool pool = pools.get(type);
			if (pool == null) {
				pool = new ReflectionPool(type, 10, 1000);
				pools.put(type, pool);
			}
			return (T)pool.obtain();
		}
	}

	static public void free(Object obj)
	{
		if (obj == null) {
			return;
		}
		synchronized (pools) {
			Pool pool = pools.get(obj.getClass());
			if (pool == null) {
				pool = new ReflectionPool(obj.getClass(), 10, 1000);
				pools.put(obj.getClass(), pool);
			}
			pool.free(obj);
		}
	}

	static public void freeAll(Array objs)
	{
		freeAll(objs, false);
	}

	static public void freeAll(Array objs, boolean same_pool)
	{
		if (objs == null) {
			return;
		}
		Pool pool = null;
		for (int obj_i = 0; obj_i < objs.size; ++ obj_i) {
			Object obj = objs.get(obj_i);
			if (obj == null) {
				continue;
			}
			synchronized (pools) {
				if (pool == null) {
					pool = pools.get(obj.getClass());
					if (pool == null) {
						pool = new ReflectionPool(obj.getClass(), 10, 1000);
						pools.put(obj.getClass(), pool);
					}
				}
				pool.free(obj);
			}
			if (!same_pool) {
				pool = null;
			}
		}
	}

	static private final ObjectMap<Class, Pool> pools = new ObjectMap<>();
}
