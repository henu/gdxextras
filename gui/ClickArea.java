package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class ClickArea extends Widget
{

	// Event types
	public static final int CLICKED = 0;
	public static final int RELEASED = 1;
	public static final int MOVED = 1;

	public ClickArea()
	{
		// Enable expanding by default
		setHorizontalExpanding(1);
		setVerticalExpanding(1);

		pressed_pointers = new HashMap<>();
	}

	// TODO: These are a little bit un-intuitive, maybe get rid of them?
	public Vector2 getLatestClickPosition()
	{
		return latest_click_pos;
	}
	public int getLatestPointerId()
	{
		return latest_pointer_id;
	}

	public Vector2 getPressedPointerPosition(int pointer_id)
	{
		return pressed_pointers.get(pointer_id);
	}

	public HashMap<Integer, Vector2> getPressedPointers()
	{
		return pressed_pointers;
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		latest_pointer_id = pointer_id;
		latest_click_pos.set(pos);
		pressed_pointers.put(pointer_id, new Vector2(pos));
		fireEvent(CLICKED);
		return true;
	}

	@Override
	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pressed_pointers.containsKey(pointer_id)) {
			pressed_pointers.get(pointer_id).set(pos);
		}
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		latest_pointer_id = pointer_id;
		pressed_pointers.remove(pointer_id);
		fireEvent(RELEASED);
		unregisterPointerListener(pointer_id);
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		// This Widget is invisible
	}

	@Override
	protected float doGetMinWidth()
	{
		return 0;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		return 0;
	}

	private final Vector2 latest_click_pos = new Vector2(0, 0);
	private int latest_pointer_id = 0;

	private final HashMap<Integer, Vector2> pressed_pointers;

}
