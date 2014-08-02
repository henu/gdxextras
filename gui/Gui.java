package fi.henu.gdxextras.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Gui implements InputProcessor
{

	public enum Anchor {
		LEFT, RIGHT, TOP, BOTTOM, CENTER
	}

	public Gui()
	{
	}

	public void close()
	{
		if (batch != null) {
			batch.dispose();
			batch = null;
		}
	}
	
	public void setEventlistener(Eventlistener eventlistener)
	{
		this.eventlistener = eventlistener;
	}

	public void setWidget(Widget widget)
	{
		if (this.widget != null) {
			this.widget.setGui(null);
		}
		this.widget = widget;
		if (widget != null) {
			widget.setGui(this);
			widget.markToNeedReposition();
		}
	}

	public void setScreenSize(int width, int height)
	{
		// Mark new screen size and order repositioning
		screen_width = width;
		screen_height = height;
		if (widget != null) {
			widget.markToNeedReposition();
		}
		// Update projection of Spritebatch
		batch_projmatrix.setToOrtho2D(0, 0, width, height);
		batch.setProjectionMatrix(batch_projmatrix);
	}

	public void repositionWidgets()
	{
		if (widget != null) {
			widget.repositionIfNeeded(0, 0, screen_width, screen_height);
		}
	}

	public void render(GL20 gl)
	{
		if (batch == null || widget == null) {
			return;
		}

		boolean cull_face_enabled = gl.glIsEnabled(GL20.GL_CULL_FACE);
		if (cull_face_enabled) {
			gl.glDisable(GL20.GL_CULL_FACE);
		}

		batch.begin();
		widget.render(gl, batch, 0, 0, -1, -1);
		batch.end();

		if (cull_face_enabled) {
			gl.glEnable(GL20.GL_CULL_FACE);
		}
	}

	public Widget getWidgetUnderPointer(int pointer_id)
	{
		if (pointer_id >= widgets_topmost.size) {
			return null;
		}
		return widgets_topmost.get(pointer_id);
	}

	// Returns topmost Widget at the given position, or null if no
	// Widgets were found. This is for pointer events only, so
	// returned Widget is guaranteed to have pointer events enabled.
	public Widget getTopmostWidget(Vector2 pos)
	{
		if (widget != null) {
			return widget.getTopmost(pos.x, pos.y);
		}
		return null;
	}

	public Widget getPointerListener(int pointer_id)
	{
		if (pointer_id >= pointerlisteners.size) {
			return null;
		}
		return pointerlisteners.get(pointer_id);
	}

	// This may be only called by that Widget, that is currently
	// pointerlistener!
	public void unregisterPointerListener(int pointer_id)
	{
		if (pointer_id >= pointerlisteners.size || pointerlisteners.get(pointer_id) == null) {
			throw new RuntimeException("Unable to unregister pointerlistener because it is not set!");
		}
		pointerlisteners.set(pointer_id, null);
	}

	public void setKeyboardListener(Widget listener)
	{
		if (listener != null) {
			Gdx.input.setOnscreenKeyboardVisible(true);
		} else {
			Gdx.input.setOnscreenKeyboardVisible(false);
		}
		keyboardlistener = listener;
	}

	public Widget getKeyboardListener()
	{
		return keyboardlistener;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		if (keycode == Keys.BACK) {
			if (eventlistener == null) {
				return false;
			}
			return eventlistener.handleGuiEvent(GuiEvent.fromKeypress(Keys.BACK));
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		if (keyboardlistener != null) {
			keyboardlistener.keyTyped(character);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		Widget mouse_topmost = getTopmostWidget(mouse_last_pos);
		if (mouse_topmost != null) {
			mouse_topmost.scrolled(amount);
			return true;
		}

		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer_id, int button)
	{
		// Swap coordinates and get topmost widget
		v2tmp.x = x;
		v2tmp.y = screen_height - y;
		Widget topmost = getTopmostWidget(v2tmp);
		storeTopmostWidget(pointer_id, topmost);

		// If there is pointer listener, inform it
		if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
			pointerlisteners.get(pointer_id).pointerDown(pointer_id, v2tmp);
			return true;
		}
		// Otherwise inform topmost Widget that is under the touch
		else {
			if (topmost != null) {
				if (topmost.pointerDown(pointer_id, v2tmp)) {
					while (pointer_id >= pointerlisteners.size) {
						pointerlisteners.add(null);
					}
					pointerlisteners.set(pointer_id, topmost);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer_id)
	{
		// TODO: In future, let Widgets to force repositioning of Widgets
		// immediately after input event! This is necessary for example when
		// dragging stuff.
		// Swap coordinates and get topmost widget
		v2tmp.x = x;
		v2tmp.y = screen_height - y;
		Widget topmost = getTopmostWidget(v2tmp);
		storeTopmostWidget(pointer_id, topmost);

		if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
			pointerlisteners.get(pointer_id).pointerMove(pointer_id, v2tmp);
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y)
	{
		// Mouse over is not supported for widgets, but store coordinates so
		// scroll wheel can be applied to correct Widget.
		mouse_last_pos.x = x;
		mouse_last_pos.y = screen_height - y;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer_id, int button)
	{
		// Swap coordinates and get topmost widget
		v2tmp.x = x;
		v2tmp.y = screen_height - y;
		Widget topmost = getTopmostWidget(v2tmp);
		storeTopmostWidget(pointer_id, topmost);

		if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
			pointerlisteners.get(pointer_id).pointerUp(pointer_id, v2tmp);
			return true;
		}

		return false;
	}

	// Called by Widget
	public void generateDragEventToWidgets(Widget widgets, int pointer_id, Vector2 down_pos, Vector2 up_pos)
	{
		// First find what Widget is at this position
		Widget widget_here = widgets.getTopmost(down_pos.x, down_pos.y);

		if (widget_here == null) {
			return;
		}
		
		// Pointer down event
		if (widget_here.pointerDown(pointer_id, down_pos)) {

			// Temporary make this widget to be the one under mouse
			assert pointer_id < widgets_topmost.size;
			Widget old_topmost = widgets_topmost.items[pointer_id];
			widgets_topmost.items[pointer_id] = widget_here;
			
			while (pointer_id >= pointerlisteners.size) {
				pointerlisteners.add(null);
			}
			pointerlisteners.set(pointer_id, widget_here);

			// Widget started listening for events for that pointer,
			// so give it drag event if pointer was moved.
			if (!down_pos.equals(up_pos)) {
				widget_here.pointerMove(pointer_id, up_pos);
			}
			
			// Finally up event, but just for the Widget that listens it.
			if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
				pointerlisteners.get(pointer_id).pointerUp(pointer_id, up_pos);
			}
			
			// Resume correct topmost widget
			widgets_topmost.items[pointer_id] = old_topmost;
		}
	}

	private int screen_width = 0;
	private int screen_height = 0;

	private SpriteBatch batch = new SpriteBatch();
	private Matrix4 batch_projmatrix = new Matrix4();

	// The Widget and the possible pointerlistener. If pointerlistener
	// is set, then all events are delivered to it.
	private Widget widget;
	private Array<Widget> widgets_topmost = new Array<Widget>(true, 0, Widget.class);
	private Array<Widget> pointerlisteners = new Array<Widget>();
	private Widget keyboardlistener = null;
	
	// Listener for special events, like back button, etc.
	private Eventlistener eventlistener = null;

	// Mouse position is used to find correct
	// Widget to receive scroll wheel events.
	private Vector2 mouse_last_pos = new Vector2(0, 0);
	
	private Vector2 v2tmp = new Vector2();

	private void storeTopmostWidget(int pointer_id, Widget topmost)
	{
		while (pointer_id >= widgets_topmost.size) {
			widgets_topmost.add(null);
		}
		widgets_topmost.set(pointer_id, topmost);
	}

}
