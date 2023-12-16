package fi.henu.gdxextras.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import fi.henu.gdxextras.collisions.Collision;
import fi.henu.gdxextras.collisions.Collision2D;
import fi.henu.gdxextras.collisions.CollisionHandler;
import fi.henu.gdxextras.collisions.Shape;
import fi.henu.gdxextras.threads.ThreadSafePools;

public class GameObject
{
	public GameObject()
	{
		pos = new Vector3(0, 0, 0);
		movement = null;
	}

	public GameWorld getGameWorld()
	{
		return world;
	}

	public void setPosition(float x, float y, float z)
	{
		pos.set(x, y, z);
	}

	public void setPosition(float x, float z)
	{
		pos.set(x, 0, z);
	}

	public Vector3 getPosition()
	{
		return pos;
	}

	public Vector2 getPosition2D()
	{
		if (tmp_v2 == null) {
			tmp_v2 = new Vector2(pos.x, pos.z);
		} else {
			tmp_v2.set(pos.x, pos.z);
		}
		return tmp_v2;
	}

	public void setRotation2D(float angle)
	{
		if (rot == null) {
			rot = ThreadSafePools.obtain(Quaternion.class);
		}
		rot.set(Vector3.Y, angle);
	}

	public float getRotation2D()
	{
		if (rot == null) {
			return 0;
		}
		return rot.getAngleAround(0, 1, 0);
	}

	public Movement getMovement()
	{
		return movement;
	}

	public void runMovement(float delta, Controls controls)
	{
		if (movement != null) {
			movement.run(this, delta, controls);
		}
	}

	public boolean collidesWithSomething()
	{
		return false;
	}

	public boolean collidesWith(GameObject obj)
	{
		return false;
	}

	public void getCollisionsTo(Array<Collision> result, GameObject obj, float extra_margin)
	{
		if (shape == null || obj.shape == null) {
			return;
		}

		float obj_rel_pos_x = obj.getPosition().x - pos.x;
		float obj_rel_pos_y = obj.getPosition().y - pos.y;
		float obj_rel_pos_z = obj.getPosition().z - pos.z;

		int result_old_size = result.size;
		shape.findCollisionsTo(result, obj.shape, obj_rel_pos_x, obj_rel_pos_y, obj_rel_pos_z, extra_margin, false);
		for (int i = result_old_size; i < result.size; ++ i) {
			Collision coll = result.get(i);
			coll.setCollider(this);
			coll.setTarget(obj);
		}
	}

	public void addCollisions(Array<Collision> colls, boolean flip_colliders, boolean copy_coll)
	{
		for (int coll_i = 0; coll_i < colls.size; ++ coll_i) {
			addCollision(colls.get(coll_i), flip_colliders, copy_coll);
		}
	}

	public void addCollision(Collision coll, boolean flip_colliders, boolean copy_coll)
	{
		if (colls == null) {
			colls = new Array<>(false, 4);
		}
		if (flip_colliders) {
			Collision coll_copy = new Collision(-coll.getNormalX(), -coll.getNormalY(), -coll.getNormalX(), coll.getDepth());
			coll_copy.setCollider(coll.getTarget());
			coll_copy.setTarget(coll.getCollider());
			colls.add(coll_copy);
		} else if (copy_coll) {
			Collision coll_copy = new Collision(coll.getNormalX(), coll.getNormalY(), coll.getNormalX(), coll.getDepth());
			coll_copy.setCollider(coll.getCollider());
			coll_copy.setTarget(coll.getTarget());
			colls.add(coll_copy);
		} else {
			colls.add(coll);
		}
	}

	public void addCollision(Collision2D coll, boolean flip_normal)
	{
		if (colls == null) {
			colls = new Array<>(false, 4);
		}
		if (flip_normal) {
			colls.add(new Collision(-coll.getNormalX(), 0, -coll.getNormalX(), coll.getDepth()));
		} else {
			colls.add(new Collision(coll.getNormalX(), 0, coll.getNormalX(), coll.getDepth()));
		}
	}

	public boolean hasCollisions()
	{
		return colls != null && colls.notEmpty();
	}

	public Array<Collision> getCollisions()
	{
		return colls;
	}

	public void handleCollisions()
	{
	}

	public void clearCollisions()
	{
		colls.clear();
	}

	public void render(SpriteBatch batch)
	{
		if (renderer != null) {
			renderer.render(batch, pos, rot, world.getCamera());
		}
	}

	// Returns true if object should exist, and false if it should be destroyed
	public boolean run(float delta, Controls controls)
	{
		return true;
	}

	// Returns true if object should exist, and false if it should be destroyed
	public boolean handleAfterRun()
	{
		return true;
	}

	// Called by GameWorld
	public void setWorld(GameWorld world)
	{
		if (world != null && this.world != null) {
			throw new RuntimeException("Already in world!");
		}
		this.world = world;
	}

	// Destroys object as soon as possible
	public void destroy()
	{
		world.destroyGameObject(this);
	}

	protected static final short INSIDE = 0;
	protected static final short PARTIALLY_INSIDE = 1;
	protected static final short OUTSIDE = 2;

	protected void setMovement(Movement movement)
	{
		if (world != null && movement != this.movement) {
			world.markColliderSortingNeeded();
		}
		this.movement = movement;
	}

	protected void setCollisionShape(Shape shape)
	{
		this.shape = shape;
	}

	protected void setRenderer(SpriteRenderer renderer)
	{
		this.renderer = renderer;
	}

	protected short isInsideViewport()
	{
		// Check errors
		if (renderer == null) {
			throw new RuntimeException("Unable to check if in viewport, because there is no Renderer!");
		}
		if (world == null) {
			throw new RuntimeException("Unable to check if in viewport, because there is no GameWorld!");
		}

		Camera camera = world.getCamera();

		// Calculate viewport properties
		float vp_left = -camera.getScroll().x;
		float vp_bottom = -camera.getScroll().y;
		float vp_right = vp_left + Gdx.graphics.getWidth() / camera.getScaling();
		float vp_top = vp_bottom + Gdx.graphics.getHeight() / camera.getScaling();

		// Get renderer properties
		float r_top = renderer.getBoundsTop(pos, rot, camera);
		float r_right = renderer.getBoundsRight(pos, rot, camera);
		float r_bottom = renderer.getBoundsBottom(pos, rot, camera);
		float r_left = renderer.getBoundsLeft(pos, rot, camera);

		// Check if fully inside
		if (r_top <= vp_top && r_right <= vp_right && r_bottom >= vp_bottom && r_left >= vp_left) {
			return INSIDE;
		}

		// Check if partially inside
		if (r_top > vp_bottom && r_bottom < vp_top && r_right > vp_left && r_left < vp_right) {
			return PARTIALLY_INSIDE;
		}

		return OUTSIDE;
	}

	// Move away from all objects this GameObject collides with. Note: This invalidates
	// depths of all collisions, and leaves only those that really collide.
	protected void moveOutOfCollisions()
	{
		moveOutOfCollisions(colls);
	}

	// Move away from specific group of collisions. Note: This invalidates
	// depths of given collisions, and leaves only those that really collide.
	protected void moveOutOfCollisions(Array<Collision> colls)
	{
		Vector3 movement = new Vector3();
		CollisionHandler.moveOut(movement, colls);
		Vector3 pos = getPosition();
		setPosition(pos.x + movement.x, pos.y + movement.y, pos.z + movement.z);
	}

	// Uses all collisions to bounce object
	protected void bounceFromCollisions()
	{
		bounceFromCollisions(colls);
	}

	// Uses given collisions to bounce object
	protected void bounceFromCollisions(Array<Collision> colls)
	{
		if (movement != null) {
			movement.bounce(colls);
		}
	}

	private GameWorld world;

	private final Vector3 pos;
	private Quaternion rot;

	private Movement movement;
	private Shape shape;
	private Renderer renderer;

	private Array<Collision> colls;

	// Used for returning 2D values
	private Vector2 tmp_v2;
}
