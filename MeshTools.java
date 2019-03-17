package fi.henu.gdxextras;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

public class MeshTools
{
	public static Model meshToModel(Mesh mesh, Material material)
	{
		Model model = new Model();
		model.meshes.add(mesh);
		model.materials.add(material);
		model.calculateTransforms();
		Node node = new Node();
		NodePart node_part = new NodePart();
		node_part.material = material;
		node_part.meshPart = new MeshPart();
		node_part.meshPart.mesh = mesh;
		node_part.meshPart.size = mesh.getMaxIndices();
		node_part.meshPart.primitiveType = GL20.GL_TRIANGLES;
		node.parts.add(node_part);
		model.nodes.add(node);
		return model;
	}
}
