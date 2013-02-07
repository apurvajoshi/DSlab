package manager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Node;
import model.Rule;
import model.TimeStampedMessage;

import org.yaml.snakeyaml.Yaml;

import workers.ListenerThread;
import workers.MulticastMsgProcessThread;
import workers.SenderThread;

import clock.ClockService;
import clock.LogicalClock;
import clock.VectorClock;

public class MessagePasser {
	
  private static MessagePasser singletonObject;	
  public ArrayList<Node> nodes;
  public ArrayList<Rule> sendRules;
  public ArrayList<Rule> receiveRules;
  public static HashMap<String, Socket> clientSockets;
  public boolean LogMessage = false;
  public ClockService clockService;
  public static String localName;

  private static Integer ID = 1;
  private long modificationTime;
  private HashMap<Rule, Integer> sendNthCount;
  private HashMap<Rule, Integer> rcvNthCount;
  private ArrayList<TimeStampedMessage> sendQueue;
  private ArrayList<TimeStampedMessage> rcvQueue;
  private List<TimeStampedMessage> applicationQueue;
  private List<TimeStampedMessage> threadRcvQueue;
  private File configFile;  
  
  
  public MessagePasser()
  {
	  nodes = new ArrayList<Node>();
	  sendRules = new ArrayList<Rule>();
	  receiveRules = new ArrayList<Rule>();
	  clientSockets = new HashMap<String, Socket>();
	  sendNthCount = new HashMap<Rule, Integer>();
	  rcvNthCount = new HashMap<Rule, Integer>();
	  sendQueue = new ArrayList<TimeStampedMessage>();
	  rcvQueue = new ArrayList<TimeStampedMessage>();
	  threadRcvQueue = Collections.synchronizedList(new ArrayList<TimeStampedMessage>());
	  applicationQueue = Collections.synchronizedList(new ArrayList<TimeStampedMessage>());
  }
  
 public static MessagePasser getInstance() {
      
      if (singletonObject == null)
          singletonObject = new MessagePasser();
      
      return singletonObject;
  }
  
  public void setUp(String configuration_filename, String local_name) {
	  configFile = new File(configuration_filename);
	  localName = local_name;
	  
	  try 
	  {
		  readConfiguration();
		  readRules();
	  } 
	  catch (FileNotFoundException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
	  
	  Node node = findNodeByName(local_name);
	  if (node == null) 
	  {
		  System.err.println("Invalid node name: " + local_name);
	  }
		  
	  try
	  {
		  Thread t = new ListenerThread(node.getPort(), this.threadRcvQueue);
		  t.start();
		  
		  Thread msgProcessThread = new MulticastMsgProcessThread(this.threadRcvQueue, this.applicationQueue);
		  msgProcessThread.start();
	  } 
	  catch(IOException e)
	  {
		  e.printStackTrace();
	
	  }
  }
  
  
  public Node findNodeByName(String name)
  {    
	  for (Node node : nodes) 
	  {
		  if (node.getName().equals(name)) 
		  {
			  return node;
		  }
	  }
	  return null; 
  }

  public void setClockService(int val)
  {
	  
	  if(val==1) 
	  {
		  clockService = new LogicalClock();
		  System.out.println(clockService.toString());
		  clockService.setTimestamp();
	  }
	  else
	  {
		  clockService = new VectorClock(nodes.size());
		  System.out.println(clockService.toString());
		  clockService.setTimestamp();
		 
	  }
  }
  
  public ClockService getClockService()
  {
	  return this.clockService;
  }
  
  int checkMessageAgainstRules(TimeStampedMessage message, ArrayList<Rule> rules, HashMap<Rule, Integer> nthCount)
  {
	  int maxMatchCount = -1;
	  int rule = -1;
	  //System.out.println("Message id = " + message.getId());
      for(int i = 0; i < rules.size(); i++)
      {
    	  int matchCount = -1;
    	  matchCount = rules.get(i).compare(message);
		  //System.out.println("matchCount = "+ matchCount);

    	  if(matchCount != -1)
    	  {    		  
    		  
    		  /* Check if the rule contains nth field and EveryNth field */
    		  if(rules.get(i).getNth() != null && rules.get(i).getEverynth() != null)
    		  {
    			  int value = nthCount.get(rules.get(i)).intValue();
    			  /* increment only once */
    			  value++;
    			  nthCount.put(rules.get(i), value);
    			  if(rules.get(i).getNth() == value)
    			  {
    				  matchCount++;
    			  }
    			  if(value % rules.get(i).getEverynth() == 0)
    			  {
    				  matchCount++;
    			  }
    			  if(rules.get(i).getNth() != value && value % rules.get(i).getEverynth() != 0)
    			  {
    				  matchCount = -1;
    			  }
    		  }
    		  else
    		  {

    			  /* Check if the rule contains nth field */
    			  if(rules.get(i).getNth() != null)
    			  {
    				  /* Check the count of the message */
    				  int value = nthCount.get(rules.get(i)).intValue();
    				  value++;
    				  nthCount.put(rules.get(i), value);

    				  if(rules.get(i).getNth() == value)
    				  {
    					  matchCount++;
    				  }
    				  else
    				  {
    					  matchCount = -1;
    				  }
    			  }

    			  /* Check if the rule contains EveryNth field */
    			  if(rules.get(i).getEverynth() != null)
    			  {
    				  /* Check the count of the message */
    				  int value = nthCount.get(rules.get(i)).intValue();
    				  value++;
    				  nthCount.put(rules.get(i), value);

    				  if(value % rules.get(i).getEverynth() == 0)
    				  {
    					  matchCount++;
    				  }
    				  else
    				  {
    					  matchCount = -1;
    				  }
    			  }
    		  
    		  }
    		
    		  if(matchCount > maxMatchCount)
    		  {
    			  maxMatchCount = matchCount;
    			  rule = i;
    		  }
    	  }
      }
      
      return rule;
  }
  
  
  public void sendMulticastMessage(TimeStampedMessage m)
  {
	  //this.getClockService().increment(MessagePasser.getInstance().nodes.indexOf(this.findNodeByName(localName)));
	  for(int i = 0; i < nodes.size(); i++)
	  {
  		TimeStampedMessage msg = new TimeStampedMessage(localName, nodes.get(i).getName(), m.getKind(), m.getData(), this.getClockService().getTimestamp());
  		send(msg);
	  }
  }
  
 
  public synchronized void send(TimeStampedMessage message)
  {	  
	  /* Set the id of the message before sending it */
	  message.setId(MessagePasser.ID);
	  MessagePasser.ID++;
	  	  
	  /* Reread the configuration file if the modification time has been changed*/	  
	  File f = configFile;	  
	  
      if(this.modificationTime != f.lastModified())
      {
    	  System.out.println("File has been modified");

    	  /* Reread the file */
    	  try {
			this.readRules();
    	  } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	  }
    	  
    	  /* Reset the Nth and EveryNth Counters if the file has been changed */

      }
	  
	  /* Check the message against any send rules */
      
	  int rule = checkMessageAgainstRules(message, this.sendRules, this.sendNthCount);
	  
      if(rule != -1)
      {
    	  System.out.println("Send rule matched!");
    	  /* Perform the action related to the matched rule */
    	  if(sendRules.get(rule).getAction().equals("drop"))
    	  { 
    		  /* Just ignore */
			  System.out.println("Dropping message with id " + message.getId());
    		  return;
    	  }
    	  else if(sendRules.get(rule).getAction().equals("duplicate"))
    	  {
    		  /* Send two identical copies */
    		  TimeStampedMessage msg = message.clone();
    		  sendQueue.add(0,message);
    		  sendQueue.add(0,msg);
    		  System.out.println("Duplicating message with id " + message.getId());
    		  
    		  /* Empty the send queue */
    		  int size =  sendQueue.size();
    		  for(int i = 0; i < size; i++)
    		  {
    			  /* Remove and call the send function */
    			  System.out.println("Sending message with id " +sendQueue.get(0).getId() + 
    					  " TIMESTAMP " + sendQueue.get(0).getTimeStamp().getCount());
    			  sendViaSocket(sendQueue.remove(0));
    		  }
    		  
    	  }
    	  else if(sendRules.get(rule).getAction().equals("delay"))
    	  {
    		  /* Add it to queue */
    		  sendQueue.add(message);
			  System.out.println("Delaying message with id " + message.getId());

    		  
    	  }
      }
      else
      {
    	  sendQueue.add(0,message);
    	  
    	  /* Empty the send queue */
    	  int size =  sendQueue.size();
		  for(int i = 0; i < size; i++)
		  {
			  /* Remove and call the send function */
			  System.out.println("Sending message with id " +sendQueue.get(0).getId() + 
					  " TIMESTAMP " + sendQueue.get(0).getTimeStamp().getCount());

			  sendViaSocket(sendQueue.remove(0));			  
		  }
      }
      
      
  }
  
  
  void sendViaSocket(TimeStampedMessage message)
  {
	  Node node = findNodeByName(message.getDest());
	  new SenderThread(message, node.getIp(), node.getPort(), clientSockets, message.getDest());
	  
	  if (LogMessage) {
		  //Send a duplicate Message to Logger
		  node = findNodeByName("logger");
		  //System.out.println("Sending to node with ip " + node.getIp() + " " + node.getPort());
		  //message.setDest("logger");
		  new SenderThread(message, node.getIp(), node.getPort(), clientSockets, "logger");		  		  
	  }	  
  }
  
  public TimeStampedMessage receive( ) {
	  TimeStampedMessage m = receiveMessage();
	
	  if (LogMessage) {
		  if (!localName.equals("logger")) {
			  //Send a duplicate Message to Logger with an updated time stamp.
			  TimeStampedMessage SendMessage = m.cloneWithUpdatedTimeStamp(clockService.getTimestamp());  
			
			  Node node = findNodeByName("logger");	  
			  new SenderThread(SendMessage, node.getIp(), node.getPort(), clientSockets, "logger");
		  }		  
	  }	  
	    
	  return m;
  }
  
  TimeStampedMessage receiveMessage( )
  {
	  //System.out.println("\n\nReceive ");

	  /* Receive message from the receiver queue in Server Socket */
	  
	  if(!this.rcvQueue.isEmpty())
	  {
		  //clockService.resyncTimeStamp(this.rcvQueue.get(0).getTimeStamp());
		  
		  /*if(clockService.getClass().getSimpleName().equals("LogicalClock"))
			  clockService.increment(0);
		  else
			  clockService.increment(nodes.indexOf(findNodeByName(localName)));*/
		  
		  System.out.println("The message is from " + this.rcvQueue.get(0).getSrc() + " to " +
	  this.rcvQueue.get(0).getDest() + " with ID " + this.rcvQueue.get(0).getId() + 
	  " TIMESTAMP " + this.rcvQueue.get(0).getTimeStamp().getCount());
		  
		  return this.rcvQueue.remove(0);
	  }
	  else
	  {
		  /* Wait if the thread receiver queue is empty */
		  //System.out.println("Waiting start...");
		  while(this.applicationQueue.isEmpty())
		  {
		  }  
		  
		  while(!this.applicationQueue.isEmpty())
		  {
			  TimeStampedMessage m = this.applicationQueue.remove(0);
			  int order =  m.getTimeStamp().compare(this.clockService.getTimestamp());
			  if(order == 2 || order == 3)
			  {
				  int rule = this.checkMessageAgainstRules(m, receiveRules, rcvNthCount);
				  if(rule != -1)
				  {
			    	  System.out.println("Receive rule matched!");
			    	  /* Perform the action related to the matched rule */
			    	  if(receiveRules.get(rule).getAction().equals("drop"))
			    	  { 
			    		  /* Just ignore */
						  System.out.println("Droping message with id " + m.getId());

			    	  }
			    	  else if(receiveRules.get(rule).getAction().equals("duplicate"))
			    	  {
			    		  /* Add two identical copies to the queue */
			    		  TimeStampedMessage msg = (TimeStampedMessage) m.clone();
			    		  this.rcvQueue.add(m);
			    		  this.rcvQueue.add(msg);
			    		  clockService.resyncTimeStamp(this.rcvQueue.get(0).getTimeStamp());
						  System.out.println("Duplicating message with id " + m.getId());

			    	  }
			    	  else if(receiveRules.get(rule).getAction().equals("delay"))
			    	  {
			    		  /* Add it to queue */
			    		  this.rcvQueue.add(m);
			    		  clockService.resyncTimeStamp(this.rcvQueue.get(0).getTimeStamp());
						  System.out.println("Delaying message with id " + m.getId());
						  
						  /* Delayed message is obtained in order */
						  while(this.applicationQueue.isEmpty())
						  {
						  }
			    	  }
				  }
				  else
				  {
			    	  //System.out.println("No Match exists");
			    	  this.rcvQueue.add(m);
					  clockService.resyncTimeStamp(this.rcvQueue.get(0).getTimeStamp());

				  }
			  }
		  }
		  
		  if(!this.rcvQueue.isEmpty())
		  {
			  //clockService.resyncTimeStamp(this.rcvQueue.get(0).getTimeStamp());
			  
			  /*if(clockService.getClass().getSimpleName().equals("LogicalClock"))
				  clockService.increment(0);
			  else
				  clockService.increment(nodes.indexOf(findNodeByName(localName)));*/
			  
			  System.out.println("The message is from " + this.rcvQueue.get(0).getSrc() + " to " +
					  this.rcvQueue.get(0).getDest() + " with ID " + this.rcvQueue.get(0).getId() + 
					  " TIMESTAMP " + this.rcvQueue.get(0).getTimeStamp().getCount());
			  return this.rcvQueue.remove(0);

		  }
		  return null;
	  }
  }
  
  void readConfiguration() throws FileNotFoundException
  {
	  InputStream input = new FileInputStream (configFile);
	  
	  Yaml yaml = new Yaml();
	  Map map = (Map) yaml.load(input);
	  
	  
	  ArrayList<HashMap> processes = (ArrayList<HashMap>) map.get("Configuration");
	  for(int i=0; i < processes.size(); i++)
	  {
		  Node n = new Node((String)processes.get(i).get("Name"),(String)processes.get(i).get("IP"), (Integer)processes.get(i).get("Port"));
		  nodes.add(n);
	  }
  }
  
  void readRules() throws FileNotFoundException
  {
	  File f = configFile;
	  
	  InputStream input = new FileInputStream (f);
	  Yaml yaml = new Yaml();
	  Map map = (Map) yaml.load(input);
	  
	  //System.out.println(map.get("SendRules"));
	  ArrayList<HashMap> rules = (ArrayList<HashMap>) map.get("SendRules");
	  if(rules != null)
	  {
		  for(int i=0; i < rules.size(); i++)
		  {
			  //System.out.println("i = " + i + " action = " + rules.get(i).get("Action"));
			  Rule r = new Rule((String)rules.get(i).get("Action"),
					  (String)rules.get(i).get("Src"), 
					  (String)rules.get(i).get("Dest"),
					  (String)rules.get(i).get("Kind"),
					  (Integer)rules.get(i).get("ID"),
					  (Integer)rules.get(i).get("Nth"),
					  (Integer)rules.get(i).get("EveryNth"));
			  
			  sendRules.add(i, r);
			  sendNthCount.put(r, 0);
			  
		  }
	  }
	  	  
	  
	  rules = (ArrayList<HashMap>) map.get("ReceiveRules");
	  if(rules != null)
	  {
		  for(int i=0; i < rules.size(); i++)
		  {
			  Rule r = new Rule((String)rules.get(i).get("Action"),
					  (String)rules.get(i).get("Src"), 
					  (String)rules.get(i).get("Dest"),
					  (String)rules.get(i).get("Kind"),
					  (Integer)rules.get(i).get("ID"),
					  (Integer)rules.get(i).get("Nth"),
					  (Integer)rules.get(i).get("EveryNth"));
			  
			  receiveRules.add(i, r);
			  rcvNthCount.put(r, 0);
		  }
	  }
      this.modificationTime = f.lastModified();
  }
}
