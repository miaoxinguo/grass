package org.miaoxg.grass.core.enhancer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.miaoxg.grass.core.enhancer.ModelClassEnhancer;
import org.miaoxg.grass.core.model.DummyModel;

/**
 * @author ganeshs
 *
 */
public class DomainClassEnhancerTest {
	
	private ClassPool classPool;
	
	private ModelClassEnhancer enhancer;
	
	@Before
	public void setup() throws NotFoundException {
		this.classPool = spy(ClassPool.getDefault());
		this.enhancer = new ModelClassEnhancer();
	}
	
	@Test
	public void shouldAllowEnhancingForDomainModel() {
		assertTrue(enhancer.canEnhance(DummyModel.class.getName()));
	}
	
	@Test
	public void shouldNotAllowEnhancingForNonModel() {
		assertFalse(enhancer.canEnhance(String.class.getName()));
	}
	
	@Test
	public void shouldAddMethodsToModelClass() throws Exception {
		CtClass ctClass = classPool.get(DummyModel.class.getName());
		doReturn(ctClass).when(classPool).get(DummyModel.class.getName());
		enhancer.enhance(this.getClass().getClassLoader(), DummyModel.class.getName());
//		assertNotNull(ctClass.getMethod("save", "()V"));
		
		assertNotNull(ctClass.getMethod("deleteById", "(Ljava/io/Serializable;)I"));
//		assertNotNull(ctClass.getMethod("deleteAll", "()V"));
//		assertNotNull(ctClass.getMethod("deleteAll", "(Lorg/mar/model/Filter;)V"));
		
		assertNotNull(ctClass.getMethod("findById", "(Ljava/io/Serializable;)Lorg/miaoxg/grass/core/model/Model;"));
//		assertNotNull(ctClass.getMethod("one", "([Ljava/lang/Object;)Lorg/mar/model/Model;"));
//		assertNotNull(ctClass.getMethod("all", "()Ljava/util/List;"));
//		assertNotNull(ctClass.getMethod("count", "()J"));  // J表示long
//		assertNotNull(ctClass.getMethod("count", "(Lorg/mar/model/Filter;)J"));
//		assertNotNull(ctClass.getMethod("where", "([Ljava/lang/Object;)Ljava/util/List;"));
//		assertNotNull(ctClass.getMethod("where", "(Lorg/mar/model/Filter;)Ljava/util/List;"));
//		assertNotNull(ctClass.getMethod("exists", "(Ljava/io/Serializable;)Z"));
	}

}
