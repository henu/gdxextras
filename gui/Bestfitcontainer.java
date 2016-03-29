package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Bestfitcontainer extends Widget
{

	public Bestfitcontainer()
	{
		super();
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

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
	}

	protected void doRepositioning()
	{
		if (widgets.size == 0) {
			return;
		}

		Array<Widget> widgets_left = new Array<Widget>(widgets);

		// Remove those Widgets, that are shrunken
		for (int i = 0; i < widgets_left.size;) {
			Widget widget = widgets_left.items[i];
			float widget_w = widget.getMinWidth();
			float widget_h = widget.getMinHeight(widget_w);
			if (widget_w == 0 || widget_h == 0) {
				widgets_left.removeIndex(i);
			} else {
				i ++;
			}
		}

		if (widgets_left.size == 0) {
			return;
		}

		// Initialize new positioning tree
		ReposNode tree = new ReposNode();

		while (widgets_left.size > 0) {
			// Position first the Widget that has biggest edge/edge ratio
			float biggest_ratio = 0;
			int biggest_ratio_i = -1;
			for (int i = 0; i < widgets_left.size; i ++) {
				Widget widget = widgets_left.items[i];
				float widget_w = widget.getMinWidth();
				float widget_h = widget.getMinHeight(widget_w);
				float ratio_horiz = widget_w / widget_h;
				float ratio_vert = widget_h / widget_w;
				if (ratio_horiz > ratio_vert) {
					if (ratio_horiz > biggest_ratio) {
						biggest_ratio = ratio_horiz;
						biggest_ratio_i = i;
					}
				} else {
					if (ratio_vert > biggest_ratio) {
						biggest_ratio = ratio_vert;
						biggest_ratio_i = i;
					}
				}
			}
			assert biggest_ratio_i >= 0;

			Widget widget = widgets_left.items[biggest_ratio_i];
			widgets_left.removeIndex(biggest_ratio_i);

			// Find all Nodes from the tree where this Widget fits, and select the one that is the smallest one.
			float widget_w = widget.getMinWidth();
			float widget_h = widget.getMinHeight(widget_w);
			fit_node = null;
			findBestFitFromTree(widget, widget_w, widget_h, tree, getPositionX(), getPositionY(), getEndX(), getEndY(), widgets_left.size == 0);

			// If space could not be found, then reposition
			// this Widget to the bottom left corner
			if (fit_node == null) {
				repositionChild(widget, getPositionX(), getPositionY(), widget_w, widget_h);
				continue;
			}

			// Split best node and add this Widget to one of its children
			if (fit_split_horizontally) {
				// Split fit node
				assert fit_node.w == null;
				fit_node.splitline = fit_rectangle.getY() + fit_rectangle.getHeight();
				fit_node.c1 = new ReposNode();
				fit_node.c2 = new ReposNode();
				fit_node.horizontal_splitline = true;
				// Split one of its children, so the Widget can be added there
				assert fit_node.c1.w == null;
				fit_node.c1.splitline = fit_rectangle.getX() + fit_rectangle.getWidth();
				fit_node.c1.horizontal_splitline = false;
				fit_node.c1.c1 = new ReposNode();
				fit_node.c1.c2 = new ReposNode();
				// Add widget to Grand child of fit node
				fit_node.c1.c1.w = widget;
				fit_node.c1.c1.w_rect = new Rectangle(fit_rectangle);
			} else {
				// Split fit node
				assert fit_node.w == null;
				fit_node.splitline = fit_rectangle.getX() + fit_rectangle.getWidth();
				fit_node.c1 = new ReposNode();
				fit_node.c2 = new ReposNode();
				fit_node.horizontal_splitline = false;
				// Split one of its children, so the Widget can be added there
				assert fit_node.c1.w == null;
				fit_node.c1.splitline = fit_rectangle.getY() + fit_rectangle.getHeight();
				fit_node.c1.horizontal_splitline = true;
				fit_node.c1.c1 = new ReposNode();
				fit_node.c1.c2 = new ReposNode();
				// Add widget to Grand child of fit node
				fit_node.c1.c1.w = widget;
				fit_node.c1.c1.w_rect = new Rectangle(fit_rectangle);
			}

		}

		// Optimize tree. This means removing those Nodes,
		// that are not splitted and do not contain Widget
		removeEmptyNodes(tree);

		// Fix rectangles so they take maximum space
		expandRectangles(tree, getPositionX(), getPositionY(), getEndX(), getEndY());

		// Go tree through and reposition Widgets
		repositionWidgetsFromTree(tree);

		// Clear reference so garbage collector can clean the tree.
		fit_node = null;

	}

	protected float doGetMinWidth()
	{
		float min_width = 0f;
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			min_width = Math.max(min_width, widget.getMinWidth());
		}
		return min_width;
	}

	protected float doGetMinHeight(float width)
	{
		float min_height = 0f;
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			min_height = Math.max(min_height, widget.getMinHeight(width));
		}
		return min_height;
	}

	private class ReposNode
	{
		public ReposNode()
		{
			horizontal_splitline = false;
			splitline = 0;
			w = null;
			w_rect = null;
			c1 = null;
			c2 = null;
		}
		public boolean horizontal_splitline;
		public float splitline;
		// Child #1 is before split line and Child #2 after it.
		// If Widget is set, then Node cannot be splitted.
		// If children are null, then split is not done yet.
		public Widget w;
		public Rectangle w_rect;
		public ReposNode c1, c2;
	}

	private final Array<Widget> widgets = new Array<Widget>(true, 0, Widget.class);

	// Results for "findBestFitFromTree()"
	private float fit_best_area = 0;
	private ReposNode fit_node = null;
	private final Rectangle fit_rectangle = new Rectangle();
	private boolean fit_split_horizontally;

	private void findBestFitFromTree(Widget widget, float widget_w, float widget_h, ReposNode node, float x1, float y1, float x2, float y2, boolean last_widget)
	{
		assert x1 <= x2;
		assert y1 <= y2;
		// If there is already Widget, then do nothing with this Node
		if (node.w != null) {
		}
		// If split is not yet done
		else if (node.c1 == null) {
			assert node.c2 == null;
			float w = x2 - x1;
			float h = y2 - y1;
			if (widget_w <= w && widget_h <= h) {
				float area_now = w * h;
				float widget_area = widget_w * widget_h;
				// Check how big the remaining space would be if this
				// Node would be splitted horizontally. Remaining
				// space is compared to original size.
				float area1 = w * widget_h - widget_area;
				float area2 = w * (h - widget_h);
				assert area1 >= 0 && area2 >= 0;
				float bigger_area = Math.max(area1, area2);
				if (!last_widget) {
					bigger_area -= area_now;
				}
				if (fit_node == null || bigger_area > fit_best_area) {
					fit_best_area = bigger_area;
					fit_node = node;
					fit_rectangle.set(x1, y1, widget_w, widget_h);
					fit_split_horizontally = true;
				}
				// Check how big the remaining space would be
				// if this Node would be splitted vertically.
				area1 = widget_w * h - widget_area;
				area2 = (w - widget_w) * h;
				assert area1 >= 0 && area2 >= 0;
				bigger_area = Math.max(area1, area2);
				if (!last_widget) {
					bigger_area -= area_now;
				}
				assert fit_node != null;
				if (bigger_area > fit_best_area) {
					fit_best_area = bigger_area;
					fit_node = node;
					fit_rectangle.set(x1, y1, widget_w, widget_h);
					fit_split_horizontally = false;
				}
			}
		}
		// Split has been done
		else {
			assert node.c2 != null;
			if (node.horizontal_splitline) {
				findBestFitFromTree(widget, widget_w, widget_h, node.c1, x1, y1, x2, node.splitline, last_widget);
				findBestFitFromTree(widget, widget_w, widget_h, node.c2, x1, node.splitline, x2, y2, last_widget);
			} else {
				findBestFitFromTree(widget, widget_w, widget_h, node.c1, x1, y1, node.splitline, y2, last_widget);
				findBestFitFromTree(widget, widget_w, widget_h, node.c2, node.splitline, y1, x2, y2, last_widget);
			}
		}
	}

	private void removeEmptyNodes(ReposNode node)
	{
		// If this node is empty, then do nothing
		if (node.c1 == null && node.w == null) {
			assert node.c2 == null;
			return;
		}

		boolean modifications_done;
		do {
			modifications_done = false;

			// If this node contains Widget, then do nothing
			if (node.w != null) {
				return;
			}

			// If first side is completely unused, then remove it
			if (node.c1.w == null && node.c1.c1 == null) {
				assert node.c1.c2 == null;

				node.w = node.c2.w;
				node.w_rect = node.c2.w_rect;
				node.horizontal_splitline = node.c2.horizontal_splitline;
				node.c1 = node.c2.c1;
				node.c2 = node.c2.c2;

				modifications_done = true;
			}
			// If second side is completely unused, then remove it
			else if (node.c2.w == null && node.c2.c1 == null) {
				assert node.c2.c2 == null;

				node.w = node.c1.w;
				node.w_rect = node.c1.w_rect;
				node.horizontal_splitline = node.c1.horizontal_splitline;
				node.c2 = node.c1.c2;
				node.c1 = node.c1.c1;

				modifications_done = true;
			}
		} while (modifications_done);

		removeEmptyNodes(node.c1);
		removeEmptyNodes(node.c2);
	}

	private void expandRectangles(ReposNode node, float x1, float y1, float x2, float y2)
	{
		assert x1 <= x2;
		assert y1 <= y2;
		// If there is Widget, then make it use the whole space
		if (node.w != null) {
			node.w_rect.set(x1, y1, x2 - x1, y2 - y1);
			return;
		}

		// In case tree contains nothing at all
		if (node.c1 == null || node.c2 == null) {
			return;
		}

		// This node is splitted, so calculate sizes for both sides
		if (node.horizontal_splitline) {
			// Calculate properties of both children and their grand children
			float side1_min_height = calculateMinHeight(node.c1);
			int side1_expanding = calculateVerticalExpanding(node.c1);
			float side2_min_height = calculateMinHeight(node.c2);
			int side2_expanding = calculateVerticalExpanding(node.c2);
			// Calculate heights
			float extra_space = y2 - y1 - side1_min_height - side2_min_height;
			int total_expanding = side1_expanding + side2_expanding;
			if (total_expanding == 0) {
				float real_splitline = y1 + side1_min_height + extra_space / 2;
				expandRectangles(node.c1, x1, y1, x2, real_splitline);
				expandRectangles(node.c2, x1, real_splitline, x2, y2);
			} else {
				float real_splitline = y1 + side1_min_height + extra_space * (side1_expanding / total_expanding);
				expandRectangles(node.c1, x1, y1, x2, real_splitline);
				expandRectangles(node.c2, x1, real_splitline, x2, y2);
			}
		} else {
			// Calculate properties of both children and their grand children
			float side1_min_width = calculateMinWidth(node.c1);
			int side1_expanding = calculateHorizontalExpanding(node.c1);
			float side2_min_width = calculateMinWidth(node.c2);
			int side2_expanding = calculateHorizontalExpanding(node.c2);
			// Calculate heights
			float extra_space = x2 - x1 - side1_min_width - side2_min_width;
			int total_expanding = side1_expanding + side2_expanding;
			if (total_expanding == 0) {
				float real_splitline = x1 + side1_min_width + extra_space / 2;
				expandRectangles(node.c1, x1, y1, real_splitline, y2);
				expandRectangles(node.c2, real_splitline, y1, x2, y2);
			} else {
				float real_splitline = x1 + side1_min_width + extra_space * (side1_expanding / total_expanding);
				expandRectangles(node.c1, x1, y1, real_splitline, y2);
				expandRectangles(node.c2, real_splitline, y1, x2, y2);
			}
		}
	}

	private void repositionWidgetsFromTree(ReposNode node)
	{
		// If this node contains Widget
		if (node.w != null) {
			repositionChild(node.w, node.w_rect.x, node.w_rect.y, node.w_rect.width, node.w_rect.height);
		}
		// If this node is splitted
		else if (node.c1 != null) {
			assert node.c2 != null;
			repositionWidgetsFromTree(node.c1);
			repositionWidgetsFromTree(node.c2);
		}
	}

	private float calculateMinWidth(ReposNode node)
	{
		if (node.w != null) {
			return node.w_rect.width;
		}
		if (node.c1 != null) {
			if (node.horizontal_splitline) {
				return Math.max(calculateMinWidth(node.c1), calculateMinWidth(node.c2));
			} else {
				return calculateMinWidth(node.c1) + calculateMinWidth(node.c2);
			}
		}
		return 0;
	}

	private float calculateMinHeight(ReposNode node)
	{
		if (node.w != null) {
			return node.w_rect.height;
		}
		if (node.c1 != null) {
			if (node.horizontal_splitline) {
				return calculateMinHeight(node.c1) + calculateMinHeight(node.c2);
			} else {
				return Math.max(calculateMinHeight(node.c1), calculateMinHeight(node.c2));
			}
		}
		return 0;
	}

	private int calculateHorizontalExpanding(ReposNode node)
	{
		if (node.w != null) {
			return node.w.getHorizontalExpanding();
		}
		if (node.c1 != null) {
			if (node.horizontal_splitline) {
				return Math.max(calculateHorizontalExpanding(node.c1), calculateHorizontalExpanding(node.c2));
			} else {
				return calculateHorizontalExpanding(node.c1) + calculateHorizontalExpanding(node.c2);
			}
		}
		return 0;
	}

	private int calculateVerticalExpanding(ReposNode node)
	{
		if (node.w != null) {
			return node.w.getVerticalExpanding();
		}
		if (node.c1 != null) {
			if (node.horizontal_splitline) {
				return calculateVerticalExpanding(node.c1) + calculateVerticalExpanding(node.c2);
			} else {
				return Math.max(calculateVerticalExpanding(node.c1), calculateVerticalExpanding(node.c2));
			}
		}
		return 0;
	}

}
