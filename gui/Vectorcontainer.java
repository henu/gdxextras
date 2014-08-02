package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Vectorcontainer extends Widget
{

	public enum Direction {
		HORIZONTAL, VERTICAL
	};

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
		int widgets_size = widgets.size;

		if (dir == Direction.HORIZONTAL) {
			float min_width = getMinWidth();
			float space_left = getWidth() - min_width;
			if (space_left < 0f) space_left = 0f;
			// Calculate total expanding, so relative expandings
			// of widgets can be calculated.
			int total_expanding = 0;
			for (int widget_id = 0; widget_id < widgets_size; widget_id ++) {
				Widget widget = widgets_buf[widget_id];
				total_expanding += widget.getHorizontalExpandingForRepositioning();
			}
			float pos_x = 0f;
			float space_distributed = 0f;
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_width = widget.getMinWidth();
				// If this is last widget, then give
				// all of remaining space to it.
				if (widget_id == widgets_size - 1) {
					child_width += space_left;
				}
				// Use expanding value to calculate the size
				// of portion that this widget gets.
				else {
					float portion;
					// If every Widget has expanding zero, then
					// distribute remaining space equally.
					if (total_expanding == 0) {
						portion = 1f / widgets_size;
					} else {
						portion = widget.getHorizontalExpandingForRepositioning() / (float)total_expanding;
					}
					float space = (space_left + space_distributed) * portion;
					if (space > space_left) space = space_left;
					child_width += space;
					space_left -= space;
					space_distributed += space;
				}
				repositionChild(widget, getPositionX() + pos_x, getPositionY() + 0f, child_width, getHeight());
				pos_x += child_width;
			}
		} else {
			float min_height = getMinHeight(getWidth());
			float space_left = getHeight() - min_height;
			if (space_left < 0f) space_left = 0f;
			// Calculate total expanding, so relative expandings
			// of widgets can be calculated.
			int total_expanding = 0;
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				total_expanding += widget.getVerticalExpandingForRepositioning();
			}
			float pos_y = 0f;
			float space_distributed = 0f;
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_height = widget.getMinHeight(getWidth());
				// If this is last widget, then give
				// all of remaining space to it.
				if (widget_id == widgets_size - 1) {
					child_height += space_left;
				}
				// Use expanding value to calculate the size
				// of portion that this widget gets.
				else {
					float portion;
					// If every Widget has expanding zero, then
					// distribute remaining space equally.
					if (total_expanding == 0) {
						portion = 1f / widgets_size;
					} else {
						portion = widget.getVerticalExpandingForRepositioning() / (float)total_expanding;
					}
					float space = (space_left + space_distributed) * portion;
					if (space > space_left) space = space_left;
					child_height += space;
					space_left -= space;
					space_distributed += space;
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
