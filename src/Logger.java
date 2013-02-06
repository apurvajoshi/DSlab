import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import workers.LoggerRunnable;


import manager.MessagePasser;
import model.TimeStampedMessage;


public class Logger {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	
	public static void main(String[] args) throws FileNotFoundException {
		
	    int input;
		
		List<TimeStampedMessage> MessageQueue;
		MessageQueue = Collections.synchronizedList(new ArrayList<TimeStampedMessage>());

	    System.out.println("Hi! This is Logger :  " + args[1] + "\n");
	    MessagePasser m = MessagePasser.getInstance();
	    m.setUp(args[0], args[1]);
	   
        Scanner inputReader = new Scanner(System.in);
        
        System.out.println("\nClock? 1. Logical 2. Vector");
        int clock = inputReader.nextInt();
        m.setClockService(clock);
        
		new Thread(
				new LoggerRunnable(m, MessageQueue)
				  ).start();        
        
        while(true) {
        	System.out.println("\nAction? 1. Print Logs 2. Clear Logs");
            input = inputReader.nextInt();
            inputReader.nextLine(); //New line
            
            switch(input) {
    	        case 1: System.out.println("Printing Logs...");
    	        
    	        		if (MessageQueue.size() == 0) {
    	        			System.out.println("No Messages received.");
    	        			break;
    	        		}
    	        
    	        		System.out.println("Pick the 2 messages to compare - ");
    	        		for (int i = 0; i < MessageQueue.size(); i++) {
    	        			System.out.println( i+1 + ") " + MessageQueue.get(i).getSrc() + " - " + 
    	        					MessageQueue.get(i).getDest() + " TS " + MessageQueue.get(i).getTimeStamp().getCount());  			
    	        		}
    	        		 
    	        		System.out.println("Enter first message no. : ");
    	        		int mesg1 = inputReader.nextInt();
    	        		inputReader.nextLine(); //New line
    	        		
    	        		System.out.println("Enter second message no. : ");
    	        		int mesg2 = inputReader.nextInt();
    	        		inputReader.nextLine(); //New line
    	        		
    	        		int result = MessageQueue.get(mesg1-1).getTimeStamp().compare(MessageQueue.get(mesg2-1).getTimeStamp());
    	        		
    	        		switch(result) {
    	        		case 0:  System.out.println("Messages are concurrent");
    	        				 break;
    	        		case 1:  System.out.println("First event happened before Second");
       				 			 break;
    	        		case 2:  System.out.println("Second event happened before First");
				 			 	 break;
    	        		case 3:  System.out.println("Messages are concurrent");
			 			 		 break;
			 			default: 
			 					 System.out.println("Program corrupted! DOOMED!");
    	        		}
    	        		    	        		
    	        		break;    	        	
    	        
    	        case 2: System.out.println("Clearing Logs...");
    	        		MessageQueue.clear();
    	        		break;
    	        
    	        default: System.out.println("Invalid Option\n");
    	    }
        }
	}


}
