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

public class TestArrayModel {
    private static final Logger logger = LoggerFactory.getLogger(TestArrayModel.class);
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
        ArrayModel m = new ArrayModel();
        byte[] smallb = new byte[]{1,2,3,4};
//        Byte[] bigb = new Byte[]{Byte.valueOf("1"),Byte.valueOf("2"),Byte.valueOf("3"),Byte.valueOf("4")};
        m.setByteArray(smallb);
//        m.setByteObjectArray(bigb);
        int count = m.save();
        Assert.assertTrue(count==1);
        logger.debug("共保存{}条记录", count);
    }
    
    @Test
    public void testfindArray(){
        byte[] ba = ArrayModel.findAll(ArrayModel.class).get(0).getByteArray();
        logger.debug("{}", ba);
    }
}
