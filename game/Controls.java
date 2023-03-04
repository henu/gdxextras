package fi.henu.gdxextras.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Controls
{
	public void readFromKeyboard()
	{
		pressed_up = Gdx.input.isKeyPressed(Input.Keys.UP);
		pressed_right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		pressed_down = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		pressed_left = Gdx.input.isKeyPressed(Input.Keys.LEFT);
	}

	public void setUpPressed(boolean pressed)
	{
		pressed_up = pressed;
	}

	public void setRightPressed(boolean pressed)
	{
		pressed_right = pressed;
	}

	public void setDownPressed(boolean pressed)
	{
		pressed_down = pressed;
	}

	public void setLeftPressed(boolean pressed)
	{
		pressed_left = pressed;
	}

	public boolean isUpPressed()
	{
		return pressed_up;
	}

	public boolean isRightPressed()
	{
		return pressed_right;
	}

	public boolean isDownPressed()
	{
		return pressed_down;
	}

	public boolean isLeftPressed()
	{
		return pressed_left;
	}

	private boolean pressed_up;
	private boolean pressed_right;
	private boolean pressed_down;
	private boolean pressed_left;
}
