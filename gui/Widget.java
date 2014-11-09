package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class Widget
{

	public enum Alignment {
		LEFT, RIGHT, TOP, BOTTOM, CENTER
	}

	protected Widget()
	{
		gui = null;
		parent = null;
		visible = true;
		shrunken = false;
		pointerevents_enabled = true;
		be_topmost_before_children = false;
		expanding_horiz = 0;
		expanding_vert = 0;
		align = Alignment.CENTER;
		valign = Alignment.CENTER;
		fixed_min_width = 0;
		fixed_min_height = 0;
		margin = 0;
		pos = new Vector2(0, 0);
		size = new Vector2(0, 0);
		reposition_needed = true;
		eventlistener = null;
	}

	public void setEventlistener(Eventlistener eventlistener)
	{
		this.eventlistener = eventlistener;
	}

	// Hides/reveals Widget. Widget is still present, but
	// it is not shown and it cannot take input or events.
	public void setVisible(boolean visible)
	{
		if (this.visible != visible) {
			markToNeedReposition();
		}
		this.visible = visible;
	}

	public boolean isVisible()
	{
		return visible && !shrunken;
	}

	// Shrinks/expands Widget. When Widget is shrunken,
	// its size is always 0 x 0 and it is also hidden.
	public void setShrunken(boolean shrunken)
	{
		if (this.shrunken != shrunken) {
			markToNeedReposition();
		}
		this.shrunken = shrunken;
	}
	
	public boolean isShrunken()
	{
		return shrunken;
	}

	// Enables/disables receiving of pointer down and scrolling events.
	public void setPointerEvents(boolean pointerevents_enabled)
	{
		this.pointerevents_enabled = pointerevents_enabled;
	}
	
	public boolean getPointerEvents()
	{
		return pointerevents_enabled;
	}
	
	public void setBeTopmostBeforeChildren(boolean be_topmost_before_children)
	{
		this.be_topmost_before_children = be_topmost_before_children;
	}
	
	public boolean getBeTopmostBeforeChildren() {
		return be_topmost_before_children;
	}

	public void setHorizontalExpanding(int expanding)
	{
		expanding_horiz = expanding;
		markToNeedReposition();
	}

	public void setVerticalExpanding(int expanding)
	{
		expanding_vert = expanding;
		markToNeedReposition();
	}

	public void setHorizontalAlignment(Alignment align)
	{
		this.align = align;
		markToNeedReposition();
	}

	public void setVerticalAlignment(Alignment valign)
	{
		this.valign = valign;
		markToNeedReposition();
	}

	public void setFixedMinimumWidth(float min_width)
	{
		if (fixed_min_width != min_width) {
			markToNeedReposition();
		}
		fixed_min_width = min_width;
	}
	
	public void setFixedMinimumHeight(float min_height)
	{
		if (fixed_min_height != min_height) {
			markToNeedReposition();
		}
		fixed_min_height = min_height;
	}

	public int getHorizontalExpanding()
	{
		return expanding_horiz;
	}

	public int getVerticalExpanding()
	{
		return expanding_vert;
	}
	
	public void setMargin(float margin)
	{
		this.margin = margin;
		markToNeedReposition();
	}

	public float getMargin()
	{
		return margin;
	}

	// Returns this Widget or one of its children, whatever of them is topmost.
	public Widget getTopmost(float x, float y)
	{
		if (!visible || shrunken) {
			return null;
		}

		// Check if position is totally out of Widget. Note! If you would like
		// to allow children to go out of this Widget, then you need to discard
		// only this Widget in this test, and not the children too.
		if (x < getPositionX() || x > getPositionX() + getWidth() || y < getPositionY() || y > getPositionY() + getHeight()) {
			return null;
		}
		
		if (be_topmost_before_children && pointerevents_enabled && isOver(x, y)) {
			return this;
		}

		// Check children first
		Widget[] children_buf = children.items;
		int children_size = children.size;
		for (int child_id = children_size - 1; child_id >= 0; child_id--) {
			Widget child = children_buf[child_id];
			Widget child_topmost = child.getTopmost(x, y);
			if (child_topmost != null) {
				return child_topmost;
			}
		}

		// Check if the Widget itself is the topmost
		if (pointerevents_enabled && isOver(x, y)) {
			return this;
		}

		// No widget was topmost
		return null;
	}

	public void keyTyped(char character)
	{
	}

	// Pointer has been pressed. Return true if you wish to capture focus to
	// this Widget.
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		return false;
	}

	public void pointerMove(int pointer_id, Vector2 pos)
	{
	}

	public void pointerUp(int pointer_id, Vector2 pos)
	{
	}

	// When scrolling of mouse wheel is applied. This event is always delivered
	// to one under mouse, no matter if some Widget tries to steal focus.
	public void scrolled(int amount)
	{
	}

	// Repositions this Widget and all of its children, if needed. This may
	// ONLY be called by Widget itself through repositionChild or by Gui!
	public void repositionIfNeeded(float x, float y, float w, float h)
	{
		if (shrunken) {
			size.set(0, 0);
			reposition_needed = false;
			return;
		}
		
		// Apply margin
		x += margin;
		y += margin;
		w -= margin * 2;
		h -= margin * 2;

		// Get real dimensions
		float real_w, real_h;
		if (expanding_horiz == 0) real_w = getMinWidth() - margin*2;
		else real_w = w;
		if (expanding_vert == 0) real_h = getMinHeight(real_w) - margin*2;
		else real_h = h;

		// Calculate real position
		float real_x, real_y;
		if (expanding_horiz > 0 || align == Alignment.LEFT) real_x = x;
		else if (align == Alignment.RIGHT) real_x = x + (w - real_w);
		else real_x = x + (w - real_w) / 2f;
		if (expanding_vert > 0 || valign == Alignment.BOTTOM) real_y = y;
		else if (valign == Alignment.TOP) real_y = y + (h - real_h);
		else real_y = y + (h - real_h) / 2f;

		// If position or size differs, then reposition is needed
		if (real_x != pos.x || real_y != pos.y || real_w != size.x || real_h != size.y) {
			reposition_needed = true;
		}

		// Reposition only if needed
		if (!reposition_needed) {
			return;
		}

		pos.set(real_x, real_y);
		size.set(real_w, real_h);

		// Do actual repositioning
		reposition_needed = false;
		doRepositioning();

	}

	public void render(GL20 gl, SpriteBatch batch, int renderarea_pos_x, int renderarea_pos_y, int renderarea_width, int renderarea_height)
	{
		if (visible && !shrunken) {
			// Render this Widget
			doRendering(batch);
			
			// Check if this Widget limits rendering of its children
			boolean arealimit_was_just_set = false;
			if (children_arealimit) {
				// Check if this is first time limit is set
				if (renderarea_width < 0) {
					renderarea_pos_x = (int)(children_arealimit_pos.x + 0.5f);
					renderarea_pos_y = (int)(children_arealimit_pos.y + 0.5f);
					renderarea_width = (int)(children_arealimit_pos.x + children_arealimit_size.x + 0.5f) - renderarea_pos_x;
					renderarea_height = (int)(children_arealimit_pos.y + children_arealimit_size.y + 0.5f) - renderarea_pos_y;

					// Enable GL scissor test
					arealimit_was_just_set = true;
					batch.end();
					gl.glEnable(GL20.GL_SCISSOR_TEST);
					gl.glEnable(GL20.GL_SCISSOR_BOX);
					gl.glScissor(renderarea_pos_x, renderarea_pos_y, renderarea_width, renderarea_height);
					batch.begin();
				} else {
					boolean update_arealimit = false;

					int new_renderarea_pos_x = (int)(children_arealimit_pos.x + 0.5f);
					int new_renderarea_pos_y = (int)(children_arealimit_pos.y + 0.5f);
					int new_renderarea_width = (int)(children_arealimit_pos.x + children_arealimit_size.x + 0.5f) - renderarea_pos_x;
					int new_renderarea_height = (int)(children_arealimit_pos.y + children_arealimit_size.y + 0.5f) - renderarea_pos_y;
					
					// Check if left edge needs to be moved
					if (new_renderarea_pos_x > renderarea_pos_x) {
						renderarea_width -= (new_renderarea_pos_x - renderarea_pos_x); 
						renderarea_pos_x = new_renderarea_pos_x; 
						update_arealimit = true;
					}
					// Check if right edge needs to be moved
					if (new_renderarea_pos_x + new_renderarea_width < renderarea_pos_x + renderarea_width) {
						renderarea_width -= ((renderarea_pos_x + renderarea_width) - (new_renderarea_pos_x + new_renderarea_width)); 
						update_arealimit = true;
					}
					// Check if bottom edge needs to be moved
					if (new_renderarea_pos_y > renderarea_pos_y) {
						renderarea_height -= (new_renderarea_pos_y - renderarea_pos_y); 
						renderarea_pos_y = new_renderarea_pos_y; 
						update_arealimit = true;
					}
					// Check if top edge needs to be moved
					if (new_renderarea_pos_y + new_renderarea_height < renderarea_pos_y + renderarea_height) {
						renderarea_height -= ((renderarea_pos_y + renderarea_height) - (new_renderarea_pos_y + new_renderarea_height)); 
						update_arealimit = true;
					}

					if (update_arealimit) {
						batch.end();
						gl.glScissor(renderarea_pos_x, renderarea_pos_y, renderarea_width, renderarea_height);
						batch.begin();
					}
				}				
			}
			
			// Render children
			Widget[] children_buf = children.items;
			int children_size = children.size;
			for (int child_id = 0; child_id < children_size; child_id++) {
				Widget child = children_buf[child_id];
				child.render(gl, batch, renderarea_pos_x, renderarea_pos_y, renderarea_width, renderarea_height);
			}
			
			// Clear possible arealimit
			if (arealimit_was_just_set) {
				batch.end();
				gl.glDisable(GL20.GL_SCISSOR_TEST);
				gl.glDisable(GL20.GL_SCISSOR_BOX);
				batch.begin();
			}
		}
	}

	// Returns true if position is over Widget. Position is relative to Widget
	// and over the rectangle of Widget, so this function is for Widgets that
	// are not rectangle shaped.
	public boolean isOver(float x, float y)
	{
		return true;
	}

	protected abstract void doRendering(SpriteBatch batch);

	protected void repositionChild(Widget child, float x, float y, float width, float height)
	{
		assert children.contains(child, true);
		child.repositionIfNeeded(x, y, width, height);
	}

	// Dimension getters
	public float getPositionX() { return pos.x; }
	public float getPositionY() { return pos.y; }
	public float getCenterX() { return pos.x + size.x / 2; }
	public float getCenterY() { return pos.y + size.y / 2; }
	public float getEndX() { return pos.x + size.x; }
	public float getEndY() { return pos.y + size.y; }
	public float getWidth() { return size.x; }
	public float getHeight() { return size.y; }

	// This may be only called by Gui and Widget!
	public void setGui(Gui gui)
	{
		// If there is old Gui, then inform it that this Widget is being
		// removed. This will unregister all pointer listenings, etc.
		if (this.gui != null) {
			this.gui.widgetRemoved(this);
		}
		// Set new Gui
		this.gui = gui;
		// Set new Gui also for children
		Widget[] children_buf = children.items;
		int children_size = children.size;
		for (int child_id = 0; child_id < children_size; child_id++) {
			Widget child = children_buf[child_id];
			child.setGui(gui);
		}
	}

	protected void fireEvent()
	{
		if (eventlistener != null) {
			eventlistener.handleGuiEvent(GuiEvent.fromWidget(this, 0));
		}
	}

	protected void fireEvent(int action)
	{
		if (eventlistener != null) {
			eventlistener.handleGuiEvent(GuiEvent.fromWidget(this, action));
		}
	}

	protected void markToNeedReposition()
	{
		reposition_needed = true;
		if (parent != null) {
			parent.markToNeedReposition();
		}
	}

	protected float getMinWidth()
	{
		if (shrunken) return 0;
		return margin * 2 + Math.max(fixed_min_width, doGetMinWidth());
	}

	protected float getMinHeight(float width)
	{
		if (shrunken) return 0;
		return margin * 2 + Math.max(fixed_min_height, doGetMinHeight(width - margin * 2));
	}

	protected int getHorizontalExpandingForRepositioning()
	{
		if (shrunken) return 0; 
		return expanding_horiz;
	}

	protected int getVerticalExpandingForRepositioning()
	{
		if (shrunken) return 0;
		return expanding_vert;
	}

	protected abstract float doGetMinWidth();

	protected abstract float doGetMinHeight(float width);

	protected void addChild(Widget widget)
	{
		children.add(widget);
		widget.parent = this;
		widget.setGui(gui);
	}

	protected void removeChild(Widget widget)
	{
		boolean removed = children.removeValue(widget, true);
		assert removed;
		widget.parent = null;
		widget.setGui(null);
	}

	protected boolean pointerOver(int pointer_id)
	{
		return gui.getWidgetUnderPointer(pointer_id) == this;
	}

	protected void unregisterPointerListener(int pointer_id)
	{
		// If this Widget is removed from Gui, then do nothing
		if (gui == null) {
			return;
		}
		// TODO: In future, make pointer listening to be started with function!
		assert gui.getPointerListener(pointer_id) == this;
		gui.unregisterPointerListener(pointer_id);
	}

	// Starts listening of keyboard events. If some other Widget was
	// listening for them, then the listening is stolen from it.
	protected void startListeningOfKeyboard()
	{
		gui.setKeyboardListener(this);
	}

	// Stops listening of keyboard events. If this widget
	// was not the keyboard listener, then nothing is done.
	protected void stopListeningOfKeyboard()
	{
		if (gui.getKeyboardListener() == this) {
			gui.setKeyboardListener(null);
		}
	}

	protected boolean listeningKeyboard()
	{
		return gui.getKeyboardListener() == this;
	}

	// Stops listening of keyboard from any Widget.
	protected void clearKeyboardListener()
	{
		gui.setKeyboardListener(null);
	}

	protected void doRepositioning()
	{
	}

	// Enables limit of rendering and pointing events for
	// all children and grandchildren of this Widget.
// TODO: This does not affect to pointing events yet!
	protected void enableArealimitForChildren(float pos_x, float pos_y, float width, float height)
	{
		children_arealimit = true;
		if (children_arealimit_pos == null) {
			children_arealimit_pos = new Vector2();
			children_arealimit_size = new Vector2();
		}

		children_arealimit_pos.set(pos_x, pos_y);
		children_arealimit_size.set(width, height);
	}

	protected void disableArealimitForChildren()
	{
		children_arealimit = false;
	}

	protected void generateDragEventToChildren(Widget children_begin, int pointer_id, Vector2 down_pos, Vector2 up_pos)
	{
		if (!children.contains(children_begin, true)) {
			throw new RuntimeException("Not my child!");
		}
		gui.generateDragEventToWidgets(children_begin, pointer_id, down_pos, up_pos);
	}

	// Renders region
	// TODO: Code own class for these Widget rendering helpers!
	protected static void renderHorizontalBar(SpriteBatch batch, AtlasRegion region_left, float region_left_realwidth, AtlasRegion region_right, float region_right_realwidth, Texture tex_center, float realheight, float x, float y, float width, float height, float scale)
	{
		float left_end_width = region_left_realwidth * scale;
		float right_end_width = region_right_realwidth * scale;
		float center_width = width - left_end_width - right_end_width;

		if (center_width > 0) {
			float center_vert_padding = ((tex_center.getHeight() - realheight) / 2) * scale;
			batch.draw(tex_center, x + left_end_width, y - center_vert_padding, center_width, tex_center.getHeight() * scale, 0, 1, center_width / tex_center.getWidth(), 0);
		}
		render(batch, region_left, x + (region_left_realwidth - region_left.originalWidth) * scale, y + (realheight - region_left.originalHeight) / 2 * scale, scale);
		render(batch, region_right, x + width - right_end_width, y + (realheight - region_right.originalHeight) / 2 * scale, scale);
	}

	protected static void render(SpriteBatch batch, AtlasRegion region, float x, float y, float scale)
	{
		batch.draw(region, x + region.offsetX * scale, y + region.offsetY * scale, region.packedWidth * scale, region.packedHeight * scale);
	}

	protected static void renderFromCenter(SpriteBatch batch, AtlasRegion region, float x, float y, float scale)
	{
		batch.draw(region, x + (region.offsetX - region.originalWidth * 0.5f) * scale, y + (region.offsetY - region.originalHeight * 0.5f) * scale, region.packedWidth * scale, region.packedHeight * scale);
	}

	// Renders region with angle
	protected static void renderFromCenter(SpriteBatch batch, AtlasRegion region, float x, float y, float scale, float angle)
	{
		float draw_x = x + (region.offsetX - region.originalWidth * 0.5f) * scale;
		float draw_y = y + (region.offsetY - region.originalHeight * 0.5f) * scale;
		batch.draw(region, draw_x, draw_y, region.getRegionWidth() / 2 * scale, region.getRegionHeight() / 2 * scale, region.packedWidth * scale, region.packedHeight * scale, 1, 1, angle);
	}

	protected static void renderToSpace(SpriteBatch batch, AtlasRegion region, float x, float y, float space_width, float space_height, float align_x, float align_y, float scale)
	{
		float extra_x = space_width - region.originalWidth * scale;
		float extra_y = space_height - region.originalHeight * scale;
		render(batch, region, x + extra_x * align_x, y + extra_y * align_y, scale);
	}
	
	protected static void renderAndRepeatTexture(SpriteBatch batch, Texture tex, float x, float y, float width, float height, float scale)
	{
		float tex_x = width / tex.getWidth() / scale;
		float tex_y = height / tex.getHeight() / scale;
		batch.draw(tex, x, y, width, height, 0, tex_y, tex_x, 0);
	}

	// Spawns gray version of given color
	protected static Color getDisabledColor(Color color)
	{
		if (color == null) {
			return null;
		}
		float lightness = 0.8f;
		float color_value = (color.r + color.g + color.b) / 3;
		Color result = new Color(color_value * lightness, color_value * lightness, color_value * lightness, 1);
		return result;
	}

	private Gui gui;
	private Widget parent;

	private Array<Widget> children = new Array<Widget>(true, 0, Widget.class);

	private boolean visible;
	private boolean shrunken;
	private boolean pointerevents_enabled;
	private boolean be_topmost_before_children;

	private int expanding_horiz, expanding_vert;
	private Alignment align, valign;
	private float fixed_min_width, fixed_min_height;
	private float margin;

	private Vector2 pos;
	private Vector2 size;

	private boolean reposition_needed;

	private Eventlistener eventlistener;

	private boolean children_arealimit = false;
	private Vector2 children_arealimit_pos = null;
	private Vector2 children_arealimit_size = null;

}
