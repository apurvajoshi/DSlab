import java.io.FileNotFoundException;
import java.util.Scanner;
import clock.TimeStamp;
import manager.MessagePasser;
import model.TimeStampedMessage;


public class ApplicationProgram {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
		
	public static void main(String[] args) throws FileNotFoundException {
		
	    int input;
		
 	    //System.out.println("arg[0] = " + args[0]);
	    
	    System.out.println("Hi! This is " + args[1] + "\n");
	    MessagePasser m = MessagePasser.getInstance(); 
	    m.setUp(args[0], args[1]);
	    	   
        try {
        	Scanner inputReader = new Scanner(System.in);
            
            System.out.println("\nClock? 1. Logical 2. Vector");
            input = inputReader.nextInt();
            m.setClockService(input);
        
        	while(true) {
            	m.LogMessage = false;
            	
            	System.out.println("\nAction? 1. Send mreq 2. Receive 3.Send mrel");
                input = inputReader.nextInt();
                inputReader.nextLine(); //New line
                
                switch(input) {
                                	
                	case 3: //m.LogMessage = true;
	                		String kind = "mrel";
	    	        		System.out.println("Data? ");
	    	        		String data = inputReader.nextLine();
	    	        		
	    	        		if(m.getClockService().getClass().getSimpleName().equals("LogicalClock"))
	    	        			m.getClockService().increment(0);
	    	        		else
	    	        			m.getClockService().increment(MessagePasser.getInstance().nodes.indexOf(m.findNodeByName(args[1])));
	    	        		
	    	        		TimeStamp t = new TimeStamp(m.getClockService().getTimestamp());
	    	        		TimeStampedMessage msg = new TimeStampedMessage(args[1], args[1], kind, data, t);
	    	        		m.msgProcessThread.setCriticalSection(false);
        	        		System.out.println("Leaving critical section");
	    	        		m.send(msg);
	    	        		break;
        	        case 1: 
        	        		/*System.out.println("Destination? ");    	        		
        	        		String dest = inputReader.nextLine();
        	        		if (dest == args[1]) {
        	        			System.out.println("Destination can't be same as source \n");
        	        			break;
        	        		}
        	        		if (m.findNodeByName(dest) == null) {
        	        			System.out.println("Invalid Destination process. Check configuration file.\n");
        	        			break;
        	        		}*/ 
        	        		
        	        		//System.out.println("Kind? ");
        	        		//String kind = inputReader.nextLine();
        	        	
        	        		kind = "mreq";
        	        		System.out.println("Data? ");
        	        		data = inputReader.nextLine();
        	        		
        	        		if(m.getClockService().getClass().getSimpleName().equals("LogicalClock"))
        	        			m.getClockService().increment(0);
        	        		else
        	        			m.getClockService().increment(MessagePasser.getInstance().nodes.indexOf(m.findNodeByName(args[1])));
        	        		
        	        		t = new TimeStamp(m.getClockService().getTimestamp());
        	        		msg = new TimeStampedMessage(args[1], args[1], kind, data, t);
        	        		m.send(msg);
        	        		//m.sendMulticastMessage(msg);
        	        		break;
        	        
        	        case 4: m.LogMessage = true;
        	        case 2: m.receive();
        	        		break;
        	        
        	        case 5: if(m.getClockService().getClass().getSimpleName().equals("LogicalClock"))    	        		
            					m.getClockService().increment(0);
            				else
            					m.getClockService().increment(MessagePasser.getInstance().nodes.indexOf(m.findNodeByName(args[1])));
        	        
        	        		System.out.println("Sent Dummy Event\n");
        	        		System.out.println(" TIMESTAMP " + m.getClockService().getTimestamp().getCount() + "\n");
        	        		break;
        	        		
        	        default: System.out.println("Invalid Option\n");
        	    }
        	}
        } catch (Exception e) {
        	//inputReader.close();
        }
	}
}
