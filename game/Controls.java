package fi.henu.gdxextras.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Controls
{
	public void readFromKeyboard()
	{
		pressed_up = Gdx.input.isKeyPressed(Input.Keys.UP) ? 1 : 0;
		pressed_right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? 1 : 0;
		pressed_down = Gdx.input.isKeyPressed(Input.Keys.DOWN) ? 1 : 0;
		pressed_left = Gdx.input.isKeyPressed(Input.Keys.LEFT) ? 1 : 0;
		pressed_fire = Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
	}

	public void set(Controls controls)
	{
		pressed_up = controls.pressed_up;
		pressed_right = controls.pressed_right;
		pressed_down = controls.pressed_down;
		pressed_left = controls.pressed_left;
		pressed_fire = controls.pressed_fire;
	}

	public void setUpPressed(boolean pressed)
	{
		pressed_up = pressed ? 1 : 0;
	}

	public void setRightPressed(boolean pressed)
	{
		pressed_right = pressed ? 1 : 0;
	}

	public void setDownPressed(boolean pressed)
	{
		pressed_down = pressed ? 1 : 0;
	}

	public void setLeftPressed(boolean pressed)
	{
		pressed_left = pressed ? 1 : 0;
	}

	public void setFirePressed(boolean pressed)
	{
		pressed_fire = pressed;
	}

	public void setUpPressed(float pressed)
	{
		pressed_up = pressed;
	}

	public void setRightPressed(float pressed)
	{
		pressed_right = pressed;
	}

	public void setDownPressed(float pressed)
	{
		pressed_down = pressed;
	}

	public void setLeftPressed(float pressed)
	{
		pressed_left = pressed;
	}

	public boolean isUpPressed()
	{
		return pressed_up > 0;
	}

	public boolean isRightPressed()
	{
		return pressed_right > 0;
	}

	public boolean isDownPressed()
	{
		return pressed_down > 0;
	}

	public boolean isLeftPressed()
	{
		return pressed_left > 0;
	}

	public boolean isFirePressed()
	{
		return pressed_fire;
	}

	public float getUpPressed()
	{
		return pressed_up;
	}

	public float getRightPressed()
	{
		return pressed_right;
	}

	public float getDownPressed()
	{
		return pressed_down;
	}

	public float getLeftPressed()
	{
		return pressed_left;
	}

	private float pressed_up;
	private float pressed_right;
	private float pressed_down;
	private float pressed_left;
	private boolean pressed_fire;
}
