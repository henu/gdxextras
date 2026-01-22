package fi.henu.gdxextras.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.concurrent.Callable;

public class GeneralUtils
{
	public static final int KB_META_ALT_LEFT = 0x0001;
	public static final int KB_META_ALT_RIGHT = 0x0002;
	public static final int KB_META_ALT = 0x0004;
	public static final int KB_META_CONTROL_LEFT = 0x0008;
	public static final int KB_META_CONTROL_RIGHT = 0x0010;
	public static final int KB_META_CONTROL = 0x0020;
	public static final int KB_META_SHIFT_LEFT = 0x0040;
	public static final int KB_META_SHIFT_RIGHT = 0x0080;
	public static final int KB_META_SHIFT = 0x0100;

	public static <T> T retry(Callable<T> func, int max_tries, long sleep_ms)
	{
		int try_i = 0;
		while (true) {
			++try_i;
			try {
				return func.call();
			}
			catch (Exception e) {
				if (try_i >= max_tries) {
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(sleep_ms);
				}
				catch (InterruptedException ignored) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	// Checks if a set of meta keys is pressed at the same time
	public static boolean KbMetaKeys(int meta_key_flags)
	{
		// Check alt keys
		if ((meta_key_flags & KB_META_ALT_LEFT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) return false;
		}
		if ((meta_key_flags & KB_META_ALT_RIGHT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) return false;
		}
		if ((meta_key_flags & KB_META_ALT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) return false;
		} else {
			if ((meta_key_flags & KB_META_ALT_LEFT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) return false;
			}
			if ((meta_key_flags & KB_META_ALT_RIGHT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) return false;
			}
		}

		// Check control keys
		if ((meta_key_flags & KB_META_CONTROL_LEFT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return false;
		}
		if ((meta_key_flags & KB_META_CONTROL_RIGHT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) return false;
		}
		if ((meta_key_flags & KB_META_CONTROL) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) return false;
		} else {
			if ((meta_key_flags & KB_META_CONTROL_LEFT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return false;
			}
			if ((meta_key_flags & KB_META_CONTROL_RIGHT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) return false;
			}
		}

		// Check alt keys
		if ((meta_key_flags & KB_META_SHIFT_LEFT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) return false;
		}
		if ((meta_key_flags & KB_META_SHIFT_RIGHT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) return false;
		}
		if ((meta_key_flags & KB_META_SHIFT) != 0) {
			if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) return false;
		} else {
			if ((meta_key_flags & KB_META_SHIFT_LEFT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) return false;
			}
			if ((meta_key_flags & KB_META_SHIFT_RIGHT) == 0) {
				if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) return false;
			}
		}

		return true;
	}
}
