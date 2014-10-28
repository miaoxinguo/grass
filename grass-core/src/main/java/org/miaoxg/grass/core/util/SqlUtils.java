package org.miaoxg.grass.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaoxg.grass.core.exception.GrassException;

/**
 * Utility class for composing SQL statements
 */
public class SqlUtils {
    
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
     * 获取一个类所有字段的名和值
     */
    public static Map<String, Object> buildColumnNameAndValues(Object sourceObj) throws Exception {
        Map<String, Object> fieldNameAndValues = new HashMap<String, Object>();
        Class<?> clazz = sourceObj.getClass();
        
        // 忽略的属性不处理
        Method getExcludedFields= clazz.getMethod("getExcludedFields");
        @SuppressWarnings("unchecked")
        List<String> excludedFieldList = (List<String>)getExcludedFields.invoke(sourceObj);
        
        for(Field field : clazz.getDeclaredFields()){
            if(excludedFieldList.contains(field.getName())){
                continue;
            }
            
            field.setAccessible(true);  // 重点，只有设置为true才能取private属性的值
            fieldNameAndValues.put(convertPropertyNameToColumnName(field.getName()), field.get(sourceObj));
        }
        
        if(fieldNameAndValues.size() == 0){
            throw new GrassException("The model '"+ clazz.getName() +"' must have one property at least");
        }
        return fieldNameAndValues;
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
