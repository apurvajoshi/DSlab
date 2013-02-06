import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import com.sun.jmx.snmp.Timestamp;

public class MulticastManager {
	private TimeStampedMessage message;
	private Timer timer;
	private ConcurrentHashMap<Node, Integer> ack;	
	
	public MulticastManager(TimeStampedMessage message, ArrayList<Node> nodes) {
		this.message = message;
		for(int i = 0 ; i < nodes.size(); i++)
		{
			ack.put(nodes.get(i), 0);
		}
		timer = new Timer();
	}
	
	public void runTimer() {
		this.timer.scheduleAtFixedRate( new UpdateAck(this.ack, this.message), 0,5*1000);
	}
	
	class UpdateAck extends TimerTask {
		private ConcurrentHashMap<Node, Integer> ack; 
		private TimeStampedMessage message;
		
		public UpdateAck(ConcurrentHashMap<Node, Integer> ack, TimeStampedMessage m) {
			this.ack = ack;
			this.message = m;
		}

		@Override
		public void run() {
			System.out.println("Every 5 seconds");
			for (Entry<Node, Integer> entry : ack.entrySet()) {
	            System.out.println("Key = " + entry.getKey().getName() + ", Value = " + entry.getValue());
	            if (entry.getValue().intValue() == 0)
	            {
	            	 TimeStampedMessage msg = new TimeStampedMessage(MessagePasser.localName, entry.getKey().getName(), 
	 	            		"replay", this.message, this.message.getTimeStamp());
	 	            new SetupClientSocket(msg, entry.getKey().getIp(), entry.getKey().getPort(), MessagePasser.clientSockets, msg.getDest());
	            }
	        }
		}
	}
	
}
