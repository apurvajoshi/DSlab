import java.net.*;
import java.util.List;
import java.io.*;

public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
	private List<TimeStampedMessage> rcvQueue;

    public WorkerRunnable(Socket clientSocket, List<TimeStampedMessage> queue) {
        this.clientSocket = clientSocket;
        this.rcvQueue = queue;
        try {
			this.clientSocket.setKeepAlive(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void run() {
        
    	while(true)
    	{
    		try {
            	
            	ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
            	//Message m;
            	TimeStampedMessage m;
    			try {
    				//m = (Message)is.readObject();
    				m = (TimeStampedMessage)is.readObject();
        			if (m != null) {
        	        	//System.out.println("Source: " + m.getSrc());
        	        	
        	        	//add to receive buffer
            			this.rcvQueue.add(m);
        	        }
    			} catch (ClassNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            } catch (IOException e) {
                //report exception somewhere.
                e.printStackTrace();
            }
    	}
    	
    }
}