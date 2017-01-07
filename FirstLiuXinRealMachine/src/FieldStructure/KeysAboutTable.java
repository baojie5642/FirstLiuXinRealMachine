package FieldStructure;

import java.util.List;

public class KeysAboutTable {

	private final String tableName;
	
	private final boolean existPrimaryKey;

	private final List<String> primaryKeyName;

	private final boolean existExportKey;

	private final List<String> exportKeyName;

	private KeysAboutTable(final String tableName,final boolean existPrimaryKey, final List<String> primaryKeyName,
			final boolean existExportKey, final List<String> exportKeyName) {
		super();
		this.tableName=tableName;
		this.existPrimaryKey = existExportKey;
		this.primaryKeyName = primaryKeyName;
		this.existExportKey = existExportKey;
		this.exportKeyName = exportKeyName;
	}

	public static KeysAboutTable createSourceTableStructure(final String tableName,final boolean existPrimaryKey,
			final List<String> primaryKeyName, final boolean existExportKey, final List<String> exportKeyName) {
		KeysAboutTable sourceTableStructure = new KeysAboutTable(tableName,existPrimaryKey, primaryKeyName,
				existExportKey, exportKeyName);
		return sourceTableStructure;
	}
	
	public String getTableName() {
		return tableName;
	}

	public boolean isExistPrimaryKey() {
		return existPrimaryKey;
	}

	public List<String> getPrimaryKeyName() {
		return primaryKeyName;
	}

	public boolean isExistExportKey() {
		return existExportKey;
	}

	public List<String> getExportKeyName() {
		return exportKeyName;
	}
	
}
