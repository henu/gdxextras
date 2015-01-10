package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class ScrollZoomArea extends Widget
{

	// Event types
	public static final int CLICKED = 0;

	public ScrollZoomArea()
	{
		scrolled = new Vector2(0, 0);
		touch_state = TouchState.NOTHING;
		touch0_down = false;
		touch1_down = false;
		touch0_last = new Vector2(0, 0);
		touch1_last = new Vector2(0, 0);
		touch_dst_begin = 0;
		touch_zoom = 1;
		touch_zoom_baseline = 1;
		// Enable expanding by default
		setHorizontalExpanding(1);
		setVerticalExpanding(1);
		
		// By default, clicks are disabled.
		click_threshold = -1;
		latest_click_pos = null;
	}
	
	// "click_threshold" determines maximum distance that can be
	// scrolled, while scrolling is still considered as click. 
	public void enableClickEvents(float click_threshold)
	{
		this.click_threshold = click_threshold;
		latest_click_pos = new Vector2();
	}
	
	public void disableClickEvents()
	{
		click_threshold = -1;
		latest_click_pos = null;
	}

	// Returns amount of scrolled from last reset of scrolled.
	// Returns null if there has not been any scrolling.
	public final Vector2 getScrolled()
	{
		if (touch_state == TouchState.CLICKING) {
			return null;
		}
		if (scrolled.x == 0 && scrolled.y == 0) {
			return null;
		}
		return scrolled;
	}

	public void resetScrolled()
	{
		scrolled.x = 0;
		scrolled.y = 0;
	}

	// Returns the amount of zoomed from last query.
	// Returns 1 if no zooming has been done.
	public float getZoomed()
	{
		float result = touch_zoom / touch_zoom_baseline;
		touch_zoom_baseline = touch_zoom;
		return result;
	}
	
	public boolean isScrolling()
	{
		return touch_state == TouchState.SCROLLING;
	}

	public boolean isZooming()
	{
		return touch_state == TouchState.ZOOMING;
	}

	public Vector2 getLatestClickPosition()
	{
		return latest_click_pos;
	}

	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		// In case of other pointers, do nothing
		if (pointer_id > 1) {
			return false;
		}
		// Store state of pointer
		if (pointer_id == 0) {
			touch0_down = true;
			touch0_last.x = pos.x;
			touch0_last.y = pos.y;
		} else if (pointer_id == 1) {
			touch1_down = true;
			touch1_last.x = pos.x;
			touch1_last.y = pos.y;
		}
		// Check if scrolling was started
		if (touch0_down != touch1_down) {
			// However, if click events are enabled,
			// then this is considered as click first.
			if (click_threshold >= 0) {
				touch_state = TouchState.CLICKING;
				latest_click_pos.set(pos);
			} else {
				touch_state = TouchState.SCROLLING;
			}
		}
		// Otherwise zooming was started
		else {
			touch_state = TouchState.ZOOMING;
			float x_diff = touch0_last.x - touch1_last.x;
			float y_diff = touch0_last.y - touch1_last.y;
			touch_dst_begin = (float)Math.sqrt(x_diff * x_diff + y_diff * y_diff);
		}
		return true;
	}

	public void pointerMove(int pointer_id, Vector2 pos)
	{
		// In case of other pointers, do nothing
		if (pointer_id > 1) {
			return;
		}
		if (touch_state == TouchState.SCROLLING || touch_state == TouchState.CLICKING) {
			if (touch0_down) {
				scrolled.x -= pos.x - touch0_last.x;
				scrolled.y -= pos.y - touch0_last.y;
				touch0_last.x = pos.x;
				touch0_last.y = pos.y;
			} else {
				scrolled.x -= pos.x - touch1_last.x;
				scrolled.y -= pos.y - touch1_last.y;
				touch1_last.x = pos.x;
				touch1_last.y = pos.y;
			}
			// If we are clicking, check if threshold has been
			// reached. If yes, then convert to scrolling.
			if (touch_state == TouchState.CLICKING && scrolled.len() > click_threshold) {
				touch_state = TouchState.SCROLLING;
			}
		} else if (touch_state == TouchState.ZOOMING) {
			if (pointer_id == 0) {
				touch0_last.x = pos.x;
				touch0_last.y = pos.y;
			} else if (pointer_id == 1) {
				touch1_last.x = pos.x;
				touch1_last.y = pos.y;
			}
			float x_diff = touch0_last.x - touch1_last.x;
			float y_diff = touch0_last.y - touch1_last.y;
			float touch_dst_now = (float)Math.sqrt(x_diff * x_diff + y_diff * y_diff);
			touch_zoom = touch_dst_now / touch_dst_begin;
		}
	}

	public void pointerUp(int pointer_id, Vector2 pos)
	{
// TODO: Does this still works? Nowadays each pointer needs to call unregisterPointerListener separately!
		// In case of other pointers, do nothing
		if (pointer_id > 1) {
			return;
		}
		if (touch_state == TouchState.CLICKING) {
			touch_state = TouchState.NOTHING;
			touch0_down = false;
			touch1_down = false;
			fireEvent(CLICKED);
		} else if (touch_state == TouchState.SCROLLING) {
			touch_state = TouchState.NOTHING;
			touch0_down = false;
			touch1_down = false;
		} else if (touch_state == TouchState.ZOOMING) {
			if (pointer_id == 0) {
				touch0_down = false;
			} else {
				touch1_down = false;
			}
			touch_zoom = 1;
			touch_zoom_baseline = 1;
			touch_state = TouchState.SCROLLING;
		}
		unregisterPointerListener(pointer_id);
	}

	public void scrolled(int amount)
	{
		touch_zoom *= Math.pow(1.25, -amount);
	}

	public void doRendering(SpriteBatch batch)
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

	private enum TouchState {
		NOTHING, SCROLLING, ZOOMING, CLICKING
	}

	private TouchState touch_state;
	private boolean touch0_down;
	private boolean touch1_down;
	private Vector2 touch0_last;
	private Vector2 touch1_last;
	private float touch_dst_begin;
	private float touch_zoom;

	private Vector2 scrolled;
	private float touch_zoom_baseline;

	private float click_threshold;
	private Vector2 latest_click_pos;

}
