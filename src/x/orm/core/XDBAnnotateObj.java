package x.orm.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public abstract class XDBAnnotateObj implements XDBConvertor{
	@Override	
	public void convert(ResultSet result) {
		try {
				Field[] fields = this.getClass().getDeclaredFields();
				for (Field f : fields) {
					if(f.isAnnotationPresent(XDBBinder.class)){
						XDBBinder dbb = f.getAnnotation(XDBBinder.class);
						String column = dbb.column();
						StringBuilder sb = new StringBuilder(f.getName());
						sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
						this.getClass().getMethod("set" + sb.toString() , String.class).invoke(this, result.getString(column));
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
