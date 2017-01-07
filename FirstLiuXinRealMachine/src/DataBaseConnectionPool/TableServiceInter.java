package DataBaseConnectionPool;



import java.sql.Connection;

public interface TableServiceInter {
	
	/**
	 * @function 从数据库表中读取数据,写入到本地磁盘的txt文件
	 * @param taskName  任务名称
	 * @param connection  数据库连接
	 * @param table  数据库表名
	 */
	public void table2Txt(String taskName,Connection connection,String table);
	
	
	/**
	 * @function 从本地磁盘读取txt文件内容,写入到数据库表中
	 * @param taskName  任务名称
	 * @param connection  数据库连接
	 * @param table  数据库表名
	 */
	public void txt2Table(String taskName,Connection connection,String table);

}
