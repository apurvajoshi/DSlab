package model;

import java.util.ArrayList;

public class Node {	
	private String name;
	private String ip;
	private Integer port;
	private ArrayList<String> processGroup;
	public Node()
	{
		
	}
	public Node(String name,String ip, Integer port, ArrayList<String> pg)
	{
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.processGroup = new ArrayList<String>();
		for(int i = 0; i < pg.size(); i++)
		{
			this.processGroup.add(pg.get(i));
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public ArrayList<String> getProcessGroup() {
		return processGroup;
	}
	public void setProcessGroup(ArrayList<String> processGroup) {
		this.processGroup = processGroup;
	}	
}