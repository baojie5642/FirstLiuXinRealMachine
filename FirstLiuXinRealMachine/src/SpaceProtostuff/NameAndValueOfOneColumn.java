package SpaceProtostuff;

public class NameAndValueOfOneColumn {
	private final String name;
	private final String value;

	private NameAndValueOfOneColumn(final String name,final String value){
		super();
		this.name=name;
		this.value=value;
	}
	
	public static NameAndValueOfOneColumn init(final String name,final String value){
		NameAndValueOfOneColumn nameAndValueOFOneColumn=new NameAndValueOfOneColumn(name, value);
		return nameAndValueOFOneColumn;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
}
