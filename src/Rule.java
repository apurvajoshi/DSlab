public class Rule {
	
	private String action;
	private String src;
	private String dest;
	private String kind;
	private Integer id;
	private Integer nth;
	private Integer everynth;
	
	public Rule(String action, String src, String dest, String kind, Integer id, Integer nth, Integer everynth)
	{
		this.action = action;
		this.src = src;
		this.dest = dest;
		this.kind = kind;
		this.id  = id;
		this.nth = nth;
		this.everynth = everynth;
	}  
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNth() {
		return nth;
	}

	public void setNth(Integer nth) {
		this.nth = nth;
	}

	public Integer getEverynth() {
		return everynth;
	}

	public void setEverynth(Integer everynth) {
		this.everynth = everynth;
	}	
	
	public int compare(TimeStampedMessage m) {
		int count = 0;
		if(this.id != null)
			if(this.id == m.getId())
				count++;
			else
				return -1;
		
		if(this.src != null)
			if(this.src.equals(m.getSrc()))
				count++;
			else
				return -1;
		
		if(this.dest != null)
			if(this.dest.equals(m.getDest()))
				count++;
			else
				return -1;
		
		if(this.kind != null)
			if(this.kind.equals(m.getKind()))
				count++;
			else
				return -1;
		
		return count;		
	}
}