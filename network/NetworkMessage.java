package fi.henu.gdxextras.network;

import fi.henu.gdxextras.ByteQueue;

public class NetworkMessage
{
	public final int type;
	public final ByteQueue data;

	public NetworkMessage(int type)
	{
		this.type = type;
		this.data = new ByteQueue();
	}

	public NetworkMessage(int type, ByteQueue data)
	{
		this.type = type;
		this.data = data;
	}
}
