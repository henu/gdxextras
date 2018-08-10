package fi.henu.gdxextras;

import java.nio.ByteBuffer;

public class ByteQueue
{
	public int getSize()
	{
		return size;
	}

	public void writeByte(byte b)
	{
		ensureSpace(1);

		bytes[write] = b;
		write = (write + 1) % bytes.length;

		size += 1;
	}

	public void writeShort(short s)
	{
		ensureSpace(2);

		bytes[write] = (byte)(s >> 8);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)s;
		write = (write + 1) % bytes.length;

		size += 2;
	}

	public void writeInt(int i)
	{
		ensureSpace(4);

		bytes[write] = (byte)(i >> 24);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(i >> 16);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(i >> 8);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)i;
		write = (write + 1) % bytes.length;

		size += 4;
	}

	public void writeLong(long l)
	{
		ensureSpace(8);

		bytes[write] = (byte)(l >> 56);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 48);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 40);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 32);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 24);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 16);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)(l >> 8);
		write = (write + 1) % bytes.length;
		bytes[write] = (byte)l;
		write = (write + 1) % bytes.length;

		size += 8;
	}

	public void writeFloat(float f)
	{
		tmp_bb.clear();
		tmp_bb.putFloat(f);
		writeBytes(tmp_bb.array(), 4);
	}

	public void writeBytes(byte[] bytes, int size)
	{
		ensureSpace(size);

		int first_part_length = Math.min(size, this.bytes.length - write);
		System.arraycopy(bytes, 0, this.bytes, write, first_part_length);
		write = (write + first_part_length) % this.bytes.length;

		int second_part_length = size - first_part_length;
		if (second_part_length > 0) {
			System.arraycopy(bytes, first_part_length, this.bytes, 0, second_part_length);
			write = second_part_length;
		}

		this.size += size;
	}

	public void readBytes(byte[] result, int size)
	{
		if (size > this.size) {
			throw new RuntimeException("Not enough bytes!");
		}

		if (size <= 0) {
			return;
		}

		int first_part_length = Math.min(size, this.bytes.length - read);
		System.arraycopy(bytes, read, result, 0, first_part_length);
		read = (read + first_part_length) % bytes.length;

		int second_part_length = size - first_part_length;
		if (second_part_length > 0) {
			System.arraycopy(bytes, 0, result, first_part_length, second_part_length);
			read = second_part_length;
		}

		this.size -= size;
	}

	private byte[] bytes;

	private int read;
	private int write;
	private int size;

	private final ByteBuffer tmp_bb = ByteBuffer.allocate(4);

	private void ensureSpace(int needed_empty_space)
	{
		if (bytes == null) {
			bytes = new byte[needed_empty_space];
			return;
		}

		int empty_space = bytes.length - size;
		if (empty_space >= needed_empty_space) {
			return;
		}

		int new_alloc = Math.max(size + needed_empty_space, bytes.length * 2);
		byte[] new_bytes = new byte[new_alloc];

		// Copy bytes so they are at the beginning of new bytes
		if (size > 0) {
			if (read < write) {
				System.arraycopy(bytes, read, new_bytes, 0, size);
			} else {
				int first_part_length = bytes.length - read;
				System.arraycopy(bytes, read, new_bytes, 0, first_part_length);
				int second_part_length = size - first_part_length;
				if (second_part_length > 0) {
					System.arraycopy(bytes, 0, new_bytes, first_part_length, second_part_length);
				}
			}
		}

		read = 0;
		write = size;
		bytes = new_bytes;
	}
}
