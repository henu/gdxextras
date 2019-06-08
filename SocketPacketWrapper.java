package fi.henu.gdxextras;

import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.net.Socket;

public class SocketPacketWrapper
{
	public static class Message
	{
		public int type;
		public ByteQueue buf;

		public Message(int type, ByteQueue buf)
		{
			this.type = type;
			this.buf = buf;
		}
	}

	public SocketPacketWrapper(Socket socket)
	{
		this.socket = socket;

		reader = new Reader();
		writer = new Writer();

		reader_thread = new Thread(reader);
		writer_thread = new Thread(writer);

		reader_thread.start();
		writer_thread.start();
	}

	public Message readMessageIfAvailable()
	{
		synchronized (reader.msg_queue) {
			if (reader.msg_queue.isEmpty()) {
				return null;
			}
			return reader.msg_queue.removeIndex(0);
		}
	}

	public void close()
	{
		// Close everything
		try {
			socket.close();
		}
		catch (IOException e) {
		}
		try {
			socket.shutdownInput();
		}
		catch (IOException e) {
		}
		try {
			socket.shutdownOutput();
		}
		catch (IOException e) {
		}

		// Join threads
		try {
			reader_thread.join();
		}
		catch (InterruptedException e) {
		}
		try {
			writer_thread.join();
		}
		catch (InterruptedException e) {
		}
	}

	public void sendMessage(int type, ByteQueue bytes)
	{
		synchronized (writer.queue) {
			writer.queue.writeInt(type);
			if (bytes != null) {
				writer.queue.writeInt(bytes.getSize());
				writer.queue.writeBytes(bytes);
			} else {
				writer.queue.writeInt(0);
			}
			writer.queue.notify();
		}
	}

	private class Reader implements Runnable
	{
		public final ByteQueue queue;

		private boolean new_message_header_got;
		private int new_message_type;
		private int new_message_len;

		private final Array<Message> msg_queue;

		public Reader()
		{
			queue = new ByteQueue();
			new_message_header_got = false;
			msg_queue = new Array<Message>();
		}

		@Override
		public void run()
		{
			final byte[] buf = new byte[1024 * 64];

			int buf_size;
			while (true) {
				// If socket is closed
				if (socket.isClosed() || socket.isOutputShutdown()) {
					break;
				}

				// Read
				try {
					buf_size = socket.getInputStream().read(buf);
				} catch (IOException e) {
					return;
				}
				if (buf_size < 0) {
					return;
				}

				// Store chunk to queue
				synchronized (queue) {
					queue.writeBytes(buf, buf_size);
				}

				// If enough data is available, then create a new messages
				while (true) {
					Message new_msg = null;
					synchronized (queue) {
						if (!new_message_header_got) {
							if (queue.getSize() >= 8) {
								new_message_type = queue.readInt();
								new_message_len = queue.readInt();
								new_message_header_got = true;
							}
						}
						if (new_message_header_got && queue.getSize() >= new_message_len) {
							if (new_message_len > 0) {
								new_msg = new Message(new_message_type, queue.readBytequeue(new_message_len));
							} else {
								new_msg = new Message(new_message_type, null);
							}
							new_message_header_got = false;
						}
					}
					if (new_msg == null) {
						break;
					}
					synchronized (msg_queue) {
						msg_queue.add(new_msg);
					}
				}
			}
		}
	}

	private class Writer implements Runnable
	{
		public final ByteQueue queue;

		public Writer()
		{
			queue = new ByteQueue();
		}

		@Override
		public void run()
		{
			final byte[] buf = new byte[1024 * 64];

			while (true) {
				// If socket is closed
				if (socket.isClosed() || socket.isOutputShutdown()) {
					break;
				}

				// Get chunk to send
				int buf_size;
				synchronized (queue) {
					buf_size = Math.min(buf.length, queue.getSize());
					if (buf_size > 0) {
						queue.readBytes(buf, buf_size);
					} else {
						try {
							queue.wait();
						}
						catch (InterruptedException e) {
							return;
						}
					}
				}

				// Send
				if (buf_size > 0) {
					try {
						socket.getOutputStream().write(buf, 0, buf_size);
					}
					catch (IOException e) {
						return;
					}
				}
			}
		}
	}

	private final Socket socket;

	private final Reader reader;
	private final Writer writer;

	private final Thread reader_thread;
	private final Thread writer_thread;
}
