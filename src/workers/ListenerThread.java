package workers;
import java.net.*;
import java.util.List;
import java.io.*;



import model.TimeStampedMessage;

public class ListenerThread extends Thread{
	  
	  private ServerSocket Server = null;
	  private Socket clientSocket = null;
	  private List<TimeStampedMessage> rcvQueue;

	  public ListenerThread(int port, List<TimeStampedMessage> queue ) throws IOException
	  {
		  this.rcvQueue = queue;
		  System.out.println("port is " + port);
		// Should not use a well-defined port number
		  if (port < 1025) {
			  System.err.println("Invalid port number: " + port);
		  }
		  
		  try {
	          Server = new ServerSocket(port);
	      }
	      catch (IOException e) {
	          System.out.println(e);
	      }
	  }
	  
	  public void run()
	  {
		  while(true)
		  {		
			  try {
				  //System.out.println("Waiting for client on port " + Server.getLocalPort() + "...");
		          clientSocket = Server.accept();
		          //System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress() + " diverted to port " + clientSocket.getPort());
		      }  
			  catch (IOException e) {
		          System.out.println(e);
		          break;
		      }
			  
			  new Thread(
					  new ReceiverThread(clientSocket, this.rcvQueue)
					  ).start();
		  }
	  }  
  }


