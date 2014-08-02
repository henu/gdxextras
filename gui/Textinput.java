package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import fi.henu.gdxextras.Font;

public class Textinput extends Widget
{
// TODO: BUG! If initial text is set, it is not shown until user changes it!

	// Event types
	// TODO: Tab is so rare in mobile devices, so not not support it!
	public static final int RETURN_PRESSED = 0;
	public static final int TAB_PRESSED = 1;
	public static final int TEXT_CHANGED = 2;

	public Textinput(float height, Font font)
	{
		super();
		
		scrollbox = new Scrollbox();
		scrollbox.setHorizontalExpanding(1);
		scrollbox.setFixedMinimumHeight(height);

		content = new Content(this, scrollbox, height, font);
		content.setHorizontalAlignment(Alignment.LEFT);
		scrollbox.setWidget(content);
		
		addChild(scrollbox);
		markToNeedReposition();

		// By default, this Widget does not receive pointer
		// events. It lets its Content to handle them.
		setPointerEvents(false);
	}

	public void setText(String text)
	{
		content.text = text;
		if (content.password_text != null) {
			content.updatePasswordText();
		}
		content.cursor = text.length();
		content.scrollSoCursorIsShown();
	}

	public String getText()
	{
		return content.text;
	}

	public void setColor(Color color)
	{
		content.color.set(color);
	}

	public void setPassword(boolean password)
	{
		if (password) {
			if (content.password_text == null) {
				content.password_text = "";
				content.updatePasswordText();
			}
		} else {
			content.password_text = null;
		}
	}

	public void setActive(boolean active)
	{
		if (active) {
			content.startListeningOfKeyboard();
		} else {
			content.stopListeningOfKeyboard();
		}
	}

	// Give null to disable shadow
	public void setShadow(Vector2 shadow)
	{
		if (shadow == null) {
			content.shadow = null;
		} else {
			content.shadow = new Vector2(shadow);
		}
	}

	protected void doRendering(SpriteBatch batch)
	{
		// Invisible Widget
	}

	protected void doRepositioning()
	{
		content.setFixedMinimumWidth(getWidth());
		repositionChild(scrollbox, getPositionX(), getPositionY(), getWidth(), getHeight());
	}

	protected float doGetMinWidth()
	{
		return 0;
	}

	protected float doGetMinHeight(float width)
	{
		return content.height;
	}
	
	private class Content extends Widget
	{
		public Content(Textinput textinput, Scrollbox scrollbox, float height, Font font)
		{
			this.textinput = textinput;
			this.scrollbox = scrollbox;
			
			text = "";
			cursor = 0;
			password_text = null;
			this.height = height;
			this.font = font;
			color = new Color(1, 1, 1, 1);
			shadow = null;
		}

		public void scrollSoCursorIsShown()
		{
			// Get cursor specs in pixels
			float cursor_pos_x_px;
			if (password_text == null) {
				cursor_pos_x_px = font.getStringWidth(text.substring(0, cursor), height);
			} else {
				cursor_pos_x_px = font.getStringWidth("*", height) * cursor;
			}
			float cursor_width_px = font.getStringWidth("_", height);
			
			if (cursor_pos_x_px < scrollbox.getScrollX()) {
				scrollbox.setScrollX(cursor_pos_x_px);
			} else if (cursor_pos_x_px + cursor_width_px > scrollbox.getWidth() + scrollbox.getScrollX()) {
				scrollbox.setScrollX(cursor_pos_x_px + cursor_width_px - scrollbox.getWidth());
			} else {
				float text_width_px;
				if (password_text == null) {
					text_width_px = font.getStringWidth(text, height);
				} else {
					text_width_px = font.getStringWidth("*", height) * text.length();
				}
				float text_and_cursor_width_px = Math.max(text_width_px, cursor_pos_x_px + cursor_width_px);
				if (scrollbox.getWidth() + scrollbox.getScrollX() > text_and_cursor_width_px) {
					scrollbox.setScrollX(Math.max(0, text_and_cursor_width_px - scrollbox.getWidth()));
				}
			}
		}

		public void keyTyped(char character)
		{
			// Backspace
			if (character == 0x08) {
				if (cursor > 0) {
					text = text.substring(0, cursor - 1) + text.substring(cursor);
					cursor--;
					scrollSoCursorIsShown();
					textinput.fireEvent(TEXT_CHANGED);
				}
			}
			// Return
			else if (character == 0x0d || character == 0x0a) {
				textinput.fireEvent(RETURN_PRESSED);
			}
			// Delete
			else if (character == 0x7f) {
				if (cursor < text.length()) {
					text = text.substring(0, cursor) + text.substring(cursor + 1);
					scrollSoCursorIsShown();
					textinput.fireEvent(TEXT_CHANGED);
				}
			}
			// Tab
			else if (character == 0x09) {
				textinput.fireEvent(TAB_PRESSED);
			}
			// Special key
			else if (character == 0x00) {
			}
			// Normal key
			else {
				text = text.substring(0, cursor) + character + text.substring(cursor);
				cursor ++;
				scrollSoCursorIsShown();
				textinput.fireEvent(TEXT_CHANGED);
			}
			if (password_text != null) {
				updatePasswordText();
			}
		}

		public boolean pointerDown(int pointer_id, Vector2 pos)
		{
			String text_check;
			if (password_text == null) {
				text_check = text;
			} else {
				text_check = password_text;
			}
			if (text_check.isEmpty()) {
				cursor = 0;
			} else {
				// Find correct cursor position with binary search
// TODO: This bugs a little bit!
				float cursor_x = pos.x - getPositionX();
				int search_begin = 0;
				int search_end = text_check.length();
				float search_begin_x = 0;
				do {
					int halfpos = (search_begin + search_end) / 2;
					String part1 = text_check.substring(search_begin, halfpos);
					String part2 = text_check.substring(halfpos, search_end);
					float part1_w = font.getStringWidth(part1, height);
					float part2_w = font.getStringWidth(part2, height);
					// Halve. If some of parts was only one character long,
					// then it can decide where cursor should be located.
					if (cursor_x < search_begin_x + part1_w) {
						if (part1.length() == 1) {
							if (cursor_x - search_begin_x < part1_w / 2) {
								cursor = search_begin;
							} else {
								assert search_begin + 1 == halfpos;
								cursor = halfpos;
							}
							break;
						}
						search_end = halfpos;
					} else {
						if (part2.length() == 1) {
							if (cursor_x - search_begin_x - part1_w < part2_w / 2) {
								cursor = halfpos;
							} else {
								assert halfpos + 1 == search_end;
								cursor = search_end;
							}
							break;
						}
						search_begin_x += part1_w;
						search_begin = halfpos;
					}
				} while (true);
			}
			
			scrollSoCursorIsShown();

			startListeningOfKeyboard();
			return false;
		}

		protected void doRendering(SpriteBatch batch)
		{
			String text_to_render;
			if (password_text == null) {
				text_to_render = text;
			} else {
				text_to_render = password_text;
			}
			if (shadow != null) {
				font.renderString(batch, text_to_render, getPositionX() + shadow.x, getPositionY() + height + shadow.y, height, Color.BLACK);
				if (listeningKeyboard()) {
					float cursor_x = font.getStringWidth(text_to_render.substring(0, cursor), height);
					font.renderString(batch, "_", getPositionX() + cursor_x + shadow.x, getPositionY() + height + shadow.y, height, Color.BLACK);
				}
			}
			font.renderString(batch, text_to_render, getPositionX(), getPositionY() + height, height, color);
			if (listeningKeyboard()) {
				float cursor_x = font.getStringWidth(text_to_render.substring(0, cursor), height);
				font.renderString(batch, "_", getPositionX() + cursor_x, getPositionY() + height, height, color);
			}
		}

		protected float doGetMinWidth()
		{
			if (password_text != null) {
				return font.getStringWidth("*", height) * password_text.length() + font.getStringWidth("_", height);
			}
			return font.getStringWidth(text + "_", height);
		}

		protected float doGetMinHeight(float width)
		{
			return height;
		}
		
		public void updatePasswordText()
		{
			assert password_text != null;
			if (text.length() == 0) {
				password_text = "";
			} else {
				password_text = String.format("%" + text.length() + "s", "").replace(' ', '*');
			}
		}

		public Textinput textinput;
		public Scrollbox scrollbox;

		public String text;
		public int cursor;
		public String password_text;
		public float height;
		public Font font;
		public Color color;
		private Vector2 shadow;

	}
	
	private Scrollbox scrollbox;
	Content content;

}
