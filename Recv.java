import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
/* include any additional imports below this line */
import java.io.InputStream;
import java.net.Socket;

/* end imports */

public class Recv {
	private Socket      socket = null;
	private InputStream inputStream = null;
	private List<Packet>packetList = null;  // not used; NOC

	private long startTime = 0;
	private FileOutputStream fileOutputStream = null;

	public Recv(String host, int port) throws IOException {

		// ~~ establish a connection using Socket
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			startTime = System.currentTimeMillis(); // ~~ start timer
		}
		catch (IOException e){
			System.out.println(
			"Error caught when listening to port number " + port);
			System.out.println(e.getMessage());
		}

		System.out.println("Connected to " +
		socket.getInetAddress().getHostAddress() +
		" on port " + socket.getPort());
	}

	public void close() throws IOException { 
		inputStream.close();
		fileOutputStream.close();
		socket.close();
	}

	// get_pkts: ADC - you may modify this method, but not signature
	public int get_pkts(String fname) throws IOException {
		byte[] buff     = new byte[1506];  // buffer to get pkts off wire
		int    npkt     = 0;               // no of pkts recd
		int    bytes    = 0;
		int    totBytes = 0;
		long delay    = 0;
		long totDelay = 0;

		// loop, get pkts, write to file fname 
		fileOutputStream = new FileOutputStream(fname);

		while((bytes = inputStream.read(buff, 0, 1506)) > 0) {
                  npkt++;
	 	  // ~~ current packet delay, reset timer
		  delay = System.currentTimeMillis() - startTime; 
		  startTime = System.currentTimeMillis(); 
		  // ~~ save number of packets, bytes, total delay
		  totBytes += bytes - 6;
		  totDelay += delay;
		  // ~~ print each packet delay
		  System.out.println("Pkt " + npkt + " delay = " + delay + " ms");
	  	    Packet pkt = new Packet(buff);
	            pkt.write(fileOutputStream);
		}
		// out of loop; print results
		if (npkt == 0){
		  System.out.println("No packets received");	
		} else {
		  System.out.println(
		    "Total " + npkt + " packets / " + totBytes + " recd. " +
		    "Total delay = " + totDelay + " ms, average = " + 
		    totDelay/npkt + " ms");
		}
		return npkt;
	}

	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.out.println("Usage: host port filename");
			return;
		}
		String hostname = args[0], fileName = args[2];
		int port = Integer.parseInt(args[1]);
		Recv recv = new Recv(hostname, port);

		// ADC: your code here, 4 lines max
		recv.get_pkts(fileName);
		recv.close();
	}
}

class Packet {
	private byte[] buff;  // NOC
	private int seqNo;    // NOC
	private short len;    // NOC

	// Ctor1: make Packet out of raw data "buff" which came off wire
	// ADC to the method but don't change the signature
	public Packet(byte[] buff) {
		// ~~ 4 bytes for sequence code (int), 2 bytes for length (short)
		seqNo = ToInt(new byte[] {buff[0], buff[1], buff[2], buff[3]});
		len = ToShort(new byte[] {buff[4], buff[5]});
		this.buff = new byte[len];
		for(int i = 0; i < len; i++){
			this.buff[i] = buff[i + 6];
		}
	}

	private short ToShort(byte[] b) {
		// ADC: can't use bit-shift operators; math only
		// || short=16bits byte=8bits short = byte << 8bits + byte
		// || << 8 = * 2^8 = 256
		// || casting to int maintains negative values
		// || if b[0] = 0b10000000 then (int)b[0] = 0xFFFFFF80
		short firstByte, secondByte;
		firstByte = (short)((int)b[0] * 256); //|| b[0] << 8
		if (b[1] < 0) {
			secondByte = (short)((int)b[1] + 0x00000100); //|| 
		} else {
			secondByte = (short)((int)b[1]);
		}

		return (short)(firstByte + secondByte);
	}

	private int ToInt(byte[] b) {
	  // ADC: can't use bit-shift operators; math only
	  // || int=32bits byte=8bits
	  // || int = byte << 24 bits + byte << 16 bits 
	  // || + byte << 8 bits + byte
	  // || 2^24 = 16777216 2^16 = 65536 2^8 = 256
	  int firstByte, secondByte, thirdByte, fourthByte;
	  firstByte = (int)b[0] * 1677216;
	  if (b[1] < 0) {
		secondByte = (int)b[1] * 65536 + 0x01000000;
	  } else {
		secondByte = (int)b[1] * 65536;
	  }
	  if (b[2] < 0) {
		thirdByte = (int)b[2] * 256 + 0x00010000;
	  } else {
		thirdByte = (int)b[2] * 256;
	  }
	  if (b[3] < 0) {
		fourthByte = (int)b[3] + 0x00000100;
	  } else {
		fourthByte = (int)b[3];
	  }
	  return firstByte + secondByte + thirdByte + fourthByte;
	}

	private byte[] ShortToByte(short value) {
		// your code here: must use this to convert short to byte[]
		byte [] newByte = new byte[2];
		newByte[0] = (byte)(value / 256);
		newByte[1] = (byte)value;
		return newByte;
	}

	private byte[] IntToByte(int value) {
		// your code here: must use this to convert int to byte[]
		byte [] newByte = new byte[4];
		newByte[0] = (byte)(value / 1677216);
		newByte[1] = (byte)(value / 65536);
		newByte[2] = (byte)(value / 256);
		newByte[3] = (byte)value;
		return newByte;
	}

	public int getSeqNo() { return seqNo; } // NOC

	public short getLen() { return len; }   // NOC

	// write packet payload to the end of file "fp"
	public void write(FileOutputStream fp) throws IOException {
		// ADC
		// ~~ write into fp with FileOutputStream
		fp.write(buff);
	}

	// your methods etc here
}
