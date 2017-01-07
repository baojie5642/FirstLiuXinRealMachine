package LiuXinTestForAllThing;

public class LiuXinStruction0 {
	private  String aString;
	private  String bString;
	private  String cString;
	private final LiuXinStruction1 liuXinStruction10;
	private final LiuXinStruction1 liuXinStruction11;
	private final LiuXinStruction1 liuXinStruction12;

	private LiuXinStruction0(final String aString, final String bString, final String cString,
			final LiuXinStruction1 liuXinStruction10, final LiuXinStruction1 liuXinStruction11,
			final LiuXinStruction1 liuXinStruction12) {
		super();
		this.aString = aString;
		this.bString = bString;
		this.cString = cString;
		this.liuXinStruction10 = liuXinStruction10;
		this.liuXinStruction11 = liuXinStruction11;
		this.liuXinStruction12 = liuXinStruction12;
	}

	public static LiuXinStruction0 init(final String aString, final String bString, final String cString,
			final LiuXinStruction1 liuXinStruction10, final LiuXinStruction1 liuXinStruction11,
			final LiuXinStruction1 liuXinStruction12) {
		LiuXinStruction0 liuXinStruction0 = null;
		try {
			liuXinStruction0 = new LiuXinStruction0(aString, bString, cString, liuXinStruction10, liuXinStruction11,
					liuXinStruction12);
			return liuXinStruction0;
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

	public void setaString(String aString) {
		this.aString = aString;
	}

	public void setbString(String bString) {
		this.bString = bString;
	}

	public void setcString(String cString) {
		this.cString = cString;
	}

	public LiuXinStruction1 getLiuXinStruction10() {
		return liuXinStruction10;
	}

	public LiuXinStruction1 getLiuXinStruction11() {
		return liuXinStruction11;
	}

	public LiuXinStruction1 getLiuXinStruction12() {
		return liuXinStruction12;
	}
	
	public void destory(){
		try{
			
			
		}finally{
			
		}
	}
	
	
	

}
