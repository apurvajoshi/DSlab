package model;
import java.io.Serializable;

public class Message implements Serializable {
	
	
	private static final long serialVersionUID = 1847568510082930960L;
	
	private int id;
	private String src;
	private String dest;
	private String kind;
	private Object data;
	
	public Message(String src, String dest, String kind, Object data)
	{
		this.src = src;
		this.dest = dest;
		this.kind = kind;
		this.data = data;
	}
	
	public Message clone()
	{
		Message msg = new Message(this.getSrc(), this.getDest(), this.getKind(), this.getData());
		msg.setId(this.getId());
		return msg;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
}