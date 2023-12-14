package fi.henu.gdxextras.game.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import fi.henu.gdxextras.game.Camera;

public abstract class Layer implements Comparable
{
	public Layer(int depth_index)
	{
		this.depth_index = depth_index;
	}

	public int getDepthIndex()
	{
		return depth_index;
	}

	public abstract void render(SpriteBatch batch, Camera camera);

	@Override
	public int compareTo(Object layer_raw)
	{
		if (layer_raw instanceof Layer) {
			Layer layer = (Layer)layer_raw;
			return Integer.compare(layer.depth_index, depth_index);
		}
		return 0;
	}

	private final int depth_index;
}
