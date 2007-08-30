/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.bytecode.compiler;

import java.math.BigDecimal;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.optimizer.IntermediateResultsInliner;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.tests.utils.AbstractIOTestBase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;



public class ByteCodeCompilerOnEngineModelTest extends AbstractIOTestBase
{


	public void testOperators() throws Exception
	{
		final double a = 100.34;
		final double b = 3.0;
		assertOperator( Operator.PLUS, a + b );
		assertOperator( Operator.MINUS, a - b );
		assertOperator( Operator.TIMES, a * b );
		assertOperator( Operator.DIV, a / b );
		assertOperator( Operator.EXP, Math.pow( a, b ) );
		assertOperator( Operator.INTERNAL_MIN, b );
		assertOperator( Operator.INTERNAL_MAX, a );
		assertUnaryOperator( Operator.MINUS, -a );
		assertUnaryOperator( Operator.PERCENT, a / 100 );
	}


	private void assertOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		assertDoubleOperator( _operator, _expectedResult );
		assertBigDecimalOperator( _operator, _expectedResult );
		assertScaledLongOperator( _operator, _expectedResult );
	}


	private void assertUnaryOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		assertDoubleUnaryOperator( _operator, _expectedResult );
		assertBigDecimalUnaryOperator( _operator, _expectedResult );
		assertScaledLongUnaryOperator( _operator, _expectedResult );
	}


	private void assertDoubleOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel, _operator.toString() );
	}


	private void assertDoubleUnaryOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel, "Unary_" + _operator.toString() );
	}


	private void assertDoubleResult( final double _expectedResult, final ComputationModel _engineModel, final String _id )
			throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, FormulaCompiler.DOUBLE, _id + "_double" );
		final double d = outputs.getResult();
		assertEquals( _expectedResult, d, 0.000001 );
	}


	private void assertBigDecimalOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalB" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel, _operator.toString(), FormulaCompiler.BIGDECIMAL_SCALE8, "_big" );
		assertBigDecimalResult( _expectedResult, engineModel, _operator.toString(), FormulaCompiler.BIGDECIMAL64, "_bigp" );
	}


	private void assertBigDecimalUnaryOperator( final Operator _operator, final double _expectedResult )
			throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel, "Unary_" + _operator.toString(), FormulaCompiler.BIGDECIMAL_SCALE8, "_big" );
		assertBigDecimalResult( _expectedResult, engineModel, "Unary_" + _operator.toString(), FormulaCompiler.BIGDECIMAL64, "_bigp" );
	}


	private void assertBigDecimalResult( final double _expectedResult, final ComputationModel _engineModel,
			final String _id, NumericType _numericType, String _suffix ) throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, _numericType, _id + _suffix );
		final BigDecimal v = outputs.getBigDecimalA();
		final double d = v.doubleValue();
		assertEquals( _expectedResult, d, 0.000001 );
	}


	private void assertScaledLongOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getScaledLongA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getScaledLongB" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getScaledLongA" ) ) );

		assertScaledLongResult( _expectedResult, engineModel, _operator.toString() );
	}


	private void assertScaledLongUnaryOperator( final Operator _operator, final double _expectedResult )
			throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getScaledLongA" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getScaledLongA" ) ) );

		assertScaledLongResult( _expectedResult, engineModel, "Unary_" + _operator.toString() );
	}


	private void assertScaledLongResult( final double _expectedResult, final ComputationModel _engineModel,
			final String _id ) throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, FormulaCompiler.LONG_SCALE4, _id + "_long4" );
		final long actual = outputs.getScaledLongA();
		final long expected = FormulaCompiler.LONG_SCALE4.valueOf( _expectedResult ).longValue();
		final long diff = actual - expected;
		if (diff > 1 || diff < -1) {
			// accept difference in the last decimal due to rounding problems with division
			assertEquals( expected, actual );
		}
	}


	private Outputs newOutputs( ComputationModel _engineModel, NumericType _numericType, String _id ) throws Exception
	{
		_engineModel.traverse( new IntermediateResultsInliner() );
		_engineModel.traverse( new TypeAnnotator() );
		final ByteCodeEngineCompiler.Config config = new ByteCodeEngineCompiler.Config();
		config.model = _engineModel;
		config.numericType = _numericType;
		final ByteCodeEngineCompiler compiler = new ByteCodeEngineCompiler( config );
		final SaveableEngine engine = compiler.compile();

		checkEngine( engine, _id );

		final ComputationFactory factory = engine.getComputationFactory();
		return (Outputs) factory.newComputation( new Inputs() );
	}

}
