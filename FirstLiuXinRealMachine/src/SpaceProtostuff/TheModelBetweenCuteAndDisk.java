package SpaceProtostuff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheModelBetweenCuteAndDisk {
	private Map<String, NameAndValueOfOneColumn> mapDataInDB;

	public TheModelBetweenCuteAndDisk() {
		super();
		this.mapDataInDB = new HashMap<String, NameAndValueOfOneColumn>();
	}

	private boolean setMapUseListPrivate(List<NameAndValueOfOneColumn> data) {
		if (null != data) {
			for (NameAndValueOfOneColumn data0 : data) {
				mapDataInDB.put(data0.getName(), data0);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean setMapPrivate(NameAndValueOfOneColumn data) {
		if (null != data) {
			mapDataInDB.put(data.getName(), data);
			return true;
		} else {
			return false;
		}
	}

	public Map<String, NameAndValueOfOneColumn> getDBMap() {
		return mapDataInDB;
	}

	public boolean setDBMap(NameAndValueOfOneColumn data) {
		return setMapPrivate(data);
	}

	public boolean setDBMapUseList(List<NameAndValueOfOneColumn> data) {
		return setMapUseListPrivate(data);
	}

}
