package fi.henu.gdxextras.network;

import java.io.IOException;
import java.net.Socket;

import fi.henu.gdxextras.ByteQueue;
import fi.henu.gdxextras.containers.RingBuffer;

public class Connection
{
	public Connection(Server server)
	{
		sleep_cond = new Object();

		// This is client side connection, so it must not know about server
		this.server = null;

		message_handler = null;

		// Create another end of local connection
		local_conn_server_end = new Connection(server, this);
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

	public Connection(Server server, MessageHandler message_handler)
	{
		sleep_cond = new Object();

		// This is client side connection, so it must not know about server
		this.server = null;

		this.message_handler = message_handler;

		// Create another end of local connection
		local_conn_server_end = new Connection(server, this);
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
		sleep_cond = new Object();

		server = null;
		message_handler = null;

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

	public Connection(String host, int port, MessageHandler message_handler)
	{
		sleep_cond = new Object();

		server = null;
		this.message_handler = message_handler;

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

	// Like traditional sleep function, but awakes
	// if there are any received messages waiting.
	public void sleep(long millis)
	{
		synchronized (sleep_cond) {
			try {
				sleep_cond.wait(millis);
			}
			catch (InterruptedException e) {
			}
		}
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

	public NetworkMessage getReceivedMessage()
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

	Connection(Server server, Socket socket)
	{
		sleep_cond = new Object();

		this.server = server;
		this.socket = socket;

		message_handler = server.getMessageHandler();

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
								try {
									new_message_type = queue.readInt();
									new_message_len = queue.readInt();
								}
								// This should never happen, as queue size is checked above
								catch (ByteQueue.InvalidData err) {
								}
								new_message_header_got = true;
							}
						}
						if (new_message_header_got && queue.getSize() >= new_message_len) {
							if (new_message_len > 0) {
								try {
									new_msg = new NetworkMessage(new_message_type, queue.readBytequeue(new_message_len));
								}
								// This should never happen, as queue size is checked above
								catch (ByteQueue.InvalidData err) {
								}
							} else {
								new_msg = new NetworkMessage(new_message_type, null);
							}
							new_message_header_got = false;
						}
					}
					if (new_msg == null) {
						break;
					}
					// Check if message handler would like to consume the message
					if (message_handler != null && message_handler.consumeReceivedMessage(conn, new_msg)) {
						continue;
					}
					// Add message to queue
					synchronized (conn.received_messages) {
						conn.received_messages.add(new_msg);
					}
					// Inform possible sleepers that there are new messages available
					if (server != null) {
						synchronized (server.getSleepCondition()) {
							server.getSleepCondition().notifyAll();
						}
					}
					synchronized (sleep_cond) {
						sleep_cond.notifyAll();
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
						try {
							queue.readBytes(buf, buf_size);
						}
						// This should never happen, as queue size is checked above
						catch (ByteQueue.InvalidData err) {
						}
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

	private final Server server;

	private final MessageHandler message_handler;

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

	private final Object sleep_cond;

	private Connection(Server server, Connection local_conn_client_end)
	{
		sleep_cond = new Object();

		this.server = server;
		message_handler = server.getMessageHandler();
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
