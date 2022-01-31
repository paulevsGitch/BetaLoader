package modloadermp;

import lombok.SneakyThrows;
import net.minecraft.network.PacketHandler;
import net.minecraft.packet.AbstractPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet230ModLoader extends AbstractPacket {
	private static final int MAX_DATA_LENGTH = 65535;
	public int modId;
	public int packetType;
	public int[] dataInt;
	public float[] dataFloat;
	public String[] dataString;
	
	public Packet230ModLoader() {
		this.dataInt = new int[0];
		this.dataFloat = new float[0];
		this.dataString = new String[0];
	}
	
	@Override
	@SneakyThrows
	public void read(final DataInputStream datainputstream) {
		try {
			this.modId = datainputstream.readInt();
			this.packetType = datainputstream.readInt();
			final int i = datainputstream.readInt();
			if (i > MAX_DATA_LENGTH) {
				throw new IOException(String.format("Integer data size of %d is higher than the max (%d).",
					new Object[] {i, MAX_DATA_LENGTH}
				));
			}
			this.dataInt = new int[i];
			for (int j = 0; j < i; ++j) {
				this.dataInt[j] = datainputstream.readInt();
			}
			final int k = datainputstream.readInt();
			if (k > MAX_DATA_LENGTH) {
				throw new IOException(String.format("Float data size of %d is higher than the max (%d).",
					new Object[] {k, MAX_DATA_LENGTH}
				));
			}
			this.dataFloat = new float[k];
			for (int l = 0; l < k; ++l) {
				this.dataFloat[l] = datainputstream.readFloat();
			}
			final int i2 = datainputstream.readInt();
			if (i2 > MAX_DATA_LENGTH) {
				throw new IOException(String.format("String data size of %d is higher than the max (%d).",
					new Object[] {i2, MAX_DATA_LENGTH}
				));
			}
			this.dataString = new String[i2];
			for (int j2 = 0; j2 < i2; ++j2) {
				final int k2 = datainputstream.readInt();
				if (k2 > MAX_DATA_LENGTH) {
					throw new IOException(String.format("String length of %d is higher than the max (%d).",
						new Object[] {k2, MAX_DATA_LENGTH}
					));
				}
				final byte[] abyte0 = new byte[k2];
				datainputstream.read(abyte0, 0, k2);
				this.dataString[j2] = new String(abyte0);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	@SneakyThrows
	public void write(final DataOutputStream dataoutputstream) {
		try {
			if (this.dataInt != null && this.dataInt.length > MAX_DATA_LENGTH) {
				throw new IOException(String.format(
					"Integer data size of %d is higher than the max (%d).",
					new Object[] {this.dataInt.length, MAX_DATA_LENGTH}
				));
			}
			if (this.dataFloat != null && this.dataFloat.length > MAX_DATA_LENGTH) {
				throw new IOException(String.format(
					"Float data size of %d is higher than the max (%d).",
					new Object[] {this.dataFloat.length, MAX_DATA_LENGTH}
				));
			}
			if (this.dataString != null && this.dataString.length > MAX_DATA_LENGTH) {
				throw new IOException(String.format(
					"String data size of %d is higher than the max (%d).",
					new Object[] {this.dataString.length, MAX_DATA_LENGTH}
				));
			}
			dataoutputstream.writeInt(this.modId);
			dataoutputstream.writeInt(this.packetType);
			if (this.dataInt == null) {
				dataoutputstream.writeInt(0);
			}
			else {
				dataoutputstream.writeInt(this.dataInt.length);
				for (int i = 0; i < this.dataInt.length; ++i) {
					dataoutputstream.writeInt(this.dataInt[i]);
				}
			}
			if (this.dataFloat == null) {
				dataoutputstream.writeInt(0);
			}
			else {
				dataoutputstream.writeInt(this.dataFloat.length);
				for (int j = 0; j < this.dataFloat.length; ++j) {
					dataoutputstream.writeFloat(this.dataFloat[j]);
				}
			}
			if (this.dataString == null) {
				dataoutputstream.writeInt(0);
			}
			else {
				dataoutputstream.writeInt(this.dataString.length);
				for (int k = 0; k < this.dataString.length; ++k) {
					if (this.dataString[k].length() > MAX_DATA_LENGTH) {
						throw new IOException(String.format(
							"String length of %d is higher than the max (%d).",
							new Object[] {this.dataString[k].length(), MAX_DATA_LENGTH}
						));
					}
					dataoutputstream.writeInt(this.dataString[k].length());
					dataoutputstream.writeBytes(this.dataString[k]);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void apply(final PacketHandler nethandler) {
		ModLoaderMp.HandleAllPackets(this);
	}
	
	@Override
	public int length() {
		int i = 1;
		++i;
		i = ++i + ((this.dataInt != null) ? (this.dataInt.length * 32) : 0);
		i = ++i + ((this.dataFloat != null) ? (this.dataFloat.length * 32) : 0);
		++i;
		if (this.dataString != null) {
			for (int j = 0; j < this.dataString.length; ++j) {
				i = ++i + this.dataString[j].length();
			}
		}
		return i;
	}
}
