package org.miaoxg.grass.core.util;

import java.lang.reflect.Field;

import org.miaoxg.grass.core.exception.GrassException;

/**
 * Utility class for composing SQL statements
 */
public class SqlUtils {

    /**
     * No construction allowed!
     */
    private SqlUtils() {
    }
    
    /**
     * 获取一个类的查询字段
     */
    public static String buildColumns(Class<?> clazz){
        StringBuffer sb = new StringBuffer();
        for(Field field : clazz.getDeclaredFields()){
            sb.append(",").append(convertPropertyNameToColumnName(field.getName()));
        }
        if(sb.length() == 0){
            throw new GrassException("The model '"+ clazz.getName() +"' must have one property at least");
        }
        return sb.substring(1);
    }
    
    /**
     * 属性名转字段名。eg: userId -> user_id
     */
    public static String convertPropertyNameToColumnName(String propertyName) {
        StringBuilder result = new StringBuilder();
        if (propertyName != null && propertyName.length() > 0) {
            result.append(propertyName.substring(0, 1).toLowerCase());
            for (int i = 1; i < propertyName.length(); i++) {
                String s = propertyName.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                }
                else {
                    result.append(s);
                }
            }
        }
        return result.toString();
    }
    
    /**
     * 字段名转属性名。 eg: user_id -> userId 
     */
    public static String convertColumnNameToPropertyName(String columneName) {
        String[] partOfNames = columneName.split("_");
        StringBuffer sb = new StringBuffer(partOfNames[0]);
        for(int i=1; i<partOfNames.length; i++){
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
    }
}
