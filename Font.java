package fi.henu.gdxextras;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Bitmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

// TODO: Support kerning!
public class Font
{

	public Font(FileHandle fh, int pixelheight)
	{
		this.pixelheight = pixelheight;
		this.glyphmodifier = null;
		addNewFont(fh);
	}

	public Font(FileHandle fh, int pixelheight, Glyphmodifier glyphmodifier)
	{
		this.pixelheight = pixelheight;
		this.glyphmodifier = glyphmodifier;
		addNewFont(fh);
	}

	// Loads new font to memory, with lower priority than already loaded fonts.
	public void addNewFont(FileHandle fh)
	{
		FreeTypeFontGenerator new_font = new FreeTypeFontGenerator(fh);
		fonts.add(new_font);
	}
	
	// This resets font. Next time the font is used, it needs to
	// re-create all Textures and glyphs to them. This should be called
	// when application is put background and OpenGL context is lost. 
	public void reset()
	{
		regions.clear();
		
		for (int tex_id = 0; tex_id < texs.size; tex_id ++) {
			Texture tex = texs.items[tex_id];
			tex.dispose();
		}
		texs.clear();
		
		next_glyph_x = 0;
		next_glyph_y = 0;
		tallest_glyph_at_row = 0;
	}
	
	public void renderString(SpriteBatch batch, String str, Vector2 pos, float pixelheight, Color color)
	{
		renderString(batch, str, pos.x, pos.y, pixelheight, color);
	}

	public void renderString(SpriteBatch batch, String str, float pos_x, float pos_y, float pixelheight, Color color)
	{
		float scale = pixelheight / this.pixelheight;

		float draw_x = 0;
		float draw_y = 0;

		batch.setColor(color);
		for (int str_idx = 0; str_idx < str.length(); str_idx++) {
			Integer codepoint = new Integer(str.codePointAt(str_idx));

			// In case of newline
			if (codepoint.intValue() == 0x0a) {
				draw_x = 0;
				draw_y -= pixelheight;
				continue;
			}

			// Get region for this codepoint
			AtlasRegion region = getRegionForCodepoint(codepoint);

			// Render single character
			if (region != null) {
				float x = draw_x + pos_x + region.offsetX * scale;
				float y = draw_y + pos_y + region.offsetY * scale;
				float region_w = region.packedWidth * scale;
				float region_h = region.packedHeight * scale;
				batch.draw(region, x, y, region_w, region_h);
				draw_x += region.originalWidth * scale / GLYPH_REAL_WIDTH_MULTIPLIER;
			}
		}
		batch.setColor(1, 1, 1, 1);
	}

	public float getStringWidth(String str, float pixelheight)
	{
		float max_line = 0;
		float current_line = 0;

		float scale = pixelheight / this.pixelheight;

		for (int str_idx = 0; str_idx < str.length(); str_idx++) {
			Integer codepoint = new Integer(str.codePointAt(str_idx));

			// In case of newline
			if (codepoint.intValue() == 0x0a) {
				max_line = Math.max(max_line, current_line);
				current_line = 0;
				continue;
			}

			// Get region for this codepoint
			AtlasRegion region = getRegionForCodepoint(codepoint);

			// Add advance of this character
			if (region != null) {
				current_line += region.originalWidth * scale / GLYPH_REAL_WIDTH_MULTIPLIER;
			}
		}
		return Math.max(max_line, current_line);
	}
	
	private static final int TEXTURE_WIDTH = 256;
	private static final int PADDING = 2;
	private static final float GLYPH_REAL_WIDTH_MULTIPLIER = 1000f;

	private int pixelheight;

	private Glyphmodifier glyphmodifier;

	private Array<FreeTypeFontGenerator> fonts = new Array<FreeTypeFontGenerator>(true, 1, FreeTypeFontGenerator.class);

	private Map<Integer, AtlasRegion> regions = new HashMap<Integer, AtlasRegion>();

	// Textures that hold glyph data and counters where next glyph
	// should be added. Also tallest glyph at current row.
	private Array<Texture> texs = new Array<Texture>(true, 1, Texture.class);
	private int next_glyph_x, next_glyph_y, tallest_glyph_at_row;

	private AtlasRegion getRegionForCodepoint(Integer codepoint)
	{
		// If region already exists, then return it
		AtlasRegion region = regions.get(codepoint);
		if (region != null) {
			return region;
		}
		// Try to generate new region
		region = generateRegion(codepoint);
		if (region != null) {
			regions.put(codepoint, region);
		}
		return region;
	}

	private AtlasRegion generateRegion(Integer codepoint)
	{
		// Go all fonts through and try to find requested codepoint from
		// them. Also go one extra round. This is used get special "empty
		// glyph" if no proper glyph could be found from any other fonts.
		for (int font_id = 0; font_id <= fonts.size; font_id++) {
			FreeTypeFontGenerator font = fonts.get(font_id % fonts.size);
			
			int c = codepoint.intValue();
			
			// Try to find glyph from this font. If no proper
			// glyph was found, then try the next font
			FreeTypeFontGenerator.GlyphAndBitmap gnbm = font.generateGlyphAndBitmap(c, font.scaleForPixelHeight(pixelheight), false);
			if (gnbm == null) {
				continue;
			}
			Glyph glyph = gnbm.glyph;
			Bitmap glyph_bitmap = gnbm.bitmap;

			float offset_x = glyph.xoffset;
			float offset_y = glyph.yoffset;
			float advance = glyph.xadvance;
			
			// For example with space, there is no bitmap. In this
			// case, mark pixmap to be null. This will spawn empty
			// region that will have its originalWidth the advance
			// of space (or whatever letter lacks bitmap).
			Pixmap glyph_pixmap;
			if (glyph_bitmap != null) {
				glyph_pixmap = glyph_bitmap.getPixmap(Format.RGBA8888);
			} else {
				glyph_pixmap = null;
			}
			
			// Apply possible glyph modifier
			if (glyphmodifier != null) {
				Vector2 offset = new Vector2(offset_x, offset_y);
				glyph_pixmap = glyphmodifier.modify_glyph(offset, glyph_pixmap, pixelheight);
				offset_x = offset.x;
				offset_y = offset.y;
			}

			AtlasRegion region = drawGlyphToTexture(glyph_pixmap, offset_x, offset_y, advance);
			
			// Clean
			glyph_pixmap.dispose();
			
			return region;

		}

		return null;
	}

	private void spawnNewTexture()
	{
		// TODO: RGBA8888 consumes too much memory! Use ALPHA if there is no glyph modifier!
		Texture new_tex = new Texture(TEXTURE_WIDTH, TEXTURE_WIDTH, Format.RGBA8888);
		new_tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		texs.add(new_tex);
		next_glyph_x = 0;
		next_glyph_y = 0;
		tallest_glyph_at_row = 0;
	}

	// Draws given glyph to texture. If it does not fit to
	// remaining texture, then new one is created and if it
	// does not fit to it, a RuntimeException is thrown.
	private AtlasRegion drawGlyphToTexture(Pixmap glyph, float offset_x, float offset_y, float advance)
	{
		// If there are no Textures, then create new one
		if (texs.size == 0) {
			spawnNewTexture();
		}
		
		// Special case, when there is nothing
		// to draw, but advance is still needed.
		if (glyph == null) {
			Texture tex = texs.get(texs.size - 1);
			AtlasRegion result = new AtlasRegion(tex, 0, 0, 0, 0);
			result.originalWidth = (int)(advance * GLYPH_REAL_WIDTH_MULTIPLIER);
			return result;
		}

		int glyph_w = glyph.getWidth();
		int glyph_h = glyph.getHeight();
		if (glyph_w + PADDING > TEXTURE_WIDTH || glyph_h + PADDING > TEXTURE_WIDTH) {
			throw new RuntimeException("Glyph is too big for texture!");
		}

		// Check if glyph needs to go to the second row
		if (next_glyph_x + glyph_w + PADDING > TEXTURE_WIDTH) {
			next_glyph_x = 0;
			next_glyph_y += tallest_glyph_at_row + PADDING;
			tallest_glyph_at_row = 0;
		}
		// Check if glyph fits vertically
		if (next_glyph_y + glyph_h + PADDING > TEXTURE_WIDTH) {
			spawnNewTexture();
		}

		// Draw to texture
		Texture tex = texs.get(texs.size - 1);
		tex.draw(glyph, next_glyph_x, next_glyph_y);

		AtlasRegion result = new AtlasRegion(tex, next_glyph_x, next_glyph_y, glyph_w, glyph_h);
		result.offsetX = offset_x;
		result.offsetY = offset_y;
		result.originalWidth = (int)(advance * GLYPH_REAL_WIDTH_MULTIPLIER);

		// Update counters
		next_glyph_x += glyph_w + PADDING;
		tallest_glyph_at_row = Math.max(tallest_glyph_at_row, glyph_h);

		return result;
	}

}
