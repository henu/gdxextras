package fi.henu.gdxextras.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;
import fi.henu.gdxextras.collisions.CollisionHandler;

public class LinearMovement implements Movement
{
	public LinearMovement()
	{
		vel = new Vector3();
	}

	public LinearMovement(float x, float y, float z)
	{
		vel = new Vector3(x, y, z);
	}

	public LinearMovement(float x, float z)
	{
		vel = new Vector3(x, 0, z);
	}

	@Override
	public void run(GameObject obj, float delta, Controls controls)
	{
		obj.getPosition().add(vel.x * delta, vel.y * delta, vel.z * delta);
	}

	@Override
	public void bounce(Array<Collision> colls)
	{
		CollisionHandler.bounce(vel, colls);
	}

	private final Vector3 vel;
}
