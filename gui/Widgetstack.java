package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Widgetstack extends Widget
{

	public Widgetstack()
	{
		super();
		setVerticalExpanding(1);
		setHorizontalExpanding(1);
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
		int pos = widgets.indexOf(widget, true);
		if (pos < 0) {
			throw new RuntimeException("Widget not found!");
		}
		widgets.removeIndex(pos);
		removeChild(widget);
		markToNeedReposition();
	}

	protected void doRepositioning()
	{
		Widget[] widgets_buf = widgets.items;
		int widgets_size = widgets.size;
		for (int widget_id = 0; widget_id < widgets_size; widget_id++) {
			Widget widget = widgets_buf[widget_id];
			repositionChild(widget, getPositionX(), getPositionY(), getWidth(), getHeight());
		}
	}

	public void doRendering(SpriteBatch batch)
	{
		// Do nothing
	}

	protected float doGetMinWidth()
	{
		return 0f;
	}

	protected float doGetMinHeight(float width)
	{
		return 0f;
	}

	private final Array<Widget> widgets = new Array<Widget>(true, 0, Widget.class);

}
