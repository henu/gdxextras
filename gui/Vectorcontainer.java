package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Vectorcontainer extends Widget
{
	public enum Direction
	{
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
		addChild(widget, index);
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
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets.get(widget_id);
			removeChild(widget);
		}
		widgets.clear();
	}

	public int getWidgetsSize()
	{
		return widgets.size;
	}

	public Widget getWidget(int idx)
	{
		return widgets.get(idx);
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

		// This array is used to calculate sizes of widgets. For horizontal
		// container it will contain widths, and for vertical it contains heights.
		Array<Float> widgets_sizes = calculateWidgetSizes(getWidth(), getHeight());
		if (widgets_sizes == null) {
			return;
		}

		// Do the actual repositioning
		if (dir == Direction.HORIZONTAL) {
			float pos_x = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				float child_width = widgets_sizes.get(widget_id);
				repositionChild(widget, getPositionX() + pos_x, getPositionY() + 0f, child_width, getHeight());
				pos_x += child_width;
			}
		} else {
			float pos_y = 0f;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				float child_height = widgets_sizes.get(widget_id);
				repositionChild(widget, getPositionX() + 0f, getPositionY() + getHeight() - child_height - pos_y, getWidth(), child_height);
				pos_y += child_height;
			}
		}
	}

	protected float doGetMinWidth()
	{
		float min_width = 0f;
		int widgets_size = widgets.size;
		if (dir == Direction.HORIZONTAL) {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				min_width += widget.getMinWidth();
			}
		} else {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				min_width = Math.max(min_width, widget.getMinWidth());
			}
		}
		return min_width;
	}

	protected float doGetMinHeight(float width)
	{
		float min_height = 0f;
		int widgets_size = widgets.size;
		if (dir == Direction.HORIZONTAL) {
			Array<Float> widgets_widths = calculateWidgetSizes(width, 0);
			if (widgets_widths == null) {
				return 0;
			}
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				min_height = Math.max(min_height, widget.getMinHeight(widgets_widths.get(widget_id)));
			}
		} else {
			for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				min_height += widget.getMinHeight(width);
			}
		}
		return min_height;
	}

	private final Direction dir;

	private final Array<Widget> widgets = new Array<>();

	private Texture background_tex;

	private Array<Float> calculateWidgetSizes(float width, float height)
	{
		// Calculate total expanding, so relative expandings
		// of widgets can be calculated.
		int total_expanding = 0;
		int non_shrunked_widgets = 0;
		for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
			Widget widget = widgets.get(widget_id);
			if (dir == Direction.HORIZONTAL) {
				total_expanding += widget.getHorizontalExpandingForRepositioning();
			} else {
				total_expanding += widget.getVerticalExpandingForRepositioning();
			}
			if (!widget.isShrunken()) {
				++non_shrunked_widgets;
			}
		}
		if (non_shrunked_widgets == 0) {
			return null;
		}

		// First set sizes of those Widgets, that are shrunken or have zero expanding. Set
		// other sizes less than zero, to indicate that their size is not yet defined.
		float extra_space_left;
		if (dir == Direction.HORIZONTAL) {
			extra_space_left = width;
		} else {
			extra_space_left = height;
		}
		int expanding_widgets_left = 0;
		int non_expandings_that_can_be_resized = 0;
		Array<Float> widgets_sizes = new Array<>(true, widgets.size);
		for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
			Widget widget = widgets.get(widget_id);
			if (widget.isShrunken()) {
				widgets_sizes.add(0f);
			} else if (dir == Direction.HORIZONTAL && widget.getHorizontalExpandingForRepositioning() == 0) {
				widgets_sizes.add(widget.getMinWidth());
				extra_space_left -= widget.getMinWidth();
				++non_expandings_that_can_be_resized;
			} else if (dir == Direction.VERTICAL && widget.getVerticalExpandingForRepositioning() == 0) {
				widgets_sizes.add(widget.getMinHeight(getWidth()));
				extra_space_left -= widget.getMinHeight(getWidth());
				++non_expandings_that_can_be_resized;
			} else {
				widgets_sizes.add(-1f);
				++expanding_widgets_left;
			}
		}

		// Now we will spread the remaining extra space to all those Widgets, that have expanding.
		// The space is spread evenly, and it might be possible, that some Widgets are too large
		// to fit into this space. In these cases, their size is set to whatever it is in minimum,
		// and this size is reduced from extra space.
		if (expanding_widgets_left > 0) {
			while (expanding_widgets_left > 0) {
				// Calculate how much space should be reserved for each expanding point
				float space_per_expanding = extra_space_left / total_expanding;
				// Check if there are widgets, that are too large to fit to the given space
				boolean too_big_ones_found = false;
				for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
					// Skip those, that already have their size set
					if (widgets_sizes.get(widget_id) >= 0f) continue;

					Widget widget = widgets.get(widget_id);
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
						widgets_sizes.set(widget_id, widget_min_size);
						too_big_ones_found = true;
						extra_space_left -= widget_min_size;
						total_expanding -= expanding_points;
						--expanding_widgets_left;
					}
				}
				// If any of the Widgets was too big, then start over again
				if (too_big_ones_found) continue;

				// Now every remaining expanding widget should fit to the extra space
				for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
					// Skip those, that already have their size set
					if (widgets_sizes.get(widget_id) >= 0f) continue;

					Widget widget = widgets.get(widget_id);
					int expanding_points;
					if (dir == Direction.HORIZONTAL) {
						expanding_points = widget.getHorizontalExpandingForRepositioning();
					} else {
						expanding_points = widget.getVerticalExpandingForRepositioning();
					}
					widgets_sizes.set(widget_id, expanding_points * space_per_expanding);
				}
				break;
			}
		}
		// If there are only non-expanding widgets and
		// still space left, then make widgets bigger
		else if (extra_space_left > 0 && non_expandings_that_can_be_resized > 0) {
			float space_per_widget = extra_space_left / non_expandings_that_can_be_resized;
			for (int widget_id = 0; widget_id < widgets.size; widget_id++) {
				Widget widget = widgets.get(widget_id);
				if (!widget.isShrunken()) {
					if ((dir == Direction.HORIZONTAL && widget.getHorizontalExpandingForRepositioning() == 0) ||
						(dir == Direction.VERTICAL && widget.getVerticalExpandingForRepositioning() == 0)) {
						widgets_sizes.set(widget_id, widgets_sizes.get(widget_id) + space_per_widget);
					}
				}
			}
		}

		return widgets_sizes;
	}
}
