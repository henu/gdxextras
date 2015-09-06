package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Vectorcontainer extends Widget
{

	public enum Direction {
		HORIZONTAL, VERTICAL
	}

	public Vectorcontainer()
	{
		super();
		dir = Direction.HORIZONTAL;
		background_tex = null;
		// By default, this Widget does not receive pointer events
		setPointerEvents(false);
	}

	public Vectorcontainer(Direction dir)
	{
		super();
		this.dir = dir;
		// By default, this Widget does not receive pointer events
		setPointerEvents(false);
	}

	public void addWidget(Widget widget)
	{
		widgets.add(widget);
		addChild(widget);
		markToNeedReposition();
	}

	public void removeWidget(Widget widget)
	{
		widgets.removeValue(widget, true);
		removeChild(widget);
		markToNeedReposition();
	}

	public void clearWidgets()
	{
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			removeChild(widget);
		}
		widgets.clear();
	}

	public void setBackground(Texture tex)
	{
		background_tex = tex;
	}

	protected void doRendering(SpriteBatch batch)
	{
		if (background_tex != null) {
			float x = getPositionX();
			float y = getPositionY();
			float w = getWidth();
			float h = getHeight();
			batch.draw(background_tex, x, y, w, h);
		}
	}

	protected void doRepositioning()
	{
		if (widgets.size == 0) {
			return;
		}

		Widget[] widgets_buf = widgets.items;

		if (dir == Direction.HORIZONTAL) {
			// Calculate total expanding, so relative expandings
			// of widgets can be calculated.
			int total_expanding = 0;
			int non_shrunked_widgets = 0;
			for (int widget_id = 0; widget_id < widgets.size; widget_id ++) {
				Widget widget = widgets_buf[widget_id];
				total_expanding += widget.getHorizontalExpandingForRepositioning();
				if (!widget.isShrunken()) {
					++ non_shrunked_widgets;
				}
			}
			if (non_shrunked_widgets == 0) {
				return;
			}
			// Check if any of the widgets requires extra space
			float extra_space_required = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_min_width = widget.getMinWidth();
				float child_reserved_width = 0f;
				if (total_expanding > 0) {
					child_reserved_width = getWidth() * widget.getHorizontalExpandingForRepositioning() / total_expanding;
				} else if (!widget.isShrunken()) {
					child_reserved_width = getWidth() / non_shrunked_widgets;
				}
				extra_space_required += Math.max(0f, child_min_width - child_reserved_width);
			}
			float space_left = getWidth();
			float space_to_spread = (getWidth() - extra_space_required * 2);
			// Do the actual repositioning
			float pos_x = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_min_width = widget.getMinWidth();
				float child_width = 0f;
				// If this is last widget, then give
				// all of remaining space to it.
				if (widget_id == widgets.size - 1) {
					child_width = Math.max(child_min_width, space_left);
				}
				else if (!widget.isShrunken()) {
					// Use expanding value to calculate the size
					// of portion that this widget gets.
					float portion;
					// If every Widget has expanding zero, then
					// distribute remaining space equally.
					if (total_expanding == 0) {
						portion = 1f / non_shrunked_widgets;
					} else {
						portion = widget.getHorizontalExpandingForRepositioning() / (float)total_expanding;
					}
					child_width = Math.max(0f, space_to_spread * portion);
					child_width = Math.max(child_width, widget.getMinWidth());
					space_left -= child_width;
				}
				repositionChild(widget, getPositionX() + pos_x, getPositionY() + 0f, child_width, getHeight());
				pos_x += child_width;
			}
		} else {
			// Calculate total expanding, so relative expandings
			// of widgets can be calculated.
			int total_expanding = 0;
			int non_shrunked_widgets = 0;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				total_expanding += widget.getVerticalExpandingForRepositioning();
				if (!widget.isShrunken()) {
					++ non_shrunked_widgets;
				}
			}
			if (non_shrunked_widgets == 0) {
				return;
			}
			// Check if any of the widgets requires extra space
			float extra_space_required = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_min_height = widget.getMinHeight(getWidth());
				float child_reserved_height = 0f;
				if (total_expanding > 0) {
					child_reserved_height = getHeight() * widget.getVerticalExpandingForRepositioning() / total_expanding;
				} else if (!widget.isShrunken()) {
					child_reserved_height = getHeight() / non_shrunked_widgets;
				}
				extra_space_required += Math.max(0f, child_min_height - child_reserved_height);
			}
			float space_left = getHeight();
			float space_to_spread = (getHeight() - extra_space_required * 2);
			// Do the actual repositioning
			float pos_y = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_min_height = widget.getMinHeight(getWidth());
				float child_height = 0f;
				// If this is last widget, then give
				// all of remaining space to it.
				if (widget_id == widgets.size - 1) {
					child_height = Math.max(child_min_height, space_left);
				}
				else if (!widget.isShrunken()) {
					// Use expanding value to calculate the size
					// of portion that this widget gets.
					float portion;
					// If every Widget has expanding zero, then
					// distribute remaining space equally.
					if (total_expanding == 0) {
						portion = 1f / non_shrunked_widgets;
					} else {
						portion = widget.getVerticalExpandingForRepositioning() / (float)total_expanding;
					}
					child_height = Math.max(0f, space_to_spread * portion);
					child_height = Math.max(child_height, widget.getMinHeight(getWidth()));
					space_left -= child_height;
				}
				repositionChild(widget, getPositionX() + 0f, getPositionY() + getHeight() - child_height - pos_y, getWidth(), child_height);
				pos_y += child_height;
			}
		}
	}

	protected float doGetMinWidth()
	{
		float min_width = 0f;
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		if (dir == Direction.HORIZONTAL) {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				min_width += widget.getMinWidth();
			}
		} else {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				min_width = Math.max(min_width, widget.getMinWidth());
			}
		}
		return min_width;
	}

	protected float doGetMinHeight(float width)
	{
		float min_height = 0f;
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		if (dir == Direction.HORIZONTAL) {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				min_height = Math.max(min_height, widget.getMinHeight(width));
			}
		} else {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				min_height += widget.getMinHeight(width);
			}
		}
		return min_height;
	}

	private Direction dir;

	private Array<Widget> widgets = new Array<Widget>(true, 0, Widget.class);

	private Texture background_tex;

}
