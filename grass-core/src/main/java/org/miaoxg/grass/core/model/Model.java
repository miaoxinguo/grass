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

    public int save() {
        Map<String, Object> nameAndValues = null;
        try {
            nameAndValues = SqlUtils.buildColumnNameAndValues(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * 使用list存放值 而不是用map.values()是为了保证字段与参数的顺序一致
         */
        List<Object> columnValueList = new ArrayList<Object>();
        StringBuffer columnNames = new StringBuffer();
        StringBuffer placeholder = new StringBuffer();

        for (String name : nameAndValues.keySet()) {
            columnNames.append(",").append(name);
            placeholder.append(",?");
            columnValueList.add(nameAndValues.get(name));
        }
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(SqlUtils.convertPropertyNameToColumnName(this.getClass().getSimpleName()))
                .append("(").append(columnNames.substring(1)).append(")").append(" values(")
                .append(placeholder.substring(1)).append(")");
        logger.trace(sql.toString());
        for (int i = 0; i < columnValueList.size(); i++) {
            logger.trace("paramter {} = {}", i + 1, columnValueList.get(i));
        }
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
        sql.append("delete from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()));

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
        sql.append("delete from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()))
                .append(" where ").append(condition);

        logger.trace(sql.toString());
        for (int i = 0; i < value.length; i++) {
            logger.trace("paramter {} = {}", i + 1, value[i]);
        }
        return getJdbcTemplate().update(sql.toString(), value);
    }

    // TODO applicationContext.xml中注入JdbcTemplate， 表名前缀
    private static JdbcTemplate getJdbcTemplate() {
        if (dataSource == null) {
            throw new GrassException("The Model.dataSource has to be set before used");
        }
        return new JdbcTemplate(dataSource);
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
        String fieldIdName = SqlUtils.convertPropertyNameToColumnName(clazz.getDeclaredFields()[0].getName());
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(SqlUtils.buildColumns(clazz)).append(" from ")
                .append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName())).append(" where ")
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
        sql.append("select ").append(SqlUtils.buildColumns(clazz)).append(" from ")
                .append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName())).append(" where ")
                .append(condition);

        logger.trace(sql.toString());
        for (int i = 0; i < value.length; i++) {
            logger.trace("paramter {} = {}", i + 1, value[i]);
        }
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
        sql.append("select ").append(SqlUtils.buildColumns(clazz)).append(" from ")
                .append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName())).append(" where ")
                .append(condition);

        logger.trace(sql.toString());
        for (int i = 0; i < value.length; i++) {
            logger.trace("paramter {} = {}", i + 1, value[i]);
        }
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
        sql.append("select ").append(SqlUtils.buildColumns(clazz)).append(" from ")
                .append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()));

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
        sql.append("select count(*) from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()))
                .append(" where ").append(condition);

        logger.trace(sql.toString());
        for (int i = 0; i < value.length; i++) {
            logger.trace("paramter {} = {}", i + 1, value[i]);
        }
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
        sql.append("select count(*) from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()));
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
                String setMethodName = SqlUtils.convertColumnNameToSetMethodName(columnName);
                Field field = clazz.getDeclaredField(SqlUtils.convertColumnNameToPropertyName(columnName));
                Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());
                setMethod.invoke(resultBean, map.get(columnName));
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return resultBean;
    }
}
