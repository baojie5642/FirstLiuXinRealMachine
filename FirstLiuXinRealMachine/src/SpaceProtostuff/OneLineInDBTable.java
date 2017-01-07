package SpaceProtostuff;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class OneLineInDBTable implements Serializable {
	private static final long serialVersionUID = 1L;
	private final ConcurrentHashMap<String, NameAndValueOfOneColumn> lineConcurrentHashMap;
	private long lineNum = 0l;

	private OneLineInDBTable(final int howManyColumnInLine) {
		super();
		this.lineConcurrentHashMap = new ConcurrentHashMap<String, NameAndValueOfOneColumn>(howManyColumnInLine);
	}

	public static OneLineInDBTable init(final int howManyColumnInLine) {
		OneLineInDBTable oneLineInDBTable = new OneLineInDBTable(howManyColumnInLine);
		return oneLineInDBTable;
	}

	public long getLineNum() {
		return lineNum;
	}

	public void setLineNum(final long lineNum) {
		this.lineNum = lineNum;
	}

	public ConcurrentHashMap<String, NameAndValueOfOneColumn> getLineConcurrentHashMap() {
		return lineConcurrentHashMap;
	}

	public void putColumnInMap(final NameAndValueOfOneColumn nameAndValueOfOneColumn){
		lineConcurrentHashMap.putIfAbsent(nameAndValueOfOneColumn.getName(), nameAndValueOfOneColumn);
	}
}
