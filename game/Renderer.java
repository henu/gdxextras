package fi.henu.gdxextras.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public interface Renderer
{
	void render(SpriteBatch batch, Vector3 pos);

	float getBoundsTop(Vector3 pos);
	float getBoundsRight(Vector3 pos);
	float getBoundsBottom(Vector3 pos);
	float getBoundsLeft(Vector3 pos);
}
