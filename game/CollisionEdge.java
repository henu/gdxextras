package fi.henu.gdxextras.game;

public class CollisionEdge extends GameObject
{
	public CollisionEdge(float pos_along_normal, float normal_x, float normal_y, float normal_z)
	{
		setCollisionShape(new PlaneShape(pos_along_normal, normal_x, normal_y, normal_z));
	}
}
