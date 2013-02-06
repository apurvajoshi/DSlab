import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import clock.ClockService;
import clock.LogicalClock;
import clock.VectorClock;

public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
	private List<TimeStampedMessage> rcvQueue;
	private ClockService clockService;
	private ArrayList<TimeStampedMessage> holdQueue;

    public WorkerRunnable(Socket clientSocket, List<TimeStampedMessage> queue, ClockService clockService) {
        this.clientSocket = clientSocket;
        this.clockService = clockService;
        
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
            	
            	TimeStampedMessage m;
    			try {
    				m = (TimeStampedMessage)is.readObject();
        			if (m != null) {
        				ProcessMulticastMessage(m);
        	        	//add to receive buffer
            			//this.rcvQueue.add(m);
        	        }
    			} catch (ClassNotFoundException e) {    				
    				e.printStackTrace();
    			}
            } catch (IOException e) {
                //report exception somewhere.
                e.printStackTrace();
            }
    	}
    }
    
    public void ProcessMulticastMessage(TimeStampedMessage msg) {
    	int msgOrder = msg.getTimeStamp().compare(this.clockService.getTimestamp());
    	
    	if (msg.getKind().compareTo("ack") != 0) { 
    		if (msgOrder == 2) {
        		//New Timestamp
        		//if exists in hashmap -- duplicate
        		//else new
        		//add to hold queue
        		this.holdQueue.add(msg);
        	}
        	else if (msgOrder == 1) {
        		//Old Message    		
        	}    		
    	}
    	else {
    		//Ack
    		if ((msgOrder == 0) || (msgOrder == 1)) {
    			//drop message
    			return; 
    		}
    	}
    	
    	
    }
}