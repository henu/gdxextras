package fi.henu.gdxextras.network;

import com.badlogic.gdx.utils.Array;

public class Server
{
	public Server()
	{
		conns = new Array<Connection>(false, 0);
		new_conns = new Array<Connection>(false, 0);
	}

	public void dispose()
	{
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

	public void run()
	{
		// Clean closed connections
		synchronized (conns) {
			for (int i = 0; i < conns.size; ) {
				Connection conn = conns.get(i);
				if (conn.isClosed()) {
					conn.dispose();
					conns.removeIndex(i);
				} else {
					++ i;
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
}
