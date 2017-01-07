package FieldStructure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import DataBaseConnectionPool.LiuXinHikariDS;

public class TestJdbcForLiuXin {
	public static void main(String args[]) {
		Connection connection = null;
		LiuXinHikariDS hikariDS = LiuXinHikariDS.createHikariDS();
		try {
			connection = hikariDS.liuxinGetConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//for(int i=0;i<10000000;i++){
		KeysAboutTable sourceTableStructure=BuildLiuXinKeysAboutTable.buildKeysAboutTable(connection, "bonecp");
		
		
		
		MainStrucOfTable mainStrucOfTable=BuildMainStructureAboutTable.buildMainStruc(connection, "bonecp");
		
		
		
		ConcurrentHashMap<String, ColumnInLine> columnsInLine=mainStrucOfTable.getLineStructureInTable();
		
		Set<Entry<String, ColumnInLine>> entries =columnsInLine.entrySet();
		Iterator<Entry<String, ColumnInLine>> iterator=entries.iterator();
		while (iterator.hasNext()) {
			Entry<String, ColumnInLine> entry=iterator.next();
			//System.out.println("字段名称："+entry.getKey());
			System.out.println("字段名称："+entry.getValue().getColumnName());
			System.out.println("字段长度："+entry.getValue().getColumnLegth());
			System.out.println("该字段在java中的sqlType："+entry.getValue().getJdbcTypeAlsoJavaSqlType());
			System.out.println("该字段类型在数据库中的名称："+entry.getValue().getSqlDataTypeName().toLowerCase());
			System.out.println("该字段在java中对应的类的类名称："+entry.getValue().getSqlTypeSwapToJavaClassName());
			System.out.println("*********************************************************");
			
		}
		
		
		
		

	//	}
	}
}
