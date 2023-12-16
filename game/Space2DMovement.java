package fi.henu.gdxextras.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;
import fi.henu.gdxextras.collisions.CollisionHandler;

public class Space2DMovement implements Movement
{
	public Space2DMovement(float acceleration, float max_velocity, float rot_speed)
	{
		vel = new Vector3();
		accel = acceleration;
		max_vel = max_velocity;
		this.rot_speed = rot_speed;
	}

	@Override
	public void run(GameObject obj, float delta, Controls controls)
	{
		obj.getPosition().add(vel.x * delta, 0, vel.z * delta);
		if (controls.isRightPressed() && !controls.isLeftPressed()) {
			obj.setRotation2D(obj.getRotation2D() - delta * rot_speed * controls.getRightPressed());
		} else if (controls.isLeftPressed() && !controls.isRightPressed()) {
			obj.setRotation2D(obj.getRotation2D() + delta * rot_speed * controls.getLeftPressed());
		}
		if (controls.isUpPressed()) {
			float angle = obj.getRotation2D();
			vel.x += -MathUtils.sinDeg(angle) * accel * delta * controls.getUpPressed();
			vel.z += MathUtils.cosDeg(angle) * accel * delta * controls.getUpPressed();
			float vel_len = vel.len();
			if (vel_len > max_vel) {
				vel.scl(max_vel / vel_len);
			}
		}
	}

	@Override
	public void bounce(Array<Collision> colls)
	{
		CollisionHandler.bounce(vel, colls);
		vel.y = 0;
	}

	// Y component will be always zero
	private final Vector3 vel;

	private final float accel;

	private final float max_vel;

	private final float rot_speed;
}
