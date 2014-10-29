package org.miaoxg.grass.core.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.miaoxg.grass.core.exception.GrassException;
import org.miaoxg.grass.core.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class Model {
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
        Map<String, Object> map = getJdbcTemplate().queryForMap(sql.toString(), id);
        return convertMapToBean(clazz, map);
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
        Map<String, Object> map = getJdbcTemplate().queryForMap(sql.toString(), value);
        return convertMapToBean(clazz, map);
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
        List<Map<String, Object>> mapList = getJdbcTemplate().queryForList(sql.toString(), value);
        List<T> list = new ArrayList<T>();
        for (Map<String, Object> map : mapList) {
            list.add(convertMapToBean(clazz, map));
        }
        return list;
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
        List<Map<String, Object>> mapList = getJdbcTemplate().queryForList(sql.toString());
        List<T> list = new ArrayList<T>();
        for (Map<String, Object> map : mapList) {
            list.add(convertMapToBean(clazz, map));
        }
        return list;
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
     * map转bean
     * 
     * 不支持Short short Char char
     * TODO 集合 对象类型暂未处理
     */
    private static <T extends Model> T convertMapToBean(Class<T> clazz, Map<String, Object> map) {
        T resultBean = null;
        try {
            resultBean = clazz.newInstance();
            
            // TODO 增加对对象、对象数组、集合的支持
            for (String columnName : map.keySet()) {
                String setMethodName = SqlUtils.toSetterName(columnName);
                Field field = clazz.getDeclaredField(SqlUtils.toPropertyName(columnName));
                Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());
                setMethod.invoke(resultBean, map.get(columnName));
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return resultBean;
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
}
