package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

	public void addWidget(Widget widget, int index)
	{
		widgets.insert(index, widget);
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

	public int getWidgetsCount()
	{
		return widgets.size;
	}

	public void setBackground(Texture tex)
	{
		background_tex = tex;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
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

		// Calculate total expanding, so relative expandings
		// of widgets can be calculated.
		int total_expanding = 0;
		int non_shrunked_widgets = 0;
		for (int widget_id = 0; widget_id < widgets.size; widget_id ++) {
			Widget widget = widgets_buf[widget_id];
			if (dir == Direction.HORIZONTAL) {
				total_expanding += widget.getHorizontalExpandingForRepositioning();
			} else {
				total_expanding += widget.getVerticalExpandingForRepositioning();
			}
			if (!widget.isShrunken()) {
				++ non_shrunked_widgets;
			}
		}
		if (non_shrunked_widgets == 0) {
			return;
		}

		// This array is used to calculate sizes of widgets
		if (widgets_sizes == null || widgets_sizes.length != widgets.size) {
			widgets_sizes = new float[widgets.size];
		}

		// First set sizes of those Widgets, that are shrunked or have zero expanding. Set
		// other sizes less than zero, to indicate that their size is not yet defined.
		float extra_space_left;
		if (dir == Direction.HORIZONTAL) {
			extra_space_left = getWidth();
		} else {
			extra_space_left = getHeight();
		}
		int expanding_widgets_left = 0;
		for (int widget_id = 0; widget_id < widgets.size; widget_id ++) {
			Widget widget = widgets_buf[widget_id];
			if (widget.isShrunken()) {
				widgets_sizes[widget_id] = 0f;
			} else if (dir == Direction.HORIZONTAL && widget.getHorizontalExpandingForRepositioning() == 0) {
				widgets_sizes[widget_id] = widget.getMinWidth();
				extra_space_left -= widget.getMinWidth();
			} else if (dir == Direction.VERTICAL && widget.getVerticalExpandingForRepositioning() == 0) {
				widgets_sizes[widget_id] = widget.getMinHeight(getWidth());
				extra_space_left -= widget.getMinHeight(getWidth());
			} else {
				widgets_sizes[widget_id] = -1f;
				++ expanding_widgets_left;
			}
		}

		// Now we will spread the remaining extra space to all those Widgets, that have expanding.
		// The space is spreaded evenly, and it might be possible, that some Widgets are too large
		// to fit into this space. In these cases, their size is set to whatever it is in minimum,
		// and this size is reduced from extra space.
		while (expanding_widgets_left > 0) {
			// Calculate how much space should be reserved for each expanding point
			float space_per_expanding = extra_space_left / total_expanding;
			// Check if there are widgets, that are too large to fit to the given space
			boolean too_big_ones_found = false;
			for (int widget_id = 0; widget_id < widgets.size; widget_id ++) {
				// Skip those, that already have their size set
				if (widgets_sizes[widget_id] >= 0f) continue;

				Widget widget = widgets_buf[widget_id];
				int expanding_points;
				float widget_min_size;
				if (dir == Direction.HORIZONTAL) {
					expanding_points = widget.getHorizontalExpandingForRepositioning();
					widget_min_size = widget.getMinWidth();
				} else {
					expanding_points = widget.getVerticalExpandingForRepositioning();
					widget_min_size = widget.getMinHeight(getWidth());
				}

				float size = expanding_points * space_per_expanding;
				if (size < widget_min_size) {
					widgets_sizes[widget_id] = widget_min_size;
					too_big_ones_found = true;
					extra_space_left -= widget_min_size;
					total_expanding -= expanding_points;
					-- expanding_widgets_left;
				}
			}
			// If any of the Widgets was too big, then start over again
			if (too_big_ones_found) continue;

			// Now every remaining expanding widget should fit to the extra space
			for (int widget_id = 0; widget_id < widgets.size; widget_id ++) {
				// Skip those, that already have their size set
				if (widgets_sizes[widget_id] >= 0f) continue;

				Widget widget = widgets_buf[widget_id];
				int expanding_points;
				if (dir == Direction.HORIZONTAL) {
					expanding_points = widget.getHorizontalExpandingForRepositioning();
				} else {
					expanding_points = widget.getVerticalExpandingForRepositioning();
				}
				widgets_sizes[widget_id] = expanding_points * space_per_expanding;
			}
			break;
		}

		// Do the actual repositioning
		if (dir == Direction.HORIZONTAL) {
			float pos_x = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_width = widgets_sizes[widget_id];
				repositionChild(widget, getPositionX() + pos_x, getPositionY() + 0f, child_width, getHeight());
				pos_x += child_width;
			}
		} else {
			float pos_y = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets_buf[widget_id];
				float child_height = widgets_sizes[widget_id];
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

	private final Array<Widget> widgets = new Array<Widget>(true, 0, Widget.class);

	// This is only used when doing repositioning
	private float[] widgets_sizes;

	private Texture background_tex;

}
