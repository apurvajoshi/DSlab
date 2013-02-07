package manager;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import model.Node;
import model.TimeStampedMessage;

public class MulticastManager {
	private TimeStampedMessage message;
	private Timer timer;
	private ConcurrentHashMap<Node, Integer> ack;	
	
	public MulticastManager(TimeStampedMessage message) {
		this.message = message;
		this.ack = new ConcurrentHashMap<Node, Integer>();
		for(int i = 0 ; i < MessagePasser.getInstance().nodes.size(); i++)
		{
			ack.put(MessagePasser.getInstance().nodes.get(i), 0);
		}
		
		setAck(message.getSrc());
		setAck(MessagePasser.localName);
		
		timer = new Timer();
		//this.timer.schedule(new UpdateAck(), 5*1000, 5*1000);
		this.timer.scheduleAtFixedRate( new UpdateAck(), 0,10*1000);
	}
	
	public void runTimer() {
		this.timer.scheduleAtFixedRate( new UpdateAck(), 0,10*1000);
	}
	
	public void setAck(String name) {
		Node node = MessagePasser.getInstance().findNodeByName(name);
		ack.put(node, 1);
	}
	
	public TimeStampedMessage getMessage() {
		return message;
	}

	public void setMessage(TimeStampedMessage message) {
		this.message = message;
	}
	
	public boolean ifAllAckReceived() {
		for (Entry<Node, Integer> entry : ack.entrySet()) {
            //System.out.println("Key = " + entry.getKey().getName() + ", Value = " + entry.getValue());
            if (entry.getValue().intValue() == 0)
            {
            	return false;
            }
        }
		return true;
	}
	
	class UpdateAck extends TimerTask {
		
		public UpdateAck() {
			
		}

		@Override
		public void run() {
			int cancelTimer = 1;
			System.out.println("Every 10 seconds");
			for (Entry<Node, Integer> entry : ack.entrySet()) {
	            //System.out.println("Key = " + entry.getKey().getName() + ", Value = " + entry.getValue());
	            if (entry.getValue().intValue() == 0)
	            {
	            	cancelTimer = 0;
	            	System.out.println("Missing ACK! Sending Replay Message to " + entry.getKey().getName());
	            	TimeStampedMessage msg = new TimeStampedMessage(MessagePasser.localName, entry.getKey().getName(), 
	 	            		"replay", message, message.getTimeStamp());
	            	MessagePasser.getInstance().send(msg);
	            }
	        }
			if(cancelTimer == 1) {
				System.out.println("All ACK's received. Cancelling Timer ");
				timer.cancel();				
			}
            	
		}
	}
	
}
