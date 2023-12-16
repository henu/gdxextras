package fi.henu.gdxextras.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;

public class EightDirsControlled implements Movement
{
	public static final int FLAG_UP = 0x01;
	public static final int FLAG_UP_RIGHT = 0x02;
	public static final int FLAG_RIGHT = 0x04;
	public static final int FLAG_DOWN_RIGHT = 0x08;
	public static final int FLAG_DOWN = 0x10;
	public static final int FLAG_DOWN_LEFT = 0x20;
	public static final int FLAG_LEFT = 0x40;
	public static final int FLAG_UP_LEFT = 0x80;

	public EightDirsControlled(float speed)
	{
		this.speed = speed;
		this.enabled_dir_flags = 0xff;
	}

	public EightDirsControlled(float speed, int enabled_dir_flags)
	{
		this.speed = speed;
		this.enabled_dir_flags = enabled_dir_flags;
	}

	private final float speed;

	private final int enabled_dir_flags;

	@Override
	public void run(GameObject obj, float delta, Controls controls)
	{
		if (controls == null) {
			return;
		}

		Vector3 pos = obj.getPosition();

		final float SIN_45 = 0.707106781f;

		boolean isometric = obj.getGameWorld().getCamera().isIsometricCamera();

		boolean key_up = controls.isUpPressed();
		boolean key_right = controls.isRightPressed();
		boolean key_down = controls.isDownPressed();
		boolean key_left = controls.isLeftPressed();
// TODO: Use analog controls!
		if (key_up && !key_down) {
			if (key_right && !key_left) {
				if ((enabled_dir_flags & FLAG_UP_RIGHT) != 0) {
					if (isometric) {
						pos.z += speed * delta;
					} else {
						pos.x += speed * delta * SIN_45;
						pos.z += speed * delta * SIN_45;
					}
				}
				return;
			}
			if (key_left && !key_right) {
				if ((enabled_dir_flags & FLAG_UP_LEFT) != 0) {
					if (isometric) {
						pos.x -= speed * delta;
					} else {
						pos.x -= speed * delta * SIN_45;
						pos.z += speed * delta * SIN_45;
					}
				}
				return;
			}
			if ((enabled_dir_flags & FLAG_UP) != 0) {
				if (isometric) {
					pos.x -= speed * delta * SIN_45;
					pos.z += speed * delta * SIN_45;
				} else {
					pos.z += speed * delta;
				}
			}
			return;
		}
		if (key_down && !key_up) {
			if (key_right && !key_left) {
				if ((enabled_dir_flags & FLAG_DOWN_RIGHT) != 0) {
					if (isometric) {
						pos.x += speed * delta;
					} else {
						pos.x += speed * delta * SIN_45;
						pos.z -= speed * delta * SIN_45;
					}
				}
				return;
			}
			if (key_left && !key_right) {
				if ((enabled_dir_flags & FLAG_DOWN_LEFT) != 0) {
					if (isometric) {
						pos.z -= speed * delta;
					} else {
						pos.x -= speed * delta * SIN_45;
						pos.z -= speed * delta * SIN_45;
					}
				}
				return;
			}
			if ((enabled_dir_flags & FLAG_DOWN) != 0) {
				if (isometric) {
					pos.x += speed * delta * SIN_45;
					pos.z -= speed * delta * SIN_45;
				} else {
					pos.z -= speed * delta;
				}
			}
			return;
		}
		if (key_right && !key_left) {
			if ((enabled_dir_flags & FLAG_RIGHT) != 0) {
				if (isometric) {
					pos.x += speed * delta * SIN_45;
					pos.z += speed * delta * SIN_45;
				} else {
					pos.x += speed * delta;
				}
			}
			return;
		}
		if (key_left && !key_right) {
			if ((enabled_dir_flags & FLAG_LEFT) != 0) {
				if (isometric) {
					pos.x -= speed * delta * SIN_45;
					pos.z -= speed * delta * SIN_45;
				} else {
					pos.x -= speed * delta;
				}
			}
		}
	}

	@Override
	public void bounce(Array<Collision> colls)
	{
	}
}
