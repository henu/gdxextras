package fi.henu.gdxextras.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class Gui implements InputProcessor
{
	public Gui()
	{
		key_press_handlers = new IntMap<>();
		key_release_handlers = new IntMap<>();
	}

	// Automatically scale GUI, so that the diagonal is always the given
	// in GUI units. If you give for example diagonal 1000 and the real
	// pixel based diagonal is 2000, then the GUI will have 200 % scaling.
	public void setAutomaticScaling(float diagonal)
	{
		if (diagonal < 1) {
			throw new RuntimeException("Diagonal must be at least one!");
		}
		auto_scaling_by_diagonal = diagonal;
	}

	public void disableAutomaticScaling()
	{
		auto_scaling_by_diagonal = -1;
	}

	public float getScaling()
	{
		return scaling;
	}

	public Widget findWidget(String widget_id)
	{
		if (widget != null) {
			return widget.findWidget(widget_id);
		}
		return null;
	}

	public void close()
	{
		if (batch != null) {
			batch.dispose();
			batch = null;
		}
		if (shaperenderer != null) {
			shaperenderer.dispose();
			shaperenderer = null;
		}
	}

	// TODO: Try to get rid of this function! It would be better to use function pointers and callbacks!
	@Deprecated
	public void setEventlistener(Eventlistener eventlistener)
	{
		this.eventlistener = eventlistener;
		// If event listener was set, then clear all specific key handlers
		if (eventlistener != null) {
			key_press_handlers.clear();
		}
	}

	public void setKeyPressEventHandler(int keycode, Eventlistener handler)
	{
		if (handler != null) {
			key_press_handlers.put(keycode, handler);
			// Make sure the generic event listener is not set
			eventlistener = null;
		} else {
			key_press_handlers.remove(keycode);
		}
	}

	public void setKeyReleaseEventHandler(int keycode, Eventlistener handler)
	{
		if (handler != null) {
			key_release_handlers.put(keycode, handler);
			// Make sure the generic event listener is not set
			eventlistener = null;
		} else {
			key_release_handlers.remove(keycode);
		}
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
		// If GUI is automatically scaled by screen diagonal
		if (auto_scaling_by_diagonal > 0) {
			// Calculate scaling
			float diagonal = (float)Math.sqrt(width * width + height * height);
			scaling = diagonal / auto_scaling_by_diagonal;
			// Mark new screen size and order repositioning
			width_in_pixels = width;
			height_in_pixels = height;
			width_in_gui_units = width / scaling;
			height_in_gui_units = height / scaling;
			if (widget != null) {
				widget.markToNeedReposition();
			}
			// Update projection of Spritebatch and Shaperenderer
			Matrix4 projection_matrix = new Matrix4();
			projection_matrix.setToOrtho2D(0, 0, width_in_gui_units, height_in_gui_units);
			batch.setProjectionMatrix(projection_matrix);
			shaperenderer.setProjectionMatrix(projection_matrix);
		}
		// If there is no automatic scaling
		else {
			// Mark new screen size and order repositioning
			width_in_pixels = width;
			height_in_pixels = height;
			width_in_gui_units = width;
			height_in_gui_units = height;
			if (widget != null) {
				widget.markToNeedReposition();
			}
			// Update projection of Spritebatch and Shaperenderer
			Matrix4 projection_matrix = new Matrix4();
			projection_matrix.setToOrtho2D(0, 0, width, height);
			batch.setProjectionMatrix(projection_matrix);
			shaperenderer.setProjectionMatrix(projection_matrix);
			// Reset scaling
			scaling = 1;
		}
	}

	public float getWidthInGuiUnits()
	{
		return width_in_gui_units;
	}

	public float getHeightInGuiUnits()
	{
		return height_in_gui_units;
	}

	public void repositionWidgets()
	{
		if (widget != null) {
			widget.repositionIfNeeded(0, 0, width_in_gui_units, height_in_gui_units);
		}
	}

	public void render(GL20 gl)
	{
		if (batch == null || shaperenderer == null || widget == null) {
			return;
		}

		boolean cull_face_enabled = gl.glIsEnabled(GL20.GL_CULL_FACE);
		if (cull_face_enabled) {
			gl.glDisable(GL20.GL_CULL_FACE);
		}

		batch.begin();
		widget.render(gl, batch, shaperenderer, 0, 0, -1, -1);
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
		Gdx.input.setOnscreenKeyboardVisible(listener != null);
		keyboardlistener = listener;
	}

	public Widget getKeyboardListener()
	{
		return keyboardlistener;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		// Check for individual key press handler
		Eventlistener key_press_handler = key_press_handlers.get(keycode);
		if (key_press_handler != null) {
			return key_press_handler.handleGuiEvent(GuiEvent.fromKeyPress(keycode));
		}

		if (eventlistener == null) {
			return false;
		}
		return eventlistener.handleGuiEvent(GuiEvent.fromKeyPress(keycode));
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
		// Check for individual key release handler
		Eventlistener key_release_handler = key_release_handlers.get(keycode);
		if (key_release_handler != null) {
			return key_release_handler.handleGuiEvent(GuiEvent.fromKeyRelease(keycode));
		}

		if (eventlistener == null) {
			return false;
		}
		return eventlistener.handleGuiEvent(GuiEvent.fromKeyRelease(keycode));
	}

	@Override
	public boolean scrolled(float amount_x, float amount_y)
	{
		Widget mouse_topmost = getTopmostWidget(mouse_last_pos);
		if (mouse_topmost != null) {
			mouse_topmost.scrolled(amount_x, amount_y);
			return true;
		}

		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer_id, int button)
	{
		// Swap coordinates and get topmost widget
		v2tmp.x = x * width_in_gui_units / width_in_pixels;
		v2tmp.y = (height_in_pixels - y) * height_in_gui_units / height_in_pixels;
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
// TODO: In future, let Widgets to force repositioning of Widgets immediately after input event! This is necessary for example when dragging stuff.
		// Swap coordinates and get topmost widget
		v2tmp.x = x * width_in_gui_units / width_in_pixels;
		v2tmp.y = (height_in_pixels - y) * height_in_gui_units / height_in_pixels;
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
		mouse_last_pos.x = x * width_in_gui_units / width_in_pixels;
		mouse_last_pos.y = (height_in_pixels - y) * height_in_gui_units / height_in_pixels;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer_id, int button)
	{
		// Swap coordinates and get topmost widget
		v2tmp.x = x * width_in_gui_units / width_in_pixels;
		v2tmp.y = (height_in_pixels - y) * height_in_gui_units / height_in_pixels;
		Widget topmost = getTopmostWidget(v2tmp);
		storeTopmostWidget(pointer_id, topmost);

		if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
			pointerlisteners.get(pointer_id).pointerUp(pointer_id, v2tmp);
			return true;
		}

		return false;
	}

	@Override
	public boolean touchCancelled(int x, int y, int pointer_id, int button)
	{
		// Swap coordinates and get topmost widget
		v2tmp.x = x * width_in_gui_units / width_in_pixels;
		v2tmp.y = (height_in_pixels - y) * height_in_gui_units / height_in_pixels;
		Widget topmost = getTopmostWidget(v2tmp);
		storeTopmostWidget(pointer_id, topmost);

		if (pointer_id < pointerlisteners.size && pointerlisteners.get(pointer_id) != null) {
			pointerlisteners.get(pointer_id).pointerCancelled(pointer_id, v2tmp);
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

	// This is called by Widget, when its Gui is changed or removed.
	// This will silently unregister this Widget as pointerlistener
	public void widgetRemoved(Widget widget)
	{
		for (int pointer_id = 0; pointer_id < pointerlisteners.size; ++pointer_id) {
			if (pointerlisteners.get(pointer_id) == widget) {
				pointerlisteners.set(pointer_id, null);
			}
		}
	}

	public float toTouchSpaceX(float x)
	{
		return x * width_in_pixels / width_in_gui_units;
	}

	public float toTouchSpaceY(float y)
	{
		return height_in_pixels - (y / height_in_gui_units * height_in_pixels);
	}

	private float auto_scaling_by_diagonal;
	private float scaling;

	private int width_in_pixels = 0;
	private int height_in_pixels = 0;
	private float width_in_gui_units = 0;
	private float height_in_gui_units = 0;

	private SpriteBatch batch = new SpriteBatch();
	private ShapeRenderer shaperenderer = new ShapeRenderer();

	// The Widget and the possible pointerlistener. If pointerlistener
	// is set, then all events are delivered to it.
	private Widget widget;
	private final Array<Widget> widgets_topmost = new Array<Widget>(true, 0, Widget.class);
	private final Array<Widget> pointerlisteners = new Array<Widget>();
	private Widget keyboardlistener = null;

	// Listener for special events, like back button, etc.
	private Eventlistener eventlistener = null;

	// Handlers of specific keys;
	private final IntMap<Eventlistener> key_press_handlers;
	private final IntMap<Eventlistener> key_release_handlers;

	// Mouse position is used to find correct
	// Widget to receive scroll wheel events.
	private final Vector2 mouse_last_pos = new Vector2(0, 0);

	private final Vector2 v2tmp = new Vector2();

	private void storeTopmostWidget(int pointer_id, Widget topmost)
	{
		while (pointer_id >= widgets_topmost.size) {
			widgets_topmost.add(null);
		}
		widgets_topmost.set(pointer_id, topmost);
	}
}
