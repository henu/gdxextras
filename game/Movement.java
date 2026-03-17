package fi.henu.gdxextras.game;

import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;

public interface Movement
{
	void run(GameObject obj, float deltatime, Controls controls);

	void bounce(Array<Collision> colls);
}
