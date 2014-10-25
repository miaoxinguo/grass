package org.miaoxg.grass.core.enhancer;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrassAgent {

	private static final Logger logger = LoggerFactory.getLogger(GrassAgent.class);

    private static Instrumentation instrumentation;
    
    /**
     * JVM hook to statically load the javaagent at startup.
     * 
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst){
        logger.info("premain method invoked with args: {} and inst: {}", args, inst);
        instrumentation = inst;
        instrumentation.addTransformer(new ModelClassFileTransformer(), true);
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     * 
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) {
        logger.info("agentmain method invoked with args: {} and inst: {}", args, inst);
        instrumentation = inst;
        instrumentation.addTransformer(new ModelClassFileTransformer(), true);
    }
}
