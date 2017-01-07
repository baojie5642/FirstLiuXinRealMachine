package FieldStructure;

import java.io.Serializable;
import java.util.List;

public class WriteToDiskOfOneLine  implements Serializable{
	private static final long serialVersionUID=2016062222571855555l;
	
	private final List<WriteToDiskOfOneColumn> columnListToWriteDisk;
	
	private WriteToDiskOfOneLine(final List<WriteToDiskOfOneColumn> columnListToWriteDisk){
		super();
		this.columnListToWriteDisk=columnListToWriteDisk;
	}
	
	public static WriteToDiskOfOneLine createLineToWriteDisk(final List<WriteToDiskOfOneColumn> columnListToWriteDisk) {
		WriteToDiskOfOneLine writeToDiskOfOneLine=new WriteToDiskOfOneLine(columnListToWriteDisk);
		return writeToDiskOfOneLine;
	}

	public List<WriteToDiskOfOneColumn> getColumnListToWriteDisk() {
		return columnListToWriteDisk;
	}
	
}
