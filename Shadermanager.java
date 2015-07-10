package fi.henu.gdxextras;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shadermanager

{
	public Shadermanager(String vertexshader_code, String fragmentshader_code)
	{
		this.vertexshader_code = vertexshader_code;
		this.fragmentshader_code = fragmentshader_code;
		shaders = new HashMap<TreeSet<String>, ShaderProgram>();
	}

	// Returns shader that is compiled with given flags
	public ShaderProgram getShader(TreeSet<String> flags)
	{
		// Check if shader is already compiled
		if (shaders.containsKey(flags)) {
			return shaders.get(flags);
		}

		// Compile new shader. Start by making defines from flags
		final StringBuilder defines_sb = new StringBuilder(128);
		for (String flags_it : flags) {
			defines_sb.append("#define ");
			defines_sb.append(flags_it);
			defines_sb.append("\n");
		}
		final String defines = defines_sb.toString();

		// Form final shader codes
		String vs_code_final = defines + vertexshader_code;
		String fs_code_final = defines + fragmentshader_code;

		// Do compiling
		ShaderProgram new_shader = new ShaderProgram(vs_code_final, fs_code_final);
		if (!new_shader.isCompiled()) {
			Gdx.app.log("WARNING", "Unable to compile shader! Trying to add \"precision mediump float;\" to the beginning of fragment shader in case it would solve the problem.");
			// Before giving up, check if shader compiles if
			// "precision mediump float;" is put at the
			// beginning of fragment shader code.
			final String fs_code_final2 = mediumPrecision + fs_code_final;
			new_shader = new ShaderProgram(vs_code_final, fs_code_final2);
			if (!new_shader.isCompiled()) {
				throw new RuntimeException("Unable to compile shader! Compile log:\n" + new_shader.getLog());
			}
		}

		// Store new shader
		// TODO: Is "new TreeSet" mandatory?
		shaders.put(new TreeSet<String>(flags), new_shader);

		return new_shader;

	}

	private static final String mediumPrecision = "precision mediump float;\n";
	private String vertexshader_code;
	private String fragmentshader_code;

	// Container of compiled shaders
	private Map<TreeSet<String>, ShaderProgram> shaders;

}
