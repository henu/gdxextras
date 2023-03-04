package fi.henu.gdxextras.game;

import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;

public interface Movement
{
	void run(GameObject obj, float delta, Controls controls);

	void bounce(Array<Collision> colls);
}
