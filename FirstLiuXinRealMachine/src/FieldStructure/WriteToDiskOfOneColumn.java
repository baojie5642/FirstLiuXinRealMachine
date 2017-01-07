package FieldStructure;

import java.io.Serializable;

public class WriteToDiskOfOneColumn implements Serializable {

	private static final long serialVersionUID = 2016062222562755555l;

	private final String columnName;

	private final Object value;

	private WriteToDiskOfOneColumn(final String columnName, final Object value) {
		super();
		this.columnName = columnName;
		this.value = value;
	}

	public static WriteToDiskOfOneColumn createColumn(final String columnName, final Object value) {
		WriteToDiskOfOneColumn writeToDiskOfOneColumn = new WriteToDiskOfOneColumn(columnName, value);
		return writeToDiskOfOneColumn;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getValue() {
		return value;
	}

}
