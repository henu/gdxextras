package fi.henu.gdxextras;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Renderable
{

	public Renderable()
	{
		transfh = null;
		inf_bb = false;
	}

	// Clears all references to other objects
	public void close()
	{
		transfh.close();
		transfh = null;
		meshes.clear();
		mats.clear();
	}

	public void setTransformhandle(Transformhandle transfh)
	{
		if (this.transfh != null) {
			this.transfh.close();
		}
		this.transfh = transfh;
		if (transfh != null) {
			if (!inf_bb) {
				// Add all boundingboxes to transformhandle
				Mesh[] meshes_buf = meshes.items;
				for (int mesh_id = 0; mesh_id < meshes.size; mesh_id++) {
					Mesh mesh = meshes_buf[mesh_id];
					addMeshToBoundingbox(mesh);
				}
			} else {
				transfh.setInfiniteBoundingbox();
			}
		}
	}

	public Transformhandle getTransformhandle()
	{
		return transfh;
	}

	// Marks BoundingBox to cover everything. This basically disables
	// BoundingBox. This makes rendering slower, but is good if there is need
	// for creating lots of temporary Renderables.
	public void setInfiniteBoundingbox()
	{
		inf_bb = true;
		if (transfh != null) {
			transfh.setInfiniteBoundingbox();
		}
	}

	// Adds new mesh/genericmaterial pair and adds
	// its boundingbox to the scenenode if its set.
	public void addMesh(Mesh mesh, Genericmaterial material)
	{
		meshes.add(mesh);
		mats.add(material);
		if (transfh != null) {
			addMeshToBoundingbox(mesh);
		}
	}

	public void render(GL20 gl, Matrix4 mat_viewproj, ArrayList<Light> lights, Vector3 ambient_light, int flag_visible)
	{
		render(gl, mat_viewproj, lights, ambient_light, flag_visible, null);
	}

	public void render(GL20 gl, Matrix4 mat_viewproj, ArrayList<Light> lights, Vector3 ambient_light, int flag_visible, Genericmaterial custom_material)
	{
		assert transfh != null;

		// Check if visible flag is enabled
		if (!transfh.getFlag(flag_visible)) {
			return;
		}

		transfh.getTransform(tmp_m);

		// Compute matrix for normals
		matrix_nrm.val[0] = tmp_m.val[0];
		matrix_nrm.val[1] = tmp_m.val[1];
		matrix_nrm.val[2] = tmp_m.val[2];
		matrix_nrm.val[4] = tmp_m.val[4];
		matrix_nrm.val[5] = tmp_m.val[5];
		matrix_nrm.val[6] = tmp_m.val[6];
		matrix_nrm.val[8] = tmp_m.val[8];
		matrix_nrm.val[9] = tmp_m.val[9];
		matrix_nrm.val[10] = tmp_m.val[10];

		// Apply transform of Renderable to camera matrix
		mat_modelviewproj.set(mat_viewproj);
		Matrix4.mul(mat_modelviewproj.val, tmp_m.val);

		Mesh[] meshes_buf = meshes.items;
		for (int mesh_id = 0; mesh_id < meshes.size; mesh_id++) {
			Mesh mesh = meshes_buf[mesh_id];
			Genericmaterial mat;
			if (custom_material == null) {
				mat = mats.get(mesh_id);
			} else {
				mat = custom_material;
			}
			mat.render(gl, mesh, mat_modelviewproj, matrix_nrm, lights, ambient_light);
		}
	}

	public int getNumMeshes() { return meshes.size; }
	public Mesh getMesh(int mesh_id) { return meshes.get(mesh_id); }
	public Genericmaterial getMaterial(int mesh_id) { return mats.get(mesh_id); }

	private Transformhandle transfh;

	private boolean inf_bb;

	// Meshes and materials
	private Array<Mesh> meshes = new Array<Mesh>(false, 0, Mesh.class);
	private Array<Genericmaterial> mats = new Array<Genericmaterial>(false, 0, Genericmaterial.class);

	private Matrix4 mat_modelviewproj = new Matrix4();
	private Matrix4 matrix_nrm = new Matrix4();

	private Matrix4 tmp_m = new Matrix4();

	private void addMeshToBoundingbox(Mesh mesh)
	{
		if (inf_bb) return;
		assert transfh != null;
		transfh.addBoundingbox(mesh.calculateBoundingBox());
	}

}
