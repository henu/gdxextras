package fi.henu.gdxextras.game.layers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import fi.henu.gdxextras.game.Camera;

public class ParallaxLayer extends Layer
{
	public ParallaxLayer(int depth_index, Texture tex, float movement_multiplier)
	{
		super(depth_index);
		this.tex = tex;
		move_m = movement_multiplier;
	}

	@Override
	public void render(SpriteBatch batch, Camera camera)
	{
		// Calculate position of screen
		float draw_x = -camera.getScroll().x;
		float draw_y = -camera.getScroll().y;
		float draw_w = Gdx.graphics.getWidth() / camera.getScaling();
		float draw_h = Gdx.graphics.getHeight() / camera.getScaling();

		// Calculate scrolling, measured in texture UV
		float u1 = -Gdx.graphics.getWidth() / 2.0f / camera.getScaling() / (float)tex.getWidth();
		float v1 = Gdx.graphics.getHeight() / 2.0f / camera.getScaling() / (float)tex.getHeight();
		float scroll_base_x = Gdx.graphics.getWidth() / camera.getScaling() / 2.0f;
		float scroll_base_y = Gdx.graphics.getHeight() / camera.getScaling() / 2.0f;
		u1 -= (camera.getScroll().x - scroll_base_x) / (float)tex.getWidth() * move_m;
		v1 += (camera.getScroll().y - scroll_base_y) / (float)tex.getHeight() * move_m;
		float u2 = u1 + Gdx.graphics.getWidth() / (float)tex.getWidth() / camera.getScaling();
		float v2 = v1 - Gdx.graphics.getHeight() / (float)tex.getHeight() / camera.getScaling();

		batch.draw(tex, draw_x, draw_y, draw_w, draw_h, u1, v1, u2, v2);
	}

	private final Texture tex;
	private final float move_m;
}
