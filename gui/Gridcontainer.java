package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Gridcontainer extends Widget
{

	public Gridcontainer(int columns)
	{
		super();
		if (columns <= 0) {
			throw new RuntimeException("There must be at least one column!");
		}
		cols = columns;
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

	public void setWidget(int widget_index, Widget widget)
	{
		// Ensure there is space
		while (widgets.size <= widget_index) {
			widgets.add(null);
		}
		// Remove possible old one
		if (widgets.items[widget_index] != null) {
			removeChild(widgets.items[widget_index]);
		}
		// Replace with new Widget
		widgets.items[widget_index] = widget;
		if (widget != null) {
			addChild(widget);
		}
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

	public int getWidgetsSize()
	{
		return widgets.size;
	}

	public Widget getWidget(int widget_index)
	{
		return widgets.items[widget_index];
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		// Draw nothing
	}

	protected void doRepositioning()
	{
		if (widgets.size == 0) {
			return;
		}

		float[] widths = getOptimalColumnWidths(getWidth());
		float[] heights = getOptimalRowHeights(widths, getHeight());

		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		int col_id = 0;
		int row_id = 0;
		float pos_x = 0;
		float pos_y = getHeight() - heights[0];
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];

			repositionChild(widget, getPositionX() + pos_x, getPositionY() + pos_y, widths[col_id], heights[row_id]);

			pos_x += widths[col_id];
			col_id ++;
			if (col_id == cols) {
				col_id = 0;
				pos_x = 0;
				pos_y -= heights[row_id];
				row_id ++;
			}
		}
	}

	protected float doGetMinWidth()
	{
		float[] widths = getMinimumColumnWidths();
		return calculateSum(widths);
	}

	protected float doGetMinHeight(float width)
	{
		float[] widths = getOptimalColumnWidths(width);

		float min_height = 0;
		float row_height = 0;
		int col_id = 0;

		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			row_height = Math.max(row_height, widget.getMinHeight(widths[col_id]));
			col_id ++;
			if (col_id == cols) {
				min_height += row_height;
				row_height = 0;
				col_id = 0;
			}
		}
		min_height += row_height;
		return min_height;
	}

	private int cols;

	private final Array<Widget> widgets = new Array<Widget>(true, 0, Widget.class);

	private int getNumOfRows()
	{
		return (widgets.size + cols - 1) / cols;
	}

	private float[] getMinimumColumnWidths()
	{
		// Initialize array of column widths
		float[] widths = new float[cols];
		for (int col_id = 0; col_id < cols; col_id ++) {
			widths[col_id] = 0;
		}

		// Calculate minimum width of each columns.
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		int col_id = 0;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			widths[col_id] = Math.max(widths[col_id], widget.getMinWidth());
			col_id = (col_id + 1) % cols;
		}

		return widths;
	}

	private float[] getMinimumRowHeights(float[] widths)
	{
		int rows = getNumOfRows();
		// Initialize array of row heights
		float[] heights = new float[rows];
		for (int row_id = 0; row_id < rows; row_id ++) {
			heights[row_id] = 0;
		}

		// Calculate minimum width of each columns.
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		int col_id = 0;
		int row_id = 0;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			heights[row_id] = Math.max(heights[row_id], widget.getMinHeight(widths[col_id]));
			col_id ++;
			if (col_id == cols) {
				col_id = 0;
				row_id ++;
			}
		}

		return heights;
	}

	private float calculateSum(float[] vals)
	{
		float result = 0;
		for (int val_id = 0; val_id < vals.length; val_id ++) {
			result += vals[val_id];
		}
		return result;
	}

	private int calculateSumInt(int[] vals)
	{
		int result = 0;
		for (int val_id = 0; val_id < vals.length; val_id ++) {
			result += vals[val_id];
		}
		return result;
	}

	private float[] getOptimalColumnWidths(float total_width)
	{
		float[] widths = getMinimumColumnWidths();
		float total_width_now = calculateSum(widths);

		// Get expandings
		int[] expandings = getHorizontalExpandings();
		int total_expanding = calculateSumInt(expandings);

		// Add new width to columns
		if (total_expanding == 0) {
			float extra_width_per_column = (total_width - total_width_now) / cols;
			for (int col_id = 0; col_id < cols; col_id ++) {
				widths[col_id] += extra_width_per_column;
			}
		} else {
			float total_extra_width = total_width - total_width_now;
			for (int col_id = 0; col_id < cols; col_id ++) {
				widths[col_id] += total_extra_width * expandings[col_id] / total_expanding;
			}
		}

		return widths;
	}

	private float[] getOptimalRowHeights(float[] widths, float total_height)
	{
		float[] heights = getMinimumRowHeights(widths);
		float total_height_now = calculateSum(heights);

		// Add new height to rows
		// TODO: Take care of vertical expandings!
		int rows = getNumOfRows();
		float extra_height_per_row = (total_height - total_height_now) / rows;
		for (int row_id = 0; row_id < rows; row_id ++) {
			heights[row_id] += extra_height_per_row;
		}

		return heights;
	}

	private int[] getHorizontalExpandings()
	{
		// Initialize array of expandings
		int[] expandings = new int[cols];
		for (int col_id = 0; col_id < cols; col_id ++) {
			expandings[col_id] = 0;
		}

		// Calculate maximum expandings of each columns.
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		int col_id = 0;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			expandings[col_id] = Math.max(expandings[col_id], widget.getHorizontalExpandingForRepositioning());
			col_id = (col_id + 1) % cols;
		}

		return expandings;
	}

}
