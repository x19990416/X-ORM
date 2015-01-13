package x.orm.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于jdbc之上封装的数据库操作对象 1. 提供自动将数据库查询结果转换为java类对象的查询<br>
 * 2. 提供基于ResultSet的表查询<br>
 * 
 * @author x19990416
 */
public class XDBDriver {
	/**
	 * 数据库连接地址
	 */
	private String dbUrl;
	/**
	 * 驱动程序
	 */
	private String dbDriverName;
	/**
	 * @see java.sql.Statement
	 */
	private Statement stmtActive;
	/**
	 * @see java.sql.Connection
	 */
	private Connection conActive;
	/**
	 * @see java.sql.ResultSet
	 */
	private ResultSet rsQuery;
	/**
	 * 是否有返回结果
	 */
	private boolean bHasResult;
	/**
	 * 是否繁忙
	 */
	private boolean isBusy;

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbDriverName() {
		return dbDriverName;
	}

	public void setDbDriverName(String dbDriverName) {
		this.dbDriverName = dbDriverName;
	}

	public boolean isbHasResult() {
		return bHasResult;
	}

	public boolean isBusy() {
		return isBusy;
	}

	/**
	 * 建立数据库连接
	 * 
	 * @param dbDriverName
	 * @param dbUrl
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void create(String dbDriverName, String dbUrl)
			throws ClassNotFoundException, SQLException {
		this.dbDriverName = dbDriverName;
		this.dbUrl = dbUrl;
		Class.forName(dbDriverName);
		conActive = DriverManager.getConnection(dbUrl);
		bHasResult = false;
	}

	/**
	 * 打开数据库连接
	 */
	public void connect() {
		try {
			if (conActive.isClosed()) {
				conActive = DriverManager.getConnection(dbUrl);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 关闭数据库连接
	 */
	public void disconnect() {
		// 关闭数据库连接
		try {
			conActive.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 打开一个 Statement
	 * 
	 * @throws SQLException
	 */
	private void openStatement() throws SQLException {
		// 打开一个 Statement
		stmtActive = conActive.createStatement();

	}

	/**
	 * 关闭 Statement
	 * 
	 * @throws SQLException
	 */
	private void closeStatment() throws SQLException {
		stmtActive.close();

	}

	/**
	 * 执行sql语句，以表结构的形式操作。
	 * 
	 * @param sSQL
	 *            SQL语句
	 * @param iInfo
	 *            0:返回结果集 1:不返回结果集
	 * @throws SQLException
	 */
	public boolean executeSQL(String sSQL, int iInfo) throws SQLException {
		this.openStatement();
		switch (iInfo) {
		case 0: {
			rsQuery = stmtActive.executeQuery(sSQL);
			if (rsQuery.next()) {
				bHasResult = true;
			} else {
				bHasResult = false;
			}
			break;
		}
		case 1: {
			stmtActive.executeUpdate(sSQL);
			bHasResult = false;
		}
		}
		this.closeStatment();
		return true;
	}

	/**
	 * 执行sql语句，并将结果自动转换为相应的java对象。
	 * 
	 * @param sSQL
	 *            SQL语句
	 * @param clazz
	 *            转换对象的类型@
	 * @throws SQLException
	 */
	public List<?> executeSQL(String sSQL, Class clazz) throws SQLException {
		List list = new ArrayList();
		try {
			this.executeSQL(sSQL, 0);
			while (bHasResult) {
				Object o = clazz.newInstance();
				clazz.getMethod("convert", java.sql.ResultSet.class).invoke(o,
						rsQuery);
				list.add(o);
				this.next();
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return list;

	}
	/**
	 * 结果集后移一条记录
	 * @return
	 */
	public boolean next() {
		// 结果集后移一条记录
		try {
			bHasResult = rsQuery.next();
			return bHasResult;
		} catch (SQLException E) {
			return false;
		}
	}
	/**
	 * 跟据字段名取获取String类型的数据
	 * @param sFieldName 字段名
	 * @return 表数据
	 */
	public String fieldByName(String sFieldName) {
		// 如果结果集为空，返回空字符串
		if (rsQuery == null) {
			return "";
		}
		// 得到指定字段的值
		String sValue;
		try {
			sValue = rsQuery.getString(sFieldName);
		} catch (SQLException E) {
			sValue = "";
		}
		// 如果该字段值为空，返回空字符串
		if (sValue == null) {
			sValue = "";
		}
		sValue = sValue.trim();
		return sValue;
	}
	/**
	 * 跟据位置取获取String类型的数据
	 * @param iIndex 字段名
	 * @return 表数据
	 */
	public String fieldByIndex(int iIndex) {
		// 如果结果集为空，返回空字符串
		if (rsQuery == null) {
			return "";
		}

		// 得到指定字段的值
		String sValue;
		try {
			sValue = rsQuery.getString(iIndex);
		} catch (SQLException E) {
			sValue = "";
		}

		// 如果该字段值为空，返回空字符串
		if (sValue == null) {
			sValue = "";
		}
		sValue = sValue.trim();
		return sValue;
	}

	public int integerByName(String sFieldName) {
		// 得到指定字段的值的整形值，如果字段值为空，返回零
		String sValue = fieldByName(sFieldName);
		if (sValue.equals("")) {
			return 0;
		} else {
			return Integer.parseInt(sValue);
		}
	}
	
	public Timestamp timestampByName(String sFieldName){
		String sValue = fieldByName(sFieldName);
		if (sValue.equals("")) {
			return null;
		} else {
			return Timestamp.valueOf(sValue);
		}
	}

	
	public Timestamp timestampByIndex(int iIndex){
		String sValue = fieldByIndex(iIndex);
		if (sValue.equals("")) {
			return null;
		} else {
			return Timestamp.valueOf(sValue);
		}
	}
	public int integerByIndex(int iIndex) {
		// 得到指定字段的值的整形值，如果字段值为空，返回零
		String sValue = fieldByIndex(iIndex);
		if (sValue.equals("")) {
			return 0;
		} else {
			return Integer.parseInt(sValue);
		}
	}

	public float floatByName(String sFieldName) {
		// 得到指定字段的值的整形值，如果字段值为空，返回零
		String sValue = fieldByName(sFieldName);
		if (sValue.equals("")) {
			return 0l;
		} else {
			return Float.parseFloat(sValue);
		}
	}

	public float floatByIndex(int iIndex) {
		// 得到指定字段的值的整形值，如果字段值为空，返回零
		String sValue = fieldByIndex(iIndex);
		if (sValue.equals("")) {
			return 0l;
		} else {
			return Float.parseFloat(sValue);
		}
	}

	public Timestamp dateTimeByName(String sFieldName) {
		// 如果结果集为空，返回空字符串
		if (rsQuery == null) {
			return null;
		}

		// 得到指定字段的时间类型的值
		Timestamp tsValue;
		try {
			tsValue = rsQuery.getTimestamp(sFieldName);
		} catch (SQLException E) {
			tsValue = null;
		}
		return tsValue;
	}

	public Timestamp dateTimeByIndex(int iIndex) {
		// 如果结果集为空，返回空字符串
		if (rsQuery == null) {
			return null;
		}

		// 得到指定字段的时间类型的值
		Timestamp tsValue;
		try {
			tsValue = rsQuery.getTimestamp(iIndex);
		} catch (SQLException E) {
			tsValue = null;
		}
		return tsValue;
	}
}
