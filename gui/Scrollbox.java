package fi.henu.gdxextras.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Scrollbox extends Widget
{
	public static void setDefaultStyle(ScrollboxStyle style)
	{
		default_style = style;
	}

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
			throw new RuntimeException("Only TOP and BOTTOM are allowed to vertical origin!");
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
		if (origin_vert == Alignment.BOTTOM) {
			return scroll_bottomleft.y;
		} else {
			return scroll_topright.y;
		}
	}

	// Returns 0 - 1 or negative. 0 is left, 1 is right,
	// negative means scrolling is not possible.
	public float getRelativeScrollX()
	{
		float left_hidden = scroll_bottomleft.x;
		float right_hidden = scroll_topright.x;

		if (left_hidden <= 0 && right_hidden <= 0) {
			return -1;
		}

		float total_hidden = right_hidden + left_hidden;
		return left_hidden / total_hidden;
	}

	// Returns 0 - 1 or negative. 0 is bottom, 1 is top,
	// negative means scrolling is not possible.
	public float getRelativeScrollY()
	{
		float bottom_hidden = scroll_bottomleft.y;
		float top_hidden = scroll_topright.y;

		if (bottom_hidden <= 0 && top_hidden <= 0) {
			return -1;
		}

		float total_hidden = top_hidden + bottom_hidden;
		return bottom_hidden / total_hidden;
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
		if (widget.getWidth() + widget.getMargin() * 2 > getWidth()) {
			scroll_bottomleft.x = scroll_when_pointer_was_pressed.x + pointer_down_pos.x - pos.x;
			if (scroll_bottomleft.x < 0) {
				scroll_bottomleft.x = 0;
			} else if (scroll_bottomleft.x > widget.getWidth() + widget.getMargin() * 2 - getWidth()) {
				scroll_bottomleft.x = widget.getWidth() + widget.getMargin() * 2 - getWidth();
			}
		}
		// Y scrolling
		if (widget.getHeight() + widget.getMargin() * 2 > getHeight()) {
			scroll_bottomleft.y = scroll_when_pointer_was_pressed.y + pointer_down_pos.y - pos.y;
			if (scroll_bottomleft.y < 0) {
				scroll_bottomleft.y = 0;
			} else if (scroll_bottomleft.y > widget.getHeight() + widget.getMargin() * 2 - getHeight()) {
				scroll_bottomleft.y = widget.getHeight() + widget.getMargin() * 2 - getHeight();
			}
		}
		// Update another scroll value too
		scroll_topright.x -= scroll_bottomleft.x - old_scroll_bottomleft_x;
		scroll_topright.y -= scroll_bottomleft.y - old_scroll_bottomleft_y;

		if (old_scroll_bottomleft_x != scroll_bottomleft.x || old_scroll_bottomleft_y != scroll_bottomleft.y) {
			markToNeedReposition();
		}
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		assert pointer_id == 0;
		assert widget != null;
		unregisterPointerListener(pointer_id);

		final float CLICK_TO_CHILD_DRAG_THRESHOLD = CLICK_TO_CHILD_DRAG_THRESHOLD_MM * Gdx.graphics.getPpcX() / 10f;

		// If pointer moved only small amount, then consider this as a click to the child.
		if (pos.dst2(pointer_down_pos) <= CLICK_TO_CHILD_DRAG_THRESHOLD * CLICK_TO_CHILD_DRAG_THRESHOLD) {
			generateDragEventToChildren(widget, pointer_id, pos, pos);
		}
	}

	@Override
	public void pointerCancelled(int pointer_id, Vector2 pos)
	{
		assert pointer_id == 0;
		assert widget != null;
		unregisterPointerListener(pointer_id);
	}

	@Override
	protected void doRenderingAfterChildren(SpriteBatch batch, ShapeRenderer shapes)
	{
		ScrollboxStyle style = getStyle();

		if (style != null && style.scroll_indicator_region != null) {

			float left_indicator_alpha = 0f;
			float right_indicator_alpha = 0f;
			float bottom_indicator_alpha = 0f;
			float top_indicator_alpha = 0f;

			// Decide how strong is the alpha of indicators
			float relative_scroll_x = getRelativeScrollX();
			float relative_scroll_y = getRelativeScrollY();
			if (relative_scroll_x >= 0f) {
				left_indicator_alpha = relative_scroll_x;
				right_indicator_alpha = 1f - relative_scroll_x;
			}
			if (relative_scroll_y >= 0f) {
				bottom_indicator_alpha = relative_scroll_y;
				top_indicator_alpha = 1f - relative_scroll_y;
			}

			// Draw nothing, if everything is zero
			if (left_indicator_alpha <= 0f && right_indicator_alpha <= 0f && bottom_indicator_alpha <= 0f && top_indicator_alpha <= 0f) {
				return;
			}

			// Left
			if (left_indicator_alpha > 0f) {
				batch.setColor(1, 1, 1, left_indicator_alpha);
				batch.draw(
						style.scroll_indicator_region,
						getPositionX(),
						getPositionY() + getHeight(),
						0,
						0,
						style.scroll_indicator_region.getRegionWidth(),
						style.scroll_indicator_region.getRegionHeight(),
						getHeight() / style.scroll_indicator_region.getRegionWidth(),
						style.scroll_indicator_scaling,
						-90f
				);
			}

			// Right
			if (right_indicator_alpha > 0f) {
				batch.setColor(1, 1, 1, right_indicator_alpha);
				batch.draw(
						style.scroll_indicator_region,
						getPositionX() + getWidth(),
						getPositionY(),
						0,
						0,
						style.scroll_indicator_region.getRegionWidth(),
						style.scroll_indicator_region.getRegionHeight(),
						getHeight() / style.scroll_indicator_region.getRegionWidth(),
						style.scroll_indicator_scaling,
						90f
				);
			}

			// Bottom indicator
			if (bottom_indicator_alpha > 0f) {
				batch.setColor(1, 1, 1, bottom_indicator_alpha);
				batch.draw(
					style.scroll_indicator_region,
					getPositionX(),
					getPositionY(),
					getWidth(),
					style.scroll_indicator_region.getRegionHeight() * style.scroll_indicator_scaling
				);
			}

			// Top indicator
			if (top_indicator_alpha > 0f) {
				batch.setColor(1, 1, 1, top_indicator_alpha);
				batch.draw(
					style.scroll_indicator_region,
					getPositionX(),
					getPositionY() + getHeight(),
					getWidth(),
					-style.scroll_indicator_region.getRegionHeight() * style.scroll_indicator_scaling
				);
			}

			batch.setColor(Color.WHITE);
		}
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

		// Make sure scrolling is not out of bounds
		if (scroll_topright.x < 0) {
			scroll_bottomleft.x += scroll_topright.x;
			scroll_topright.x = 0;
		}
		if (scroll_bottomleft.x < 0) {
			scroll_topright.x += scroll_bottomleft.x;
			scroll_bottomleft.x = 0;
		}
		if (scroll_topright.y < 0) {
			scroll_bottomleft.y += scroll_topright.y;
			scroll_topright.y = 0;
		}
		if (scroll_bottomleft.y < 0) {
			scroll_topright.y += scroll_bottomleft.y;
			scroll_bottomleft.y = 0;
		}

		repositionChild(widget, getPositionX() - scroll_bottomleft.x, getPositionY() - scroll_bottomleft.y, width, height);

		// Request rendering and pointing limit for children
		enableArealimitForChildren(getPositionX(), getPositionY(), getWidth(), getHeight());
	}

	protected float doGetMinWidth()
	{
		if (horizontal_scrolling_enabled || widget == null) {
			return 0;
		}
		return widget.getMinWidth();
	}

	protected float doGetMinHeight(float width)
	{
		if (vertical_scrolling_enabled || widget == null) {
			return 0;
		}
		return widget.getMinHeight(99999999);
	}

	// Drags that are less or equal than this in
	// pixels are considered clicks to child.
	private static final float CLICK_TO_CHILD_DRAG_THRESHOLD_MM = 3f;

	private static ScrollboxStyle default_style;

	private Widget widget;

	// These tell what part of the element is hidden
	private final Vector2 scroll_bottomleft = new Vector2(0, 0);
	private final Vector2 scroll_topright = new Vector2(0, 0);

	private Alignment origin_horiz;
	private Alignment origin_vert;

	private boolean horizontal_scrolling_enabled;
	private boolean vertical_scrolling_enabled;

	private final Vector2 scroll_when_pointer_was_pressed = new Vector2();
	private final Vector2 pointer_down_pos = new Vector2();

	private ScrollboxStyle style;

	private ScrollboxStyle getStyle()
	{
		if (style == null) return default_style;
		return style;
	}
}
