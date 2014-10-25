package org.miaoxg.grass.test.dao;

import org.junit.Test;
import org.miaoxg.grass.core.enhancer.GrassAgentLoader;
import org.miaoxg.grass.test.model.User;

public class TestUser {
    
    @Test
    public void testUser(){
        GrassAgentLoader.instance().loadAgent();
        
        User.findById(1);
    }
}
