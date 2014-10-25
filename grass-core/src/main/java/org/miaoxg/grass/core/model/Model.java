package org.miaoxg.grass.core.model;

import java.io.Serializable;

import org.miaoxg.grass.core.exception.GrassException;


public abstract class Model {
    
    private static final String NIE = "Your models are not instrumented. Make sure you load the agent using GrassAgentLoader.instance().loadAgent()";
    
    /**
     * 根据主键查询
     * @param id 主键
     * @return
     */
    public static <T extends Model> T findById(Integer id){
        throw new GrassException(NIE);
    }
    
    /**
     * 根据主键查询
     * @param clazz model类型
     * @param id 主键
     * @return
     */
    public static <T extends Model> T findById(Class<T> clazz, Serializable id) {
        return null;
    }
    
    /**
     * 根据主键删除
     * @param id 主键
     * @return
     */
    public static <T extends Model> T deleteById(Integer id){
        throw new GrassException(NIE);
    }
    
    /**
     * 根据主键删除
     * @param clazz model类型
     * @param id 主键
     * @return
     */
    public static <T extends Model> T deleteById(Class<T> clazz, Serializable id) {
        return null;
    }
}