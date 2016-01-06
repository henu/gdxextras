package fi.henu.gdxextras.gui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DecoratedEdges extends Widget
{

	// This is used to set default background and selected graphics for DecoratedEdges
	public static void setDefaultStyle(DecoratedEdgesStyle style)
	{
		default_style = style;
	}

	public void setWidget(Widget widget)
	{
		if (this.widget != null) {
			removeChild(this.widget);
		}
		this.widget = widget;
		if (widget != null) {
			addChild(widget);
		}
		markToNeedReposition();
	}

	@Override
	protected void doRepositioning()
	{
		if (widget != null) {
			float left_width = calculateLeftWidth();
			float right_width = calculateRightWidth();
			float top_height = calculateTopHeight();
			float bottom_height = calculateBottomHeight();
			repositionChild(widget, getPositionX() + left_width, getPositionY() + bottom_height, getWidth() - left_width - right_width, getHeight() - top_height - bottom_height);
		}
	}

	@Override
	protected void doRendering(SpriteBatch batch, ShapeRenderer shapes)
	{
		DecoratedEdgesStyle style = getStyle();

		float left_width = calculateLeftWidth();
		float right_width = calculateRightWidth();
		float top_height = calculateTopHeight();
		float bottom_height = calculateBottomHeight();

		// Render corners
		renderToSpace(batch, style.region_bottomleft_corner, getPositionX(), getPositionY(), left_width, bottom_height, 1, 1, style.scaling);
		renderToSpace(batch, style.region_bottomright_corner, getEndX() - right_width, getPositionY(), right_width, bottom_height, 0, 1, style.scaling);
		renderToSpace(batch, style.region_topleft_corner, getPositionX(), getEndY() - top_height, left_width, top_height, 1, 0, style.scaling);
		renderToSpace(batch, style.region_topright_corner, getEndX() - right_width, getEndY() - top_height, right_width, top_height, 0, 0, style.scaling);

		// Render edges
		renderAndRepeatTexture(batch, style.tex_top_edge, getPositionX() + left_width, getEndY() - top_height, getWidth() - left_width - right_width, style.tex_top_edge.getHeight() * style.scaling, style.scaling);
		renderAndRepeatTexture(batch, style.tex_bottom_edge, getPositionX() + left_width, getPositionY() + bottom_height - style.tex_bottom_edge.getHeight() * style.scaling, getWidth() - left_width - right_width, style.tex_bottom_edge.getHeight() * style.scaling, style.scaling);
		renderAndRepeatTexture(batch, style.tex_right_edge, getEndX() - right_width, getPositionY() + bottom_height, style.tex_right_edge.getWidth() * style.scaling, getHeight() - bottom_height - top_height, style.scaling);
		renderAndRepeatTexture(batch, style.tex_left_edge, getPositionX() + left_width - style.tex_left_edge.getWidth() * style.scaling, getPositionY() + bottom_height, style.tex_left_edge.getWidth() * style.scaling, getHeight() - bottom_height - top_height, style.scaling);

		// Render background
		renderAndRepeatTexture(batch, style.tex_background, getPositionX() + left_width, getPositionY() + bottom_height, getWidth() - left_width - right_width, getHeight() - bottom_height - top_height, style.scaling);
	}

	@Override
	protected float doGetMinWidth()
	{
		float content_width = 0;
		if (widget != null) {
			content_width = widget.getMinWidth();
		}
		float left_width = calculateLeftWidth();
		float right_width = calculateRightWidth();
		return content_width + left_width + right_width;
	}

	private float calculateLeftWidth()
	{
		DecoratedEdgesStyle style = getStyle();
		return style.scaling * Math.max(Math.max(style.region_topleft_corner.originalWidth, style.region_bottomleft_corner.originalWidth), style.tex_left_edge.getWidth());
	}

	private float calculateRightWidth()
	{
		DecoratedEdgesStyle style = getStyle();
		return style.scaling * Math.max(Math.max(style.region_topright_corner.originalWidth, style.region_bottomright_corner.originalWidth), style.tex_right_edge.getWidth());
	}

	private float calculateTopHeight()
	{
		DecoratedEdgesStyle style = getStyle();
		return style.scaling * Math.max(Math.max(style.region_topleft_corner.originalWidth, style.region_topright_corner.originalHeight), style.tex_top_edge.getHeight());
	}

	private float calculateBottomHeight()
	{
		DecoratedEdgesStyle style = getStyle();
		return style.scaling * Math.max(Math.max(style.region_bottomleft_corner.originalHeight, style.region_bottomright_corner.originalHeight), style.tex_bottom_edge.getHeight());
	}

	@Override
	protected float doGetMinHeight(float width)
	{
		float left_width = calculateLeftWidth();
		float right_width = calculateRightWidth();
		float top_height = calculateTopHeight();
		float bottom_height = calculateBottomHeight();
		float content_height = 0;
		if (widget != null) {
			content_height = widget.getMinHeight(Math.max(0, width - left_width - right_width));
		}
		return content_height + top_height + bottom_height;
	}

	private static DecoratedEdgesStyle default_style;

	private Widget widget;

	private DecoratedEdgesStyle getStyle()
	{
		return default_style;
	}
}
