package fi.henu.gdxextras.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public interface Renderer
{
	void render(SpriteBatch batch, Vector3 pos, Camera camera);

	// Return bounds, measured in game units, not in pixels.
	float getBoundsTop(Vector3 pos, Camera camera);
	float getBoundsRight(Vector3 pos, Camera camera);
	float getBoundsBottom(Vector3 pos, Camera camera);
	float getBoundsLeft(Vector3 pos, Camera camera);
}
