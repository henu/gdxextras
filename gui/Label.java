package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class Label extends Widget
{
	public static void setDefaultStyle(LabelStyle style)
	{
		default_style = style;
	}

	public static LabelStyle getDefaultStyle()
	{
		return default_style;
	}

	public Label()
	{
		super();
		text = "";
		text_align = Alignment.LEFT;
		multiline = false;
		text_layout_height = -1;
	}

	public void setText(String text)
	{
		this.text = text;
		text_layout = null;
		text_layout_height = -1;
		markToNeedReposition();
	}

	public String getText()
	{
		return text;
	}

	public void setTextAlignment(Alignment align)
	{
		text_align = align;
		text_layout = null;
		text_layout_height = -1;
	}

	public void setMultiline(boolean multiline)
	{
		if (this.multiline != multiline) {
			markToNeedReposition();
		}
		this.multiline = multiline;
		text_layout = null;
		text_layout_height = -1;
	}

	public void setStyle(LabelStyle style)
	{
		this.style = style;
	}

	public void setEventListener(Eventlistener event_listener)
	{
		this.event_listener = event_listener;
	}

	@Override
	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (event_listener != null) {
			event_listener.handleGuiEvent(GuiEvent.fromWidget(this, 0));
		}
		return false;
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		// Get font and scale it correctly
		BitmapFont font = getStyle().getFont();
		font.getData().setScale(1);
		float lineheight = font.getLineHeight();
		font.getData().setScale(getStyle().getHeight() / lineheight);

		if (!multiline) {
			if (getStyle().getShadow() != null && getStyle().getShadow().x != 0 && getStyle().getShadow().y != 0) {
				renderLineWithAlignment(batch, text, font, getPositionX() + getStyle().getShadow().x, getPositionY() + getStyle().getShadow().y, getWidth(), Color.BLACK);
			}
			renderLineWithAlignment(batch, text, font, getPositionX(), getPositionY(), getWidth(), getStyle().getColor());
		} else {
			// Read all lines first
			Array<String> lines = new Array<String>(true, 0, String.class);
			int text_ofs = 0;
			while (text_ofs < text.length()) {
				// Read one line and update counters
				String line = extractLine(text, text_ofs, font, getWidth());
				text_ofs += line.length();
				lines.add(line);
				// Skip whitespace after the line
				while (text_ofs < text.length() && (text.charAt(text_ofs) == ' ' || text.charAt(text_ofs) == '\t' || text.charAt(text_ofs) == '\n')) {
					boolean is_endline = (text.charAt(text_ofs) == '\n');
					++ text_ofs;
					if (is_endline) {
						break;
					}
				}
			}
			// Now render lines
			float draw_y = getPositionY() + (lines.size - 1) * getStyle().getHeight();
			for (int line = 0; line < lines.size; ++ line) {
				if (getStyle().getShadow() != null && getStyle().getShadow().x != 0 && getStyle().getShadow().y != 0) {
					renderLineWithAlignment(batch, lines.get(line), font, getPositionX() + getStyle().getShadow().x, draw_y + getStyle().getShadow().y, getWidth(), Color.BLACK);
				}
				renderLineWithAlignment(batch, lines.get(line), font, getPositionX(), draw_y, getWidth(), getStyle().getColor());
				draw_y -= getStyle().getHeight();
			}
		}
	}

	protected float doGetMinWidth()
	{
		// Get font and scale it correctly
		BitmapFont font = getStyle().getFont();
		font.getData().setScale(1);
		float lineheight = font.getLineHeight();
		font.getData().setScale(getStyle().getHeight() / lineheight);

		// Calculate minimum width
		if (!multiline) {
			if (text_layout == null || text_layout_height != getStyle().getHeight()) {
				text_layout = new GlyphLayout(font, text);
				text_layout_height = getStyle().getHeight();
			}
			return text_layout.width;
		} else {
			float result = 0;
			GlyphLayout letter_layout;
			for (int text_idx = 0; text_idx < text.length(); text_idx ++) {
				char c = text.charAt(text_idx);
				if (c != '\t' && c != '\n') {
					letter_layout = new GlyphLayout(font, "" + c);
					result = Math.max(result, letter_layout.width);
				}
			}
			return result;
		}
	}

	protected float doGetMinHeight(float width)
	{
		if (!multiline) {
			return getStyle().getHeight();
		} else {
			// Get font and scale it correctly
			BitmapFont font = getStyle().getFont();
			font.getData().setScale(1);
			float lineheight = font.getLineHeight();
			font.getData().setScale(getStyle().getHeight() / lineheight);

			int text_ofs = 0;
			int lines = 0;
			while (text_ofs < text.length()) {
				// Read one line and update counters
				String line = extractLine(text, text_ofs, font, width);
				text_ofs += line.length();
				++ lines;
				// Skip whitespace after the line
				while (text_ofs < text.length() && (text.charAt(text_ofs) == ' ' || text.charAt(text_ofs) == '\t' || text.charAt(text_ofs) == '\n')) {
					boolean is_endline = (text.charAt(text_ofs) == '\n');
					++ text_ofs;
					if (is_endline) {
						break;
					}
				}
			}
			return lines * getStyle().getHeight();
		}
	}

	private static LabelStyle default_style;

	private String text;
	private Alignment text_align;
	private GlyphLayout text_layout;
	private float text_layout_height;

	// This also tells if multiple lines are supported. Null means not.
	private boolean multiline;

	private LabelStyle style;

	private Eventlistener event_listener;

	private void renderLineWithAlignment(SpriteBatch batch, String text, BitmapFont font, float pos_x, float pos_y, float width, Color color)
	{
// TODO: Do not allocate this at every call!
text_layout = new GlyphLayout(font, text, color, 0, Align.left, false);

		float text_width = text_layout.width;
		if (text_align == Alignment.LEFT) {
			font.draw(batch, text_layout, pos_x, pos_y + getStyle().getHeight());
		} else {
			float extra_space = width - text_width;
			if (text_align == Alignment.RIGHT) {
				font.draw(batch, text_layout, pos_x + extra_space, pos_y + getStyle().getHeight());
			} else {
				font.draw(batch, text_layout, pos_x + extra_space / 2, pos_y + getStyle().getHeight());
			}
		}
	}

	// Extracts one line that fits to given width. Whitespace after the
	// line is not included, not even the possible endline. If first
	// word does not fit to line, then substring of it is returned.
	private static String extractLine(String text, int text_ofs, BitmapFont font, float width)
	{
		String line = "";
		String word = "";
		String whitespace = "";
		while (true) {
			char c;
			if (text_ofs < text.length()) {
				c = text.charAt(text_ofs);
			} else {
				c = '\0';
			}
			// Whitespace or end of input
			if (c == ' ' || c == '\t' || c == '\n' || text_ofs >= text.length()) {
				// If there is word
				if (!word.isEmpty()) {
					// If word fits to the line
					GlyphLayout temp_layout = new GlyphLayout(font, line + whitespace + word);
					if (temp_layout.width <= width) {
						line += whitespace + word;
					}
					// If word does not fit and it's the only word
					else if (line.isEmpty()) {
						// Word is so long, that it does not fit to one line. We
						// need to cut it. Use binary search to find good length
						// quickly. Also include possible whitespace to the line.
						line = whitespace + word;
						int good_length_min = 0;
						int good_length_max = line.length();
						// "good_length_min" might be the correct answer,
						// while "good_length_max" is always too long.
						while (good_length_max - good_length_min > 1) {
							int good_length_half = (good_length_min + good_length_max) / 2;
							String good_length_half_substr = line.substring(0, good_length_half);
							temp_layout = new GlyphLayout(font, good_length_half_substr);
							float width_now = temp_layout.width;
							if (width_now == width) {
								return good_length_half_substr;
							} else if (width_now < width) {
								good_length_min = good_length_half;
							} else {
								good_length_max = good_length_half;
							}
						}
						// Ensure at least first letter of word is got
						good_length_min = Math.max(good_length_min, 1);
						return line.substring(0, good_length_min);
					}
					// Word does not fit, and there is
					// already other stuff on the line
					else {
						return line;
					}
					whitespace = "";
					word = "";
				}

				// If whitespace was line break or if end was
				// reached, then stop and return current line
				if (c == '\n' || text_ofs >= text.length()) {
					return line;
				}

				whitespace += c;
			} else {
				word += c;
			}
			++ text_ofs;
		}
	}

	private LabelStyle getStyle()
	{
		if (style != null) {
			return style;
		}
		return default_style;
	}
}
