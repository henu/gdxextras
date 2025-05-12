package fi.henu.gdxextras;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.screens.FrameRecorder;
import fi.henu.gdxextras.testing.InputOverrider;

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

	public Screen getTopScreen()
	{
		return screens.get(screens.size - 1);
	}

	public void overrideInputs(InputOverrider input_overrider)
	{
		this.input_overrider = input_overrider;
	}

	@Override
	public void render()
	{
		// If there are no screens, then quit
		if (screens.size == 0) {
			Gdx.app.exit();
			return;
		}

		if (input_overrider != null) {
			if (!input_overrider.run(this)) {
				input_overrider.dispose();
				input_overrider = null;
			}
		}

// TODO: If there is FrameRecorder, then force screen size!
		// Override super method completely
		if (screen != null) {
			float delta = Gdx.graphics.getDeltaTime();
			// If there is a frame recorder, then override delta time. Also
			// sleep if necessary, so the game will not run too fast.
			if (frame_recorder != null) {
				float new_delta = 1.0f / frame_recorder.getFps();
				float sleep = new_delta - delta;
				if (sleep > 0) {
					try {
						Thread.sleep(Math.round(1000 * sleep));
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				delta = new_delta;
			}
			screen.render(delta);
		}

		// If there is a frame recorder, then record this frame
		if (frame_recorder != null) {
			frame_recorder.recordFrame();
		}
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
		frame_recorder = null;
		super.dispose();
	}

	public void setFrameRecorder(FrameRecorder frame_recorder)
	{
		this.frame_recorder = frame_recorder;
	}

	public FrameRecorder getFrameRecorder()
	{
		return frame_recorder;
	}

	// Stack of screens
	private final Array<Screen> screens = new Array<>(true, 0, Screen.class);

	// Special testing class, that can generate input events
	private InputOverrider input_overrider;

	// Poor man's video recorder
	private FrameRecorder frame_recorder;
}
