package fi.henu.gdxextras;

import java.io.UnsupportedEncodingException;
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

	public void pushByte(byte b)
	{
		ensureAvailable(1);
		buf[items] = b;
		items ++;
	}

	public void pushShort(short s)
	{
		tmp_bb.clear();
		tmp_bb.putShort(s);
		pushFromTemporaryByteBuffer();
	}

	public void pushInt(int i)
	{
		tmp_bb.clear();
		tmp_bb.putInt(i);
		pushFromTemporaryByteBuffer();
	}

	public void pushLong(long l)
	{
		tmp_bb.clear();
		tmp_bb.putLong(l);
		pushFromTemporaryByteBuffer();
	}

	public void pushFloat(float f)
	{
		tmp_bb.clear();
		tmp_bb.putFloat(f);
		pushFromTemporaryByteBuffer();
	}

	public void pushByteserializable(Byteserializable serializable)
	{
		serializable.serializeToBytes(this);
	}

	public void push(Bytes bytes)
	{
		pushBytes(bytes.buf, 0, bytes.items);
	}

	// This will also push length of string to bytes, when encoded with
	// UTF-8. This will take four extra bytes and it will be located
	// before actual string. null is considered as empty string.
	public void pushString(String str)
	{
		if (str == null) {
			pushInt(0);
			return;
		}
		byte[] str_bytes_utf8;
		try {
			str_bytes_utf8 = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to serialize string!");
		}
		pushInt(str_bytes_utf8.length);
		ensureAvailable(str_bytes_utf8.length);
		System.arraycopy(str_bytes_utf8, 0, buf, items, str_bytes_utf8.length);
		items += str_bytes_utf8.length;
	}

	public void pushBytes(byte[] bytes)
	{
		pushBytes(bytes, 0, bytes.length);
	}

	public void pushBytes(byte[] bytes, int ofs, int len)
	{
		ensureAvailable(len);
		System.arraycopy(bytes, ofs, buf, items, len);
		items += len;
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

	// This helper function will read Strings that are serialized
	// with push() method. If length is zero, then null is returned.
	public static String readStringFromByteBuffer(ByteBuffer buf)
	{
		int str_utf8_size = buf.getInt();
		if (str_utf8_size == 0) {
			return null;
		}
		byte[] str_utf8 = new byte[str_utf8_size];
		buf.get(str_utf8);
		try {
			return new String(str_utf8, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to encode string from bytes with UTF-8!");
		}
	}

	private byte[] buf = null;
	private int items = 0;
	private int alloc = 0;

	private final ByteBuffer tmp_bb = ByteBuffer.allocate(8);

	private void pushFromTemporaryByteBuffer()
	{
		int amount = tmp_bb.position();
		ensureAvailable(amount);
		System.arraycopy(tmp_bb.array(), 0, buf, items, amount);
		items += amount;
	}

}
