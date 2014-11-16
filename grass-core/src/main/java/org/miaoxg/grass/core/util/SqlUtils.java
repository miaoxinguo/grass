package org.miaoxg.grass.core.util;

/**
 * Utility class for composing SQL statements
 */
public class SqlUtils {
    
    private SqlUtils() {
    }   
    
    /**
     * 类名转表名。eg: User -> user | perfix_user
     */
    public static String toTableName(String className, String perfix) {
        boolean hasPerfix = perfix != null && perfix.trim().length() > 0;
        return hasPerfix ? perfix +"_"+toColumnName(className) : toColumnName(className);
    }
    
    /**
     * 属性名转字段名。eg: userId -> user_id
     */
    public static String toColumnName(String propertyName) {
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
    public static String toPropertyName(String columneName) {
        String[] partOfNames = columneName.split("_");
        StringBuffer sb = new StringBuffer(partOfNames[0]);
        for(int i=1; i<partOfNames.length; i++){
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
    }
    
    /**
     * 字段名转set方法名。 eg: user_id -> setUserId 
     */
    public static String toSetterName(String columneName) {
        String[] partOfNames = columneName.split("_");
        StringBuffer sb = new StringBuffer("set");
        for(int i=0; i<partOfNames.length; i++){
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
    }
}
