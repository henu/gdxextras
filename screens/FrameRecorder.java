package fi.henu.gdxextras.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public class FrameRecorder
{
	public FrameRecorder(float fps)
	{
		this.fps = fps;
		frame_number = 1;
	}

	public float getFps()
	{
		return fps;
	}

	public void recordFrame()
	{
		// Take screenshot from the frame buffer
		Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

		// Mark possible alpha channel full opaque
		ByteBuffer pixels = pixmap.getPixels();
		int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
		for (int i = 3; i < size; i += 4) {
			pixels.put(i, (byte)255);
		}

		// Write screenshot to disk
		String frame_filename = String.format("frame_%08d.png", frame_number ++);
		PixmapIO.writePNG(Gdx.files.external(frame_filename), pixmap, Deflater.DEFAULT_COMPRESSION, true);
		pixmap.dispose();
	}

	private final float fps;
	private int frame_number;
}
