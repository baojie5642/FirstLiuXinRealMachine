package FieldStructure;

import java.util.concurrent.ConcurrentHashMap;

public class JavaClassNameAndClass {
	private static final ConcurrentHashMap<String, Class<?>> className_And_Class = new ConcurrentHashMap<String, Class<?>>();
	static {
		className_And_Class.put("blob", byte[].class);
		className_And_Class.put("bit", java.lang.Boolean.class);
		className_And_Class.put("bigint", java.lang.Long.class);
		className_And_Class.put("binary", byte[].class);
		className_And_Class.put("char", java.lang.String.class);
		className_And_Class.put("datetime", java.sql.Timestamp.class);
		className_And_Class.put("date", java.sql.Date.class);
		className_And_Class.put("double", java.lang.Double.class);
		className_And_Class.put("decimal", java.math.BigDecimal.class);
		className_And_Class.put("geometry", byte[].class);
		className_And_Class.put("int", java.lang.Integer.class);
		className_And_Class.put("longblob", byte[].class);
		className_And_Class.put("mediumint", java.lang.Integer.class);
		className_And_Class.put("mediumblob", byte[].class);
		className_And_Class.put("smallint", java.lang.Integer.class);
		className_And_Class.put("timestamp", java.sql.Timestamp.class);
		className_And_Class.put("tinyint", java.lang.Integer.class);
		className_And_Class.put("tinyblob", byte[].class);
		className_And_Class.put("time", java.sql.Time.class);
		className_And_Class.put("varchar", java.lang.String.class);
		className_And_Class.put("varbinary", byte[].class);
		className_And_Class.put("year", java.sql.Date.class);
	}

	public static Class<?>  getClassByName(final String className){
		return className_And_Class.get(className);
	}
	
	public static void  main(String args[]) {
		System.out.println(getClassByName("tinyblob"));
	}
	
	
	
	
	
}
