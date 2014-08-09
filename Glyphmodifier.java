package fi.henu.gdxextras;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;

public interface Glyphmodifier
{
	Pixmap modify_glyph(Vector2 result_offset, Pixmap original_glyph, int full_pixelheight);
}
