package x.orm.core;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
/**
 * LDAP查询结果与固定字段转换-偷懒专用
 * @see ShlibLDAPDriver.search();
 * 
 * @author guo
 * 
 * @exp
 * List<String> llist = (List<String>) this.ldapBaseDao.search("cn=function", "(|(shLibFunUid=" + sid + "0B)(shLibFunUid=" + sid + "0D)(shLibFunUid=" + sid + "0A)(shLibFunUid=" + sid + "0C))", new AttributesMapper() {
						public Object mapFromAttributes(Attributes arg0) throws NamingException {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							long endDate = 0l;
							try {
								endDate = sdf.parse((String) arg0.get("shLibFunEndDate").get()).getTime();
							} catch (ParseException e) {
								logger.error(e.getMessage(), e);
								;
							}
							if (endDate < current) {
								return (String) (arg0.get("shLibFunPID").get()) + ":true";
							} else {
								return (String) (arg0.get("shLibFunPID").get()) + ":false";
							}
						}
 *					});
 * 
 *
 */
public interface XAttributesMapper {
	public Object mapFromAttributes(Attributes arg0)throws NamingException;
}
