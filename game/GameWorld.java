package fi.henu.gdxextras.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;

import fi.henu.gdxextras.collisions.Collision;

public class GameWorld
{
	public GameWorld()
	{
		objs = new Array<>(false, 50);
		colliders_sorting_needed = true;
		collision_extra_margin = -1;
		objs_to_destroy = new HashSet<>();

		batch = new SpriteBatch();

		controls = new Controls();
		camera = new Camera();
	}

	public void dispose()
	{
		if (batch != null) {
			batch.dispose();
			batch = null;
		}
	}

	public void setCollisionExtraMargin(float extra_margin)
	{
		collision_extra_margin = extra_margin;
	}

	public Camera getCamera()
	{
		return camera;
	}

	public Controls getControls()
	{
		return controls;
	}

	public void updateControls()
	{
		// Initialize controls from keyboard, and then let subclass to alter it
		controls.readFromKeyboard();
		overrideControls(controls);
	}

	public void run(float delta)
	{
		updateControls();

		// Run movement of all GameObjects
		for (int obj_i = 0; obj_i < objs.size; ++ obj_i) {
			GameObject obj = objs.get(obj_i);
			if (!objs_to_destroy.contains(obj)) {
				obj.runMovement(delta, controls);
			}
		}

		destroyPendingGameObjects();

		// Add collisions between GameObjects
		if (colliders_sorting_needed) {
			sortColliders();
			colliders_sorting_needed = false;
		}
		Array<Collision> temp_colls = new Array<>(false, 4);
		for (int collider_i = 0; collider_i < colliders_size; ++ collider_i) {
			GameObject collider = objs.get(collider_i);
			for (int target_i = collider_i + 1; target_i < objs.size; ++ target_i) {
				GameObject target = objs.get(target_i);
				boolean collider_collides = collider.collidesWith(target);
				boolean target_collides = target.collidesWith(collider);
				if ((collider_collides || target_collides) && collision_extra_margin < 0) {
					throw new RuntimeException("Collision extra margin is not set! Please call GameWorld.setCollisionExtraMargin() to set it!");
				}
				if (collider_collides) {
					temp_colls.clear();
					collider.getCollisionsTo(temp_colls, target, collision_extra_margin);
					collider.addCollisions(temp_colls, false, target_collides);
				}
				if (target_collides) {
					temp_colls.clear();
					target.getCollisionsTo(temp_colls, collider, collision_extra_margin);
					target.addCollisions(temp_colls, false, collider_collides);
				}
			}
		}

		destroyPendingGameObjects();

		// Handle collisions and perform after running callback
		for (int obj_i = 0; obj_i < objs.size; ) {
			GameObject obj = objs.get(obj_i);
			// If this object should not exist any more, then destroy it
			if (objs_to_destroy.contains(obj)) {
				objs.removeIndex(obj_i);
				colliders_sorting_needed = true;
				continue;
			}
			// Handle collisions
			if (obj.hasCollisions()) {
				obj.handleCollisions();
				obj.clearCollisions();
			}
			// Call possible callback, and destroy object if requested
			if (obj.handleAfterRun()) {
				++ obj_i;
			} else {
				objs.removeIndex(obj_i);
				colliders_sorting_needed = true;
			}
		}

		destroyPendingGameObjects();
	}

	public void render()
	{
		if (batch != null) {
			batch.setProjectionMatrix(camera.getProjectionMatrix());
			batch.begin();
			for (int obj_i = 0; obj_i < objs.size; ++obj_i) {
				GameObject obj = objs.get(obj_i);
				obj.render(batch);
			}
			batch.end();
		}
	}

	public void addGameObject(GameObject obj)
	{
		objs.add(obj);
		obj.setWorld(this);
		colliders_sorting_needed = true;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public int getScore()
	{
		return score;
	}

	public void increaseScore(int points)
	{
		score += points;
	}

	public void markColliderSortingNeeded()
	{
		colliders_sorting_needed = true;
	}

	public boolean containsGameObject(Class obj_class)
	{
		for (int obj_i = 0; obj_i < objs.size; ++ obj_i) {
			GameObject obj = objs.get(obj_i);
			if (obj_class.isInstance(obj)) {
				return true;
			}
		}
		return false;
	}

	// Destroys specific GameObject as soon as possible
	public void destroyGameObject(GameObject obj)
	{
		objs_to_destroy.add(obj);
	}

	protected void overrideControls(Controls controls)
	{
	}

	private float collision_extra_margin;

	// Objects are sorted so that first comes those objects
	// that move and can collide to other objects.
	private Array<GameObject> objs;
	private int colliders_size;
	private boolean colliders_sorting_needed;

	private final HashSet<GameObject> objs_to_destroy;

	private SpriteBatch batch;

	private final Camera camera;

	private final Controls controls;

	private int score;

	private void sortColliders()
	{
		colliders_size = 0;
		int noncolliders_size = 0;
		while (colliders_size + noncolliders_size < objs.size) {
			// If the next GameObject after last collider is also a collider,
			// then it is possible just to increase the number of colliders.
			GameObject obj1 = objs.get(colliders_size);
			if (obj1.getMovement() != null && obj1.collidesWithSomething()) {
				++ colliders_size;
				continue;
			}
			// If the next GameObject before the first non-collider is also a non-collider,
			// then it is possible to just increase the number of colliders
			GameObject obj2 = objs.get(objs.size - noncolliders_size - 1);
			if (obj2.getMovement() == null || !obj2.collidesWithSomething()) {
				++ noncolliders_size;
				continue;
			}
			// This is the most tricky situation. Both objects are on wrong sides, so swap them
			objs.set(colliders_size, obj2);
			objs.set(objs.size - noncolliders_size - 1, obj1);
			++ colliders_size;
			++ noncolliders_size;
		}
	}

	private void destroyPendingGameObjects()
	{
		if (!objs_to_destroy.isEmpty()) {
			for (int obj_i = 0; obj_i < objs.size; ) {
				if (objs_to_destroy.contains(objs.get(obj_i))) {
					objs.removeIndex(obj_i);
				} else {
					++ obj_i;
				}
			}
			objs_to_destroy.clear();
			colliders_sorting_needed = true;
		}
	}
}
