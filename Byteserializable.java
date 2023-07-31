package fi.henu.gdxextras;

public interface Byteserializable
{
	void serializeToBytes(Bytes bytes);

	void serialize(ByteQueue data);
	void deserialize(ByteQueue data) throws ByteQueue.InvalidData;
}
