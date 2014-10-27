package org.miaoxg.grass.core.model;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.miaoxg.grass.core.exception.GrassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestDb {
    private static final Logger logger = LoggerFactory.getLogger(TestDb.class);
    private static ApplicationContext context;

    @BeforeClass
    public static void initContext(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        DataSource dataSource = context.getBean( "dataSource", DataSource.class);
        if(dataSource == null){
            throw new GrassException("The Model.dataSource has to be set before used");
        }
        Model.dataSource = dataSource;
    }
    
    @Test
    public void testFindOne(){
        DummyModel d = Model.findOne(DummyModel.class, "id = ? and column_1 = ?", 1, "sfj");
        Assert.assertNotNull(d);
        logger.debug("id = {}, column1 = {}", d.getId(), d.getColumn1());
    }
    
    @Test
    public void testFindById(){
        DummyModel d = Model.findById(DummyModel.class, 1);
        Assert.assertNotNull(d);
        logger.debug("id = {}, column1 = {}", d.getId(), d.getColumn1());
    }
    
    @Test
    public void testDelete(){
        int i= Model.deleteAll(DummyModel.class, "id = ? and column_1 = ?", 2, "sd");
        Assert.assertTrue(i>=0);
    }
}
