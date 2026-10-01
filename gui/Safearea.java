package fi.henu.gdxextras.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Safearea extends Widget
{
	public Safearea()
	{
		super();
		setPointerEvents(false);
	}

	public void setWidget(Widget widget)
	{
		if (this.widget == widget) {
			return;
		}
		if (this.widget != null) {
			removeChild(this.widget);
		}
		if (widget != null) {
			addChild(widget);
		}
		this.widget = widget;
		markToNeedReposition();
	}

	public void setOuterColor(Color color)
	{
		outer_color = color;
	}

	@Override
	protected float doGetMinWidth()
	{
		if (widget != null) {
			return widget.getMinWidth();
		}
		return 0;
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		if (widget != null) {
			return widget.getMinHeight(width);
		}
		return 0;
	}

	@Override
	protected void doRepositioning()
	{
		if (widget == null) {
			return;
		}

		// Calculate position of child
		float begin_x = Math.max(getPositionX(), getSafeLeft());
		float begin_y = Math.max(getPositionY(), getSafeBottom());
		float end_x = Math.min(getPositionX() + getWidth(), getSafeRight());
		float end_y = Math.min(getPositionY() + getHeight(), getSafeTop());

		repositionChild(widget, begin_x, begin_y, end_x - begin_x, end_y - begin_y);
	}

	@Override
	protected void doRenderingAfterChildren(SpriteBatch batch, ShapeRenderer shapes)
	{
		if (outer_color != null) {
			// Calculate the part of this Widget that is inside the safe area. Clamp it to the
			// bounds of this Widget, so the strips below never overlap or exceed the Widget,
			// even if the safe area does not touch this Widget at all.
			float begin_x = MathUtils.clamp(getSafeLeft(), getPositionX(), getEndX());
			float begin_y = MathUtils.clamp(getSafeBottom(), getPositionY(), getEndY());
			float end_x = MathUtils.clamp(getSafeRight(), begin_x, getEndX());
			float end_y = MathUtils.clamp(getSafeTop(), begin_y, getEndY());

			batch.end();
			Gdx.gl.glEnable(GL20.GL_BLEND);
			shapes.begin(ShapeRenderer.ShapeType.Filled);
			shapes.setColor(outer_color);

			// Left and right strips span the full height, bottom and top strips fill what
			// is left between them. Empty strips are skipped.
			if (begin_x > getPositionX()) {
				shapes.rect(getPositionX(), getPositionY(), begin_x - getPositionX(), getHeight());
			}
			if (end_x < getEndX()) {
				shapes.rect(end_x, getPositionY(), getEndX() - end_x, getHeight());
			}
			if (begin_y > getPositionY()) {
				shapes.rect(begin_x, getPositionY(), end_x - begin_x, begin_y - getPositionY());
			}
			if (end_y < getEndY()) {
				shapes.rect(begin_x, end_y, end_x - begin_x, getEndY() - end_y);
			}

			shapes.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
			batch.begin();
		}
	}

	private Widget widget;

	private Color outer_color;

	private float getSafeLeft()
	{
		return Gdx.graphics.getSafeInsetLeft();
	}

	private float getSafeBottom()
	{
		return Gdx.graphics.getSafeInsetBottom();
	}

	private float getSafeRight()
	{
		return Gdx.graphics.getWidth() - Gdx.graphics.getSafeInsetRight();
	}

	private float getSafeTop()
	{
		return Gdx.graphics.getHeight() - Gdx.graphics.getSafeInsetTop();
	}
}
