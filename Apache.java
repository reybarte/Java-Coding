import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
// other imports go here
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
/*--------------- end imports --------------*/

class Apache {

	// NOC these 3 fields
	private byte []     HOST ;      /* should be initialized to 1024 bytes in the constructor */
	private int         PORT ;      /* port this Apache should listen on, from cmdline        */
	private InetAddress PREFERRED ; /* must set this in dns() */
	// ADC additional fields here
	private byte[]      FILE;      /* name of the file in URL, if you like */
	private int			REQUESTS;	//Counter for the request number
	private int			hostLen; //number of valid characters in HOST
	private byte[]		req; //full http header in bytes

	public static void main(String [] a) { // NOC - do not change main()
		Apache apache = new Apache(Integer.parseInt(a[0]));
		apache.run(2);
	}

	Apache(int port) {
		PORT = port;
		// other init stuff ADC here
		HOST = new byte[1024];
		REQUESTS = 0;
	}
	// Note: parse() must set HOST correctly
	//|| use this to get the hostname part only out of the given input
	int parse(byte [] buffer)
	{
		int bufferCount = 0;
		int pathStart = 0;
		int hostStart = 0;
		int hostEnd = 0;
		int pathLen = 0;
		boolean methodFound = false;
		boolean hostFound = false;
		boolean versionFound = false;
		boolean http = true;
		boolean ftp = false;
		boolean absolutePath = false;

		byte[] method = {71, 69, 84};
		//check first line of request if its proper
		//check for method until space is encountered
		//System.out.println("method");
		while(!methodFound) {
			//check if method is "GET"
			if(buffer[bufferCount] == method[0]){
				methodFound = true;
				for (int i = 1; i < method.length; i++, bufferCount++){
					if(buffer[bufferCount + 1] != method[i]){
						methodFound = false;
						break;
					}	
				}
			}//else if for other methods 
			else {
				return 400;
			}
			bufferCount++;
		}
		if (!methodFound) {
			return 400;	
		}
		bufferCount++;
		//check for path until space is encountered
		//byte array for 'http://'
		byte [] absolute = {104, 116, 116, 112, 58, 47, 47};
		pathStart = bufferCount;
		if(buffer[bufferCount] == absolute[0]) {
			absolutePath = true;
			for (int i = 1; i < absolute.length; i++) {
				if(buffer[bufferCount + i] != absolute[i]) {
					absolutePath = false;
					http = false;
					break;
				}	
			}
			if(absolutePath){
				bufferCount += absolute.length;
				hostStart = bufferCount;
			}
		}

		//look for "ftp://"
		byte [] ftpArr = {102, 116, 112, 58, 47, 47};
		if(buffer[bufferCount] == ftpArr[0]) {
			absolutePath = true;
			ftp = true;
			for(int i = 1; i < ftpArr.length; i++) {
				if(buffer[bufferCount + i] != ftpArr[i]) {
					absolutePath = false;
					ftp = false;
					break;
				}
			}
			if(ftp) {
				bufferCount += ftpArr.length;
				ftp = true;
				http = false;
				hostStart = bufferCount;
			}
		}

		//look for hostname if given absolute path
		while(absolutePath) {
			if(buffer[bufferCount] == 47 || buffer[bufferCount] == 32){
				hostEnd = bufferCount;	
				hostFound = true;
				break;
			}
			bufferCount++;
		}
		if(hostFound) {
			hostLen = hostEnd - hostStart;
			for(int i = 0; i < hostLen; i++){
				HOST[i] = buffer[hostStart + i];	
			}
			while(buffer[bufferCount] != 32) {
				pathLen++;	
				bufferCount++;
			}
			if(pathLen == 0){
				pathLen = 1;	
			}
			FILE = new byte[pathLen];
			if(pathLen == 1) {
				FILE[0] = 47;
			} else {
				for(int i = 0; i < pathLen; i++) {
					FILE[i] = buffer[hostEnd + i];
				}
			}
		}

		//System.out.println("relative path");
		//if not absolute path then relative path is given
		if(!absolutePath){
			bufferCount = pathStart;
			//if first character of where path should be is not '/' then
			//faultyrequest
			if (buffer[bufferCount] == 47) {
				while(buffer[bufferCount] != 32) {
					pathLen++;
					bufferCount++;
				}
				FILE = new byte[pathLen];
				for(int i = 0; i < pathLen; i++) {
					FILE[i] = buffer[pathStart + i];	
				}
			} else {
				return 400;
			}
		}
		bufferCount++;

		//check HTTP version
		//byte array for 'HTTP/1.1'
		byte[] version = {72, 84, 84, 80, 47, 49, 46, 49};
		for (int i = 0; i < version.length; i++) {
			if(buffer[bufferCount + i] != version[i]) {
				return 400;
			}
		}
		bufferCount += version.length;

		//System.out.println("relative host: " + hostFound);
		//check for hostname if not given absolute url
		//look for 'Host: ' in http request header/
		if (!absolutePath) {
			byte[] host = {72, 111, 115, 116, 58, 32};
			while(bufferCount < buffer.length && buffer[bufferCount] != 0) {
				if (buffer[bufferCount] == host[0]) {
					hostFound = true;
					for(int i = 0; i < host.length; i++, bufferCount++) {
						if(buffer[bufferCount] != host[i]){
							hostFound = false;
							break;	
						}	
					}
					if (hostFound) {
						hostStart = bufferCount;
						int counter = 0;
						while(buffer[bufferCount] != 13 && buffer[bufferCount] != 10) {
							HOST[counter] = buffer[hostStart + counter];
							hostLen++;
							counter++;
							bufferCount++;
						}
						break;
					}
				}
				bufferCount++;
			}
		}
	/*	
		System.out.println("end");
		if(absolutePath && (http || ftp) || (!absolutePath && http)) {
			System.out.println("Host: " + (new String(HOST)).substring(0, hostLen));	
			System.out.println("Path: " + new String(FILE));
		}
		
		System.out.println("HOST: " + hostFound);
		System.out.println("HTTP: " + http);
		System.out.println("FTP: " + ftp);
	*/	
		if(http && hostFound) {
			return 1;
		} else if (ftp && hostFound) {
			return 2;	
		}
		
		return 400;
	}

	// Note: dns() must set PREFERRED
	//|| return value and int parameter does not matter
	//|| .getAllByName returns array of InetAddress, we just pick one for PREFERRED
	int dns(int X)  // NOC - do not change this signature; X is whatever you want
	{
		//byte representation of "NOT FOUND"
		byte [] notFound = {78, 79, 32, 72, 79, 83, 84, 32, 70, 79, 85, 78, 68};
		int j = 0;
		//if HOST is empty, replace with "NOT FOUND"
		if(HOST[0] == 0){
			for(int i = 0; i < notFound.length; i++) {
				HOST[i] = notFound[i];
			}
			j = notFound.length;
		}
		else {
			//read through HOST until end
			for (int i = 0; i < 1024; i++){
				if (HOST[i] == 0) {
					j = i;
					break;
				}
			}
		}
		InetAddress[] addrArr;
		try{
			addrArr = InetAddress.getAllByName(byte2str(HOST, 0, j));
		} catch (Exception e){
			
			return 0;
		}
		
		int timeout = 300;
		long connectTime = 0;;
		int pref = -1;
		//try to connect to each IP address, if the time it takes to connect is shorter
		//than a previous connection, make it the new preferred IP
		for(int x = 0; x < addrArr.length; x++) {
			Socket client = new Socket();
			try{
				if(addrArr[x].getAddress().length == 4) {
					connectTime = System.nanoTime();
					if(X == 1){
						client.connect(new InetSocketAddress(addrArr[x], 21), timeout);
					} else {
						client.connect(new InetSocketAddress(addrArr[x], 80), timeout);
					}
					connectTime = System.nanoTime() - connectTime;
				}
				if(client.isConnected() && connectTime/1000000 < timeout) {
					timeout = (int)connectTime/1000000;
					pref = x;
					client.close();
					break;
				}
				client.close();
			} catch (Exception e){
				continue;
			}
		}
		if(pref >= 0) {
			PREFERRED = addrArr[pref];
		}
		return 1;
	}

	int http_fetch(Socket client) // NOC - don't change signature
	{
		//System.out.println("HTTP FETCH");
		Socket p; // peer, connection to HOST
		int transferred = 0;
		try {			// -- connect to peer
			p = new Socket();
			p.connect(new InetSocketAddress(PREFERRED, 80), 300);
		} catch (Exception e) {
			
			byte[] err = {69, 82, 82, 79, 82};
			System.out.println(byte2str(err, 0, err.length));
			//System.out.println("Error connecting to peer");
			return 0;
		}		try {			
			/*
			byte[] host = hostAsBytes();			// --- build request to send to peer --- //			// get represents "GET / HTTP/1.1"
			byte[] request = {71, 69, 84, 32};
			byte[] protocol = {32, 72, 84, 84, 80, 47, 49, 46, 49};
			byte[] get = new byte[request.length + FILE.length + protocol.length];			for (int i=0; i < get.length; i++) {
				if (i < request.length) {
					// add "GET "
					get[i] = request[i];
				} else if (i < request.length + FILE.length) {
					// add "<FILE>"
					get[i] = FILE[i - request.length];
				} else {
					// add " HTTP/1.1"
					get[i] = protocol[i - request.length - FILE.length];
				}
			}			// host_header represents "Host: <host_name>"
			byte[] host_header_prefix = {13, 10, 72, 111, 115, 116, 58, 32};
			byte[] host_header = new byte[host_header_prefix.length + hostLen];
			for (int i = 0; i < host_header.length; i++) {
				if (i < host_header_prefix.length) {
					host_header[i] = host_header_prefix[i];
				} else {
					host_header[i] = host[i - host_header_prefix.length];
				}
			}			// connection_close represents "Connection: close"
			byte[] connection_close = {13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32,
				99, 108, 111, 115, 101, 13, 10, 13, 10};			
			*/

			// --- send request to peer --- //
			OutputStream p_out = p.getOutputStream();
			p_out.write(req);
			// --- send response back to client --- //			
			// read from peer
			InputStream in = p.getInputStream();
			// write to client
			OutputStream out = client.getOutputStream();
			
			int rb = 0;
			byte[] last4 = new byte [4];
			int contentLength = 0;
			int count = 0;
			//byte for "Content-Length"
			byte[] content = {67, 111, 110, 116, 101, 110, 116, 45, 76, 101, 110, 103, 116, 104, 58, 32};
			boolean cLenFound = false;
			while ((rb = in.read()) != -1) {
				out.write(rb);

				//System.out.print(new String(new byte[]{(byte)rb}));

				last4[0] = last4[1];
				last4[1] = last4[2];
				last4[2] = last4[3];
				last4[3] = (byte)rb;
				if(!cLenFound){
					if (count == content.length) {
						if(rb == 13){
							cLenFound = true;
						} else {
							contentLength = contentLength * 10 + (byte)rb - 48;
						}
					}
					else if(rb == content[count]){
						count++;
					} else {
						count = 0;	
					}
				}
				if(last4[3] == 10 && last4[0] == 13 && last4[1] == 10 && last4[2] == 13 ){
					//System.out.println("header end");
					break;
				}
			}
		
			transferred += contentLength;
			int read = 0;
			for (int i = 0; i < contentLength; i ++) {
				out.write((byte)in.read());
			}
			
			//System.out.println("http reading end");
			// close the peer connection
			in.close();
			out.close();
			p.close();
		} catch (Exception e) {
			
			byte[] err = {69, 82, 82, 79, 82};
			System.out.println(byte2str(err, 0, err.length));
			//System.out.println("Exeption: " + e);
			try {
				p.close();
			} catch (IOException ioe) {
				System.out.println(byte2str(err, 0, err.length));
				//System.out.println("Error closing peer socket: " + ioe);
			}
		}		
		//System.out.println("HTTP FETCH ENDED: " + transferred);
		
		return transferred;
	}
	int  ftp_fetch(Socket client) // NOC - don't change signature
	{
		//cs.purdue.edu works for practice
		Socket p; // peer, connection to HOST
		p = new Socket();
		int transferred = 0;
		try{
			p.connect(new InetSocketAddress(PREFERRED, 21), 300);
			//System.out.println("FTP CONNECTED");
			
			InputStream serverInput = p.getInputStream();
			OutputStream serverOutput = p.getOutputStream();
			OutputStream clientOutput = client.getOutputStream();

			// do FTP transaction with peer, get file, send back to c
			// Note: do not 'store' the file locally; it must be sent
			// back as it arrives
			//read ftp connection greeting
			byte lastRead = 0;
			int read = 0;
			boolean lastLine = false;
			boolean firstChar = true;
			//System.out.println("Greetings");
			while(((read = serverInput.read()) != -1)) {		
				if(lastRead == 3) {
					if(read == 32) {
						//System.out.println("Last Line");
						lastLine = true;	
					}	
				}	
				//System.out.print(new String(new byte[]{(byte)read}));
				//System.out.print(read + "|");
				if(read == 10) {
					lastRead = 0;
					if(lastLine) {
						//System.out.println("exited");

						break;	
					}
					continue;
				}	
				lastRead++;
			}



			//Enter Username assume anonymous ftp
			//{85, 83, 69, 82, 32, 97, 110, 111, 110, 121, 109, 111, 117, 115} for "USER anonymous"
				serverOutput.write(new byte[]{85, 83, 69, 82, 32, 97, 110, 111, 110, 121, 109, 111, 117, 115, 13, 10});
			//username for "demo"
			//serverOutput.write(new byte[]{85, 83, 69, 82, 32, 100, 101, 109, 111, 13, 10});
			//System.out.println("Entered Username");
			boolean badAcc = false;
			while(((read = serverInput.read()) != -1)) {
                if(lastRead == 4) {
                    if(read == 32) {
                        lastLine = true;
                    }
                }
               //System.out.print(new String(new byte[]{(byte)read}));
               // System.out.print(read + "|");

                if(read == 10) {
                    lastRead = 0;
                    if(lastLine) {
                        break;
                    }
                }
                lastRead++;
            }

			//Enter Password assume anonymous ftp
			//{65} for "A", random password
			//serverOutput.write(new byte[]{65});
			//byte for "password"
			serverOutput.write(new byte[]{80, 65, 83, 83, 32, 112, 97, 115, 115, 119, 111, 114, 100, 13, 10});
			//System.out.println("Entered Password");
			firstChar = true;
			while(((read = serverInput.read()) != -1)) {
                if(firstChar && (read != 49 && read != 50)){
					badAcc = true;
					break;
				}
				firstChar = false;
				if(lastRead == 4) {
                    if(read == 32) {
                        lastLine = true;
                    }
                }
                // System.out.print(new String(new byte[]{(byte)read}));
                //System.out.print(read + "|");
                if(read == 10) {
                    lastRead = 0;
                    if(lastLine) {
                        break;
                    }
                }
                lastRead++;
            }
			if (!badAcc) {
			//System.out.println("PASSIVE");
			//Enter passive mode
			//{80, 65, 83, 86} for "PASV"
			serverOutput.write(new byte[]{80, 65, 83, 86, 13, 10});
			read = 0;
			while(((read = serverInput.read()) != -1 && read != 40)) {
                if(lastRead == 4) {
                    if(read == 32) {
                        lastLine = true;
                    }
                }
                //System.out.print(new String(new byte[]{(byte)read}));
                //System.out.print(read + "|");
                if(read == 10) {
                    lastRead = 0;
                    if(lastLine) {
                        break;
                    }
                }
                lastRead++;
            }
			//System.out.println("Calculate IP");

			int[] arr = new int[4];
			byte[] ip = new byte[4];
			int count = 0;
			int port = 0;
			read = 0;
			//calculate ip from passive
			for (int i = 0; i < 4; i++) {
				while((read = serverInput.read()) != -1) {	
					if (read == 44) {
                		//System.out.print(new String(new byte[]{(byte)read}));
						arr[3] = count;
						count = 0;
						break;
					}
                	//System.out.print(new String(new byte[]{(byte)read}));
					arr[count] = read - 48;
					count++;
				}

				if(arr[3] == 1) {
					ip[i] += arr[0];
				} else if (arr[3] == 2) {
					ip[i] += arr[0] * 10;
					ip[i] += arr[1];
				} else if (arr[3] == 3) {
					ip[i] += arr[0] * 100;
					ip[i] += arr[1] * 10;
					ip[i] += arr[2];
				}
				arr[0] = 0;
				arr[1] = 0;
				arr[2] = 0;
				arr[3] = 0;

			}

			//System.out.println("Calculate Port");
			//calculate port from passive
			
			while (true) {
				if ((read = serverInput.read()) == 44) {
                	//System.out.print(new String(new byte[]{(byte)read}));
					arr[3] = count;
					count = 0;
					break;
				} else {
                	//System.out.print(new String(new byte[]{(byte)read}));
					arr[count] = read - 48;
					count++;
				}
			}
			if(arr[3] == 1) {
				port += arr[0] * 256;
			} else if (arr[3] == 2) {
				port += arr[0] * 10 * 256;
				port += arr[1] * 256;
			} else if (arr[3] == 3) {
				port += arr[0] * 100 * 256;
				port += arr[1] * 10 * 256;
				port += arr[2] * 256;
			}
			//System.out.println("Calculate Second Port");
			while (true) {
				if ((read = serverInput.read()) == 41) {
					//System.out.print(new String(new byte[]{(byte)read}));
					arr[3] = count;
					serverInput.read();
					serverInput.read();
					break;
				} else {
					//System.out.print(new String(new byte[]{(byte)read}));
					arr[count] = read - 48;
					count++;
				}
			}
			if(arr[3] == 1) {
				port += arr[0];
			} else if (arr[3] == 2) {
				port += arr[0] * 10;
				port += arr[1];
			} else if (arr[3] == 3) {
				port += arr[0] * 100;
				port += arr[1] * 10;
				port += arr[2];
			}
			//System.out.println(new String(ip) + " " + port);
			Socket peerFTP = new Socket();
			peerFTP.connect(new InetSocketAddress(InetAddress.getByAddress(ip), port), 300);
			InputStream inFTP = peerFTP.getInputStream();
			//System.out.println("connected");
			//Retrieve file with given path to file in FILE
			//{80, 65, 83, 86, 32} for "RETR"
			byte[] retrieve = {114, 101, 116, 114, 32};

			serverOutput.write(retrieve);
			serverOutput.write(FILE);
			serverOutput.write(new byte[]{13, 10});

			boolean notFound = false;
			//System.out.println("requested" + new String(FILE));
			while((read = serverInput.read()) != 10) {
				if((read != 50 && read != 49) && firstChar) {
					//System.out.println(read + " Not Found");
					notFound = true;
					break;
				}
				firstChar = false;
				if(read == 10) {
					firstChar = true;
				}
				//System.out.print(new String(new byte[]{(byte)read}));
			}
			InputStream clientInput = client.getInputStream();
			if(!notFound) {
				//System.out.println("Transferred: ");
				//"HTTP/1.1 200 OK"
				//"Content-Type: "
				byte[] status = {72, 84, 84, 80, 47, 49, 46, 49, 32, 50, 48, 48, 32, 79, 75, 13, 10};
				byte[] content = {67, 111, 110, 116, 101, 110, 116, 45, 84, 121, 112, 101, 58, 32, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 111, 99, 116, 101, 116, 45, 115, 116, 114, 101, 97, 109, 13, 10, 13, 10};	
				clientOutput.write(status);
				clientOutput.write(content);
				while((read = inFTP.read()) != -1) {
					clientOutput.write(read);
					transferred++;
					//System.out.print(new String(new byte[]{(byte)read}));
				}
			} else {
				//"HTTP/1.1 404 NOT FOUND"
				byte[] response = {72, 84, 84, 80, 47, 49, 46, 49, 32, 52, 48, 52, 32, 78, 79, 84, 32, 70, 79, 85, 78, 68, 13, 10};	
				clientOutput.write(response);
			}
			//Read 
			read = 0;
			//byte for "QUIT"
			serverOutput.write(new byte[]{81, 85, 73, 84, 13, 10});
			/*
			while((read = serverInput.read()) != -1) {
				System.out.print(new String(new byte[]{(byte)read}));
			}*/
			p.close();
			peerFTP.close();
		} else {
			
		}
		}
		catch (Exception e){
			byte[] err = {69, 82, 82, 79, 82};
			System.out.println(byte2str(err, 0, err.length));
		}

		return transferred;
		}

		int  echo_req(Socket client) // NOC - don't change signature
		{
			// used in Part 1 only; echo the HTTP req with added info
			// from PREFERRED
			if(HOST == null && PREFERRED == null){
				//System.out.println("REQ: ERROR / RESP: ERROR");
				return 0;
			}
			//msg1 represents "DNS LOOKUP: "
			byte[] msg1 = {68, 78, 83, 32, 76, 79, 79, 75, 85, 80, 58, 32};
			//msg2 represents "PREFERRED IP: "
			byte[] msg2 = {80, 114, 101, 102, 101, 114, 114, 101, 100, 32, 73, 80, 58, 32};
			byte[] host = new byte[hostLen + 1];
			//resp200 represents "HTTP/1.1 200 OK"
			byte[] resp200 = {72, 84, 84, 80, 47, 49, 46, 49, 32, 50, 48, 48, 32, 79, 75, 10};
			//resp204 represents "HTTP/1.1 204 No Content"
			//byte[] resp204 = {72, 84, 84, 80, 47, 49, 46, 49, 32, 50, 48, 52, 32, 78, 111, 32, 67, 111, 110, 116, 101, 110, 116, 10};
			//badResp represents "HTTP/1.1 404 Not Found"
			byte[] resp404 = {72, 84, 84, 80, 47, 49, 46, 49, 32, 52, 48, 52, 32, 78, 111, 116, 32, 70, 111, 117, 110, 100, 10};

			for(int i = 0; i <= hostLen; i++) {
				if(i == hostLen) {
					host[i] = 10;
				} else {
					host[i] = HOST[i];
				}
			}
			//System.out.print("     REQ: " + byte2str(HOST, 0, hostLen));
			try {
				OutputStream out = client.getOutputStream();
				if(HOST != null){
					if(PREFERRED == null){
						System.out.println(" / RESP: ERROR");
						out.write(resp404);
					} else {
						out.write(resp200);
					}
				}
				//send response to the client/browser
				out.write(10);
				out.write(req);
				out.write(msg1);
				out.write(host);
				out.write(msg2);
				if(PREFERRED != null){
					out.write(ipToByte(PREFERRED.getAddress()));
					//System.out.println(" / RESP: " + PREFERRED.getHostAddress());
				}
				out.write(10);
			}
			catch(Exception e){
				
				byte[] err = {69, 82, 82, 79, 82};
				System.out.println(byte2str(err, 0, err.length));
				//System.out.println("Writing to client error " + e);
			}
			return 1;
		}

		int run(int X)// NOC - do not change the signature for run()
		{
			ServerSocket s0 = null; // NOC - this is the listening socket
			Socket       s1 = null; // NOC - this is the accept-ed socket i.e. client
			byte []      b0;  // ADC - general purpose buffer

			// ADC here
			//byte for "Apache listening on socket"
			byte[] listen = {65, 112, 97, 99, 104, 101, 32, 108, 105, 115, 116, 101, 110, 105, 110, 103, 32, 111, 110, 32, 115, 111, 99, 107, 101, 116, 32};
			System.out.println(byte2str(listen, 0, listen.length) + PORT);
			while ( true ) {        // NOC - main loop, do not change this!
				// ADC from here to LOOPEND : add or change code
				try {
					// server socket for given port
					b0 = new byte[1000];
					byte[] reqCopy = new byte[65535];
					s0 = new ServerSocket();
					s0.bind(new InetSocketAddress(InetAddress.getLocalHost(), PORT), 20);
					// increment # of clients serviced counter
					REQUESTS++;
					// client socket accepted by server socket
					s1 = s0.accept();
					//byte for "("
					byte[] reqLog1 = {40};
					//byte for ") Incoming client connection from ["
					byte[] reqLog2 = {41, 32, 73, 110, 99, 111, 109, 105, 110, 103, 32, 99, 108, 105, 101, 110, 116, 32, 99, 111, 110, 110, 101, 99, 116, 105, 111, 110, 32, 102, 114, 111, 109, 32, 91};
					//byte for ":"
					byte[] reqLog3 = {58};
					//byte for "] to me ["
					byte[] reqLog4 = {93, 32, 116, 111, 32, 109, 101, 32, 91};
					//byte for "]"
					byte[] reqLog5 = {93};
					
					System.out.println(byte2str(reqLog1, 0, reqLog1.length) + REQUESTS + byte2str(reqLog2, 0, reqLog2.length) + s1.getInetAddress().getHostAddress() + byte2str(reqLog3, 0, reqLog3.length) + s1.getPort() + byte2str(reqLog4, 0, reqLog4.length) + s0.getInetAddress().getHostAddress() + byte2str(reqLog3, 0, reqLog3.length) + s0.getLocalPort() + byte2str(reqLog5, 0, reqLog5.length));
					int byteCount = 0;
					int totalBytes = 0;
					int read = 0;
					while(read != -1) {
						read = s1.getInputStream().read();
						reqCopy[totalBytes] = (byte)read;
						if(read == 10 && totalBytes >= 3 && reqCopy[totalBytes - 3] == 13 &&reqCopy[totalBytes - 2] == 10 && reqCopy[totalBytes - 1] == 13 ) {
							totalBytes++;
							//System.out.println("reading end");
							break;
						}
						totalBytes++;
					}
					req = new byte[totalBytes];
					for (int i = 0; i < totalBytes; i++) {
						req[i] = reqCopy[i];
						//	System.out.print(reqCopy[i] + "|");
					}
					//System.out.println(new String(req));
					//System.out.println("PARSING");
					int fetch = parse(req); // set HOST as 's' 'i' 't' 'e' '.' 'c' 'o' 'm'
					//System.out.println("PARSED: " + fetch);

					// uncomment below to test your server
					//echo_req(s1);  // used in Part 1 only

					/* Part 2 - hints
					   is it http_fetch or ftp_fetch ??
					   nbytes = http_fetch(s1) or ftp_fetch(s1);
					   LOG "REQ http://site.com/dir1/dir2/file2.html transferred nbytes"
					   */

					// todo: determine which method based on request "protocol" http:// or ftp://

					int transferred = 0;
					if(fetch == 1) {
						//System.out.println("DNS Done");
						dns(0);
						//System.out.println("RUN HTTP");
						transferred = http_fetch(s1);	
					} else if(fetch == 2) {
						//System.out.println("RUN FTP");
						dns(1);
						//System.out.println("DNS Done");
						transferred = ftp_fetch(s1);	
					} else if(fetch == 400) {
						//byte for "BAD REQUEST"
						HOST = new byte[]{66, 65, 68, 32, 82, 69, 81, 85, 69, 83, 84};
						hostLen = HOST.length;
						byte[] badRequest = {66, 65, 68, 32, 82, 69, 81, 85, 69, 83, 84, 13, 10, 72, 84, 84, 80, 47, 49, 46, 49, 32, 52, 48, 48, 32, 66, 97, 100, 32, 82, 101, 113, 117, 101, 115, 116, 10, 10, 60, 104, 116, 109, 108, 62, 60, 104, 101, 97, 100, 62, 10, 60, 116, 105, 116, 108, 101, 62, 52, 48, 48, 32, 66, 97, 100, 32, 82, 101, 113, 117, 101, 115, 116, 60, 47, 116, 105, 116, 108, 101, 62, 10, 60, 47, 104, 101, 97, 100, 62, 60, 98, 111, 100, 121, 62, 10, 60, 104, 49, 62, 66, 97, 100, 32, 82, 101, 113, 117, 101, 115, 116, 60, 47, 104, 49, 62, 10, 60, 112, 62, 89, 111, 117, 114, 32, 98, 114, 111, 119, 115, 101, 114, 32, 115, 101, 110, 116, 32, 97, 32, 114, 101, 113, 117, 101, 115, 116, 32, 116, 104, 97, 116, 32, 116, 104, 105, 115, 32, 115, 101, 114, 118, 101, 114, 32, 99, 111, 117, 108, 100, 32, 110, 111, 116, 32, 117, 110, 100, 101, 114, 115, 116, 97, 110, 100, 46, 60, 98, 114, 32, 47, 62, 10, 60, 47, 112, 62, 10, 60, 47, 98, 111, 100, 121, 62, 60, 47, 104, 116, 109, 108, 62};
						s1.getOutputStream().write(badRequest);
					}

						//REQ
						byte[] log1 = {82, 69, 81, 58, 32};
						byte[] log2 = {32, 40};
						byte[] log3 = {32, 98, 121, 116, 101, 115, 32, 116, 114, 97, 110, 115, 102, 101, 114, 114, 101, 100, 41};
						System.out.print(byte2str(log1, 0, log1.length));
						System.out.print(byte2str(HOST, 0, hostLen));
						if ( FILE != null) {
							System.out.print(byte2str(FILE, 0, FILE.length));
						}
						System.out.println(byte2str(log2, 0, log2.length) + transferred + byte2str(log3, 0, log3.length));
					
					s0.close();
					s1.close();
					HOST = new byte[1024];
					PREFERRED = null;
					FILE = null;
					hostLen = 0;
				}
				catch(Exception e) {
						byte[] err = {69, 82, 82, 79, 82};
					try {
						HOST = new byte[1024];
						PREFERRED = null;
						FILE = null;
						hostLen = 0;
						System.out.println(byte2str(err, 0, err.length));
						if (!s0.isClosed()) {
							s0.close();
						}
						if (!s1.isClosed()) {
							s1.close();
						}
					}
					catch(Exception er) {
						System.out.println(byte2str(err, 0, err.length));
					}
				}
				// LOOPEND
			}// NOC - main loop
		}

		/* ------------- your own methods below this line ONLY ----- */

		// Use this to convert b[i] to b[j-1] to string
		String byte2str(byte []b, int i, int j)
		{
			byte [] fin = new byte[j - i];
			char [] newStr = new char[j - i];
			for (int k = 0;i<j; i++, k++) {
				newStr[k] = (char)b[i];
			}
			return String.valueOf(newStr);
		}

		//given a byte representation of an IPv4 address, return a byte array of the string
		//representation
		byte[] ipToByte(byte []b){
			//4 sets of 3 ints to represent IP, 128.1.1.1 would have 1 row of [1, 2, 8] to
			//then become [49, 50, 56] for ASCII value of 1, 2, and 8 respectively
			int[][] ipInt = new int [4][3];
			int val = 0;
			int finLen = 0;
			for(int i = 0; i < 4; i++)
			{
				int counter = 100;
				if(b[i] < 0) {
					val = 256 - (-1) * b[i];
				}
				else {
					val = b[i];
				}
				if(val >= 100) {
					finLen += 3;
				}
				else if (val >= 10){
					finLen += 2;
				}
				else {
					finLen += 1;
				}
				//separates byte into their decimal places [128] to [1,2,8]
				for(int j = 0; j < 3; j++) {
					ipInt[i][j] = val / counter;
					val %= counter;
					counter /= 10;
				}
			}
			byte[] arrFin = new byte[finLen + 3];
			for(int i = 0, j = 0; i < 4; i++) {
				if(ipInt[i][0] == 0 && ipInt[i][1] == 0) {
					arrFin[j] = (byte)(ipInt[i][2] + 48);
					j++;
				}
				else if(ipInt[i][0] == 0) {
					arrFin[j] = (byte)(ipInt[i][1] + 48);
					arrFin[j + 1] = (byte)(ipInt[i][2] + 48);
					j += 2;
				}
				else {
					arrFin[j] = (byte)(ipInt[i][0] + 48);
					arrFin[j + 1] = (byte)(ipInt[i][1] + 48);
					arrFin[j + 2] = (byte)(ipInt[i][2] + 48);
					j += 3;
				}
				if(j != arrFin.length){
					arrFin[j] = 46;
					j++;
				}
			}
			return arrFin;
		}

		byte[] hostAsBytes() {
			byte[] host = new byte[hostLen + 1];
			for(int i = 0; i <= hostLen; i++) {
				if(i == hostLen) {
					host[i] = 10;
				} else {
					host[i] = HOST[i];
				}
			}
			return host;
		}

	} // class Apache
