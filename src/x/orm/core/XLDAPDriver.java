package x.orm.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class XLDAPDriver {
	/**
	 * @see javax.naming.directory.DirContext
	 */
	private DirContext ctx;
	/**
	 * dcĿ¼
	 */
	private String root;// "DC=SHLIB"
	/**
	 * ldap���ݿ��ַ<br>
	 * ��ʽΪ:<b>ldap://xxx.xxx.xxx.xx/</b>
	 */
	private String url;// ldap://10.1.20.70/
	/**
	 * �û���
	 */
	private String user;// cn=root
	/**
	 * ����
	 */
	private String password;// Tivoli123


	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	private String shlibVpnType;
	public String getShlibVpnType() {
		return shlibVpnType;
	}

	public void setShlibVpnType(String shlibVpnType) {
		this.shlibVpnType = shlibVpnType;
	}


	public void init() throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url + root);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		// env.put(Context.SECURITY_PRINCIPAL,
		// "uid=ecardadmin, cn=appadmins,cn=apps,DC=SHLIB" );
		env.put(Context.SECURITY_PRINCIPAL, user);
		env.put(Context.SECURITY_CREDENTIALS, password);
		try {
			ctx = new InitialDirContext(env);
			System.out.println("ldap:��֤�ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * �ر�����
	 */
	public void closeLDAP() {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * xiacj 2011-02-21 �û���֤ ����baseDN�������֧����cn=users
	 * ����attribute_1����һ������ֵ�ԣ�������������ֵ֮���á�=���ָ�
	 * ����attribute_2���ڶ�������ֵ�ԣ�������������ֵ֮���á�=���ָ� ���ؽ��trueΪ��֤ͨ����falseΪ��֤ʧ��
	 */
	@SuppressWarnings("unchecked")
	public boolean userAuthentication(String baseDN, String attribute_1, String attribute_2) {
		boolean bTmp = false;
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			// System.out.println("ִ�в�ѯ");
			NamingEnumeration en = ctx.search(baseDN, "(&(" + attribute_1 + ")(" + attribute_2 + "))", constraints); // ��ѯ�����û�
			// System.out.println("ִ�в�ѯ����");

			if (en != null && en.hasMoreElements()) {
				// System.out.println("�õ����");
				bTmp = true;

			} else {
				System.out.println(attribute_1 + "--" + attribute_2 + ":��֤ʧ��!");
			}
		} catch (NamingException e) {
			e.printStackTrace();

		}
		return bTmp;
	}

	/*
	 * xiacj 2011-02-21 �õ�һ��ldap ��������Լ� ����baseDN�������֧����cn=users
	 * ����condition������������������shLibBorrower=1371404 ���������ӦΪΨһ���� ����һ���������������ֵ
	 */
	@SuppressWarnings("unchecked")
	public Attributes getAttributes(String baseDN, String condition) {
		Attributes attrs = null;
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			// System.out.println("ִ�в�ѯ");
			NamingEnumeration en = ctx.search(baseDN, condition, constraints); // ��ѯ�����û�
			// System.out.println("ִ�в�ѯ����");

			if (en != null && en.hasMoreElements()) {
				// System.out.println("�õ����");
				Object obj = en.nextElement();
				if (obj instanceof SearchResult) {
					SearchResult si = (SearchResult) obj;

					attrs = si.getAttributes();

				} else {
					System.out.println(obj);
					attrs = null;
				}

			} else {
				System.out.println(condition + ":�ò����������");
			}
		} catch (NamingException e) {
			e.printStackTrace();
			attrs = null;
		}

		return attrs;

	}

	/*
	 * xiacj 2011-02-21 �õ�����ֵ ����baseDN�������֧����cn=users
	 * ����condition�������������������shLibBorrower=1371404���������ӦΪΨһ����
	 * ����targetAttributeName����Ҫ�õ�����ֵ���������� ��������ֵ
	 */
	public String getAttributeValue(String baseDN, String condition, String targetAttributeName) {
		String sTmp = "";
		Attributes attrs = getAttributes(baseDN, condition);

		if (attrs != null) {
			Attribute attr = attrs.get(targetAttributeName);
			if (attr != null) {

				Object o;
				try {
					o = attr.get();
					if (o instanceof byte[])
						sTmp = new String((byte[]) o);
					else
						sTmp = o.toString();

				} catch (NamingException e) {
					e.printStackTrace();
				}

			}
		}
		return sTmp;
	}

	public int addAttribute(String baseDN, Map<String, String> attributes) {
		int result=0;
        try {
        	StringBuilder sb  = new StringBuilder();
            BasicAttributes attrs = new BasicAttributes();
            for( Map.Entry<String, String> entry:  attributes.entrySet()){
            	attrs.put(entry.getKey(), entry.getValue());
            	sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
            }
            System.out.println(sb.substring(0,sb.length()-1));
            ctx.createSubcontext("uid=6D9853D4AB8C7132", attrs);
    		return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
	}
	/*
	 * xiacj 2011-02-21 �޸�����ֵ ����baseDN�������֧����cn=users
	 * ����condition�������������������shLibBorrower=1371404���������ӦΪΨһ����
	 * ����AttributeName���������� ��AttributeValue������ֵ ����ֵ��Ϊ0��Ϊ�޸ĳɹ��������޸�ʧ��
	 */
	
	
	
	@SuppressWarnings("unchecked")
	public int modifyAttribute(String baseDN, String condition, String AttributeName, String AttributeValue) {
		int result = 0;
		ModificationItem modificationItem[] = new ModificationItem[1];
		modificationItem[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(AttributeName,
				AttributeValue));

		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String name = "";
		try {
			NamingEnumeration en = ctx.search(baseDN, condition, constraints);

			if (en != null && en.hasMoreElements()) {
				Object obj = en.nextElement();
				if (obj instanceof SearchResult) {
					SearchResult si = (SearchResult) obj;
					name = si.getName() + ", " + baseDN;
				}
			} else {
				System.out.println(condition + ":�ò����������");
				return 1;
			}

		} catch (NamingException e) {
			e.printStackTrace();
			return 2;
		}
		try {
			ctx.modifyAttributes(name, modificationItem);
			System.out.println(name + ":" + AttributeName + "���޸ġ�");
		} catch (NamingException e) {
			System.out.println(name + ":�Ҳ�������");
			e.printStackTrace();
			return 3;
			// e.printStackTrace();
		}
		return result;
	}

	/*
	 * xiacj 2011-02-21; �õ���������condition������ldap��������Լ� ; ����baseDN�������֧����cn=users;
	 * ����condition���������������;
	 */
	@SuppressWarnings("unchecked")
	public Attributes[] getAllAttributes(String baseDN, String condition) {
		List<Attributes> list = new ArrayList<Attributes>();
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			NamingEnumeration en = ctx.search(baseDN, condition, constraints);
			while (en != null && en.hasMoreElements()) {
				Object obj = en.nextElement();
				if (obj instanceof SearchResult) {
					SearchResult si = (SearchResult) obj;

					Attributes attrs = si.getAttributes();
					list.add(attrs);

				} else {
					System.out.println(obj);
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

		return list.toArray(new Attributes[list.size()]);
	}

	/*
	 * xiacj 2011-02-21; �õ���������condition������ldap�����ĳ�����Ե�����ֵ ;
	 * ����baseDN�������֧����cn=users; ����condition�������������� ; ����һ���������������ֵ;
	 */
	/**
	 * �õ���������condition������ldap�����ĳ�����Ե�����ֵ
	 * 
	 * @param base
	 *            �����֧
	 * @param filter
	 *            ������������
	 * @param clazz
	 *            �ֶ�����
	 * @return ��ѯ���
	 * @throws NamingException
	 */
	public String[] getAllAttributeValue(String baseDN, String condition, String attributeName) {
		List<String> list = new ArrayList<String>();
		Attribute attr;
		Attributes[] attrs_array = getAllAttributes(baseDN, condition);
		for (Attributes attrs : attrs_array) {
			attr = attrs.get(attributeName);
			if (attr != null) {
				try {
					NamingEnumeration<?> o = attr.getAll();
					while(o.hasMore()){
						list.add(o.next().toString());
					}
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/*
	 * xiacj 2011-02-21; �õ���������condition��һ��ldap�������������ֵ��;
	 * ����baseDN�������֧����cn=users; ����condition�������������� ; ����List<Map<String,
	 * String>>;
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getAttributesValue(String baseDN, String condition) {
		List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
		Attributes attrs = getAttributes(baseDN, condition);
		if (attrs == null) {
			System.out.println("No   attributes");
		} else {
			Map<String, String> keyValues = new HashMap<String, String>();
			for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();) {
				Attribute attr;
				try {
					attr = (Attribute) ae.next();
					String attrId = attr.getID();
					for (Enumeration vals = attr.getAll(); vals.hasMoreElements();) {
						Object o = vals.nextElement();
						keyValues.put(attrId, o.toString());
					}
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
			datas.add(keyValues);
		}
		return datas;
	}

	/**
	 * ��ѯ������ѯ���ת���ɶ�Ӧ������
	 * 
	 * @param base
	 *            Ҫ���ҵ�context������
	 * @param filter
	 *            ��ѯ����
	 * @param clazz
	 *            ת���������
	 * @return ��ѯ���
	 * @throws NamingException
	 */
	@SuppressWarnings("unchecked")
	public List<?> search(String base, String filter, final Class<?> clazz) throws NamingException {
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = ctx.search(base, filter, constraints);
		List list = new ArrayList();
		while (en != null && en.hasMoreElements()) {
			Object obj = en.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult si = (SearchResult) obj;
				Attributes attrs = si.getAttributes();
				list.add(new XAttributesMapper() {
					public Object mapFromAttributes(Attributes arg0) throws NamingException {
						try {
							Object o = clazz.newInstance();
							converAttsToModel(o, arg0);
							return o;
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						return null;
					}
				}.mapFromAttributes(attrs));
			}
		}
		return list;
	}

	public boolean hasValues(String base,String filter) throws NamingException{
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = ctx.search(base, filter, constraints);
		 return en.hasMoreElements();
	}
	

	/**
	 * ����tag��ָ�����ֶ�����ȡ����Ӧ���ֶη���
	 * 
	 * @param base
	 *            Ҫ���ҵ�context������
	 * @param filter
	 *            ��ѯ����
	 * @param tag
	 *            �ֶ�����
	 * @return 
	 *         ��List<String[]>��ʽ���أ�����ֶβ��������Ӧ�ķ���ֵΪ"",�����ѯ�Ľ������String�����������toString
	 *         ()����
	 */
	@SuppressWarnings("unchecked")
	public List<?> search(String base, String filter, final String... tag) throws NamingException {
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = ctx.search(base, filter, constraints);
		List list = new ArrayList();
		while (en != null && en.hasMoreElements()) {
			Object obj = en.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult si = (SearchResult) obj;
				Attributes attrs = si.getAttributes();
				list.add(new 	XAttributesMapper() {
					public Object mapFromAttributes(Attributes arg0) throws NamingException {
						String[] ret = new String[tag.length];
						int i = 0;
						for (String fieldName : tag) {
							Attribute a = arg0.get(fieldName);
							if (a == null) {
								ret[i++] = "";
								continue;
							}
							Object o = a.get();
							if (o instanceof String) {
								ret[i++] = (String) o;
							} 
							else if(o instanceof byte[]){
								ret[i++] = new String((byte[])o);
							}
							else {
								ret[i++] = o.toString();
							}
						}
						return ret;
					}
				}.mapFromAttributes(attrs));
			}
		}
		return list;
	}

	/**
	 * ��ѯ������ѯ���ת���ɶ�Ӧ������
	 * 
	 * @param base
	 *            Ҫ���ҵ�context������
	 * @param filter
	 *            ��ѯ����
	 * @param mapper
	 *            ת����ʽ
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<?> search(String base, String filter, final XAttributesMapper mapper) throws NamingException {
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = ctx.search(base, filter, constraints);
		List list = new ArrayList();
		while (en != null && en.hasMoreElements()) {
			Object obj = en.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult si = (SearchResult) obj;
				Attributes attrs = si.getAttributes();
				list.add(mapper.mapFromAttributes(attrs));
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	static private <T> void converAttsToModel(T reader, Attributes attrs) {
		Class readerClass = reader.getClass();
		Map methodSetMap = new HashMap();
		Class[] classes = null;
		if (readerClass.getSuperclass().getName().equals("java.lang.Object")) {
			classes = new Class[] { readerClass };
		} else {
			classes = new Class[] { readerClass, readerClass.getSuperclass() };
		}

		// �ռ�set����
		for (Class clazz : classes) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.getName().startsWith("set")) {
					String fieldName = method.getName().substring(3);
					fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1, fieldName.length());
					methodSetMap.put(fieldName, method);
				}
			}

		}

		// ����set����
		for (Iterator iterator = methodSetMap.keySet().iterator(); iterator.hasNext();) {
			String fieldName = (String) iterator.next();
			Method method = (Method) methodSetMap.get(fieldName);
			if (method != null) {
				Class paramCalss = method.getParameterTypes()[0];

				Attribute o = attrs.get(fieldName);
				if (o == null)
					continue;

				try {
					if (paramCalss.getName().equals("java.lang.String")) {
						if (o.get() instanceof String) {
							method.invoke(reader, new Object[] { (String) o.get() });
						} else if (o.get() instanceof byte[]) {
							method.invoke(reader, new Object[] { new String((byte[]) o.get()) });
						} else {
							System.out.println("error");
						}
					} else {
						List<String> valuesList = new ArrayList<String>();
						for (int i = 0; i < o.size(); i++) {
							valuesList.add((String) o.get(i));
						}
						method.invoke(reader, new Object[] { valuesList });
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

	}
}
