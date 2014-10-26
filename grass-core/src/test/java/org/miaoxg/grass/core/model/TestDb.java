package org.miaoxg.grass.core.model;

import javax.sql.DataSource;

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
    public void testFindById(){
        DummyModel d = Model.findById(DummyModel.class, 1);
        logger.debug("id = {}", d.getId());
        logger.debug("column1 = {}", d.getColumn1());
        logger.debug("column2 = {}", d.getColumn2());
        logger.debug("column3 = {}", d.getColumn3());
    }
    
    @Test
    public void testDeleteById(){
        int i= Model.deleteById(DummyModel.class, 1);
        logger.debug("删除 {} 行", i);
    }
}
