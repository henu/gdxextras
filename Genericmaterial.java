package fi.henu.gdxextras;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;

public class Genericmaterial
{
	// TODO: Support point light!

	public Genericmaterial()
	{
		ensureShadermanagerExists();
		reset();
	}

	@SuppressWarnings("unchecked")
	public Genericmaterial(FileHandle fh, Map< String, Texture > texs)
	{
		ensureShadermanagerExists();
		reset();
		// Read file and convert it to JSON object
		String json_str = fh.readString("utf-8");
		JsonReader jsonreader = new JsonReader();
		Object json_raw = jsonreader.parse(json_str);
		OrderedMap< String, Object > json = (OrderedMap< String, Object >)json_raw;
		// Extract data from JSON
		if (json.containsKey("colormap")) {
			colormap = texs.get(json.get("colormap"));
		}
		if (json.containsKey("twosided")) {
			twosided = (Boolean)json.get("twosided");
		}
		if (json.containsKey("shadeless")) {
			shadeless = (Boolean)json.get("shadeless");
		}
		if (json.containsKey("round alpha")) {
			round_alpha = (Boolean)json.get("round alpha");
		}
		if (json.containsKey("color")) {
			Array< Object > color_arr = (Array< Object >)json.get("color");
			color = new Vector3();
			color.x = (Float)color_arr.get(0);
			color.y = (Float)color_arr.get(1);
			color.z = (Float)color_arr.get(2);
		}
	}

	public static void clear()
	{
		shaderm = null;
	}

	// Setters of options
	public void setColormap(Texture colormap) { this.colormap = colormap; shader = null; }
	public void setTwosided(boolean twosided) { this.twosided = twosided; shader = null; }
	public void setShadeless(boolean shadeless) { this.shadeless = shadeless; shader = null; }
	public void setRoundAlpha(boolean round_alpha) { this.round_alpha = round_alpha; shader = null; }
	public void setColor(Vector3 color) { this.color.set(color); shader = null; }

	// Rendering functions
	private final float[] render_light_color_buf = { 0, 0, 0, 1 };
	private final float[] render_light_dir_neg_buf = { 0, 0, 0, 1 };
	private final float[] render_light_ambient_buf = { 0, 0, 0, 1 };
	private final float[] render_color_buf = { 0, 0, 0, 1 };
	public void render(GL20 gl, Mesh mesh, Matrix4 mat_modelviewproj, Matrix4 mat_nrm, ArrayList<Light> lights, Vector3 ambient_light)
	{

		// TODO: Support multipasslighting!
		Light light = null;
		if (lights != null && !lights.isEmpty()) light = lights.get(0);

		// Set pre-rendering options
		if (light != null) {
			if (!this.light) shader = null;
			this.light = true;
		} else {
			if (this.light) shader = null;
			this.light = false;
		}
		if (ambient_light != null && (ambient_light.x > 0 || ambient_light.y > 0 || ambient_light.z > 0)) {
			if (!this.ambient_light) shader = null;
			this.ambient_light = true;
		} else {
			if (this.ambient_light) shader = null;
			this.ambient_light = false;
		}

		if (shader == null) getShader();

		if (colormap != null) {
			colormap.bind();
			shader.setUniformi("colormap", colormap.getTextureObjectHandle());
		}

		// Render front sides
		shader.begin();
		// Tune light
		if (this.light && !shadeless) {
			final Vector3 light_color = light.getColor();
			final Vector3 light_dir = light.getDir();
			render_light_color_buf[0] = light_color.x;
			render_light_color_buf[1] = light_color.y;
			render_light_color_buf[2] = light_color.z;
			render_light_dir_neg_buf[0] = -light_dir.x;
			render_light_dir_neg_buf[1] = -light_dir.y;
			render_light_dir_neg_buf[2] = -light_dir.z;
			shader.setUniform4fv("light", render_light_color_buf, 0, 4);
			shader.setUniform4fv("light_dir_neg", render_light_dir_neg_buf, 0, 4);
		}
		if (this.ambient_light && !shadeless) {
			render_light_ambient_buf[0] = ambient_light.x;
			render_light_ambient_buf[1] = ambient_light.y;
			render_light_ambient_buf[2] = ambient_light.z;
			shader.setUniform4fv("light_ambient", render_light_ambient_buf, 0, 4);
		}
		if (color != null) {
			render_color_buf[0] = color.x;
			render_color_buf[1] = color.y;
			render_color_buf[2] = color.z;
			shader.setUniform4fv("color", render_color_buf, 0, 4);
		}
		// Set matrices
		shader.setUniformMatrix("matrix", mat_modelviewproj);
		if (this.light && !shadeless) {
			shader.setUniformMatrix("matrix_nrm", mat_nrm);
		}
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();

		// Render possible back sides
		if (twosided) {
			gl.glCullFace(GL20.GL_FRONT);
			shader.begin();
			// Tune light
			if (this.light && !shadeless) {
				final Vector3 light_color = light.getColor();
				final Vector3 light_dir = light.getDir();
				render_light_color_buf[0] = light_color.x;
				render_light_color_buf[1] = light_color.y;
				render_light_color_buf[2] = light_color.z;
				render_light_dir_neg_buf[0] = light_dir.x;
				render_light_dir_neg_buf[1] = light_dir.y;
				render_light_dir_neg_buf[2] = light_dir.z;
				shader.setUniform4fv("light", render_light_color_buf, 0, 4);
				shader.setUniform4fv("light_dir_neg", render_light_dir_neg_buf, 0, 4);
			}
			if (this.ambient_light && !shadeless) {
				render_light_ambient_buf[0] = ambient_light.x;
				render_light_ambient_buf[1] = ambient_light.y;
				render_light_ambient_buf[2] = ambient_light.z;
				shader.setUniform4fv("light_ambient", render_light_ambient_buf, 0, 4);
			}
			if (color != null) {
				render_color_buf[0] = color.x;
				render_color_buf[1] = color.y;
				render_color_buf[2] = color.z;
				shader.setUniform4fv("color", render_color_buf, 0, 4);
			}
			// Set matrices
			shader.setUniformMatrix("matrix", mat_modelviewproj);
			if (this.light && !shadeless) {
				shader.setUniformMatrix("matrix_nrm", mat_nrm);
			}
			mesh.render(shader, GL20.GL_TRIANGLES);
			shader.end();
			gl.glCullFace(GL20.GL_BACK);
		}
	}

	// User options
	private boolean twosided;
	private boolean round_alpha;
	private boolean shadeless;
	private Texture colormap;
	private Vector3 color;

	// Pre-rendering options
	private boolean light;
	private boolean ambient_light;

	private ShaderProgram shader;

	private static final String vertexshader_code =
		// Position, UV and normal
		"attribute vec4 pos;\n" +
		"#ifdef COLORMAP\n" +
		"attribute vec4 uv;\n" +
		"#endif\n" +
		"#ifdef LIGHT\n" +
		"attribute vec4 nrm;\n" +
		"#endif\n" +
		"\n" +
		// Matrices
		"uniform mat4 matrix;\n" +
		"#ifdef LIGHT\n" +
		"uniform mat4 matrix_nrm;\n" +
		"#endif\n" +
		"\n" +
		// Lights
		"#ifdef LIGHT\n" +
		"uniform vec4 light;\n" +
		"uniform vec4 light_dir_neg;\n" +
		"#endif\n" +
		"#ifdef AMBIENT_LIGHT\n" +
		"uniform vec4 light_ambient;\n" +
		"#endif\n" +
		"\n" +
		// Variables to fragment
		"varying vec2 frag_uv;\n" +
		"varying vec3 frag_color;\n" +
		"\n" +
		// Main vertex function
		"void main()\n" +
		"{\n" +
		"	gl_Position = matrix * pos;\n" +
		"	#ifdef COLORMAP\n" +
		"	frag_uv = uv.xy;\n" +
		"	#endif\n" +
		"	\n" +
		// Lighting
		"	#ifdef AMBIENT_LIGHT\n" +
		"	frag_color = light_ambient.xyz;\n" +
		"	#else\n" +
		"	#ifdef LIGHT\n" +
		"	frag_color = vec3(0, 0, 0);\n" +
		"	#else\n" +
		"	frag_color = vec3(1, 1, 1);\n" +
		"	#endif\n" +
		"	#endif\n" +
		"	#ifdef LIGHT\n" +
		"	vec3 nrm_nrmz = normalize((matrix_nrm * nrm).xyz);\n" +
		"	float light_m = max(0.0, dot(nrm_nrmz, light_dir_neg.xyz));\n" +
		"	frag_color += light.xyz * light_m;\n" +
		"	#endif\n" +
		"}\n";
	private static final String fragmentshader_code =
		"varying vec2 frag_uv;\n" +
		"varying vec3 frag_color;\n" +
		"#ifdef COLORMAP\n" +
		"uniform sampler2D colormap;\n" +
		"#endif\n" +
		"#ifdef COLOR\n" +
		"uniform vec4 color;\n" +
		"#endif\n" +
		"void main()\n" +
		"{\n" +
		"	#ifdef COLORMAP\n" +
		"	vec4 color_raw = texture2D(colormap, frag_uv);\n" +
		"	#else\n" +
		"	vec4 color_raw = vec4(1, 1, 1, 1);\n" +
		"	#endif\n" +
		"	#ifdef COLOR\n" +
		"	color_raw *= vec4(color.xyz, 1);\n" +
		"	#endif\n" +
		"	#ifdef ROUND_ALPHA\n" +
		"	if (color_raw.w < 0.5) {\n" +
		"		discard;\n" +
		"	}\n" +
		"	gl_FragColor = vec4(color_raw.xyz * frag_color, 1);\n" +
		"	#else\n" +
		"	gl_FragColor = vec4(color_raw.xyz * frag_color, color_raw.w);\n" +
		"	#endif\n" +
		"}\n";

	private static Shadermanager shaderm;

	private static void ensureShadermanagerExists()
	{
		// TODO: Handle concurrency!
		if (shaderm == null) {
			shaderm = new Shadermanager(vertexshader_code, fragmentshader_code);
		}
	}

	private void reset()
	{
		// Reset options
		twosided = false;
		round_alpha = false;
		shadeless = false;
		colormap = null;
		light = false;
		ambient_light = false;
		color = null;
		// Mark shader as not get
		shader = null;
	}

	private void getShader()
	{
		// Construct flags
		TreeSet<String> flags = new TreeSet< String >();

		// Go options through and enable flags
		if (round_alpha) flags.add("ROUND_ALPHA");
		if (colormap != null) flags.add("COLORMAP");
		if (light & !shadeless) flags.add("LIGHT");
		if (ambient_light && !shadeless) flags.add("AMBIENT_LIGHT");
		if (color != null) flags.add("COLOR");

		shader = shaderm.getShader(flags);
	}

}
