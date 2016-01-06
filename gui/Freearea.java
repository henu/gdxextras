package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Freearea extends Widget
{

	public Freearea()
	{
		super();
		// By default, this Widget does not receive pointer events
		setPointerEvents(false);
	}

	public void addHandle(Freehandle handle)
	{
		assert handles.indexOf(handle, true) < 0;
		handles.add(handle);
		addChild(handle.getWidget());
		markToNeedReposition();
	}

	public void removeHandle(Freehandle handle)
	{
		int handle_id = handles.indexOf(handle, true);
		if (handle_id < 0) {
			throw new RuntimeException("Unable to remove handle, because it does not belong to this area!");
		}
		handles.removeIndex(handle_id);
		removeChild(handle.getWidget());
	}

	protected void doRepositioning()
	{
		Freehandle[] handles_buf = handles.items;
		int handles_size = handles.size;
		for (int handle_id = 0; handle_id < handles_size; handle_id++) {
			Freehandle handle = handles_buf[handle_id];
			Vector2 handle_pos = handle.getPosition();
			float handle_w = handle.getWidth();
			float handle_h = handle.getHeight();
			Widget widget = handle.getWidget();
			repositionChild(widget, handle_pos.x - handle_w / 2f, handle_pos.y - handle_h / 2f, handle_w, handle_h);
		}
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		// Draw nothing
	}

	protected float doGetMinWidth()
	{
		return 0f;
	}

	protected float doGetMinHeight(float width)
	{
		return 0f;
	}

	private Array<Freehandle> handles = new Array<Freehandle>(true, 0, Freehandle.class);

}
