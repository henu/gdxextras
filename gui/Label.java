package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import fi.henu.gdxextras.Font;

public class Label extends Widget
{

	public Label(float height, Font font)
	{
		super();
		text = "";
		text_total_width = 0;
		this.height = height;
		this.font = font;
		color = new Color(1, 1, 1, 1);
		text_align = Alignment.LEFT;
		multiline = null;
		shadow = null;
	}

	public void setText(String text)
	{
		this.text = text;
		text_total_width = font.getStringWidth(text, height);
		markToNeedReposition();
	}
	
	public void setColor(Color color)
	{
		this.color.set(color);
	}

	public void setTextAlignment(Alignment align)
	{
		text_align = align;
	}
	
	public void setMultiline(boolean multiline)
	{
		if (multiline) {
			if (this.multiline == null) {
				this.multiline = new Array<String>();
				markToNeedReposition();
			}
		} else {
			if (this.multiline != null) {
				this.multiline = null;
				markToNeedReposition();
			}
		}
	}
	
	// Give null to disable shadow
	public void setShadow(Vector2 shadow)
	{
		if (shadow == null) {
			this.shadow = null;
		} else {
			this.shadow = new Vector2(shadow);
		}
	}

	protected void doRendering(SpriteBatch batch)
	{
		if (multiline == null) {
			if (shadow != null) {
				renderLineWithAlignment(batch, text, text_total_width, getPositionX() + shadow.x, getPositionY() + shadow.y, getWidth(), Color.BLACK);
			}
			renderLineWithAlignment(batch, text, text_total_width, getPositionX(), getPositionY(), getWidth(), color);
		} else {
			if (shadow != null) {
				float render_pos_y = getPositionY() + shadow.y;
				for (int line_id = multiline.size - 1; line_id >= 0; line_id --) {
					String line = multiline.get(line_id);
					float line_width = multiline_widths[line_id];
					renderLineWithAlignment(batch, line, line_width, getPositionX() + shadow.x, render_pos_y, getWidth(), Color.BLACK);
					render_pos_y += height;
				}
			}
			float render_pos_y = getPositionY();
			for (int line_id = multiline.size - 1; line_id >= 0; line_id --) {
				String line = multiline.get(line_id);
				float line_width = multiline_widths[line_id];
				renderLineWithAlignment(batch, line, line_width, getPositionX(), render_pos_y, getWidth(), color);
				render_pos_y += height;
			}
		}
	}

	protected void doRepositioning()
	{
		// Only multiline support needs something to be done
		if (multiline != null) {
			formMultilines(multiline, getWidth(), true);
		}
	}

	protected float doGetMinWidth()
	{
		if (multiline == null) {
			return text_total_width;
		} else {
			float result = 0;
			String word = "";
			for (int text_idx = 0; text_idx < text.length(); text_idx ++) {
				char c = text.charAt(text_idx);
				if (c == ' ' || c == '\n') {
					result = Math.max(result, font.getStringWidth(word, height));
					word = "";
				} else {
					word += c;
				}
			}
			result = Math.max(result, font.getStringWidth(word, height));
			return result;
		}
	}

	protected float doGetMinHeight(float width)
	{
		if (multiline == null) {
			return height;
		} else {
			Array<String> temp_lines = new Array<String>();
			formMultilines(temp_lines, width, false);
			return temp_lines.size * height;
		}
	}
	
	private String text;
	private float text_total_width;
	private float height;
	private Font font;
	private Color color;
	private Alignment text_align;
	private Vector2 shadow = null;

	// This also tells if multiple lines are supported. Null means not.
	private Array<String> multiline;
	
	private float[] multiline_widths = null;

	private void renderLineWithAlignment(SpriteBatch batch, String text, float text_width, float pos_x, float pos_y, float width, Color color)
	{
		if (text_align == Alignment.LEFT) {
			font.renderString(batch, text, pos_x, pos_y + height, height, color);
		} else {
			float extra_space = width - text_width;
			if (text_align == Alignment.RIGHT) {
				font.renderString(batch, text, pos_x + extra_space, pos_y + height, height, color);
			} else {
				font.renderString(batch, text, pos_x + extra_space / 2, pos_y + height, height, color);
			}
		}
	}

	private void formMultilines(Array<String> lines, float width, boolean form_multiline_widths)
	{
		lines.clear();
		
		String line = "";
		String word = "";
		for (int text_idx = 0; text_idx < text.length(); text_idx ++) {
			char c = text.charAt(text_idx);
			if (c == ' ' || c == '\n') {
				// If this is first word, then add it to line, no matter what
				if (line.length() == 0) {
					line = word + " ";
				}
				// If there is already stuff at current line, then
				// we have to check if this word fits there. 
				else {
					float line_width = font.getStringWidth(line + word, height);
					if (line_width > width && word.length() > 0) {
						lines.add(line);
						line = word + " ";
					} else {
						line += word + " ";
					}
				}
				word = "";
				// If this was endline, then add new line
				if (c == '\n') {
					lines.add(line);
					line = "";
				}
			} else {
				word += c;
			}
		}
		if (word.length() > 0) {
			// If this is first word, then add it to line, no matter what
			if (line.length() == 0) {
				lines.add(word);
			}
			// If there is already stuff at current line, then
			// we have to check if this word fits there. 
			else {
				float line_width = font.getStringWidth(line + word, height);
				if (line_width > width) {
					lines.add(line);
					lines.add(word);
				} else {
					lines.add(line + word);
				}
			}
		}
		
		// Go lines through and remove whitespace from their tails.
		for (int line_id = 0; line_id < lines.size; line_id ++) {
			lines.set(line_id, lines.get(line_id).replaceAll("[\\s]+$", ""));
		}
		
		// Form widths, if requested
		if (form_multiline_widths) {
			multiline_widths = new float[lines.size];
			for (int line_id = 0; line_id < lines.size; line_id ++) {
				multiline_widths[line_id] = font.getStringWidth(lines.get(line_id), height);
			}
		}
	}

}
