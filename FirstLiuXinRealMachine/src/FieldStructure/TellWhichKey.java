package FieldStructure;

import java.util.List;

public class TellWhichKey {

	private TellWhichKey(){
		
	}
	
	public static boolean isAnyKeyInTable(final KeysAboutTable keysAboutTable){
		boolean existPriKey=keysAboutTable.isExistPrimaryKey();
		boolean existExpKey=keysAboutTable.isExistExportKey();
		if((existPriKey==false)&&(existExpKey==false)){
			return false;
		}else {
			return true;
		}
	}
	
	public static  int isThisColumnPrimaryKey(final String columnName,final KeysAboutTable keysAboutTable){
		return innerCheckKey(columnName, keysAboutTable, true);
	}
	
	public static int isThisColumnExportedKey(final String columnName,final KeysAboutTable keysAboutTable){
		return innerCheckKey(columnName, keysAboutTable, false);
	}
	
	private static int innerCheckKey(final String columnName,final KeysAboutTable keysAboutTable,final boolean isPri){
		List<String> keys=null;
		if(isPri){
			keys=keysAboutTable.getPrimaryKeyName();
		}else {
			keys=keysAboutTable.getExportKeyName();
		}
		int size=keys.size();
		int whichKey=0;
		for(int i=0;i<size;i++){
			if(columnName.equalsIgnoreCase(keys.get(i))){
				if(isPri){
					whichKey=1;
				}else {
					whichKey=2;
				}
				break;
			}
		}
		return whichKey;
	}
	
}
