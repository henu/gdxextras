package fi.henu.gdxextras.gui;

public class GuiEvent
{

	public Type getType()
	{
		return type;
	}

	// For Widget event
	public Widget getWidget()
	{
		return (Widget)obj0;
	}

	public int getAction()
	{
		return int0;
	}

	// For Key press and release events
	public int getKeycode()
	{
		return int0;
	}

	public static GuiEvent fromWidget(Widget widget, int action)
	{
		GuiEvent result = new GuiEvent();
		result.type = Type.WIDGET;
		result.obj0 = widget;
		result.int0 = action;
		return result;
	}

	public static GuiEvent fromKeyPress(int keycode)
	{
		GuiEvent result = new GuiEvent();
		result.type = Type.KEY_PRESS;
		result.int0 = keycode;
		return result;
	}

	public static GuiEvent fromKeyRelease(int keycode)
	{
		GuiEvent result = new GuiEvent();
		result.type = Type.KEY_RELEASE;
		result.int0 = keycode;
		return result;
	}

	public enum Type
	{
		WIDGET,
		KEY_PRESS,
		KEY_RELEASE,
	}

	private Type type;
	private Object obj0;
	private int int0;

}
