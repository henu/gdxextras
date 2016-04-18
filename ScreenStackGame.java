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
		removeAndDisposeScreenAt(0);
	}

	// Removes specific screen and disposes it
	public void removeAndDisposeScreen(Screen screen)
	{
		int screen_index = screens.indexOf(screen, true);
		if (screen_index < 0) {
			throw new RuntimeException("Screen not found!");
		}
		removeAndDisposeScreenAt(screens.size - 1 - screen_index);
	}

	// Removes specific screen and disposes it. Note, that
	// index starts from beginning, so that 0 means top screen.
	public void removeAndDisposeScreenAt(int negative_index)
	{
		if (screens.size == 0) {
			throw new RuntimeException("No screens!");
		}
		int index = screens.size - 1 - negative_index;
		if (index < 0 || index >= screens.size) {
			throw new RuntimeException("Index out of range!");
		}
		Screen disposed_screen = screens.removeIndex(index);
		disposed_screen.dispose();
		// Check if this was the last screen
		if (screens.size == 0) {
			setScreen(null);
		} else {
			// Only set top screen, if removed Screen was on the top
			if (negative_index == 0) {
				Screen new_top_screen = screens.get(screens.size - 1);
				setScreen(new_top_screen);
			}
		}
	}

	@Override
	public void render ()
	{
		// If there are no screens, then quit
		if (screens.size == 0) {
			Gdx.app.exit();
			return;
		}

		super.render();
	}

	@Override
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
		setScreen(null);
		super.dispose();
	}

	// Stack of screens
	private Array<Screen> screens = new Array<Screen>(true, 0, Screen.class);
}
