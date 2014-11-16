package org.miaoxg.grass.core.model;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.miaoxg.grass.core.exception.GrassException;
import org.miaoxg.grass.core.util.SqlUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * 通过反射构造对象
 */
public class GenericRowMapper<T extends Model> implements RowMapper<T>{
    
    Class<T> clazz;
    public GenericRowMapper(Class<T> clazz){
        this.clazz = clazz;
    }
    
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T obj = null;
        Field[] fields = clazz.getDeclaredFields();
        try {
            obj = clazz.newInstance();
            
            for (Field field : fields) {  
                // 不打算映射的类型不处理
                if(BaseModel.isMapping(field) == false){
                    continue;
                }
                //修改相应filed的权限  
                boolean accessFlag = field.isAccessible();  
                field.setAccessible(true);  
                
                String columnName = SqlUtils.toColumnName(field.getName());
                if(rs.getObject(columnName) != null){
                    field.set(obj, rs.getObject(columnName));
                }
                
                //恢复相应field的权限  
                field.setAccessible(accessFlag);  
            }
        } catch (Exception e) {
            throw new GrassException(e);
        } 
        return obj;
    }
}