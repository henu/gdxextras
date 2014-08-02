package fi.henu.gdxextras;

import com.badlogic.gdx.math.Vector3;

public class Light
{
	
	public enum Type { SUN }

	public Light()
	{
		type = Type.SUN;
		color = new Vector3(0, 0, 0);
		dir = new Vector3(0, 0, 0);
	}

	public void setType(Type type) { this.type = type; }
	public void setColor(float red, float green, float blue) { color.x = red; color.y = green; color.z = blue; } 
	public void setDir(float x, float y, float z) { dir.x = x; dir.y = y; dir.z = z; dir.nor(); }
	public void setColor(Vector3 color) { this.color.set(color); } 
	public void setDir(Vector3 dir) { this.dir.set(dir);  }

	public Type getType() { return type; }
	public final Vector3 getColor() { return color; }
	public final Vector3 getDir() { return dir; }

	private Type type;
	private Vector3 color;
	private Vector3 dir;

}
