package org.miaoxg.grass.core.enhancer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.instrument.IllegalClassFormatException;

import org.junit.Before;
import org.junit.Test;
import org.miaoxg.grass.core.enhancer.ModelClassEnhancer;
import org.miaoxg.grass.core.enhancer.ModelClassFileTransformer;

/**
 * @author ganeshs
 *
 */
public class DomainClassFileTransformerTest {

	private ModelClassEnhancer enhancer;
	
	@Before
	public void setup() {
		enhancer = mock(ModelClassEnhancer.class);
	}
	
	@Test
	public void shouldTransform() throws IllegalClassFormatException {
		ClassLoader loader = mock(ClassLoader.class);
		ModelClassFileTransformer transformer = new ModelClassFileTransformer(enhancer);
		transformer.transform(loader, ModelClassEnhancer.class.getName(), null, null, null);
		verify(enhancer).enhance(loader, ModelClassEnhancer.class.getName());
	}
	
	@Test
	public void shouldNotTransformIgnoredPackageClasses() throws IllegalClassFormatException {
	    ClassLoader loader = mock(ClassLoader.class);
		ModelClassFileTransformer transformer = new ModelClassFileTransformer(enhancer);
		transformer.transform(loader, "javax/test", null, null, null);
		transformer.transform(loader, "java/lang/String", null, null, null);
		transformer.transform(loader, "com/sun/test", null, null, null);
		transformer.transform(loader, "sun/test", null, null, null);
		verify(enhancer, never()).enhance(eq(loader), anyString());
	}
}
