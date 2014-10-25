package org.miaoxg.grass.test.model;

import org.miaoxg.grass.core.model.Model;

public class User extends Model{
    
    private Integer id;
    private String name;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
