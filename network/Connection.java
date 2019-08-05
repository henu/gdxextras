package fi.henu.gdxextras.network;

import java.io.IOException;
import java.net.Socket;

import fi.henu.gdxextras.ByteQueue;
import fi.henu.gdxextras.containers.RingBuffer;

public class Connection
{
	public Connection(Server server)
	{
		// Create another end of local connection
		local_conn_server_end = new Connection(this);
		local_conn_client_end = null;

		// Inform server about this
		server.register_local_connection_server_end(local_conn_server_end);

		// This is local connection, so set remote stuff null
		socket = null;
		reader = null;
		writer = null;
		reader_thread = null;
		writer_thread = null;

		received_messages = new RingBuffer<NetworkMessage>();
	}

	public Connection(String host, int port)
	{
		try {
			socket = new Socket(host, port);
		}
		catch (IOException e) {
			throw new RuntimeException("Connection failed!");
		}

		reader = new Reader(this);
		writer = new Writer();

		reader_thread = new Thread(reader);
		writer_thread = new Thread(writer);

		// This is remote connection, so set local stuff null
		local_conn_server_end = null;
		local_conn_client_end = null;

		received_messages = new RingBuffer<NetworkMessage>();

		// Start threads
		reader_thread.start();
		writer_thread.start();
	}

	public void close()
	{
		closed = true;

		// Close everything
		if (socket != null) {
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
		}

		dispose();
	}

	public void dispose()
	{
		if (reader_thread != null) {
			try {
				reader_thread.join();
			}
			catch (InterruptedException e) {
			}
			reader_thread = null;
		}
		if (writer_thread != null) {
			synchronized (writer.queue) {
				writer.queue.notify();
			}
			try {
				writer_thread.join();
			}
			catch (InterruptedException e) {
			}
			writer_thread = null;
		}
	}

	public void sendMessage(NetworkMessage msg)
	{
		if (local_conn_server_end != null) {
			synchronized (local_conn_server_end.received_messages) {
				local_conn_server_end.received_messages.add(msg);
			}
		} else if (local_conn_client_end != null) {
			synchronized (local_conn_client_end.received_messages) {
				local_conn_client_end.received_messages.add(msg);
			}
		} else {
			assert writer != null;
			synchronized (writer.queue) {
				writer.queue.writeInt(msg.type);
				if (msg.data != null && msg.data.getSize() > 0) {
					writer.queue.writeInt(msg.data.getSize());
					writer.queue.writeBytes(msg.data);
				} else {
					writer.queue.writeInt(0);
				}
				writer.queue.notify();
			}
		}
	}

	public NetworkMessage get_received_message()
	{
		synchronized (received_messages) {
			if (received_messages.empty()) {
				return null;
			}
			return received_messages.pop();
		}
	}

	public boolean isClosed()
	{
		if (closed) return true;
		if (socket == null) return false;
		return socket.isClosed() || socket.isOutputShutdown() || socket.isInputShutdown();
	}

	Connection(Socket socket)
	{
		this.socket = socket;

		reader = new Reader(this);
		writer = new Writer();

		reader_thread = new Thread(reader);
		writer_thread = new Thread(writer);

		// This is remote connection, so set local stuff null
		local_conn_server_end = null;
		local_conn_client_end = null;

		received_messages = new RingBuffer<NetworkMessage>();

		// Start threads
		reader_thread.start();
		writer_thread.start();
	}

	private class Reader implements Runnable
	{
		public final ByteQueue queue;

		public Reader(Connection conn)
		{
			this.conn = conn;
			queue = new ByteQueue();
			new_message_header_got = false;
		}

		@Override
		public void run()
		{
			final byte[] buf = new byte[1024 * 64];

			int buf_size;
			while (true) {
				// If socket is closed
				if (closed || socket.isClosed() || socket.isOutputShutdown()) {
					closed = true;
					break;
				}

				// Read
				try {
					buf_size = socket.getInputStream().read(buf);
				} catch (IOException e) {
					closed = true;
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
					NetworkMessage new_msg = null;
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
								new_msg = new NetworkMessage(new_message_type, queue.readBytequeue(new_message_len));
							} else {
								new_msg = new NetworkMessage(new_message_type, null);
							}
							new_message_header_got = false;
						}
					}
					if (new_msg == null) {
						break;
					}
					synchronized (conn.received_messages) {
						conn.received_messages.add(new_msg);
					}
				}
			}
		}

		private final Connection conn;
		private boolean new_message_header_got;
		private int new_message_type;
		private int new_message_len;
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
				if (closed || socket.isClosed() || socket.isOutputShutdown()) {
					closed = true;
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
						}
					}
				}

				// Send
				if (buf_size > 0) {
					try {
						socket.getOutputStream().write(buf, 0, buf_size);
					}
					catch (IOException e) {
						closed = true;
						return;
					}
				}
			}
		}
	}

	// For local connection
	private final Connection local_conn_server_end;
	private final Connection local_conn_client_end;

	// For remote connection
	private boolean closed;
	private final Socket socket;
	private final Reader reader;
	private final Writer writer;
	private Thread reader_thread;
	private Thread writer_thread;

	private final RingBuffer<NetworkMessage> received_messages;

	private Connection(Connection local_conn_client_end)
	{
		local_conn_server_end = null;
		this.local_conn_client_end = local_conn_client_end;
		socket = null;
		reader = null;
		writer = null;
		reader_thread = null;
		writer_thread = null;
		received_messages = new RingBuffer<NetworkMessage>();
	}
}
