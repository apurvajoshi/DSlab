package workers;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import clock.TimeStamp;
import manager.MessagePasser;
import manager.MulticastManager;
import model.TimeStampedMessage;

public class MulticastMsgProcessThread extends Thread {

	private ConcurrentHashMap<String, MulticastManager> holdQueue;
	private List<TimeStampedMessage> threadRcvQueue;


	public MulticastMsgProcessThread(List<TimeStampedMessage> threadRcvQueue) throws IOException {
		holdQueue = new ConcurrentHashMap<String, MulticastManager>();
		this.threadRcvQueue = threadRcvQueue;
	}

	public void run() {
		while (true) 
		{
			/* Wait for the threadRcvQueue to get filled */
			while(this.threadRcvQueue.isEmpty());
			
			while(!this.threadRcvQueue.isEmpty())
			{
				TimeStampedMessage m = this.threadRcvQueue.remove(0);
				ProcessMulticastMessage(m);
			}
		}
	}

	public void ProcessMulticastMessage(TimeStampedMessage msg) {
		//System.out.println("ProcessMulticastMessage");
		int msgOrder = msg.getTimeStamp().compare(
				MessagePasser.getInstance().clockService.getTimestamp());

		//change
		//System.out.println("msg order = " + msgOrder);

		if (!msg.getKind().equals("ack")) {
			//System.out.println("Not an ack");
			if (msg.getSrc().equals(MessagePasser.localName)) {
				if (!this.holdQueue.containsKey(msg.getTimeStamp().getCount()
						.toString())) {
					this.holdQueue.put(
							msg.getTimeStamp().getCount().toString(),
							new MulticastManager(msg));
					System.out
							.println("Added to hold queue - received from  myself. KEY TS "
									+ msg.getTimeStamp().getCount().toString());
					/*
					System.out.println("Print holdQueue = "
							+ holdQueue.toString() + "holdQueue size = "
							+ holdQueue.size());
					*/
				}
				return;
			}

			if (msgOrder == 2) {
				// New Timestamp
				if (holdQueue.containsKey(msg.getTimeStamp().getCount()
						.toString())) {
					// Duplicate message, already exists in hashmap
					System.out
							.println("Duplicate message already exists in hashmap = "
									+ msg.getTimeStamp().getCount().toString());
					sendMulticastAck(msg);
				} else {
					// else new message , add to hold queue
					System.out.println("New Message received = " + msg.getSrc() + " to " + msg.getDest()
							+ msg.getTimeStamp().getCount().toString() + "|| Curr TS - " + MessagePasser.getInstance().clockService.getTimestamp().getCount());
					this.holdQueue.put(
							msg.getTimeStamp().getCount().toString(),
							new MulticastManager(msg));
					sendMulticastAck(msg);
					if (holdQueue.get(msg.getTimeStamp().getCount().toString())
							.ifAllAckReceived()) {
						while (sendToApplication() == 1)
							;
					}

				}
			} else if ((msgOrder == 1) || (msgOrder == 0)
					|| (msg.getKind().equals("replay"))) {
				// Old Message
				System.out.println("Old Message received"
						+ msg.getTimeStamp().getCount().toString()
						+ "  msgorder = " + msgOrder
						+ "(1, 0 = Already Processed)");
				sendMulticastAck(msg);
			}
		} else {

			//System.out.println("An ack");
			// Ack
			// If Ack is < Current Timestamp or if I am the source, drop it.
			if ((msgOrder == 1)
					|| (msg.getSrc().equals(MessagePasser.localName))) {
				// drop message
				System.out.println("Drop ACK - old / equal or mine");
				return;
			} else if (msgOrder == 0) {
				// Receiving ACK for a message I sent
				//System.out.println("Print holdQueue = " + holdQueue.toString());

				if (holdQueue.containsKey(msg.getTimeStamp().getCount().toString())) {
					// New ACK but in hold queue
					System.out.println("New ACK in hold queue for my own message, so update ACK ");
					holdQueue.get(msg.getTimeStamp().getCount().toString()).setAck(msg.getSrc());
					if (holdQueue.get(msg.getTimeStamp().getCount().toString()).ifAllAckReceived()) {
						while (sendToApplication() == 1)
							;
					}

				} else {
					// Old ACK
					System.out.println("Old ACK, dropping it. TS "
							+ msg.getTimeStamp().getCount().toString());
					return;
				}
			} else {
				if (holdQueue
						.containsKey(msg.getTimeStamp().getCount().toString())) {
					// New ACK but in hold queue
					System.out.println("New ACK in hold queue, so update ACK ");
					holdQueue.get(msg.getTimeStamp().getCount().toString())
							.setAck(msg.getSrc());
					if (holdQueue.get(msg.getTimeStamp().getCount().toString())
							.ifAllAckReceived()) {
						while (sendToApplication() == 1);
					}

				} else {
					this.holdQueue.put(
							msg.getTimeStamp().getCount().toString(),
							new MulticastManager((TimeStampedMessage) msg
									.getData()));
					System.out.println("New Message received through ACK ");
					holdQueue.get(msg.getTimeStamp().getCount().toString())
							.setAck(msg.getSrc());
					sendMulticastAck((TimeStampedMessage) msg.getData());
					if (holdQueue.get(msg.getTimeStamp().getCount().toString())
							.ifAllAckReceived()) {
						while (sendToApplication() == 1);
					}
				}
			}

		}
	}

	private void sendMulticastAck(TimeStampedMessage msg) {
		System.out.println("Multicasting ACK");
		for (int i = 0; i < MessagePasser.getInstance().nodes.size(); i++) {
			TimeStampedMessage m = new TimeStampedMessage(
					MessagePasser.localName, MessagePasser.getInstance().nodes
							.get(i).getName(), "ack", msg, msg.getTimeStamp());
			MessagePasser.getInstance().send(m);
		}
	}

	private int sendToApplication() {
		//System.out.println("sendToApplicationQueue");

		for (Entry<String, MulticastManager> entry : holdQueue.entrySet()) {
			if (entry.getValue().ifAllAckReceived()) {
				System.out.println("All acks received");
				if (entry.getValue().getMessage().getSrc()
						.equals(MessagePasser.localName)) {					
					System.out
							.println("Drop message because I" + entry.getValue().getMessage().getSrc() + 
									" was the sender of the message");
					holdQueue.remove(entry.getKey());
				} else {
					if (inorder(entry.getValue().getMessage())) {

						System.out
								.println("Message inorder - so move it to rcvQueue " + 
										entry.getValue().getMessage().getTimeStamp().getCount());

						TimeStampedMessage m = holdQueue.remove(entry.getKey())
								.getMessage();
						MessagePasser.getInstance().addToRcvQueue(m);
						return 1;
					}

				}
			}
		}
		return 0;
	}

	private boolean inorder(TimeStampedMessage m) {
		// inorder
		TimeStamp localTS = MessagePasser.getInstance().clockService
				.getTimestamp();
		TimeStamp mesgTS = m.getTimeStamp();

		int mesgIdx = MessagePasser.getInstance().nodes.indexOf(MessagePasser
				.getInstance().findNodeByName(m.getSrc()));

		if (mesgTS.getCount().get(mesgIdx) != (localTS.getCount().get(mesgIdx) + 1)) {
			System.out.println("1 Message out of order " + mesgTS.getCount().get(mesgIdx) + " " +
					localTS.getCount().get(mesgIdx));
			return false;
		}

		for (int i = 0; i < mesgTS.getCount().size(); i++) {
			if (i != mesgIdx) {
				if (mesgTS.getCount().get(i) > localTS.getCount().get(i)) {
					System.out.println("2 Message out of order " + mesgTS.getCount().get(i)
							+ " " + localTS.getCount().get(i));
					return false;
				}
			}
		}
		System.out.println("Message in order");
		return true;
	}

}