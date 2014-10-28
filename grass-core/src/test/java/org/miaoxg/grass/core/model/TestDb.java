package org.miaoxg.grass.core.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

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
    public void save(){
        DummyModel d = new DummyModel();
        d.setColumn1("test1");
        d.setColumn2("test2");
        d.setColumn3("test3");
        d.setLongValue(123L);
        d.setFloatValue(123.45f);
        d.setDateValue(new Date(System.currentTimeMillis()));
        d.setTimestampValue(new Timestamp(System.currentTimeMillis()));
        int count = d.exclude("id").save();
        Assert.assertTrue(count==1);
        logger.debug("共保存{}条记录", count);
    }
    
    @Test
    public void testCount(){
        long count = Model.count(DummyModel.class, "id > ?", 1);
        Assert.assertTrue(count>=0);
        logger.debug("共{}条记录", count);
        
        count = Model.count(DummyModel.class);
        Assert.assertTrue(count>=0);
        logger.debug("共{}条记录", count);
    }
    
    @Test
    public void testFindAll(){
        List<DummyModel> d = Model.findAll(DummyModel.class, "id > ?", 1);
        Assert.assertNotNull(d);
        logger.debug("查询到{}行", d.size());
        
        d = Model.findAll(DummyModel.class);
        Assert.assertNotNull(d);
        logger.debug("查询到{}行", d.size());
    }
    
    @Test
    public void testFindOne(){
        DummyModel d = Model.findOne(DummyModel.class, "id = ? ", 29);
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
        int count = Model.deleteAll(DummyModel.class, "id < ? ", 15);
        Assert.assertTrue(count >=0 );
    }
    
    @Test
    public void testDeleteAll(){
        int i= Model.deleteAll(DummyModel.class);
        Assert.assertTrue(i>=0);
    }
}
