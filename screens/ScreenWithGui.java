package fi.henu.gdxextras.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import fi.henu.gdxextras.gui.Eventlistener;
import fi.henu.gdxextras.gui.Gui;

public abstract class ScreenWithGui implements Screen
{
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
	}

	protected Gui getGui()
	{
		makeSureGuiExists();
		return gui;
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

	private void makeSureGuiExists()
	{
		if (gui == null) {
			gui = new Gui();
			buildGui(gui);
		}
	}
}
