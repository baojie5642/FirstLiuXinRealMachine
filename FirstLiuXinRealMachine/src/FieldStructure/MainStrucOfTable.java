package FieldStructure;

import java.util.concurrent.ConcurrentHashMap;

public class MainStrucOfTable {

	private final KeysAboutTable keysAboutTable;

	private final ConcurrentHashMap<String, ColumnInLine> lineStructureInTable;

	private MainStrucOfTable(final KeysAboutTable keysAboutTable, final ConcurrentHashMap<String, ColumnInLine> lineStructureInTable) {
		super();
		this.keysAboutTable = keysAboutTable;
		this.lineStructureInTable = lineStructureInTable;
	}

	public static MainStrucOfTable createMainStrucOfTable(final KeysAboutTable keysAboutTable,
			final ConcurrentHashMap<String, ColumnInLine> lineStructureInTable) {
		MainStrucOfTable mainStrucOfTable = new MainStrucOfTable(keysAboutTable, lineStructureInTable);
		return mainStrucOfTable;
	}

	public KeysAboutTable getKeysAboutTable() {
		return keysAboutTable;
	}

	public ConcurrentHashMap<String, ColumnInLine> getLineStructureInTable() {
		return lineStructureInTable;
	}
	
}
