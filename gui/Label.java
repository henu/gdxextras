package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Label extends Widget
{
	public static void setDefaultStyle(LabelStyle style)
	{
		default_style = style;
	}

	public Label()
	{
		super();
		text = "";
		text_align = Alignment.LEFT;
		multiline = false;
	}

	public void setText(String text)
	{
		this.text = text;
		markToNeedReposition();
	}

	public void setTextAlignment(Alignment align)
	{
		text_align = align;
	}

	public void setMultiline(boolean multiline)
	{
		if (this.multiline != multiline) {
			markToNeedReposition();
		}
		this.multiline = multiline;
	}

	public void setStyle(LabelStyle style)
	{
		this.style = style;
	}

	protected void doRendering(SpriteBatch batch)
	{
		// Get font and scale it correctly
		BitmapFont font = getStyle().font;
		font.setScale(1);
		float lineheight = 	font.getLineHeight();
		font.setScale(getStyle().height / lineheight);

		if (!multiline) {
			if (getStyle().shadow != null) {
				renderLineWithAlignment(batch, text, font, getPositionX() + getStyle().shadow.x, getPositionY() + getStyle().shadow.y, getWidth(), Color.BLACK);
			}
			renderLineWithAlignment(batch, text, font, getPositionX(), getPositionY(), getWidth(), getStyle().color);
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
			float draw_y = getPositionY() + (lines.size - 1) * getStyle().height;
			for (int line = 0; line < lines.size; ++ line) {
				if (getStyle().shadow != null) {
					renderLineWithAlignment(batch, lines.get(line), font, getPositionX() + getStyle().shadow.x, draw_y + getStyle().shadow.y, getWidth(), Color.BLACK);
				}
				renderLineWithAlignment(batch, lines.get(line), font, getPositionX(), draw_y, getWidth(), getStyle().color);
				draw_y -= getStyle().height;
			}
		}
	}

	protected float doGetMinWidth()
	{
		// Get font and scale it correctly
		BitmapFont font = getStyle().font;
		font.setScale(1);
		float lineheight = 	font.getLineHeight();
		font.setScale(getStyle().height / lineheight);

		// Calculate minimum width
		if (!multiline) {
			TextBounds bounds = font.getBounds(text);
			return bounds.width;
		} else {
			float result = 0;
			String word = "";
			for (int text_idx = 0; text_idx < text.length(); text_idx ++) {
				char c = text.charAt(text_idx);
				if (c == ' ' || c == '\t' || c == '\n') {
					TextBounds bounds = font.getBounds(word);
					result = Math.max(result, bounds.width);
					word = "";
				} else {
					word += c;
				}
			}
			TextBounds bounds = font.getBounds(word);
			result = Math.max(result, bounds.width);
			return result;
		}
	}

	protected float doGetMinHeight(float width)
	{
		if (!multiline) {
			return getStyle().height;
		} else {
			// Get font and scale it correctly
			BitmapFont font = getStyle().font;
			font.setScale(1);
			float lineheight = 	font.getLineHeight();
			font.setScale(getStyle().height / lineheight);

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
			return lines * getStyle().height;
		}
	}

	private static LabelStyle default_style;

	private String text;
	private Alignment text_align;

	// This also tells if multiple lines are supported. Null means not.
	private boolean multiline;

	private LabelStyle style;

	private void renderLineWithAlignment(SpriteBatch batch, String text, BitmapFont font, float pos_x, float pos_y, float width, Color color)
	{
		// Set color
		font.setColor(color);

		float text_width = font.getBounds(text).width;
		if (text_align == Alignment.LEFT) {
			font.draw(batch, text, pos_x, pos_y + getStyle().height);
		} else {
			float extra_space = width - text_width;
			if (text_align == Alignment.RIGHT) {
				font.draw(batch, text, pos_x + extra_space, pos_y + getStyle().height);
			} else {
				font.draw(batch, text, pos_x + extra_space / 2, pos_y + getStyle().height);
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
				if (word.length() > 0) {
					// If word fits to the line
					if (font.getBounds(line + whitespace + word).width <= width) {
						line += whitespace + word;
					}
					// If word does not fit and it's the only word
					else if (line.length() == 0) {
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
							float width_now = font.getBounds(good_length_half_substr).width;
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
