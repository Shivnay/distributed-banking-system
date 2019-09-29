//java server example
package somebank_atm;

import java.io.*;
import java.net.*;

public class Handler {
	public static void main(String args[]) { 
        //Socket Attributes
		ServerSocket Service = null;
		Socket NewRequest = null;
        final int SOCKETPORT = 5000;
        Thread Client = null;
		try {
                    Service = new ServerSocket(SOCKETPORT);
                    echo("Server Online on Port: " + SOCKETPORT);
                    while(true) {
                        NewRequest = Service.accept();
                        echo("Connection received from " + NewRequest.getInetAddress().getHostName() + " : " + NewRequest.getPort());
                        
                        DataInputStream requestStream = new DataInputStream(NewRequest.getInputStream());
                        //get request parameters
                        Client = new Thread(new Client(NewRequest,SOCKETPORT)); 
                        Client.start();
                    }
		} catch(IOException exception) {
			echoerr("Problem Starting Client Service: " + exception.getMessage());
		}
	}
	/*
		Log Service Console
		@param prompt; status of service
	*/
	public static void echo(String prompt) {
		System.out.println(prompt);
	}
	/*
		Log Service Errors (unbuffered)
		@param prompt; status of service
	*/
	public static void echoerr(String prompt) {
		System.err.println(prompt);
	}
}