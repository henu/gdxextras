package fi.henu.gdxextras;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Meshcombiner
{

	// Adds new Mesh to the list
	public void addMesh(Mesh mesh)
	{
		meshes.add(mesh);
		transfs.add(null);
	}

	// Adds new Mesh with custom transformation to the list
	public void addMesh(Mesh mesh, Matrix4 transf)
	{
		if (mesh == null) {
			throw new NullPointerException();
		}
		meshes.add(mesh);
		transfs.add(transf);
	}

	// Combines meshes and returns result
	public Mesh combine(boolean is_static)
	{
		// TODO: Code support for case where all Meshes are Indexless! In that case, the total number of indices should be zero!
		if (meshes.size == 0) {
			throw new RuntimeException("Unable to combine empty list of Meshes!");
		}

		// First ensure all meshes have the same Vertexattributes.
		// Also count size of one vertex in floats.
		int vrt_size = 0;
		VertexAttributes mesh0_attrs = meshes.get(0).getVertexAttributes();
		Map<String, Integer> attrs_usages = new HashMap<String, Integer>();
		for (int attr_id = 0; attr_id < mesh0_attrs.size(); attr_id++) {
			VertexAttribute attr = mesh0_attrs.get(attr_id);
			attrs_usages.put(attr.alias, attr.usage);
			// Update size of single vertex
			if (attr.usage == Usage.ColorPacked) {
				vrt_size += 1;
			} else if (attr.usage == Usage.Generic) {
				throw new RuntimeException("Support for Usage.Generic is not implement yet!");
			} else {
				vrt_size += attr.numComponents;
			}
		}
		Set<String> attrs_names = attrs_usages.keySet();
		for (int mesh_id = 1; mesh_id < meshes.size; mesh_id++) {
			VertexAttributes attrs = meshes.get(mesh_id).getVertexAttributes();
			for (int attr_id = 0; attr_id < attrs.size(); attr_id++) {
				VertexAttribute attr = attrs.get(attr_id);
				// Ensure VertexAttribute exists in first mesh
				if (!attrs_usages.containsKey(attr.alias)) {
					throw new RuntimeException("Not all of meshes contain VertexAttribute \"" + attr.alias + "\", that is found from mesh #" + (mesh_id + 1) + "!");
				}
				// Ensure usage matches
				if (attr.usage != attrs_usages.get(attr.alias).intValue()) {
					throw new RuntimeException("VertexAttribute \"" + attr.alias + "\" has more than one usages in Meshes!");
				}
			}
			// Also ensure that all attributes are found from this mesh
			for (String attr_name : attrs_names) {
				boolean found = false;
				for (int attr_id = 0; attr_id < attrs.size(); attr_id++) {
					VertexAttribute attr = attrs.get(attr_id);
					if (attr.alias.equals(attr_name)) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("Mesh #" + (mesh_id + 1) + " does not contain VertexAttribute \"" + attr_name + "\"!");
				}
			}
		}

		// Go all meshes through and calculate the amount of vertices and
		// triangles
		int vrts_count = 0;
		int tris_count = 0;
		for (Mesh mesh : meshes) {
			vrts_count += mesh.getNumVertices();
			if (mesh.getNumIndices() > 0) {
				assert mesh.getNumIndices() % 3 == 0;
				tris_count += mesh.getNumIndices() / 3;
			} else {
				assert mesh.getNumVertices() % 3 == 0;
				tris_count += mesh.getNumVertices() / 3;
			}
		}

		// Ensure there is not too many vertices
		if (vrts_count >= 0x8000) {
			throw new RuntimeException("Cannot combine Meshes, because there would be " + vrts_count + " vertices and maximum amount is " + 0x8000 + "!");
		}

		// Generate vertexdata
		Vector3 v3tmp = new Vector3();
		assert vrts_count != 0;
		assert vrt_size != 0;
		float[] vdata = new float[vrts_count * vrt_size];
		int vdata_ofs = 0;
		for (int mesh_id = 0; mesh_id < meshes.size; mesh_id++) {
			Mesh mesh = meshes.get(mesh_id);
			Matrix4 transf = transfs.get(mesh_id);
			VertexAttributes mesh_attrs = mesh.getVertexAttributes();
			FloatBuffer mesh_vrts_buf = mesh.getVerticesBuffer();
			mesh_vrts_buf.position(0);
			for (int vrt_id = 0; vrt_id < mesh.getNumVertices(); vrt_id++) {
				for (int mesh0_attr_id = 0; mesh0_attr_id < mesh0_attrs.size(); mesh0_attr_id++) {
					VertexAttribute mesh0_attr = mesh_attrs.get(mesh0_attr_id);
					// Search correct attribute from this Mesh
					int mesh_attr_find = 0;
					while (!mesh_attrs.get(mesh_attr_find).alias.equals(mesh0_attr.alias)) {
						mesh_attr_find++;
						assert mesh_attr_find < mesh_attrs.size();
					}

					VertexAttribute attr = mesh_attrs.get(mesh_attr_find);
					int float_components = attr.numComponents;
					if (attr.usage == Usage.Position) {
						v3tmp.x = mesh_vrts_buf.get();
						v3tmp.y = mesh_vrts_buf.get();
						v3tmp.z = mesh_vrts_buf.get();
						if (transf != null) {
							v3tmp.mul(transf);
						}
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 0] = v3tmp.x;
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 1] = v3tmp.y;
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 2] = v3tmp.z;
					} else if (attr.usage == Usage.Normal) {
						v3tmp.x = mesh_vrts_buf.get();
						v3tmp.y = mesh_vrts_buf.get();
						v3tmp.z = mesh_vrts_buf.get();
						if (transf != null) {
							v3tmp.rot(transf);
						}
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 0] = v3tmp.x;
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 1] = v3tmp.y;
						vdata[vdata_ofs + mesh0_attr.offset / 4 + 2] = v3tmp.z;
					} else {
						for (int float_id = 0; float_id < float_components; float_id++) {
							vdata[vdata_ofs + mesh0_attr.offset / 4 + float_id] = mesh_vrts_buf.get();
						}
					}
				}
				vdata_ofs += vrt_size;
			}
		}
		assert vdata_ofs == vdata.length;

		// Generate indices data
		short[] idata = new short[tris_count * 3];
		int idata_ofs = 0;
		int vrts_from_others = 0;
		for (Mesh mesh : meshes) {
			if (mesh.getNumIndices() > 0) {
				ShortBuffer idxs = mesh.getIndicesBuffer();
				for (int idx_ofs = 0; idx_ofs < mesh.getNumIndices(); idx_ofs++) {
					short idx = idxs.get(idx_ofs);
					idx += vrts_from_others;
					idata[idata_ofs] = idx;
					idata_ofs++;
				}
			} else {
				for (short vrt_id = 0; vrt_id < mesh.getNumVertices(); vrt_id++) {
					short idx = vrt_id;
					idx += vrts_from_others;
					idata[idata_ofs] = idx;
					idata_ofs++;
				}
			}
			vrts_from_others += mesh.getNumVertices();
		}
		assert idata_ofs == idata.length;

		Mesh result = new Mesh(is_static, vrts_count, tris_count * 3, mesh0_attrs);
		result.setVertices(vdata);
		result.setIndices(idata);

		clear();

		return result;
	}

	// Clears the list
	public void clear()
	{
		meshes.clear();
		transfs.clear();
	}

	private Array<Mesh> meshes = new Array<Mesh>(false, 0, Mesh.class);
	private Array<Matrix4> transfs = new Array<Matrix4>(false, 0, Matrix4.class);

}
