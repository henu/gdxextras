package fi.henu.gdxextras;

import java.nio.ByteBuffer;

public class SVector2 implements Byteserializable
{
	public short x, y;

	public SVector2()
	{
		x = 0;
		y = 0;
	}

	public SVector2(short x, short y)
	{
		this.x = x;
		this.y = y;
	}

	public SVector2(int x, int y)
	{
		this.x = (short)x;
		this.y = (short)y;
	}

	public SVector2(SVector2 v)
	{
		x = v.x;
		y = v.y;
	}

	public SVector2(ByteBuffer buf)
	{
		x = buf.getShort();
		y = buf.getShort();
	}

	public void set(SVector2 pos)
	{
		x = pos.x;
		y = pos.y;
	}

	public void set(short x, short y)
	{
		this.x = x;
		this.y = y;
	}

	public void set(int x, int y)
	{
		this.x = (short)x;
		this.y = (short)y;
	}

	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	@Override
	public final boolean equals(Object o)
	{
		if (o instanceof SVector2) {
			SVector2 v = (SVector2)o;
			return equals(v.x, v.y);
		}
		return false;
	}

	public boolean equals(short x, short y)
	{
		return x == this.x && y == this.y;
	}

	public float distanceTo(SVector2 pos)
	{
		return (float)Math.sqrt(distanceTo2(pos));
	}

	public long distanceTo2(SVector2 pos)
	{
		long xdiff = pos.x - x;
		long ydiff = pos.y - y;
		return xdiff * xdiff + ydiff * ydiff;
	}

	public short chebyshevDistanceTo(short x, short y)
	{
		return (short)Math.max(Math.abs(this.x - x), Math.abs(this.y - y));
	}

	public short chebyshevDistanceTo(SVector2 pos)
	{
		return chebyshevDistanceTo(pos.x, pos.y);
	}

	public short chebyshevDistanceTo(int x, int y)
	{
		return chebyshevDistanceTo((short)x, (short)y);
	}

	public short manhattanDistanceTo(short x, short y)
	{
		return (short)(Math.abs(this.x - x) + Math.abs(this.y - y));
	}

	public short manhattanDistanceTo(SVector2 pos)
	{
		return manhattanDistanceTo(pos.x, pos.y);
	}

	public short manhattanDistanceTo(int x, int y)
	{
		return manhattanDistanceTo((short)x, (short)y);
	}

	public SVector2 cpy()
	{
		return new SVector2(x, y);
	}

	public void scl(short s) {
		x *= s;
		y *= s;
	}

	public void add(SVector2 v)
	{
		add(v.x, v.y);
	}

	public void add(short x, short y)
	{
		this.x += x;
		this.y += y;
	}

	public void sub(SVector2 v)
	{
		sub(v.x, v.y);
	}

	public void sub(short x, short y)
	{
		this.x -= x;
		this.y -= y;
	}

	@Override
	public int hashCode()
	{
		return x * 991 + y * 997;
	}

	@Override
	public void serializeToBytes(Bytes bytes)
	{
		bytes.pushShort(x);
		bytes.pushShort(y);
	}

	@Override
	public void serialize(ByteQueue data)
	{
		data.writeShort(x);
		data.writeShort(y);
	}

	@Override
	public void deserialize(ByteQueue data) throws ByteQueue.InvalidData
	{
		x = data.readShort();
		y = data.readShort();
	}
}
