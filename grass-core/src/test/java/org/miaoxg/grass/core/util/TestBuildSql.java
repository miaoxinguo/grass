package org.miaoxg.grass.core.util;

import org.junit.Test;
import org.miaoxg.grass.core.model.DummyModel;
import org.miaoxg.grass.core.util.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBuildSql {
    private static final Logger logger = LoggerFactory.getLogger(TestBuildSql.class);
    @Test
    public void testFindById(){
        logger.debug(SqlUtils.buildColumns(DummyModel.class));
    }
}
