package org.miaoxg.grass.core.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBuildSql {
    private static final Logger logger = LoggerFactory.getLogger(TestBuildSql.class);
    @Test
    public void testConvert(){
        logger.debug(SqlUtils.toColumnName("userId"));
        logger.debug(SqlUtils.toPropertyName("user_id_test"));;
    }
}
