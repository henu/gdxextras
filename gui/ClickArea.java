package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class ClickArea extends Widget
{
	
	// Event types
	public static final int CLICKED = 0;
	public static final int RELEASED = 1;

	public ClickArea()
	{
		// Enable expanding by default
		setHorizontalExpanding(1);
		setVerticalExpanding(1);
	}

	public Vector2 getLatestClickPosition()
	{
		return latest_click_pos;
	}

	public int getLatestPointerId()
	{
		return latest_pointer_id;
	}

	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		latest_pointer_id = pointer_id;
		latest_click_pos.set(pos);
		fireEvent(CLICKED);
		return true;
	}

	public void pointerUp(int pointer_id, Vector2 pos)
	{
		latest_pointer_id = pointer_id;
		fireEvent(RELEASED);
		unregisterPointerListener(pointer_id);
	}

	protected void doRendering(SpriteBatch batch)
	{
		// This Widget is invisible
	}

	protected float doGetMinWidth()
	{
		return 0;
	}

	protected float doGetMinHeight(float width)
	{
		return 0;
	}

	private Vector2 latest_click_pos = new Vector2(0, 0);
	private int latest_pointer_id = 0;
	
}
