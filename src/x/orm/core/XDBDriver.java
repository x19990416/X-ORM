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
 * ����jdbc֮�Ϸ�װ�����ݿ�������� 1. �ṩ�Զ������ݿ��ѯ���ת��Ϊjava�����Ĳ�ѯ<br>
 * 2. �ṩ����ResultSet�ı��ѯ<br>
 * 
 * @author x19990416
 */
public class XDBDriver {
	/**
	 * ���ݿ����ӵ�ַ
	 */
	private String dbUrl;
	/**
	 * ��������
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
	 * �Ƿ��з��ؽ��
	 */
	private boolean bHasResult;
	/**
	 * �Ƿ�æ
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
	 * �������ݿ�����
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
	 * �����ݿ�����
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
	 * �ر����ݿ�����
	 */
	public void disconnect() {
		// �ر����ݿ�����
		try {
			conActive.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ��һ�� Statement
	 * 
	 * @throws SQLException
	 */
	private void openStatement() throws SQLException {
		// ��һ�� Statement
		stmtActive = conActive.createStatement();

	}

	/**
	 * �ر� Statement
	 * 
	 * @throws SQLException
	 */
	private void closeStatment() throws SQLException {
		stmtActive.close();

	}

	/**
	 * ִ��sql��䣬�Ա�ṹ����ʽ������
	 * 
	 * @param sSQL
	 *            SQL���
	 * @param iInfo
	 *            0:���ؽ���� 1:�����ؽ����
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
	 * ִ��sql��䣬��������Զ�ת��Ϊ��Ӧ��java����
	 * 
	 * @param sSQL
	 *            SQL���
	 * @param clazz
	 *            ת�����������@
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
	 * ���������һ����¼
	 * @return
	 */
	public boolean next() {
		// ���������һ����¼
		try {
			bHasResult = rsQuery.next();
			return bHasResult;
		} catch (SQLException E) {
			return false;
		}
	}
	/**
	 * �����ֶ���ȡ��ȡString���͵�����
	 * @param sFieldName �ֶ���
	 * @return ������
	 */
	public String fieldByName(String sFieldName) {
		// ��������Ϊ�գ����ؿ��ַ���
		if (rsQuery == null) {
			return "";
		}
		// �õ�ָ���ֶε�ֵ
		String sValue;
		try {
			sValue = rsQuery.getString(sFieldName);
		} catch (SQLException E) {
			sValue = "";
		}
		// ������ֶ�ֵΪ�գ����ؿ��ַ���
		if (sValue == null) {
			sValue = "";
		}
		sValue = sValue.trim();
		return sValue;
	}
	/**
	 * ����λ��ȡ��ȡString���͵�����
	 * @param iIndex �ֶ���
	 * @return ������
	 */
	public String fieldByIndex(int iIndex) {
		// ��������Ϊ�գ����ؿ��ַ���
		if (rsQuery == null) {
			return "";
		}

		// �õ�ָ���ֶε�ֵ
		String sValue;
		try {
			sValue = rsQuery.getString(iIndex);
		} catch (SQLException E) {
			sValue = "";
		}

		// ������ֶ�ֵΪ�գ����ؿ��ַ���
		if (sValue == null) {
			sValue = "";
		}
		sValue = sValue.trim();
		return sValue;
	}

	public int integerByName(String sFieldName) {
		// �õ�ָ���ֶε�ֵ������ֵ������ֶ�ֵΪ�գ�������
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
		// �õ�ָ���ֶε�ֵ������ֵ������ֶ�ֵΪ�գ�������
		String sValue = fieldByIndex(iIndex);
		if (sValue.equals("")) {
			return 0;
		} else {
			return Integer.parseInt(sValue);
		}
	}

	public float floatByName(String sFieldName) {
		// �õ�ָ���ֶε�ֵ������ֵ������ֶ�ֵΪ�գ�������
		String sValue = fieldByName(sFieldName);
		if (sValue.equals("")) {
			return 0l;
		} else {
			return Float.parseFloat(sValue);
		}
	}

	public float floatByIndex(int iIndex) {
		// �õ�ָ���ֶε�ֵ������ֵ������ֶ�ֵΪ�գ�������
		String sValue = fieldByIndex(iIndex);
		if (sValue.equals("")) {
			return 0l;
		} else {
			return Float.parseFloat(sValue);
		}
	}

	public Timestamp dateTimeByName(String sFieldName) {
		// ��������Ϊ�գ����ؿ��ַ���
		if (rsQuery == null) {
			return null;
		}

		// �õ�ָ���ֶε�ʱ�����͵�ֵ
		Timestamp tsValue;
		try {
			tsValue = rsQuery.getTimestamp(sFieldName);
		} catch (SQLException E) {
			tsValue = null;
		}
		return tsValue;
	}

	public Timestamp dateTimeByIndex(int iIndex) {
		// ��������Ϊ�գ����ؿ��ַ���
		if (rsQuery == null) {
			return null;
		}

		// �õ�ָ���ֶε�ʱ�����͵�ֵ
		Timestamp tsValue;
		try {
			tsValue = rsQuery.getTimestamp(iIndex);
		} catch (SQLException E) {
			tsValue = null;
		}
		return tsValue;
	}
}
