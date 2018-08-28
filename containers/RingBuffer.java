package fi.henu.gdxextras.containers;

import java.nio.BufferUnderflowException;

public class RingBuffer<T>
{
	public void add(T obj)
	{
		ensureSpace(size + 1);
		items[write] = obj;
		write = (write + 1) % items.length;
		size += 1;
	}

	public T pop()
	{
		if (size <= 0) {
			throw new BufferUnderflowException();
		}
		T result = items[read ++];
		read = (read + 1) % items.length;
		-- size;
		ensureSpace(size);
		return result;
	}

	public boolean empty()
	{
		return size == 0;
	}

	private T[] items;

	private int read;
	private int write;
	private int size;

	private void ensureSpace(int new_size)
	{
		// Initial allocation
		if (items == null) {
			if (new_size > 0) {
				items = (T[])new Object[new_size];
			}
			return;
		}

		// If there is too much space
		if (new_size * 4 < items.length) {
// TODO: Code this!
			return;
		}

		// If there is enough space already
		if (new_size <= items.length) {
			return;
		}

		// If bigger space is needed
		new_size = Math.max(new_size, items.length * 2);
		T[] new_items = (T[])new Object[new_size];

		// Copy items so they are at the beginning of new items
		if (size > 0) {
			if (read < write) {
				System.arraycopy(items, read, new_items, 0, size);
			} else {
				int first_part_length = items.length - read;
				System.arraycopy(items, read, new_items, 0, first_part_length);
				int second_part_length = size - first_part_length;
				if (second_part_length > 0) {
					System.arraycopy(items, 0, new_items, first_part_length, second_part_length);
				}
			}
		}

		read = 0;
		write = size;
		items = new_items;
	}
}
