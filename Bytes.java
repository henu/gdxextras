package fi.henu.gdxextras;

import java.nio.ByteBuffer;

public class Bytes
{
	
	public boolean empty()
	{
		return items == 0;
	}

	public int size()
	{
		return items;
	}

	public void clear()
	{
		items = 0;
	}

	public void push(byte b)
	{
		ensureAvailable(1);
		buf[items] = b;
		items ++;
	}

	public void push(short s)
	{
		tmp_bb.clear();
		tmp_bb.putShort(s);
		pushFromTemporaryByteBuffer();
	}

	public void push(int i)
	{
		tmp_bb.clear();
		tmp_bb.putInt(i);
		pushFromTemporaryByteBuffer();
	}

	public void push(long l)
	{
		tmp_bb.clear();
		tmp_bb.putLong(l);
		pushFromTemporaryByteBuffer();
	}

	public void push(float f)
	{
		tmp_bb.clear();
		tmp_bb.putFloat(f);
		pushFromTemporaryByteBuffer();
	}

	public void push(Byteserializable serializable)
	{
		serializable.serializeToBytes(this);
	}

	// Ensures there is specific amount of empty space available
	public void ensureAvailable(int amount)
	{
		if (alloc == 0) {
			alloc = Math.max(amount, 16);
			buf = new byte[alloc];
			assert items == 0;
		} else {
			if (alloc - items < amount) {
				alloc = Math.max(items + amount, alloc * 2);
				byte[] new_buf = new byte[alloc];
				System.arraycopy(buf, 0, new_buf, 0, items);
				buf = new_buf;
			}
		}
	}

	// Returns raw array that is used in this Bytes.
	public byte[] array()
	{
		return buf;
	}


	private byte[] buf = null;
	private int items = 0;
	private int alloc = 0;
	
	private ByteBuffer tmp_bb = ByteBuffer.allocate(8);

	private void pushFromTemporaryByteBuffer()
	{
		int amount = tmp_bb.remaining();
		ensureAvailable(amount);
		System.arraycopy(tmp_bb.array(), 0, buf, items, amount);
		items += amount;
	}

}
