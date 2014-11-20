package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Scrollbox extends Widget
{
	public Scrollbox()
	{
		super();
		widget = null;
		origin_horiz = Alignment.LEFT;
		origin_vert = Alignment.TOP;
		horizontal_scrolling_enabled = true;
		vertical_scrolling_enabled = true;
		// This widget has ability to receive
		// pointer events before its children.
		setBeTopmostBeforeChildren(true);
	}

	public void setWidget(Widget widget)
	{
		if (this.widget != null) {
			removeChild(this.widget);
		}
		this.widget = widget;
		if (widget != null) {
			addChild(widget);
		}
		markToNeedReposition();
	}
	
	public void setHorizontalOrigin(Alignment origin)
	{
		if (origin != Alignment.LEFT && origin != Alignment.RIGHT) {
			throw new RuntimeException("Only LEFT and RIGHT are allowed to horizontal origin!");
		}
		origin_horiz = origin;
	}
	
	public void setVerticalOrigin(Alignment origin)
	{
		if (origin != Alignment.TOP && origin != Alignment.BOTTOM) {
			throw new RuntimeException("Only LEFT and RIGHT are allowed to horizontal origin!");
		}
		origin_vert = origin;
	}
	
	public Alignment getHorizontalOrigin() { return origin_horiz; }
	public Alignment getVerticalOrigin() { return origin_vert; }
	
	public void setScroll(Vector2 scroll)
	{
		setScroll(scroll.x, scroll.y);
	}

	public void setScroll(float x, float y)
	{
		setScrollX(x);
		setScrollY(y);
	}

	public void setScrollX(float x)
	{
		if (origin_horiz == Alignment.LEFT) {
			scroll_topright.x -= (x - scroll_bottomleft.x);
			scroll_bottomleft.x = x;
		} else {
			scroll_bottomleft.x += (x - scroll_bottomleft.x);
			scroll_topright.x = x;
		}
		markToNeedReposition();
	}

	public void setScrollY(float y)
	{
		if (origin_vert == Alignment.BOTTOM) {
			scroll_topright.y -= (y - scroll_bottomleft.y);
			scroll_bottomleft.y = y;
		} else {
			scroll_bottomleft.y += (y - scroll_bottomleft.y);
			scroll_topright.y = y;
		}
		markToNeedReposition();
	}

	public void getScroll(Vector2 result)
	{
		result.x = getScrollX();
		result.y = getScrollY();
	}

	public float getScrollX()
	{
		if (origin_horiz == Alignment.LEFT) {
			return scroll_bottomleft.x;
		} else {
			return scroll_topright.x;
		}
	}

	public float getScrollY()
	{
		if (origin_horiz == Alignment.BOTTOM) {
			return scroll_bottomleft.y;
		} else {
			return scroll_topright.y;
		}
	}

	// Scrolls show that specific rectangle becomes shown as much
	// as possible. Coordinates are given relative to Scrollbox.
	public void showRectangle(float x, float y, float width, float height)
	{
		float old_scroll_bottomleft_x = scroll_bottomleft.x;
		float old_scroll_bottomleft_y = scroll_bottomleft.y;
		// Scroll horizontally
		if (x < scroll_bottomleft.x) {
			// If it fits nicely, then show at left edge
			if (getWidth() > width) {
				scroll_bottomleft.x = x;
				markToNeedReposition();
			}
			// If it does not fit, then center
			else {
				scroll_bottomleft.x = x + (width - getWidth()) / 2;
				markToNeedReposition();
			}
		} else if (x + width > getWidth() + scroll_bottomleft.x) {
			// If it fits nicely, then show at right edge
			if (getWidth() > width) {
				scroll_bottomleft.x = x + width - getWidth();
				markToNeedReposition();
			}
			// If it does not fit, then center
			else {
				scroll_bottomleft.x = x + (width - getWidth()) / 2;
				markToNeedReposition();
			}
		}
		// Scroll vertically
		if (y < scroll_bottomleft.y) {
			// If it fits nicely, then show at bottom edge
			if (getHeight() > height) {
				scroll_bottomleft.y = y;
				markToNeedReposition();
			}
			// If it does not fit, then center
			else {
				scroll_bottomleft.y = y + (height - getHeight()) / 2;
				markToNeedReposition();
			}
		} else if (y + height > getHeight() + scroll_bottomleft.y) {
			// If it fits nicely, then show at top edge
			if (getHeight() > height) {
				scroll_bottomleft.y = y + height - getHeight();
				markToNeedReposition();
			}
			// If it does not fit, then center
			else {
				scroll_bottomleft.y = y + (height - getHeight()) / 2;
				markToNeedReposition();
			}
		}
		// Update another scroll value too
		scroll_topright.x -= scroll_bottomleft.x - old_scroll_bottomleft_x;
		scroll_topright.y -= scroll_bottomleft.y - old_scroll_bottomleft_y;
	}

	// If horizontal scrolling is enabled, then scrollbox can be really thin.
	// If disabled, then scrollbox needs to be as wide as its child.
	public void enableHorizontalScrolling(boolean horizontal_scrolling_enabled)
	{
		this.horizontal_scrolling_enabled = horizontal_scrolling_enabled;
		markToNeedReposition();
	}

	// If vertical scrolling is enabled, then scrollbox can be really shallow.
	// If disabled, then scrollbox needs to be as tall as its child.
	public void enableVerticalScrolling(boolean vertical_scrolling_enabled)
	{
		this.vertical_scrolling_enabled = vertical_scrolling_enabled;
		markToNeedReposition();
	}

	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (pointer_id != 0 || widget == null) {
			return false;
		}
		pointer_down_pos.set(pos);
		scroll_when_pointer_was_pressed.set(scroll_bottomleft);
		return true;
	}

	public void pointerMove(int pointer_id, Vector2 pos)
	{
		assert pointer_id == 0;
		assert widget != null;
		float old_scroll_bottomleft_x = scroll_bottomleft.x;
		float old_scroll_bottomleft_y = scroll_bottomleft.y;
		// X scrolling
		if (widget.getWidth() > getWidth()) {
			scroll_bottomleft.x = scroll_when_pointer_was_pressed.x + pointer_down_pos.x - pos.x;
			if (scroll_bottomleft.x < 0) {
				scroll_bottomleft.x = 0;
			} else if (scroll_bottomleft.x > widget.getWidth() - getWidth()) {
				scroll_bottomleft.x = widget.getWidth() - getWidth();
			}
		}
		// Y scrolling
		if (widget.getHeight() > getHeight()) {
			scroll_bottomleft.y = scroll_when_pointer_was_pressed.y + pointer_down_pos.y - pos.y;
			if (scroll_bottomleft.y < 0) {
				scroll_bottomleft.y = 0;
			} else if (scroll_bottomleft.y > widget.getHeight() - getHeight()) {
				scroll_bottomleft.y = widget.getHeight() - getHeight();
			}
		}
		// Update another scroll value too
		scroll_topright.x -= scroll_bottomleft.x - old_scroll_bottomleft_x;
		scroll_topright.y -= scroll_bottomleft.y - old_scroll_bottomleft_y;

		if (old_scroll_bottomleft_x != scroll_bottomleft.x || old_scroll_bottomleft_y != scroll_bottomleft.y) {
			markToNeedReposition();
		}
	}

	public void pointerUp(int pointer_id, Vector2 pos)
	{
		assert pointer_id == 0;
		assert widget != null;
		unregisterPointerListener(pointer_id);
		
		// If pointer moved only small amount, then consider this as a click to the child.
		if (pos.dst2(pointer_down_pos) <= CLICK_TO_CHILD_DRAG_THRESHOLD * CLICK_TO_CHILD_DRAG_THRESHOLD) {
			generateDragEventToChildren(widget, pointer_id, pos, pos);
		}
	}

	protected void doRendering(SpriteBatch batch)
	{
		// This Widget is invisible
	}

	protected void doRepositioning()
	{
		if (widget == null) {
			return;
		}

		float width = Math.max(widget.getMinWidth(), getWidth());
		float height = Math.max(widget.getMinHeight(width), getHeight());
		
		// If scrolling values are not sync, then sync them
		if (Math.abs(scroll_bottomleft.x + getWidth() + scroll_topright.x - width) > 0.5f) {
			if (origin_horiz == Alignment.LEFT) {
				scroll_topright.x = width - scroll_bottomleft.x - getWidth();
			} else {
				scroll_bottomleft.x = width - scroll_topright.x - getWidth();
			}
		}
		if (Math.abs(scroll_bottomleft.y + getHeight() + scroll_topright.y - height) > 0.5f) {
			if (origin_vert == Alignment.BOTTOM) {
				scroll_topright.y = height - scroll_bottomleft.y - getHeight();
			} else {
				scroll_bottomleft.y = height - scroll_topright.y - getHeight();
			}
		}
		
		repositionChild(widget, getPositionX() - scroll_bottomleft.x, getPositionY() - scroll_bottomleft.y, width, height);
		
		// Request rendering and pointing limit for children
		enableArealimitForChildren(getPositionX(), getPositionY(), getWidth(), getHeight());
	}

	protected float doGetMinWidth()
	{
		if (vertical_scrolling_enabled || widget == null) {
			return 0;
		}
		return widget.getMinWidth();
	}

	protected float doGetMinHeight(float width)
	{
		if (horizontal_scrolling_enabled || widget == null) {
			return 0;
		}
		return widget.getMinHeight(99999999);
	}

	// Drags that are less or equal than this in
	// pixels are considered clicks to child.
	private static final int CLICK_TO_CHILD_DRAG_THRESHOLD = 4;

	private Widget widget;

	private Vector2 scroll_bottomleft = new Vector2(0, 0);
	private Vector2 scroll_topright = new Vector2(0, 0);
	
	private Alignment origin_horiz;
	private Alignment origin_vert;

	private boolean horizontal_scrolling_enabled;
	private boolean vertical_scrolling_enabled;

	private Vector2 scroll_when_pointer_was_pressed = new Vector2();
	private Vector2 pointer_down_pos = new Vector2();
}
