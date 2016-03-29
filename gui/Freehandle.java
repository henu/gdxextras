package fi.henu.gdxextras.gui;

import com.badlogic.gdx.math.Vector2;

public class Freehandle
{

	public Freehandle(Widget widget)
	{
		this.widget = widget;
	}

	public void setPosition(Vector2 pos)
	{
		this.pos.set(pos);
		widget.markToNeedReposition();
	}

	public Widget getWidget()
	{
		return widget;
	}

	public Vector2 getPosition()
	{
		return pos;
	}

	public float getWidth()
	{
		return widget.getMinWidth();
	}

	public float getHeight()
	{
		return widget.getMinHeight(widget.getMinWidth());
	}

	private Widget widget = null;

	private final Vector2 pos = new Vector2();

}
