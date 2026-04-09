package fi.henu.gdxextras.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public class FrameRecorder
{
	public FrameRecorder(float fps)
	{
		this.fps = fps;
		this.framing_guide_dim1 = 0;
		this.framing_guide_dim2 = 0;
		frame_number = 1;
	}

	public FrameRecorder(float fps, int framing_guide_dim1, int framing_guide_dim2)
	{
		this.fps = fps;
		this.framing_guide_dim1 = framing_guide_dim1;
		this.framing_guide_dim2 = framing_guide_dim2;
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
		String frame_filename = String.format("frame_%08d.png", frame_number++);
		PixmapIO.writePNG(Gdx.files.external(frame_filename), pixmap, Deflater.DEFAULT_COMPRESSION, true);
		pixmap.dispose();

		// If there is a framing guide, render it now
		if (framing_guide_dim1 > 0 && framing_guide_dim2 > 0) {
			ShapeRenderer shapes = new ShapeRenderer();
			shapes.begin(ShapeRenderer.ShapeType.Line);
			shapes.setColor(Color.RED);
			int center_x = Gdx.graphics.getWidth() / 2;
			int center_y = Gdx.graphics.getHeight() / 2;
			shapes.rect(center_x - framing_guide_dim1 / 2f, center_y - framing_guide_dim2 / 2f, framing_guide_dim1, framing_guide_dim2);
			if (framing_guide_dim1 != framing_guide_dim2) {
				shapes.rect(center_x - framing_guide_dim2 / 2f, center_y - framing_guide_dim1 / 2f, framing_guide_dim2, framing_guide_dim1);
			}
			shapes.end();
		}
	}

	private final float fps;
	private int frame_number;
	private final int framing_guide_dim1, framing_guide_dim2;
}
