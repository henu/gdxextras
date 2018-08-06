package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class Textinput extends Widget
{
	// TODO: BUG! If initial text is set, it is not shown until user changes it!
	// This is used to set default style for Textinput
	public static void setDefaultStyle(TextinputStyle style)
	{
		default_style = style;
	}

	// Event types
	// TODO: Tab is so rare in mobile devices, so not not support it!
	public static final int RETURN_PRESSED = 0;
	public static final int TAB_PRESSED = 1;
	public static final int TEXT_CHANGED = 2;

	public Textinput()
	{
		super();

		scrollbox = new Scrollbox();
		scrollbox.setHorizontalExpanding(1);
		scrollbox.enableVerticalScrolling(false);

		content = new Content(this, scrollbox);
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
		content.text_layout = null;
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

	public void setMaxLength(int max_length)
	{
		this.max_length = max_length;
	}

	public void setActive(boolean active)
	{
		if (active) {
			content.startListeningOfKeyboard();
		} else {
			content.stopListeningOfKeyboard();
		}
	}

	public void setEnabled(boolean enabled)
	{
		content.stopListeningOfKeyboard();
		content.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return content.enabled;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
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
		return content.doGetMinHeight(0);
	}

	private class Content extends Widget
	{
		public Content(Textinput textinput, Scrollbox scrollbox)
		{
			this.textinput = textinput;
			this.scrollbox = scrollbox;

			text = "";
			cursor = 0;
			password_text = null;

			enabled = true;
		}

		public void scrollSoCursorIsShown()
		{
			GlyphLayout temp_layout;

			BitmapFont font = getStyle().font;
			font.getData().setScale(getStyle().scaling);

			// Get cursor specs in pixels
			float cursor_pos_x_px;
			if (password_text == null) {
				temp_layout = new GlyphLayout(font, text.substring(0, cursor));
				cursor_pos_x_px = temp_layout.width;
			} else {
				temp_layout = new GlyphLayout(font, "*");
				cursor_pos_x_px = temp_layout.width * cursor;
			}
			temp_layout = new GlyphLayout(font, "_");
			float cursor_width_px = temp_layout.width;

			if (cursor_pos_x_px < scrollbox.getScrollX()) {
				scrollbox.setScrollX(cursor_pos_x_px);
			} else if (cursor_pos_x_px + cursor_width_px > scrollbox.getWidth() + scrollbox.getScrollX()) {
				scrollbox.setScrollX(cursor_pos_x_px + cursor_width_px - scrollbox.getWidth());
			} else {
				float text_width_px;
				if (password_text == null) {
					if (text_layout == null) {
						text_layout = new GlyphLayout(font, text);
					}
					text_width_px = text_layout.width;
				} else {
					temp_layout = new GlyphLayout(font, "*");
					text_width_px = temp_layout.width * text.length();
				}
				float text_and_cursor_width_px = Math.max(text_width_px, cursor_pos_x_px + cursor_width_px);
				if (scrollbox.getWidth() + scrollbox.getScrollX() > text_and_cursor_width_px) {
					scrollbox.setScrollX(Math.max(0, text_and_cursor_width_px - scrollbox.getWidth()));
				}
			}
		}

		public void keyTyped(char character)
		{
			if (!enabled) {
				return;
			}

			// Backspace
			if (character == 0x08) {
				if (cursor > 0) {
					text = text.substring(0, cursor - 1) + text.substring(cursor);
					text_layout = null;
					-- cursor;
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
					text_layout = null;
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
				// If maximum length is reached, then do nothing
				if (max_length > 0 && text.length() >= max_length) {
					return;
				}

				text = text.substring(0, cursor) + character + text.substring(cursor);
				text_layout = null;
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
			if (!enabled) {
				return false;
			}

			BitmapFont font = getStyle().font;
			font.getData().setScale(getStyle().scaling);

			String text_check;
			if (password_text == null) {
				text_check = text;
			} else {
				text_check = password_text;
			}
			if (text_check.length() == 0) {
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
					GlyphLayout part1_layout = new GlyphLayout(font, part1);
					GlyphLayout part2_layout = new GlyphLayout(font, part2);
					float part1_w = part1_layout.width;
					float part2_w = part2_layout.width;
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

		@Override
		protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
		{
			BitmapFont font = getStyle().font;
			font.getData().setScale(1);
			float height = font.getLineHeight() * getStyle().scaling;
			font.getData().setScale(getStyle().scaling);
			Vector2 shadow = getStyle().shadow;

			String text_to_render;
			if (password_text == null) {
				text_to_render = text;
			} else {
				text_to_render = password_text;
			}
			if (shadow != null) {
// TODO: Do not allocate this at every frame!
GlyphLayout temp_layout = new GlyphLayout(font, text_to_render, Color.BLACK, 0, Align.left, false);
				font.draw(batch, temp_layout, getPositionX() + shadow.x, getPositionY() + height + shadow.y);
				if (listeningKeyboard()) {
// TODO: Do not allocate this at every frame!
temp_layout = new GlyphLayout(font, text_to_render.substring(0, cursor));
					float cursor_x = temp_layout.width;
temp_layout = new GlyphLayout(font, "_", Color.BLACK, 0, Align.left, false);
					font.draw(batch, temp_layout, getPositionX() + cursor_x + shadow.x, getPositionY() + height + shadow.y);
				}
			}
// TODO: Do not allocate this at every frame!
GlyphLayout temp_layout = new GlyphLayout(font, text_to_render, getStyle().color, 0, Align.left, false);
			font.draw(batch, temp_layout, getPositionX(), getPositionY() + height);
			if (listeningKeyboard()) {
// TODO: Do not allocate this at every frame!
temp_layout = new GlyphLayout(font, text_to_render.substring(0, cursor));
				float cursor_x = temp_layout.width;
temp_layout = new GlyphLayout(font, "_", getStyle().color, 0, Align.left, false);
				font.draw(batch, temp_layout, getPositionX() + cursor_x, getPositionY() + height);
			}
		}

		protected float doGetMinWidth()
		{
			BitmapFont font = getStyle().font;
			font.getData().setScale(getStyle().scaling);

			GlyphLayout temp_layout1 = new GlyphLayout(font, "_");

			if (password_text != null) {
				GlyphLayout temp_layout2 = new GlyphLayout(font, "*");
				return temp_layout2.width * password_text.length() + temp_layout1.width;
			}
			return temp_layout1.width;
		}

		protected float doGetMinHeight(float width)
		{
			BitmapFont font = getStyle().font;
			font.getData().setScale(getStyle().scaling);

			TextinputStyle textinput_style = getStyle();
			return textinput_style.font.getLineHeight();
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
		public GlyphLayout text_layout;
		public int cursor;
		public String password_text;

		public boolean enabled;
	}

	private static TextinputStyle default_style;

	private Scrollbox scrollbox;
	Content content;

	private int max_length;

	private TextinputStyle getStyle()
	{
		return default_style;
	}
}
