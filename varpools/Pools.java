package fi.henu.gdxextras.varpools;

public class Pools
{
	public static final SVector2Pool sv2s = new SVector2Pool(40, 5000);
	public static final Vector2Pool v2s = new Vector2Pool(40, 5000);
	public static final Vector3Pool v3s = new Vector3Pool(40, 5000);
	public static final ColorPool colors = new ColorPool(10, 500);
}
