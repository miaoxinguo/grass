package org.miaoxg.grass.core.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.miaoxg.grass.core.exception.GrassException;
import org.miaoxg.grass.core.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public abstract class Model {
    
    // TODO 操作时 要忽略这个字段
//    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    
    private static final String NIE = "Your models are not instrumented. Make sure you load the agent using GrassAgentLoader.instance().loadAgent()";
    
    public static DataSource dataSource = null;

    /**
     * 忽略, 插入或更新时不处理的字段
     */
    private List<String> excludedFields = new ArrayList<String>();

    /**
     * 忽略字段
     * 
     * @param fieldNames 字段名
     * @return
     */
    public Model exclude(String... fieldNames) {
        Collections.addAll(excludedFields, fieldNames);
        return this;
    }

    public List<String> getExcludedFields() {
        return this.excludedFields;
    }

    /**
     * 将当前对象的值存入数据库
     */
    @SuppressWarnings("unchecked")
    public int save() {
        StringBuffer columnNames = new StringBuffer();  // 要插入值的列
        StringBuffer placeholder = new StringBuffer();  // 占位符
        List<Object> columnValueList = new ArrayList<Object>();  // 参数集合
        
        Method getExcludedFields = null;
        List<String> excludedFieldList = null;
        try {
            getExcludedFields = this.getClass().getMethod("getExcludedFields");
            excludedFieldList = (List<String>)getExcludedFields.invoke(this);
        }  catch (Exception e){
            logger.error("save model error:", e);
            // ingore
        }
        
        for(Field field : this.getClass().getDeclaredFields()){
            // 忽略的属性不处理
            if(excludedFieldList.contains(field.getName())){
                continue;
            }
            // TODO 排除非基本类型
            
            field.setAccessible(true);  // 重点，只有设置为true才能取private属性的值
            columnNames.append(",").append(SqlUtils.toColumnName(field.getName()));
            placeholder.append(",?");
            try {
                columnValueList.add(field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error("save model error:", e);
                // ingore
            }
        }

        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(SqlUtils.toTableName(this.getClass().getSimpleName(), null))
                .append("(").append(columnNames.substring(1)).append(")").append(" values(")
                .append(placeholder.substring(1)).append(")");
        logger.trace(sql.toString());
        logger.trace("paramter: {}", columnValueList);
        // 每次执行后清楚忽略字段，避免对后面操作的影响
        excludedFields.clear();
        return getJdbcTemplate().update(sql.toString(), columnValueList.toArray());
    }

    /**
     * 删除全部
     * 
     * @return 影响的行数
     */
    public static int deleteAll() {
        throw new GrassException(NIE);
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
     * 
     * @param c 条件
     * @param value 参数
     * @return 影响的行数
     */
    public static int deleteAll(String condition, Object... value) {
        throw new GrassException(NIE);
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
     * 根据主键查询
     * 
     * @param clazz model类型
     * @param id 主键
     * @return
     */
    public static <T extends Model> T findById(Integer id) {
        throw new GrassException(NIE);
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
    public static <T extends Model> T findOne(String condition, Object... value) {
        throw new GrassException(NIE);
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
     * 
     * @param condition 查询条件
     * @param value 查询参数
     * @return
     */
    public static <T extends Model> T findAll(String condition, Object... value) {
        throw new GrassException(NIE);
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
     * 
     * @return
     */
    public static <T extends Model> T findAll() {
        throw new GrassException(NIE);
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
     * 
     * @param condition 查询条件
     * @param value 查询参数
     * @return
     */
    public static long count(String condition, Object... value) {
        throw new GrassException(NIE);
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
     * 
     * @return
     */
    public static long count() {
        throw new GrassException(NIE);
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
            sb.append(",").append(SqlUtils.toColumnName(field.getName()));
        }
        if(sb.length() == 0){
            throw new GrassException("The model '"+ clazz.getName() +"' must have one property at least");
        }
        return sb.substring(1);
    }
    
    // TODO applicationContext.xml中注入JdbcTemplate， 表名前缀
    private static JdbcTemplate getJdbcTemplate() {
        if (dataSource == null) {
            throw new GrassException("The Model.dataSource has to be set before used");
        }
        return new JdbcTemplate(dataSource);
    }
    
    /**
     * 通过反射构造对象
     */
    private static class GenericRowMapper<T extends Model> implements RowMapper<T>{
        Class<T> clazz;
        public GenericRowMapper(Class<T> clazz){
            logger.trace("new RowMapper");
            this.clazz = clazz;
        }
        
        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T t = null;
            Field[] fields = clazz.getDeclaredFields();
            try {
                t = clazz.newInstance();
                
                for (Field field : fields) {  
                    //修改相应filed的权限  
                    boolean accessFlag = field.isAccessible();  
                    field.setAccessible(true);  
                    
                    String columnName = SqlUtils.toColumnName(field.getName());
                    if(rs.getObject(columnName) != null){
                        field.set(t, rs.getObject(columnName));
                    }
                    
                    //恢复相应field的权限  
                    field.setAccessible(accessFlag);  
                }
            } catch (Exception e) {
                logger.error("", e);
                throw new GrassException(e);
            } 
            return t;
        }
    }
}
