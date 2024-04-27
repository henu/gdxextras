package fi.henu.gdxextras.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import fi.henu.gdxextras.gui.Eventlistener;
import fi.henu.gdxextras.gui.Gui;

public abstract class ScreenWithGui implements Screen
{
	public ScreenWithGui()
	{
		super();
		batch = new SpriteBatch();
	}

	@Override
	public void render(float delta)
	{
		makeSureGuiExists();

		clearScreen();

		// Let user do other rendering
		renderBeforeGui(delta);

		// Render the GUI
		GL20 gl = Gdx.graphics.getGL20();
		gui.repositionWidgets();
		gui.render(gl);
	}

	@Override
	public void resize(int width, int height)
	{
		makeSureGuiExists();
		gui.setScreenSize(width, height);
		// Map 1 pixel to 1 unit and Y axis up.
		float[] projection_matrix = {
			2f / width, 0f, 0f, 0f,
			0f, 2f / height, 0f, 0f,
			0f, 0f, -2f, 0f,
			-1f, -1f, -1f, 1f
		};
		batch.setProjectionMatrix(new Matrix4(projection_matrix));
	}

	@Override
	public void show()
	{
		makeSureGuiExists();
		Gdx.input.setInputProcessor(gui);
	}

	@Override
	public void hide()
	{
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause()
	{
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void resume()
	{
		Gdx.input.setInputProcessor(gui);
	}

	@Override
	public void dispose()
	{
		gui.close();
		batch.dispose();
		batch = null;
	}

	public Gui getGui()
	{
		makeSureGuiExists();
		return gui;
	}

	protected SpriteBatch getSpriteBatch()
	{
		if (batch == null) {
			return null;
		}
		if (!batch.isDrawing()) {
			batch.begin();
		}
		return batch;
	}

	protected void endSpriteBatch()
	{
		batch.end();
	}

	protected void setKeyPressEventHandler(int keycode, Eventlistener handler)
	{
		makeSureGuiExists();
		gui.setKeyPressEventHandler(keycode, handler);
	}

	protected void setKeyReleaseEventHandler(int keycode, Eventlistener handler)
	{
		makeSureGuiExists();
		gui.setKeyReleaseEventHandler(keycode, handler);
	}

	protected void clearScreen()
	{
		// Clear screen
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	protected void renderBeforeGui(float delta)
	{
	}

	protected abstract void buildGui(Gui gui);

	private Gui gui;

	// Default batch for sprite rendering. This is not needed by GUI, it is just a general help.
	SpriteBatch batch;

	private void makeSureGuiExists()
	{
		if (gui == null) {
			gui = new Gui();
			buildGui(gui);
		}
	}
}
