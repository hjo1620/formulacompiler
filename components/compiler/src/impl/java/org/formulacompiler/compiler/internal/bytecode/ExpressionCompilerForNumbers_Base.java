/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.bytecode;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.Milliseconds;
import org.formulacompiler.runtime.MillisecondsSinceUTC1970;
import org.formulacompiler.runtime.ScaledLong;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;


abstract class ExpressionCompilerForNumbers_Base extends ExpressionCompilerForAll_Generated
{
	static ExpressionCompilerForNumbers compilerFor( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		if (Double.TYPE == _numericType.valueType()) {
			return new ExpressionCompilerForDoubles( _methodCompiler, _numericType );
		}
		else if (Long.TYPE == _numericType.valueType()) {
			return new ExpressionCompilerForScaledLongs( _methodCompiler, _numericType );
		}
		else if (BigDecimal.class == _numericType.valueType()) {
			if (null != _numericType.mathContext()) {
				return new ExpressionCompilerForPrecisionBigDecimals( _methodCompiler, _numericType );
			}
			else {
				return new ExpressionCompilerForScaledBigDecimals( _methodCompiler, _numericType );
			}
		}
		else {
			throw new IllegalArgumentException( "Unsupported data type " + _numericType + " for byte code compilation." );
		}
	}


	private final NumericType numericType;

	public ExpressionCompilerForNumbers_Base( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		super( _methodCompiler );
		this.numericType = _numericType;
	}


	NumericType numericType()
	{
		return this.numericType;
	}

	@Override
	protected TypeCompiler typeCompiler()
	{
		return section().engineCompiler().numberCompiler();
	}


	protected abstract void compileConversionFrom( ScaledLong _scale ) throws CompilerException;
	protected abstract void compileConversionTo( ScaledLong _scale ) throws CompilerException;


	protected void compileConversionFromLong() throws CompilerException
	{
		compile_util_fromLong();
	}

	protected void compileConversionFromInt() throws CompilerException
	{
		compile_util_fromInt();
	}

	protected void compileConversionFromNumber() throws CompilerException
	{
		compile_util_fromNumber();
	}

	protected void compileConversionFromBigInteger() throws CompilerException
	{
		compile_util_fromBigInteger();
	}

	protected void compileConversionFromBigDecimal() throws CompilerException
	{
		compile_util_fromBigDecimal();
	}

	protected void compileConversionFromFloat() throws CompilerException
	{
		compile_util_fromFloat();
	}

	protected void compileConversionFromDouble() throws CompilerException
	{
		compile_util_fromDouble();
	}

	protected void compileConversionToLong() throws CompilerException
	{
		compile_util_toLong();
	}

	protected void compileConversionToInt() throws CompilerException
	{
		compile_util_toInt();
	}

	protected void compileConversionToShort() throws CompilerException
	{
		compile_util_toShort();
	}

	protected void compileConversionToByte() throws CompilerException
	{
		compile_util_toByte();
	}

	protected void compileConversionToBigDecimal() throws CompilerException
	{
		compile_util_toBigDecimal();
	}

	protected void compileConversionToBigInteger() throws CompilerException
	{
		compile_util_toBigInteger();
	}

	protected void compileConversionToFloat() throws CompilerException
	{
		compile_util_toFloat();
	}

	protected void compileConversionToDouble() throws CompilerException
	{
		compile_util_toDouble();
	}

	protected void compileConversionToBoolean() throws CompilerException
	{
		compile_util_toBoolean();
	}

	protected void compileConversionToNumber() throws CompilerException
	{
		compile_util_toNumber();
	}

	protected final void compileInt( ExpressionNode _node ) throws CompilerException
	{
		compile( _node );
		if (!(_node instanceof ExpressionNodeForFunction)
				|| !((ExpressionNodeForFunction) _node).getFunction().returnsInt()) {
			compileConversionToInt();
		}
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (_class == Number.class) {
			compileConversionToNumber();
		}
		else {
			final Class unboxed = unboxed( _class );
			if (null == unboxed) {
				compileConversionToUnboxed( _class );
			}
			else {
				compileConversionToUnboxed( unboxed );
				compileBoxing( _class );
			}
		}
	}


	protected void compileConversionToUnboxed( Class _class ) throws CompilerException
	{
		if (_class == Long.TYPE) {
			compileConversionToLong();
		}

		else if (_class == Integer.TYPE) {
			compileConversionToInt();
		}

		else if (_class == Short.TYPE) {
			compileConversionToShort();
		}

		else if (_class == Byte.TYPE) {
			compileConversionToByte();
		}

		else if (_class == Boolean.TYPE) {
			compileConversionToBoolean();
		}

		else if (_class == Double.TYPE) {
			compileConversionToDouble();
		}

		else if (_class == Float.TYPE) {
			compileConversionToFloat();
		}

		else if (_class == BigInteger.class) {
			compileConversionToBigInteger();
		}

		else if (_class == BigDecimal.class) {
			compileConversionToBigDecimal();
		}

		else if (_class == Date.class) {
			compile_util_toDate();
		}

		else if (_class == String.class) {
			compile_util_toString();
		}

		else {
			super.compileConversionTo( _class );
		}
	}

	protected abstract void compile_util_toByte() throws CompilerException;
	protected abstract void compile_util_toShort() throws CompilerException;
	protected abstract void compile_util_toInt() throws CompilerException;
	protected abstract void compile_util_toLong() throws CompilerException;
	protected abstract void compile_util_toDouble() throws CompilerException;
	protected abstract void compile_util_toFloat() throws CompilerException;
	protected abstract void compile_util_toBigDecimal() throws CompilerException;
	protected abstract void compile_util_toBigInteger() throws CompilerException;
	protected abstract void compile_util_toBoolean() throws CompilerException;
	protected abstract void compile_util_toDate() throws CompilerException;
	protected abstract void compile_util_toString() throws CompilerException;
	protected abstract void compile_util_toNumber() throws CompilerException;


	private Class unboxed( Class _class )
	{
		if (Byte.class == _class) {
			return Byte.TYPE;
		}
		else if (Short.class == _class) {
			return Short.TYPE;
		}
		else if (Integer.class == _class) {
			return Integer.TYPE;
		}
		else if (Long.class == _class) {
			return Long.TYPE;
		}
		else if (Float.class == _class) {
			return Float.TYPE;
		}
		else if (Double.class == _class) {
			return Double.TYPE;
		}
		else if (Character.class == _class) {
			return Character.TYPE;
		}
		else if (Boolean.class == _class) {
			return Boolean.TYPE;
		}
		return null;
	}


	private void compileBoxing( Class _class ) throws CompilerException
	{
		if (Byte.class == _class) {
			compile_util_boxByte();
		}
		else if (Short.class == _class) {
			compile_util_boxShort();
		}
		else if (Integer.class == _class) {
			compile_util_boxInteger();
		}
		else if (Long.class == _class) {
			compile_util_boxLong();
		}
		else if (Float.class == _class) {
			compile_util_boxFloat();
		}
		else if (Double.class == _class) {
			compile_util_boxDouble();
		}
		else if (Character.class == _class) {
			compile_util_boxCharacter();
		}
		else if (Boolean.class == _class) {
			compile_util_boxBoolean();
		}
	}


	protected void compileConversionToString() throws CompilerException
	{
		compile_util_toString();
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		compileConversionFromUnboxed( compileUnboxing( _class ) );
	}


	protected void compileConversionFromUnboxed( Class _class ) throws CompilerException
	{
		if (_class == Integer.TYPE || _class == Short.TYPE || _class == Byte.TYPE) {
			compileConversionFromInt();
		}

		else if (_class == Long.TYPE) {
			compileConversionFromLong();
		}

		else if (_class == Double.TYPE) {
			compileConversionFromDouble();
		}

		else if (_class == Float.TYPE) {
			compileConversionFromFloat();
		}

		else if (BigDecimal.class.isAssignableFrom( _class )) {
			compileConversionFromBigDecimal();
		}

		else if (BigInteger.class.isAssignableFrom( _class )) {
			compileConversionFromBigInteger();
		}

		else if (Number.class.isAssignableFrom( _class )) {
			compileConversionFromNumber();
		}

		else if (_class == Boolean.TYPE) {
			compile_util_fromBoolean();
		}

		else if (Date.class.isAssignableFrom( _class )) {
			compile_util_fromDate();
		}

		else {
			super.compileConversionFrom( _class );
		}
	}

	protected abstract void compile_util_fromInt() throws CompilerException;
	protected abstract void compile_util_fromLong() throws CompilerException;
	protected abstract void compile_util_fromDouble() throws CompilerException;
	protected abstract void compile_util_fromFloat() throws CompilerException;
	protected abstract void compile_util_fromNumber() throws CompilerException;
	protected abstract void compile_util_fromBoolean() throws CompilerException;
	protected abstract void compile_util_fromDate() throws CompilerException;

	protected void compile_util_fromBigDecimal() throws CompilerException
	{
		compile_util_fromNumber();
	}

	protected void compile_util_fromBigInteger() throws CompilerException
	{
		compile_util_fromNumber();
	}


	Class compileUnboxing( Class _class ) throws CompilerException
	{
		if (Byte.class.isAssignableFrom( _class )) {
			compile_util_unboxByte();
			return Byte.TYPE;
		}
		else if (Short.class.isAssignableFrom( _class )) {
			compile_util_unboxShort();
			return Short.TYPE;
		}
		else if (Integer.class.isAssignableFrom( _class )) {
			compile_util_unboxInteger();
			return Integer.TYPE;
		}
		else if (Long.class.isAssignableFrom( _class )) {
			compile_util_unboxLong();
			return Long.TYPE;
		}
		else if (Float.class.isAssignableFrom( _class )) {
			compile_util_unboxFloat();
			return Float.TYPE;
		}
		else if (Double.class.isAssignableFrom( _class )) {
			compile_util_unboxDouble();
			return Double.TYPE;
		}
		else if (Character.class.isAssignableFrom( _class )) {
			compile_util_unboxCharacter();
			return Character.TYPE;
		}
		else if (Boolean.class.isAssignableFrom( _class )) {
			compile_util_unboxBoolean();
			return Boolean.TYPE;
		}
		return _class;
	}


	protected abstract void compile_util_fromMs() throws CompilerException;
	protected abstract void compile_util_fromMsSinceUTC1970() throws CompilerException;

	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {

			if ((_method.getAnnotation( Milliseconds.class ) != null)) {
				if (returnType == Long.class) {
					compile_util_unboxLong();
				}
				compile_util_fromMs();
				return;
			}

			if ((_method.getAnnotation( MillisecondsSinceUTC1970.class ) != null)) {
				if (returnType == Long.class) {
					compile_util_unboxLong();
				}
				compile_util_fromMsSinceUTC1970();
				return;
			}

			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				if (returnType == Long.class) {
					compile_util_unboxLong();
				}
				compileConversionFrom( scale );
				return;
			}

		}
		compileConversionFrom( returnType );
	}


	protected abstract void compile_util_toMs() throws CompilerException;
	protected abstract void compile_util_toMsSinceUTC1970() throws CompilerException;

	@Override
	protected void innerCompileConversionToResultOf( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();
		if (returnType == Long.TYPE || returnType == Long.class) {

			if ((_method.getAnnotation( Milliseconds.class ) != null)) {
				compile_util_toMs();
				if (returnType == Long.class) {
					compile_util_boxLong();
				}
				return;
			}

			if ((_method.getAnnotation( MillisecondsSinceUTC1970.class ) != null)) {
				compile_util_toMsSinceUTC1970();
				if (returnType == Long.class) {
					compile_util_boxLong();
				}
				return;
			}

			final ScaledLong scale = scaleOf( _method );
			if (scale != null && scale.value() != 0) {
				compileConversionTo( scale );
				if (returnType == Long.class) {
					compile_util_boxLong();
				}
				return;
			}

		}
		if (!isNativeType( returnType )) {
			compileConversionTo( returnType );
		}
	}

	protected abstract boolean isNativeType( Class _type );


	protected final ScaledLong scaleOf( Method _method )
	{
		final ScaledLong typeScale = _method.getDeclaringClass().getAnnotation( ScaledLong.class );
		final ScaledLong mtdScale = _method.getAnnotation( ScaledLong.class );
		final ScaledLong scale = (mtdScale != null) ? mtdScale : typeScale;
		return scale;
	}


	@Override
	protected void compileConst( Object _value ) throws CompilerException
	{
		if (_value instanceof Date) {
			mv().push( ((Date) _value).getTime() );
			compile_util_fromMsSinceUTC1970();
		}
		else {
			super.compileConst( _value );
		}
	}


	@Override
	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.getFunction()) {

			case MATCH:
			case INTERNAL_MATCH_INT:
				compileMatch( _node );
				return;

		}
		super.compileFunction( _node );
	}


	private void compileMatch( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.cardinality()) {

			case 2: {
				compileMatch( _node, 1 );
				return;
			}

			case 3: {
				final ExpressionNode typeArg = _node.arguments().get( 2 );
				if (typeArg instanceof ExpressionNodeForConstantValue) {
					final ExpressionNodeForConstantValue constTypeArg = (ExpressionNodeForConstantValue) typeArg;
					final Object typeVal = constTypeArg.value();
					if (typeVal instanceof Number) {
						compileMatch( _node, ((Number) typeVal).intValue() );
					}
					else if (typeVal instanceof Boolean) {
						compileMatch( _node, (Boolean) typeVal ? 1 : 0 );
					}
					else {
						compileMatch( _node, 1 );
					}
					return;
				}
				throw new InnerExpressionException( typeArg, new CompilerException.UnsupportedExpression(
						"The last argument to MATCH, the match type, must be constant, but is " + typeArg.describe() + "." ) );
			}

		}
		throw new CompilerException.UnsupportedExpression( "MATCH must have two or three arguments." );
	}

	private static final String[] TYPE_SUFFIXES = { "Descending", "Exact", "Ascending" };

	private void compileMatch( ExpressionNodeForFunction _node, int _type ) throws CompilerException
	{
		final ExpressionNode valNode = _node.argument( 0 );
		final ExpressionNodeForArrayReference arrayNode = (ExpressionNodeForArrayReference) _node.argument( 1 );
		if (valNode.getDataType() != arrayNode.getDataType()) {
			throw new CompilerException.UnsupportedExpression(
					"MATCH must have the same type of argument in the first and second slot." );
		}
		final int type = (_type > 0) ? +1 : (_type < 0) ? -1 : 0;

		final ExpressionCompilerForNumbers numCompiler = method().numericCompiler();
		final ExpressionCompiler valCompiler = method().expressionCompiler( valNode.getDataType() );
		// Trim trailing nulls. See http://code.google.com/p/formulacompiler/issues/detail?id=29.
		final ArrayAccessorCompiler acc = section().getArrayAccessorForFullData( arrayNode, true );

		// return Runtime.fun_MATCH_xy( val, vals [, env] );
		final boolean needEnv = (valNode.getDataType() == DataType.STRING && type != 0);
		final String envDescriptor = needEnv ? ByteCodeEngineCompiler.ENV_DESC : "";
		valCompiler.compile( valNode );
		mv().loadThis();
		acc.compileCall( mv() );
		if (needEnv) {
			compile_environment();
		}
		valCompiler.compileRuntimeMethod( "fun_MATCH_" + TYPE_SUFFIXES[ type + 1 ],
				"(" + acc.elementDescriptor() + acc.arrayDescriptor() + envDescriptor + ")I" );

		if (_node.getFunction() != Function.INTERNAL_MATCH_INT) {
			numCompiler.compileConversionFromInt();
		}
	}


	protected abstract void compile_util_round( int _maxFractionalDigits ) throws CompilerException;


	@Override
	protected final void compileCount( ExpressionNodeForCount _node ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		mv.push( _node.staticValueCount() );

		final SectionModel[] subs = _node.subSectionModels();
		final int[] cnts = _node.subSectionStaticValueCounts();
		for (int i = 0; i < subs.length; i++) {
			final SubSectionCompiler sub = sectionInContext().subSectionCompiler( subs[ i ] );
			mv.visitVarInsn( Opcodes.ALOAD, method().objectInContext() );
			sectionInContext().compileCallToGetterFor( mv, sub );
			mv.arrayLength();
			if (cnts[ i ] != 1) {
				mv.push( cnts[ i ] );
				mv.visitInsn( Opcodes.IMUL );
			}
			mv.visitInsn( Opcodes.IADD );
		}

		compileConversionFromInt();
	}


	final void compileTest( ExpressionNode _test, Label _notMet ) throws CompilerException
	{
		new TestCompilerBranchingWhenFalse( _test, _notMet ).compileTest();
	}


	private abstract class TestCompiler
	{
		protected final ExpressionNode node;
		protected final Label branchTo;

		TestCompiler( ExpressionNode _node, Label _branchTo )
		{
			super();
			this.node = _node;
			this.branchTo = _branchTo;
		}

		final void compileTest() throws CompilerException
		{
			if (this.node instanceof ExpressionNodeForOperator) {
				final ExpressionNodeForOperator opNode = (ExpressionNodeForOperator) this.node;
				final Operator operator = opNode.getOperator();

				switch (operator) {

					case EQUAL:
					case NOTEQUAL:
					case GREATER:
					case GREATEROREQUAL:
					case LESS:
					case LESSOREQUAL:
						final List<ExpressionNode> args = this.node.arguments();
						compileComparison( operator, args.get( 0 ), args.get( 1 ) );
						return;
				}
			}

			else if (this.node instanceof ExpressionNodeForFunction) {
				final ExpressionNodeForFunction fnNode = (ExpressionNodeForFunction) this.node;
				final Function fn = fnNode.getFunction();

				switch (fn) {

					case AND:
						compileAnd();
						return;

					case OR:
						compileOr();
						return;

					case NOT:
						compileNot();
						return;

				}
			}

			compileValue();
		}


		private final void compileComparison( Operator _operator, ExpressionNode _left, ExpressionNode _right )
				throws CompilerException
		{
			final boolean leftIsString = _left.getDataType() == DataType.STRING;
			final boolean rightIsString = _right.getDataType() == DataType.STRING;

			// String is always greater than number in Excel:
			if (leftIsString && !rightIsString) {
				compileStaticComparisonResult( _operator, +1 );
			}
			else if (!leftIsString && rightIsString) {
				compileStaticComparisonResult( _operator, -1 );
			}
			else {
				final ExpressionCompiler comparisonCompiler = (leftIsString || rightIsString) ? method().stringCompiler()
						: method().numericCompiler();
				comparisonCompiler.compile( _left );
				comparisonCompiler.compile( _right );
				compileComparison( _operator, comparisonCompiler );
			}
		}

		private final void compileStaticComparisonResult( Operator _operator, int _result ) throws CompilerException
		{
			switch (_operator) {
				case EQUAL:
					compileStaticConditionResult( _result == 0 );
					break;
				case NOTEQUAL:
					compileStaticConditionResult( _result != 0 );
					break;
				case GREATER:
					compileStaticConditionResult( _result > 0 );
					break;
				case GREATEROREQUAL:
					compileStaticConditionResult( _result >= 0 );
					break;
				case LESS:
					compileStaticConditionResult( _result < 0 );
					break;
				case LESSOREQUAL:
					compileStaticConditionResult( _result <= 0 );
					break;
				default:
					throw new IllegalArgumentException( "Comparison expected" );
			}
		}

		protected abstract TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo );
		protected abstract void compileAnd() throws CompilerException;
		protected abstract void compileOr() throws CompilerException;
		protected abstract void compileComparison( Operator _comparison, ExpressionCompiler _comparisonCompiler )
				throws CompilerException;
		protected abstract void compileStaticConditionResult( boolean _result ) throws CompilerException;

		protected final void compileComparison( ExpressionCompiler _comparisonCompiler, int _ifOpcode,
				int _comparisonOpcode ) throws CompilerException
		{
			final int effectiveIfOpcode = _comparisonCompiler.compileComparison( _ifOpcode, _comparisonOpcode );
			mv().visitJumpInsn( effectiveIfOpcode, this.branchTo );
		}

		private final void compileNot() throws CompilerException
		{
			final List<ExpressionNode> args = this.node.arguments();
			if (1 == args.size()) {
				newInverseCompiler( args.get( 0 ), this.branchTo ).compileTest();
			}
			else {
				throw new CompilerException.UnsupportedExpression( "NOT must have exactly one argument." );
			}
		}

		final void compileValue() throws CompilerException
		{
			compile( this.node );
			compileZero();
			compileComparison( Operator.NOTEQUAL, method().numericCompiler() );
		}

		protected abstract void compileBooleanTest() throws CompilerException;
	}


	private class TestCompilerBranchingWhenFalse extends TestCompiler
	{

		TestCompilerBranchingWhenFalse( ExpressionNode _node, Label _branchTo )
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison, ExpressionCompiler _comparisonCompiler )
				throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( _comparisonCompiler, Opcodes.IFLE, Opcodes.DCMPL );
					return;

				case GREATEROREQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESS:
					compileComparison( _comparisonCompiler, Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESSOREQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFGT, Opcodes.DCMPG );
					return;

			}
		}

		@Override
		protected void compileStaticConditionResult( boolean _result ) throws CompilerException
		{
			if (!_result) {
				mv().visitJumpInsn( Opcodes.GOTO, this.branchTo );
			}
		}

		@Override
		protected TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo )
		{
			return new TestCompilerBranchingWhenTrue( _node, _branchTo );
		}

		@Override
		protected void compileOr() throws CompilerException
		{
			final Label met = mv().newLabel();
			final int nArg = this.node.arguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.arguments().get( iArg );
				new TestCompilerBranchingWhenTrue( arg, met ).compileTest();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenFalse( lastArg, this.branchTo ).compileTest();
			mv().mark( met );
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			for (ExpressionNode arg : this.node.arguments()) {
				new TestCompilerBranchingWhenFalse( arg, this.branchTo ).compileTest();
			}
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFEQ, this.branchTo );
		}
	}


	private class TestCompilerBranchingWhenTrue extends TestCompiler
	{

		TestCompilerBranchingWhenTrue( ExpressionNode _node, Label _branchTo )
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison, ExpressionCompiler _comparisonCompiler )
				throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( _comparisonCompiler, Opcodes.IFGT, Opcodes.DCMPG );
					return;

				case GREATEROREQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESS:
					compileComparison( _comparisonCompiler, Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESSOREQUAL:
					compileComparison( _comparisonCompiler, Opcodes.IFLE, Opcodes.DCMPL );
					return;

			}
		}

		@Override
		protected void compileStaticConditionResult( boolean _result ) throws CompilerException
		{
			if (_result) {
				mv().visitJumpInsn( Opcodes.GOTO, this.branchTo );
			}
		}

		@Override
		protected TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo )
		{
			return new TestCompilerBranchingWhenFalse( _node, _branchTo );
		}

		@Override
		protected void compileOr() throws CompilerException
		{
			for (ExpressionNode arg : this.node.arguments()) {
				new TestCompilerBranchingWhenTrue( arg, this.branchTo ).compileTest();
			}
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			final Label notMet = mv().newLabel();
			final int nArg = this.node.arguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.arguments().get( iArg );
				new TestCompilerBranchingWhenFalse( arg, notMet ).compileTest();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.arguments().get( iArg );
			new TestCompilerBranchingWhenTrue( lastArg, this.branchTo ).compileTest();
			mv().mark( notMet );
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFNE, this.branchTo );
		}
	}


	@Override
	public String toString()
	{
		return numericType().toString();
	}

}
