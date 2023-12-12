package fi.henu.gdxextras.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public interface Renderer
{
	void render(SpriteBatch batch, Vector3 pos, Quaternion rot, Camera camera);

	// Return bounds, measured in game units, not in pixels.
	float getBoundsTop(Vector3 pos, Quaternion rot, Camera camera);
	float getBoundsRight(Vector3 pos, Quaternion rot, Camera camera);
	float getBoundsBottom(Vector3 pos, Quaternion rot, Camera camera);
	float getBoundsLeft(Vector3 pos, Quaternion rot, Camera camera);
}
