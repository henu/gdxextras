package fi.henu.gdxextras.network;

import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
	public Server()
	{
		conns = new Array<Connection>(false, 0);
		new_conns = new Array<Connection>(false, 0);

		listening_socket = null;

		keep_running = true;
		thread = new Thread(this);
		thread.start();
	}

	public Server(int port)
	{
		conns = new Array<Connection>(false, 0);
		new_conns = new Array<Connection>(false, 0);

		try {
			listening_socket = new ServerSocket(port);
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to listen port " + port + "!");
		}

		keep_running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void dispose()
	{
		// Stop accepting new connections
		if (listening_socket != null) {
			try {
				listening_socket.close();
			}
			catch (IOException e) {
			}
			listening_socket = null;
		}

		// Stop thread and clean it
		keep_running = false;
		if (thread != null) {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
			}
			thread = null;
		}

		// Close connections
		synchronized (conns) {
			for (Connection conn : conns) {
				conn.close();
			}
			conns.clear();
		}
		synchronized (new_conns) {
			new_conns.clear();
		}
	}

	public Connection get_new_connection()
	{
		synchronized (new_conns) {
			if (new_conns.isEmpty()) {
				return null;
			}
			return new_conns.pop();
		}
	}

	@Override
	public void run()
	{
		while (keep_running) {
			// Clean closed connections
			synchronized (conns) {
				for (int i = 0; i < conns.size; ) {
					Connection conn = conns.get(i);
					if (conn.isClosed()) {
						conn.dispose();
						conns.removeIndex(i);
					} else {
						++i;
					}
				}
			}

			// Try to accept new connection.
			// In case of I/O error, quit thread.
			if (listening_socket != null) {
				Socket client_socket;
				try {
					client_socket = listening_socket.accept();
				}
				catch (IOException e) {
					break;
				}

				Connection conn = new Connection(client_socket);
				synchronized (conns) {
					conns.add(conn);
				}
				synchronized (new_conns) {
					new_conns.add(conn);
				}
			}
		}
	}

	void register_local_connection_server_end(Connection conn)
	{
		synchronized (conns) {
			conns.add(conn);
		}
		synchronized (new_conns) {
			new_conns.add(conn);
		}
	}

	private final Array<Connection> conns;
	private final Array<Connection> new_conns;

	private ServerSocket listening_socket;

	private Thread thread;
	private boolean keep_running;
}