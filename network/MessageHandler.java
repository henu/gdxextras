package fi.henu.gdxextras.network;

public interface MessageHandler
{
	// Return true if message was consumed
	boolean consumeReceivedMessage(Connection conn, NetworkMessage msg);
}
