package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;

public class ImageButton extends Widget
{

	public ImageButton()
	{
		pointers_listened = new IntSet();
	}

	public void setImage(AtlasRegion region)
	{
		this.region = region;
		markToNeedReposition();
	}

	public void setScale(float scale)
	{
		this.scale = scale;
		markToNeedReposition();
	}

	public boolean isPressed()
	{
		for (IntSet.IntSetIterator i = pointers_listened.iterator(); i.hasNext;) {
			if (pointerOver(i.next())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		pointers_listened.add(pointer_id);
// TODO: Why this was here?
//		clearKeyboardListener();
		return true;
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		if (pointerOver(pointer_id)) {
			fireEvent();
		}
		pointers_listened.remove(pointer_id);
		unregisterPointerListener(pointer_id);
	}

	// Returns true if position is over Widget. Position is relative to Widget
	// and over the rectangle of Widget, so this function is for Widgets that
	// are not rectangle shaped.
	@Override
	public boolean isOver(float x, float y)
	{
		return true;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		render(batch, region, getPositionX(), getPositionY(), scale);
	}

	@Override
	protected float doGetMinWidth()
	{
		if (region != null) {
			return region.originalWidth * scale;
		}
		return 0;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		if (region != null) {
			return region.originalHeight * scale;
		}
		return 0;
	}

	private AtlasRegion region;
	private float scale;

	private final IntSet pointers_listened;
}
