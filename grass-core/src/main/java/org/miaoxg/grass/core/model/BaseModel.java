package org.miaoxg.grass.core.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.miaoxg.grass.core.exception.GrassException;
import org.miaoxg.grass.core.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 方法实现提供者
 * 
 * TODO 批量插入 批量更新
 * 
 * @author miaoxinguo2002@163.com
 */
public abstract class BaseModel {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseModel.class);
    
    /**
     * TODO 数据源，需要注入或手动设置
     */
    public static DataSource dataSource = null;
    
    /**
     * 只有这些类型的字段能保存、修改、查询
     * 
     * 使用小写保存，判断时需要将字段类型转为小写形式
     */
    @SuppressWarnings({"serial" })
    private static List<String> supportTypes = new ArrayList<String>(){{
        add("string");     add("char");   add("character");
        add("integer");    add("int");    add("short");       add("byte");
        add("long");       add("float");  add("double");
        add("date");       add("time");   add("timestamp"); 
        add("boolean");
    }};
    
    /**
     * 忽略列表, 插入、更新、查询时不处理的字段
     * 
     * 每个子类维护自己的排除字段
     */
    private List<String> excludedFields = new ArrayList<String>();
    
    /**
     * 设置忽略字段
     */
    public void setExclude(String... fieldNames) {
        Collections.addAll(excludedFields, fieldNames);
    }
    
    /**
     * 获取忽略字段集合
     */
    private List<String> getExcludedFields() {
        return this.excludedFields;
    } 
    
    /**
     * 清空忽略字段集合
     */
    private void clearExcludedFields() {
        excludedFields.clear();
    } 
    
    /**
     * 将当前对象的值存入数据库
     */
   protected int save() {
        StringBuffer columnNames = new StringBuffer();  // 要插入值的列
        StringBuffer placeholder = new StringBuffer();  // 占位符
        List<Object> columnValueList = new ArrayList<Object>();  // 参数集合
        
        for(Field field : this.getClass().getDeclaredFields()){
            // 忽略的属性不处理
            if(getExcludedFields().contains(field.getName())){
                continue;
            }
            // 不打算映射的类型不处理
            if(isMapping(field) == false){
                continue;
            }
            
            field.setAccessible(true);  // 重点，只有设置为true才能取private属性的值
            columnNames.append(",").append(SqlUtils.toColumnName(field.getName()));
            placeholder.append(",?");
            try {
                columnValueList.add(field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new GrassException("illegal argument or Access:", e);
            }
        }
        //TODO 增加对表名前缀的处理
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(SqlUtils.toTableName(this.getClass().getSimpleName(), null))
                .append("(").append(columnNames.substring(1)).append(")").append(" values(")
                .append(placeholder.substring(1)).append(")");
        logger.trace(sql.toString());
        logger.trace("paramter: {}", columnValueList);
        
        // 每次执行后清除忽略字段，避免对后面操作的影响
        clearExcludedFields();
        return getJdbcTemplate().update(sql.toString(), columnValueList.toArray());
    }

    /**
     * 删除全部
     */
    protected static int deleteAll(Class<? extends Model> clazz) {
        StringBuffer sql = new StringBuffer();
        sql.append("delete from ").append(SqlUtils.toColumnName(clazz.getSimpleName()));

        logger.trace(sql.toString());
        return getJdbcTemplate().update(sql.toString());
    }

    /**
     * 删除
     */
    protected static int deleteAll(Class<? extends Model> clazz, String condition, Object... value) {
        StringBuffer sql = new StringBuffer();
        sql.append("delete from ").append(SqlUtils.toColumnName(clazz.getSimpleName()))
                .append(" where ").append(condition);

        logger.trace(sql.toString());
        logger.trace("paramter: {}", value);
        return getJdbcTemplate().update(sql.toString(), value);
    }

    /**
     * 修改记录
     */
    public int update() {
        StringBuffer columnNames = new StringBuffer();  // 要插入值的列
        List<Object> columnValueList = new ArrayList<Object>();  // 参数集合
        
        for(Field field : this.getClass().getDeclaredFields()){
            // 忽略的属性不处理
            if(getExcludedFields().contains(field.getName())){
                continue;
            }
            // 不打算映射的类型不处理
            if(isMapping(field) == false){
                continue;
            }
            
            field.setAccessible(true);  // 重点，只有设置为true才能取private属性的值
            columnNames.append(",").append(SqlUtils.toColumnName(field.getName())).append("=?");
            
            try {
                columnValueList.add(field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new GrassException("illegal argument or Access:", e);
            }
        }
        //TODO 增加对表名前缀的处理
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(SqlUtils.toTableName(this.getClass().getSimpleName(), null))
                .append(" set ").append(columnNames.substring(1));
        logger.trace(sql.toString());
        logger.trace("paramter: {}", columnValueList);
        
        // 每次执行后清除忽略字段，避免对后面操作的影响
        clearExcludedFields();
        return getJdbcTemplate().update(sql.toString(), columnValueList.toArray());
    }
    
    /**
     * 根据主键查询, 默认使用model的第一个属性作为主键
     */
    protected static <T extends Model> T findById(Class<T> clazz, Serializable id) {
        String fieldIdName = SqlUtils.toColumnName(clazz.getDeclaredFields()[0].getName());
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(buildColumns(clazz)).append(" from ")
                .append(SqlUtils.toColumnName(clazz.getSimpleName())).append(" where ")
                .append(fieldIdName).append("=?");

        logger.trace(sql.toString());
        logger.trace("id = {}", id);
        return getJdbcTemplate().queryForObject(sql.toString(), new GenericRowMapper<T>(clazz), id);
    }

    /**
     * 查询一条
     */
    protected static <T extends Model> T findOne(Class<T> clazz, String condition, Object... value) {
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(buildColumns(clazz)).append(" from ")
                .append(SqlUtils.toColumnName(clazz.getSimpleName())).append(" where ")
                .append(condition);

        logger.trace(sql.toString());
        logger.trace("paramter: {}", value);
        return getJdbcTemplate().queryForObject(sql.toString(), new GenericRowMapper<T>(clazz), value);
    }

    /**
     * 根据条件查询
     */
    protected static <T extends Model> List<T> findAll(Class<T> clazz, String condition, Object... value) {
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(buildColumns(clazz)).append(" from ")
                .append(SqlUtils.toColumnName(clazz.getSimpleName())).append(" where ")
                .append(condition);

        logger.trace(sql.toString());
        logger.trace("paramter: {}", value);
        return getJdbcTemplate().query(sql.toString(), new GenericRowMapper<T>(clazz));
    }

    /**
     * 查询全部
     */
    protected static <T extends Model> List<T> findAll(Class<T> clazz) {
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(buildColumns(clazz)).append(" from ")
                .append(SqlUtils.toColumnName(clazz.getSimpleName()));
        logger.trace(sql.toString());
        return getJdbcTemplate().query(sql.toString(), new GenericRowMapper<T>(clazz));
    }

    /**
     * 根据条件查询记录数
     */
    protected static long count(Class<? extends Model> clazz, String condition, Object... value) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) from ").append(SqlUtils.toColumnName(clazz.getSimpleName()))
                .append(" where ").append(condition);

        logger.trace(sql.toString());
        logger.trace("paramter: ", value);
        return getJdbcTemplate().queryForObject(sql.toString(), Long.class, value);
    }

    /**
     * 查询总记录数
     */
    protected static long count(Class<? extends Model> clazz) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) from ").append(SqlUtils.toColumnName(clazz.getSimpleName()));
        logger.trace(sql.toString());
        return getJdbcTemplate().queryForObject(sql.toString(), Long.class);
    }
    
    /**
     * 获取一个类的查询字段
     */
    private static String buildColumns(Class<?> clazz){
        StringBuffer sb = new StringBuffer();
        for(Field field : clazz.getDeclaredFields()){
            // 不打算映射的类型不处理，包括对象、枚举、集合等
            if(isMapping(field) == false){
                continue;
            }
            sb.append(",").append(SqlUtils.toColumnName(field.getName()));
        }
        if(sb.length() == 0){
            throw new GrassException("The model '"+ clazz.getName() +"' must have one property at least");
        }
        return sb.substring(1);
    }
    
    /**
     * 判断字段是否参与映射
     */
    static boolean isMapping(Field field){
        // 序列化字段不处理
        if("serialVersionUID".equals(field.getName())){
            return false;
        }
        // 排除非基本类型（包括封装类）、字符串等
        String typeName = field.getType().getSimpleName().toLowerCase();
        if(!supportTypes.contains(typeName)){
            return false;
        }
        return true;
    }
    
    /**
     *  TODO applicationContext.xml中注入JdbcTemplate， 表名前缀
     */
    private static JdbcTemplate getJdbcTemplate() {
        
        if (dataSource == null) {
            throw new GrassException("The Model.dataSource has to be set before used");
        }
        return new JdbcTemplate(dataSource);
    }
}
