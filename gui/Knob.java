package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import fi.henu.gdxextras.Mathutils;

public class Knob extends Widget
{

	public static void setDefaults(AtlasRegion bg, AtlasRegion dimple, float regions_diameter, float dimple_diameter, float dimple_padding, float padding)
	{
		default_region_bg = bg;
		default_region_dimple = dimple;
		default_knob_diameter = regions_diameter;
		default_dimple_diameter = dimple_diameter;
		default_dimple_padding = dimple_padding;
		default_padding = padding;
	}

	public Knob(float scale, Color color)
	{
		this.scale = scale;
		this.color = color;
		angle = 0;
		enabled = true;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public float getAngle()
	{
		return angle;
	}

	public boolean pointerDown(int pointer_id, Vector2 pos)
	{
		if (!enabled) return false;

		if (pointer_id == 0) {
			pressed_angle = getAngleToPointer(pos);
			return true;
		}
		return false;
	}

	public void pointerMove(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			float new_angle = getAngleToPointer(pos);
			float diff_angle = new_angle - pressed_angle;
			pressed_angle = new_angle;
			diff_angle = Mathutils.fixAngle(diff_angle);
			angle = Mathutils.fixAngle(angle + diff_angle);
			if (diff_angle != 0) {
				fireEvent();
			}
		}
	}

	@Override
	public void pointerUp(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			pointerMove(0, pos);
			unregisterPointerListener(pointer_id);
		}
	}

	@Override
	public void pointerCancelled(int pointer_id, Vector2 pos)
	{
		if (pointer_id == 0) {
			unregisterPointerListener(pointer_id);
		}
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		if (!enabled) {
			batch.setColor(0.5f, 0.5f, 0.5f, 1f);
		} else {
			batch.setColor(color);
		}
		// Background
		renderFromCenter(batch, default_region_bg, getCenterX(), getCenterY(), scale);
		// Dimples
		final int DIMPLES = 8;
		final float DIMPLE_DISTANCE_FROM_CENTER = (default_knob_diameter / 2f - default_dimple_diameter / 2f - default_dimple_padding) * scale;
		for (int dimple_id = 0; dimple_id < DIMPLES; dimple_id ++) {
			float draw_angle = (float)dimple_id / DIMPLES * 360f + angle;
			float draw_x = getCenterX() - DIMPLE_DISTANCE_FROM_CENTER * MathUtils.sinDeg(draw_angle);
			float draw_y = getCenterY() + DIMPLE_DISTANCE_FROM_CENTER * MathUtils.cosDeg(draw_angle);
			renderFromCenter(batch, default_region_dimple, draw_x, draw_y, scale);
		}
		batch.setColor(1, 1, 1, 1);
	}

	protected float doGetMinWidth()
	{
		return (default_knob_diameter + default_padding * 2f) * scale;
	}

	protected float doGetMinHeight(float width)
	{
		return doGetMinWidth();
	}

	public boolean isOver(float x, float y)
	{
		float radius = default_knob_diameter / 2f * scale;
		x -= getCenterX();
		y -= getCenterY();
		float dst_to_2 = x * x + y * y;
		return dst_to_2 < radius * radius;
	}

	// Defaults
	private static AtlasRegion default_region_bg;
	private static AtlasRegion default_region_dimple;
	private static float default_knob_diameter;
	private static float default_dimple_diameter;
	private static float default_dimple_padding;
	private static float default_padding;

	// Options
	private float scale;
	private Color color;

	// State
	private float angle;
	private float pressed_angle;
	private boolean enabled;

	private float getAngleToPointer(Vector2 pos)
	{
		float diff_x = pos.x - getCenterX();
		float diff_y = pos.y - getCenterY();
		return Mathutils.getAngle(diff_x, diff_y) + 90;
	}

}
