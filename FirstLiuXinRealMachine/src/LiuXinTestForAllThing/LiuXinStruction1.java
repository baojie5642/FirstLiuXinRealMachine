package LiuXinTestForAllThing;

public class LiuXinStruction1 {
	private String aString;
	private String bString;
	private String cString;

	private LiuXinStruction1(final String aString, final String bString, final String cString) {
		super();
		this.aString = aString;
		this.bString = bString;
		this.cString = cString;
	}

	public void setaString(String aString) {
		this.aString = aString;
	}

	public void setbString(String bString) {
		this.bString = bString;
	}

	public void setcString(String cString) {
		this.cString = cString;
	}

	public static LiuXinStruction1 init(final String aString, final String bString, final String cString) {
		LiuXinStruction1 liuXinStruction1 = null;
		try {
			liuXinStruction1 = new LiuXinStruction1(aString, bString, cString);
			return liuXinStruction1;
		} finally {
		}
	}

	public String getaString() {
		return aString;
	}

	public String getbString() {
		return bString;
	}

	public String getcString() {
		return cString;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
