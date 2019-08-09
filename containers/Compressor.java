package fi.henu.gdxextras.containers;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fi.henu.gdxextras.ByteQueue;

public class Compressor
{
	public static void compress(ByteQueue result, ByteQueue data, int size)
	{
		Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);

		// Set input
		byte[] input = new byte[size];
		data.readBytes(input, size);
		compresser.setInput(input);
		compresser.finish();

		// Compress
		byte[] output = new byte[1024];
		int output_size;
		while ((output_size = compresser.deflate(output)) > 0) {
			result.writeBytes(output, output_size);
		}
		compresser.end();
	}

	public static void uncompress(ByteQueue result, ByteQueue data, int size)
	{
		Inflater decompresser = new Inflater();

		// Set input
		byte[] input = new byte[size];
		data.readBytes(input, size);
		decompresser.setInput(input);

		// Decompress
		byte[] output = new byte[1024];
		int output_size;
		try {
			while ((output_size = decompresser.inflate(output)) > 0) {
				result.writeBytes(output, output_size);
			}
		}
		catch (DataFormatException e) {
			throw new RuntimeException("Unable to uncompress data!");
		}
		decompresser.end();
	}
}
