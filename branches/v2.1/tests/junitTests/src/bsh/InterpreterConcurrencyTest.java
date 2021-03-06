package bsh;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class InterpreterConcurrencyTest {

	static final String script =
			/*  1 */ "call(param) {" +
			/*  3 */ "	class Echo {\n" +
			/*  4 */ "		\n" +
			/*  5 */ "   	final Object _s;\n" +
			/*  6 */ "		\n" +
			/*  7 */ "   	Echo(Object s) {\n" +
			/*  8 */ "      	_s = s;\n" +
			/*  9 */ "   	}\n" +
			/*  0 */ "		\n" +
			/* 11 */ "   	public Object echo() {\n" +
			/* 12 */ "      	return param;\n" +
			/* 13 */ "   	}\n" +
			/* 14 */ "		\n" +
			/* 15 */ "	}\n" +
			/* 16 */ "	\n" +
			/* 17 */ "	return new Echo(param).echo();\n" +
			/* 18 */ "}" +
			/* 19 */ "return this;";


	@Test
	public void single_threaded() throws Exception {
		final This callable = createCallable();
		Assert.assertEquals("foo", callable.invokeMethod("call", new Object[] { "foo" }));
		Assert.assertEquals(42, callable.invokeMethod("call", new Object[] { 42 }));
	}


	@Test
	public void multi_threaded_callable() throws Exception {
		final AtomicInteger counter = new AtomicInteger();
		final String script =
				"call(v) {"+
				"	return v;" +
				"}" +
				"return this;";
		final Interpreter interpreter = new Interpreter();
		final This callable = (This) interpreter.eval(script);
		final Runnable runnable = new Runnable() {
			public void run() {
				final int value = counter.incrementAndGet();
				try {
					Assert.assertEquals(value, callable.invokeMethod("call", new Object[] { value }  ));
				} catch (final EvalError evalError) {
					throw new RuntimeException(evalError);
				}
			}
		};
		TestUtil.measureConcurrentTime(runnable, 30, 30, 100);
	}


	@Test
	public void multi_threaded_class_generation() throws Exception {
		final This callable = createCallable();
		final AtomicInteger counter = new AtomicInteger();
		final Runnable runnable = new Runnable() {
			public void run() {
				try {
					final int i = counter.incrementAndGet();
					final Object o = callable.invokeMethod("call", new Object[]{i});
					Assert.assertEquals(i, o);
				} catch (final EvalError evalError) {
					throw new RuntimeException(evalError);
				}
			}
		};
		TestUtil.measureConcurrentTime(runnable, 30, 30, 100);
	}


	private This createCallable() throws EvalError {
		final Interpreter interpreter = new Interpreter();
		return (This) interpreter.eval(script);
	}

}
