package org.miaoxg.grass.core.model;

import java.util.ArrayList;
import java.util.List;

public enum SupportedType {
    instance;
    
    public static boolean contains(String name){
        return supportedTypeList.contains(name.toLowerCase());
    }
    
    /**
     * 只有这些类型的字段能保存、修改、查询
     * 
     * 使用小写保存，判断时需要将字段类型转为小写形式
     */
    @SuppressWarnings({"serial"})
    private static List<String> supportedTypeList = new ArrayList<String>(){{
        add("string");     add("char");   add("character");
        add("integer");    add("int");    add("short");       add("byte");
        add("long");       add("float");  add("double");
        add("date");       add("time");   add("timestamp"); 
        add("boolean");
    }};
}



