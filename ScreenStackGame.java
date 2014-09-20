package fi.henu.gdxextras;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;

public abstract class ScreenStackGame extends Game
{

	// Push another screen to the stack
	public void pushScreen(Screen new_screen)
	{
		screens.add(new_screen);
		setScreen(new_screen);
	}

	// Removes top screen and disposes it
	public void popAndDisposeScreen()
	{
		assert screens.size > 0;
		Screen disposed_screen = screens.pop();
		disposed_screen.dispose();
		// Check if this was the last screen
		if (screens.size == 0) {
			Gdx.app.exit();
		} else {
			Screen new_top_screen = screens.get(screens.size - 1);
			setScreen(new_top_screen);
		}
	}

	public void dispose()
	{
		// Clear screens
		Screen[] screens_buf = screens.items;
		int screens_size = screens.size;
		for (int screen_id = screens_size - 1; screen_id >= 0; screen_id--) {
			Screen screen = screens_buf[screen_id];
			screen.dispose();
		}
		screens.clear();
	}

	// Stack of screens
	private Array<Screen> screens = new Array<Screen>(false, 0, Screen.class);

}
