import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastManager {
	private TimeStampedMessage message;
	private Timer timer;
	private ConcurrentHashMap<Node, Integer> ack;	
	
	public MulticastManager(TimeStampedMessage message) {
		
		this.message = message;
		this.ack = new ConcurrentHashMap<Node, Integer>();
		for(int i = 0 ; i < MessagePasser.nodes.size(); i++)
		{
			ack.put(MessagePasser.nodes.get(i), 0);
		}
		timer = new Timer();
		
	}
	
	public void runTimer() {
		this.timer.scheduleAtFixedRate( new UpdateAck(), 0,5*1000);
	}
	
	public void setAck(String name) {
		Node node = MessagePasser.getInstance().findNodeByName(name);
		ack.put(node, 1);
	}
	
	
	class UpdateAck extends TimerTask {
		
		public UpdateAck() {
			
		}

		@Override
		public void run() {
			int cancelTimer = 0;
			System.out.println("Every 5 seconds");
			for (Entry<Node, Integer> entry : ack.entrySet()) {
	            System.out.println("Key = " + entry.getKey().getName() + ", Value = " + entry.getValue());
	            if (entry.getValue().intValue() == 0)
	            {
	            	cancelTimer = 1;
	            	 TimeStampedMessage msg = new TimeStampedMessage(MessagePasser.localName, entry.getKey().getName(), 
	 	            		"replay", message, message.getTimeStamp());
	 	            new SetupClientSocket(msg, entry.getKey().getIp(), entry.getKey().getPort(), MessagePasser.clientSockets, msg.getDest());
	            }
	            
	            if(cancelTimer == 1)
	            	timer.cancel();
	        }
		}
	}
	
}
