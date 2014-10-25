package org.miaoxg.grass.core.enhancer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelClassFileTransformer implements ClassFileTransformer {
	
	private ModelClassEnhancer enhancer;
	
    private static final Logger logger = LoggerFactory.getLogger(ModelClassFileTransformer.class);
    
    public ModelClassFileTransformer() {
        this(new ModelClassEnhancer());
    }
    
    public ModelClassFileTransformer(ModelClassEnhancer enhancer) {
    	this.enhancer = enhancer;
	}
    
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return enhancer.enhance(loader, className);
        } catch (Exception e) {
            logger.error("Failed while transforming the class {}" , className, e);
            throw new IllegalClassFormatException(e.getMessage());
        }
	}
}
