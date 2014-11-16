package org.miaoxg.grass.core.model;

import java.util.List;

import org.miaoxg.grass.core.exception.GrassException;

/**
 * 定义方法
 * 
 * @author miaoxinguo2002@163.com
 */
public abstract class Model extends BaseModel{
    
    private static final String NIE = "Your models are not instrumented. Make sure you load the agent using GrassAgentLoader.instance().loadAgent()";

    /**
     * 设置忽略字段
     * 
     * @param fieldNames 多个字段名
     */
    public Model exclude(String... fieldNames) {
        setExclude(fieldNames);
        return this;
    }

    /**
     * 将当前对象的值存入数据库
     */
    public int save() {
        return super.save();
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
     * 修改记录
     */
    public int update() {
        return super.update();
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
     * 查询一条
     */
    public static <T extends Model> T findOne(String condition, Object... value) {
        throw new GrassException(NIE);
    }

    /**
     * 根据条件查询
     * 
     * @param condition 查询条件
     * @param value 查询参数
     * @return
     */
    public static <T extends Model> List<T> findAll(String condition, Object... value) {
        throw new GrassException(NIE);
    }

    /**
     * 查询全部
     * 
     * @return
     */
    public static <T extends Model> List<T> findAll() {
        throw new GrassException(NIE);
    }
    
    /**
     * 使用完整的sql语句进行查询
     * 
     * @return
     */
    public static <T extends Model> List<T> findAll(String sql) {
        throw new GrassException(NIE);
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
     * 查询总记录数
     * 
     * @return
     */
    public static long count() {
        throw new GrassException(NIE);
    }
    
    
}
