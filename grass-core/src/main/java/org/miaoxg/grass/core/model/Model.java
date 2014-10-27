package org.miaoxg.grass.core.model;

import java.io.Serializable;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
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
     * 根据主键查询
     * 
     * @param id
     *            主键
     * @return
     */
    public static <T extends Model> T findById(Integer id) {
        throw new GrassException(NIE);
    }

    /**
     * 根据主键查询, 默认使用model的第一个属性作为主键
     * 
     * @param clazz
     *            model类型
     * @param id
     *            主键
     * @return
     */
    protected static <T extends Model> T findById(Class<T> clazz, Serializable id) {
        String fieldIdName = SqlUtils.convertPropertyNameToColumnName(clazz.getDeclaredFields()[0].getName());
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(SqlUtils.buildColumns(clazz))
        .append(" from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()))
        .append(" where ").append(fieldIdName).append("=?");

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
        sql.append("select ").append(SqlUtils.buildColumns(clazz))
        .append(" from ").append(SqlUtils.convertPropertyNameToColumnName(clazz.getSimpleName()))
        .append(" where ").append(condition);

        logger.trace(sql.toString());
        for(int i=0; i<value.length; i++){
            logger.trace("paramter {} = {}", i+1, value[i]);
        }
        Map<String, Object> map =  getJdbcTemplate().queryForMap(sql.toString(), value);
        return convertMapToBean(clazz, map);
    }
    
    /**
     * 删除
     * 
     * @param c 条件
     * @param value 参数
     * @return 影响的行数
     */
    public int delete(String condition, Object... value) {
        return 1;
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
        for(int i=0; i<value.length; i++){
            logger.trace("paramter {} = {}", i+1, value[i]);
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
     * map转bean
     * 
     * TODO 集合 对象类型暂未处理
     */
    private static <T extends Model> T convertMapToBean(Class<T> clazz, Map<String, Object> map) {
        T resultBean = null;
        try {
            resultBean = clazz.newInstance();
            for (String key : map.keySet()) {
                PropertyUtils.setProperty(resultBean, SqlUtils.convertColumnNameToPropertyName(key), map.get(key));
            }
        } catch (Exception e) {
            logger.error("", e);
            throw new GrassException(e);
        }
        return resultBean;
    }
}
