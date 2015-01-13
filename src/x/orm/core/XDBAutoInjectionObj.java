package x.orm.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;
	
public class XDBAutoInjectionObj  implements XDBConvertor {
	public void convert(ResultSet result) {
		try {
			int count = result.getMetaData().getColumnCount();
			String[] name = new String[count];
			for (int i = 0; i < count; i++) {
				name[i] = result.getMetaData().getColumnLabel(i + 1);
				StringBuilder sb = new StringBuilder(result.getMetaData()
						.getColumnLabel(i + 1).toLowerCase());
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
				String prop = sb.toString();
				Field[] fields = this.getClass().getDeclaredFields();
				boolean flag = false;
				System.out.println(prop);
				for (Field f : fields) {
					if (f.getName().equals(prop.toLowerCase())) {
						flag = true;
						break;
					}
				}
				System.out.println(flag);
				if (!flag)
					continue;
				this.getClass().getMethod("set"+prop, String.class).invoke(this, result.getString(i+1));

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
