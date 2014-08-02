package fi.henu.gdxextras.gui;

public class GuiEvent
{
	
	public Type getType() { return type; }
	
	// For Widget event
	public Widget getWidget() { return (Widget)obj0; }
	public int getAction() { return int0; }
	
	// For Keypress event
	public int getKeycode() { return int0; }
	
	public static GuiEvent fromWidget(Widget widget, int action)
	{
		GuiEvent result = new GuiEvent();
		result.type = Type.WIDGET;
		result.obj0 = widget;
		result.int0 = action;
		return result;
	}
	
	public static GuiEvent fromKeypress(int keycode)
	{
		GuiEvent result = new GuiEvent();
		result.type = Type.KEYPRESS;
		result.int0 = keycode;
		return result;
	}

	public enum Type {
		WIDGET,
		KEYPRESS
	}

	private Type type;
	private Object obj0;
	private int int0;

}
