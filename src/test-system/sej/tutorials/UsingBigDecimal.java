/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.tutorials;

import java.math.BigDecimal;

import sej.EngineBuilder;
import sej.SEJ;
import sej.runtime.Engine;
import junit.framework.TestCase;

public class UsingBigDecimal extends TestCase
{
	private static final String PATH = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";


	public void testUsingBigDecimal() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler
		builder.setNumericType( /**/SEJ.getNumericType( BigDecimal.class, 20, BigDecimal.ROUND_UP )/**/ );
		// ---- buildCompiler
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		// ---- checkResult
		Output output = factory.newInstance( new Input( 6 ) );
		assertEquals( /**/"1.16666666666666666667"/**/, output.getResult().toPlainString() );
		// ---- checkResult
	}


	public void testUsingBigDecimal8() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler8
		builder.setNumericType( /**/SEJ.BIGDECIMAL8/**/ );
		// ---- buildCompiler8
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		{
			// ---- checkResult8a
			Output output = factory.newInstance( new Input( 6 ) );
			assertEquals( /**/"1.16666667"/**/, output.getResult().toPlainString() );
			// ---- checkResult8a
		}

		{
			// ---- checkResult8b
			Output output = factory.newInstance( new Input( /**/3/**/ ) );
			assertEquals( /**/"1.33333333"/**/, output.getResult().toPlainString() );
			// ---- checkResult8b
		}
	}


	public void testUsingBigDecimalN() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompilerN
		builder.setNumericType( /**/SEJ.getNumericType( BigDecimal.class )/**/ );
		// ---- buildCompilerN
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		{
			// ---- checkResultNa
			Output output = factory.newInstance( new Input( /**/4/**/ ) );
			assertEquals( /**/"1.25"/**/, output.getResult().toPlainString() );
			// ---- checkResultNa
		}

		// ---- checkResultNb
		try {
			Output output = factory.newInstance( new Input( /**/3/**/ ) );
			output.getResult();
			fail( "ArithmeticException expected" );
		}
		catch (/**/ArithmeticException e/**/) {
			assertEquals( "Non-terminating decimal expansion; no exact representable decimal result.", e.getMessage() );
		}
		// ---- checkResultNb
	}


	// ---- IO
	public static class Input
	{
		public Input(int b)  { this.b = b; }
		public /**/BigDecimal/**/ getA()  { return BigDecimal.valueOf( 1 ); }
		public /**/BigDecimal/**/ getB()  { return BigDecimal.valueOf( this.b ); }
		private final int b;
	}

	public static interface Output
	{
		/**/BigDecimal/**/ getResult();
	}
	// ---- IO

	public static interface Factory
	{
		Output newInstance( Input _input );
	}


}