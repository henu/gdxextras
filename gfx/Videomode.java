package fi.henu.gdxextras.gfx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.utils.Array;

import java.util.Objects;

import fi.henu.gdxextras.utils.StringUtils;

public class Videomode implements Comparable<Videomode>
{
	public enum Mode
	{
		WINDOWED,
		FULLSCREEN,
		UNDECORATED
	}

	public static class InvalidKey extends RuntimeException
	{
		public InvalidKey(String message)
		{
			super(message);
		}
	}

	public static void setDefaultWindowedSize(int width, int height)
	{
		default_windowed_width = width;
		default_windowed_height = height;
	}

	public static Videomode getCurrentVideomode()
	{
		if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
			throw new RuntimeException("Videomodes are only supported on Desktop!");
		}

		if (Gdx.graphics.isFullscreen()) {
			Graphics.DisplayMode displaymode = Gdx.graphics.getDisplayMode();
			return new Videomode(Mode.FULLSCREEN, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), displaymode.refreshRate);
		}

		if (undecorated == UndecoratedState.UNDECORATED) {
			return new Videomode(Mode.UNDECORATED, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), -1);
		}

		return new Videomode(Mode.WINDOWED, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), -1);
	}

	public static Array<Videomode> getAvailableVideomodes(boolean minimal_list)
	{
		Array<Videomode> result = new Array<>();

		// Windowed mode
		result.add(new Videomode(Mode.WINDOWED, -1, -1, -1));

		// Fullscreen modes
		for (Graphics.Monitor monitor : Gdx.graphics.getMonitors()) {
			if (minimal_list) {
				Graphics.DisplayMode best_displaymode = null;
				Graphics.DisplayMode current_displaymode = Gdx.graphics.getDisplayMode();
				for (Graphics.DisplayMode displaymode : Gdx.graphics.getDisplayModes(monitor)) {
					// If display mode is not set
					if (best_displaymode == null) {
						best_displaymode = displaymode;
					}
					// If wider is found
					else if (displaymode.width > best_displaymode.width) {
						best_displaymode = displaymode;
					}
					// If taller is found
					else if (displaymode.width == best_displaymode.width && displaymode.height > best_displaymode.height) {
						best_displaymode = displaymode;
					}
					// If one with current refreshrate is found
					else if (displaymode.width == best_displaymode.width && displaymode.height == best_displaymode.height && displaymode.refreshRate == current_displaymode.refreshRate) {
						best_displaymode = displaymode;
					}
					// If one with better refreshrate is found
					else if (displaymode.width == best_displaymode.width && displaymode.height == best_displaymode.height && displaymode.refreshRate > best_displaymode.refreshRate && displaymode.refreshRate < current_displaymode.refreshRate) {
						best_displaymode = displaymode;
					}
				}
				if (best_displaymode != null) {
					Videomode videomode = new Videomode(Mode.FULLSCREEN, best_displaymode.width, best_displaymode.height, best_displaymode.refreshRate);
					if (!result.contains(videomode, false)) {
						result.add(videomode);
					}
				}
			} else {
				for (Graphics.DisplayMode displaymode : Gdx.graphics.getDisplayModes()) {
					Videomode videomode = new Videomode(Mode.FULLSCREEN, displaymode.width, displaymode.height, displaymode.refreshRate);
					if (!result.contains(videomode, false)) {
						result.add(videomode);
					}
				}
			}
		}

		// Undecorated modes
		for (Graphics.Monitor monitor : Gdx.graphics.getMonitors()) {
			if (minimal_list) {
				Graphics.DisplayMode best_displaymode = null;
				for (Graphics.DisplayMode displaymode : Gdx.graphics.getDisplayModes(monitor)) {
					// If display mode is not set
					if (best_displaymode == null) {
						best_displaymode = displaymode;
					}
					// If wider is found
					else if (displaymode.width > best_displaymode.width) {
						best_displaymode = displaymode;
					}
					// If taller is found
					else if (displaymode.width == best_displaymode.width && displaymode.height > best_displaymode.height) {
						best_displaymode = displaymode;
					}
				}
				if (best_displaymode != null) {
					Videomode videomode = new Videomode(Mode.UNDECORATED, best_displaymode.width, best_displaymode.height, -1);
					if (!result.contains(videomode, false)) {
						result.add(videomode);
					}
				}
			} else {
				for (Graphics.DisplayMode displaymode : Gdx.graphics.getDisplayModes(monitor)) {
					Videomode videomode = new Videomode(Mode.UNDECORATED, displaymode.width, displaymode.height, -1);
					if (!result.contains(videomode, false)) {
						result.add(videomode);
					}
				}
			}
		}

		result.sort();

		return result;
	}

	public static Videomode createWindowed()
	{
		return new Videomode(Mode.WINDOWED, -1, -1, -1);
	}

	public static Videomode createFromKey(String key)
	{
		Array<String> args = StringUtils.split(key, " ");
		if (args.isEmpty()) {
			throw new Videomode.InvalidKey("Empty!");
		}
		// Windowed
		if (args.get(0).equals("windowed")) {
			return new Videomode(Mode.WINDOWED, -1, -1, -1);
		}
		// Fullscreen
		if (args.get(0).equals("fullscreen")) {
			if (args.size < 4) {
				throw new Videomode.InvalidKey("Not enough values!");
			}
			int width = readPositiveIntegerFromKey(args.get(1));
			int height = readPositiveIntegerFromKey(args.get(2));
			int refreshrate = readPositiveIntegerFromKey(args.get(3));
			return new Videomode(Mode.FULLSCREEN, width, height, refreshrate);
		}
		// Undecorated
		if (args.get(0).equals("undecorated")) {
			if (args.size < 3) {
				throw new Videomode.InvalidKey("Not enough values!");
			}
			int width = readPositiveIntegerFromKey(args.get(1));
			int height = readPositiveIntegerFromKey(args.get(2));
			return new Videomode(Mode.UNDECORATED, width, height, -1);
		}
		throw new Videomode.InvalidKey("Unknown type!");
	}

	public boolean equalsDisplayMode(Graphics.DisplayMode displaymode)
	{
		return mode == Mode.FULLSCREEN && width == displaymode.width && height == displaymode.height && refreshrate == displaymode.refreshRate;
	}

	public void apply()
	{
		if (mode == Mode.WINDOWED) {
			Gdx.graphics.setUndecorated(false);
			Gdx.graphics.setResizable(true);
			Gdx.graphics.setWindowedMode(width > 0 ? width : default_windowed_width, height > 0 ? height : default_windowed_height);
			// Keep track of undecorated, as LibGDX apparently cannot tell this
			undecorated = UndecoratedState.DECORATED;
		} else if (mode == Mode.FULLSCREEN) {
			// Find the correct display mode
			Graphics.DisplayMode displaymode = null;
			int best_bpp = 0;
			for (Graphics.DisplayMode available_displaymode : Gdx.graphics.getDisplayModes()) {
				if (available_displaymode.width == width && available_displaymode.height == height && available_displaymode.refreshRate == refreshrate) {
					if (available_displaymode.bitsPerPixel > best_bpp) {
						best_bpp = available_displaymode.bitsPerPixel;
						displaymode = available_displaymode;
					}
				}
			}
			if (displaymode != null) {
				// Make sure the videomode actually changes
				if (!Gdx.graphics.isFullscreen() || !Gdx.graphics.getDisplayMode().equals(displaymode)) {
					Gdx.graphics.setFullscreenMode(displaymode);
				}
			}
		} else if (mode == Mode.UNDECORATED) {
			Gdx.graphics.setUndecorated(true);
			Gdx.graphics.setResizable(false);
			Gdx.graphics.setWindowedMode(width, height);
			// Keep track of undecorated, as LibGDX apparently cannot tell this
			undecorated = UndecoratedState.UNDECORATED;
		}
	}

	public void applyUndecoratedFlagOnly()
	{
		if (mode == Mode.UNDECORATED) {
			undecorated = UndecoratedState.UNDECORATED;
		} else {
			undecorated = UndecoratedState.DECORATED;
		}
	}

	public boolean isWindowed()
	{
		return mode == Mode.WINDOWED;
	}

	public boolean isFullscreen()
	{
		return mode == Mode.FULLSCREEN;
	}

	public boolean isUndecorated()
	{
		return mode == Mode.UNDECORATED;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getRefreshRate()
	{
		return refreshrate;
	}

	public String getKey()
	{
		if (mode == Mode.WINDOWED) {
			return "windowed";
		}
		if (mode == Mode.FULLSCREEN) {
			return "fullscreen " + width + " " + height + " " + refreshrate;
		}
		if (mode == Mode.UNDECORATED) {
			return "undecorated " + width + " " + height;
		}
		return null;
	}

	@Override
	public int compareTo(Videomode other)
	{
		// Windowed mode is always first, and doesn't care about resolution
		if (mode == Mode.WINDOWED) {
			if (other.mode == Mode.WINDOWED) {
				return 0;
			} else {
				return -1;
			}
		} else if (other.mode == Mode.WINDOWED) {
			return 1;
		}

		// Fullscreen comes before undecorated
		if (mode == Mode.FULLSCREEN && other.mode == Mode.UNDECORATED) {
			return -1;
		}
		if (mode == Mode.UNDECORATED && other.mode == Mode.FULLSCREEN) {
			return 1;
		}

		// Primarily sort by width
		if (width < other.width) {
			return 1;
		}
		if (width > other.width) {
			return -1;
		}

		// Secondarily sort by height
		if (height < other.height) {
			return 1;
		}
		if (height > other.height) {
			return -1;
		}

		// Undecorated doesn't care about refreshrate
		if (mode == Mode.UNDECORATED) {
			return 0;
		}

		return Integer.compare(other.refreshrate, refreshrate);
	}

	@Override
	public boolean equals(Object other_raw)
	{
		if (this == other_raw) return true;
		if (!(other_raw instanceof Videomode)) return false;
		Videomode other = (Videomode)other_raw;

		if (mode != other.mode) return false;

		// In windowed mode, resolution or refreshrate doesn't matter
		if (mode == Mode.WINDOWED) return true;

		// In undecorated mode, resolution doesn't matter
		if (mode == Mode.UNDECORATED) {
			return width == other.width && height == other.height;
		}

		// In fullscreen mode, resolution and refreshrate matter
		return width == other.width && height == other.height && refreshrate == other.refreshrate;
	}

	@Override
	public int hashCode()
	{
		if (mode == Mode.WINDOWED) {
			return 0;
		} else if (mode == Mode.FULLSCREEN) {
			return Objects.hash(1, width, height, refreshrate);
		} else {
			return Objects.hash(2, width, height);
		}
	}

	private enum UndecoratedState
	{
		UNDECORATED,
		DECORATED,
		NOT_SURE,
	}

	private static int default_windowed_width = 800;
	private static int default_windowed_height = 450;

	private static UndecoratedState undecorated = UndecoratedState.NOT_SURE;

	private final Mode mode;
	private final int width;
	private final int height;
	private final int refreshrate;

	private Videomode(Mode mode, int width, int height, int refreshrate)
	{
		this.mode = mode;
		this.width = width;
		this.height = height;
		this.refreshrate = refreshrate;
	}

	private static int readPositiveIntegerFromKey(String str)
	{
		int value;
		try {
			value = Integer.parseInt(str);
		}
		catch (NumberFormatException ignored) {
			throw new InvalidKey("Invalid data!");
		}
		if (value <= 0) {
			throw new InvalidKey("Invalid data!");
		}
		return value;
	}
}
