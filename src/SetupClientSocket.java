import java.net.*;
import java.util.HashMap;
import java.io.*;

public class SetupClientSocket {
  
	  Socket clientSocket = null;  

      public SetupClientSocket(TimeStampedMessage message, String ip, int port, HashMap<String, Socket> clientSockets, String Dest)
      {
    	  
    	  //System.out.println("Client: Connecting to " + ip + " on port " + port);
    	  
    	  if (!clientSockets.containsKey(Dest)) {
    		  try {
            	  clientSocket = new Socket(ip, port);
            	  clientSocket.setKeepAlive(true);
            	  
            	  //System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());
            	  ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
            	  
                  os.writeObject(message);
                  
                  //Add to HashMap
                  clientSockets.put(Dest, clientSocket);
                  //clientSocket.close();              
              } catch (UnknownHostException e) {
                  System.err.println("Don't know about host:" + ip + e);
              } catch (IOException e) {
                  System.err.println("Couldn't get I/O for the connection to" + ip + e);
              }
    	  }
    	  else {
    		  try {
        		  clientSocket = clientSockets.get(Dest);
        		  
            	  //System.out.println("Already connected to " + clientSocket.getRemoteSocketAddress());
                        
            	  ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
            	  
                  os.writeObject(message);
                                
              } catch (UnknownHostException e) {
                  System.err.println("Don't know about host:" + ip + e);
              } catch (IOException e) {
                  System.err.println("Couldn't get I/O for the connection to" + ip + e);
              }
    	  }
    	  
      }  
  }