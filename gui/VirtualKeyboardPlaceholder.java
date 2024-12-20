package fi.henu.gdxextras.gui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

// TODO: It would be nice if this Widget could automatically detect if the keyboard is open!
public class VirtualKeyboardPlaceholder extends Widget
{
	public void setKeyboardVisible(boolean visible)
	{
		if (visible != keyboard_visible) {
			markToNeedReposition();
		}
		keyboard_visible = visible;
	}

	@Override
	protected float doGetMinWidth()
	{
		return 0;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		if (Gdx.app.getType() != Application.ApplicationType.Android && Gdx.app.getType() != Application.ApplicationType.iOS) {
			return 0;
		}
		if (keyboard_visible) {
			return Gdx.graphics.getHeight() * 0.518518519f;
		}
		return 0;
	}

	private boolean keyboard_visible = false;
}
