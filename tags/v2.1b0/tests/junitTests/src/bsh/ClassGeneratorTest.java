package bsh;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class ClassGeneratorTest {

	@Test
	public void create_class_with_default_constructor() throws Exception {
		eval("class X1 {}");
	}


	@Test
	public void create_instance() throws Exception {
		Assert.assertNotNull(
			eval(
				"class X2 {}",
				"return new X2();"
		));
	}


	@Test
	public void constructor_args() throws Exception {
		final Object[] oa = (Object[]) eval(
			"class X3 implements java.util.concurrent.Callable {",
				"Object _instanceVar;",
				"public X3(Object arg) { _instanceVar = arg; }",
				"public Object call() { return _instanceVar; }",
			"}",
			"return new Object[] { new X3(0), new X3(1) } ");
		assertEquals(0, ( (Callable) oa[0] ).call());
		assertEquals(1, ( (Callable) oa[1] ).call());
	}


	@Test
	public void outer_namespace_visibility() throws Exception {
		final Callable callable = (Callable) eval(
			"class X4 implements java.util.concurrent.Callable {",
				"public Object call() { return var; }",
			"}",
			"var = 0;",
			"a = new X4();",
			"var = 1;",
			"return a;");
		assertEquals(1, callable.call());
	}


	@Test
	public void static_fields_should_be_frozen() throws Exception {
		final Callable callable = (Callable) eval(
				"var = 0;",
				"class X5 implements java.util.concurrent.Callable {",
					"static final Object VAR = var;",
					"public Object call() { return VAR; }",
				"}",
				"a = new X5();",
				"var = 1;",
				"return a;"
		);
		assertEquals(0, callable.call());
	}


	@Test
	public void inner_class() throws Exception {
		
	}


	public Object eval(final String ... code) throws Exception {
		StringBuffer buffer = new StringBuffer();
		for (String s : code) {
			buffer.append(s).append('\n');
		}
		return new Interpreter().eval(buffer.toString());
	}
}
