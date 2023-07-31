package fi.henu.gdxextras.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import fi.henu.gdxextras.ByteQueue;

public class RSA
{
	public static KeyPair generateKeyPair(int keysize)
	{
		KeyPairGenerator key_gen;
		try {
			key_gen = KeyPairGenerator.getInstance("RSA");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize RSA algorithm!");
		}
		key_gen.initialize(keysize);
		return key_gen.generateKeyPair();
	}

	public static byte[] privateKeyToBytes(PrivateKey private_key)
	{
		PKCS8EncodedKeySpec private_key_pkcs8_encoded_spec = new PKCS8EncodedKeySpec(private_key.getEncoded());
		return private_key_pkcs8_encoded_spec.getEncoded();
	}

	public static void privateKeyToBytes(ByteQueue result, PrivateKey private_key)
	{
		byte[] private_key_bytes = privateKeyToBytes(private_key);
		result.writeBytes(private_key_bytes, private_key_bytes.length);
	}

	public static PrivateKey bytesToPrivateKey(byte[] bytes)
	{
		KeyFactory key_factory;
		try {
			key_factory = KeyFactory.getInstance("RSA");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize RSA algorithm!");
		}
		try {
			return key_factory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
		}
		catch (InvalidKeySpecException e) {
			throw new RuntimeException("Unable to load RSA private key from bytes!");
		}
	}

	public static PrivateKey bytesToPrivateKey(ByteQueue bytes)
	{
		byte[] private_key_bytes = new byte[bytes.getSize()];
		try {
			bytes.readBytes(private_key_bytes, bytes.getSize());
		}
		// This should never happen, as we are reading all the data there is
		catch (ByteQueue.InvalidData err) {
		}
		return bytesToPrivateKey(private_key_bytes);
	}

	public static byte[] publicKeyToBytes(PublicKey public_key)
	{
		X509EncodedKeySpec public_key_x509_encoded_spec = new X509EncodedKeySpec(public_key.getEncoded());
		return public_key_x509_encoded_spec.getEncoded();
	}

	public static void publicKeyToBytes(ByteQueue result, PublicKey public_key)
	{
		byte[] public_key_bytes = publicKeyToBytes(public_key);
		result.writeBytes(public_key_bytes, public_key_bytes.length);
	}

	public static PublicKey bytesToPublicKey(byte[] bytes)
	{
		// Load public key
		KeyFactory key_factory;
		try {
			key_factory = KeyFactory.getInstance("RSA");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize RSA algorithm!");
		}
		try {
			return key_factory.generatePublic(new X509EncodedKeySpec(bytes));
		}
		catch (InvalidKeySpecException e) {
			throw new RuntimeException("Unable to load RSA public key from bytes!");
		}
	}

	public static PublicKey bytesToPublicKey(ByteQueue bytes)
	{
		byte[] public_key_bytes = new byte[bytes.getSize()];
		try {
			bytes.readBytes(public_key_bytes, bytes.getSize());
		}
		// This should never happen, as we are reading all the data there is
		catch (ByteQueue.InvalidData err) {
		}
		return bytesToPublicKey(public_key_bytes);
	}

	public static void sign(ByteQueue result, byte[] data, PrivateKey private_key)
	{
		Signature private_signature;
		byte[] signature;
		try {
			private_signature = Signature.getInstance("SHA256withRSA");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize SHA256+RSA algorithm");
		}
		try {
			private_signature.initSign(private_key);
		}
		catch (InvalidKeyException e) {
			throw new RuntimeException("Invalid RSA private key!");
		}
		try {
			private_signature.update(data);
			signature = private_signature.sign();
		}
		catch (SignatureException e) {
			throw new RuntimeException("Unable to create a SHA256+RSA signature!");
		}
		result.writeBytes(signature, signature.length);
	}

	public static boolean verifySignature(byte[] signature, byte[] data, PublicKey public_key)
	{
		Signature public_signature;
		try {
			public_signature = Signature.getInstance("SHA256withRSA");
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to initialize SHA256+RSA algorithm!");
		}
		try {
			public_signature.initVerify(public_key);
		}
		catch (InvalidKeyException e) {
			throw new RuntimeException("Invalid RSA public key!");
		}
		try {
			public_signature.update(data);
			return public_signature.verify(signature);
		}
		catch (SignatureException e) {
			return false;
		}
	}
}
