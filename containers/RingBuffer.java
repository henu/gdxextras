package fi.henu.gdxextras.containers;

import java.nio.BufferUnderflowException;

public class RingBuffer<T>
{
	public void add(T item)
	{
		ensureSpace(items + 1);
		arr[write] = item;
		write = (write + 1) % arr.length;
		items += 1;
	}

	public T pop()
	{
		if (items <= 0) {
			throw new BufferUnderflowException();
		}
		T result = arr[read];
		read = (read + 1) % arr.length;
		--items;
		ensureSpace(items);
		return result;
	}

	public int size()
	{
		return items;
	}

	public boolean empty()
	{
		return items == 0;
	}

	private T[] arr;

	private int read;
	private int write;
	private int items;

	private void ensureSpace(int new_size)
	{
		// Initial allocation
		if (arr == null) {
			if (new_size > 0) {
				arr = (T[])new Object[new_size];
			}
			return;
		}

		// If there is too much space
		if (new_size * 4 < arr.length) {
// TODO: Code this!
			return;
		}

		// If there is enough space already
		if (new_size <= arr.length) {
			return;
		}

		// If bigger space is needed
		new_size = Math.max(new_size, arr.length * 2);
		T[] new_arr = (T[])new Object[new_size];

		// Copy items so they are at the beginning of new items
		if (items > 0) {
			if (read < write) {
				System.arraycopy(arr, read, new_arr, 0, items);
			} else {
				int first_part_length = arr.length - read;
				System.arraycopy(arr, read, new_arr, 0, first_part_length);
				int second_part_length = items - first_part_length;
				if (second_part_length > 0) {
					System.arraycopy(arr, 0, new_arr, first_part_length, second_part_length);
				}
			}
		}

		read = 0;
		write = items;
		arr = new_arr;
	}
}
