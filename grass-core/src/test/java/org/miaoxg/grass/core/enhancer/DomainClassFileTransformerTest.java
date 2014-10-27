package org.miaoxg.grass.core.enhancer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.instrument.IllegalClassFormatException;

import org.junit.Before;
import org.junit.Test;

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
}
