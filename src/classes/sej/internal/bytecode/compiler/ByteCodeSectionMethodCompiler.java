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
package sej.internal.bytecode.compiler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.Aggregator;
import sej.CallFrame;
import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForPartialAggregation;
import sej.internal.model.ExpressionNodeForSubSectionModel;


abstract class ByteCodeSectionMethodCompiler
{
	private static final ExpressionNode TRUENODE = new ExpressionNodeForConstantValue( Boolean.TRUE );
	private static final ExpressionNode FALSENODE = new ExpressionNodeForConstantValue( Boolean.FALSE );

	private final ByteCodeSectionCompiler section;
	private final String methodName;
	private final GeneratorAdapter mv;


	ByteCodeSectionMethodCompiler(ByteCodeSectionCompiler _section, String _methodName)
	{
		this( _section, _methodName, Opcodes.ACC_PRIVATE );
	}


	ByteCodeSectionMethodCompiler(ByteCodeSectionCompiler _section, String _methodName, int _access)
	{
		super();
		this.section = _section;
		this.methodName = _methodName;
		this.mv = newAdapter( _access );
	}


	private GeneratorAdapter newAdapter( int _access )
	{
		final String name = getMethodName();
		final String signature = "()" + getNumericType().getDescriptor();
		final int access = Opcodes.ACC_FINAL | _access;
		return new GeneratorAdapter( cw().visitMethod( access, name, signature, null, null ), access, name, signature );
	}


	ByteCodeSectionCompiler getSection()
	{
		return this.section;
	}


	ByteCodeNumericType getNumericType()
	{
		return getSection().getNumericType();
	}


	protected Type getRuntimeType()
	{
		return getNumericType().getRuntimeType();
	}


	String getMethodName()
	{
		return this.methodName;
	}


	protected ClassWriter cw()
	{
		return getSection().cw();
	}


	protected GeneratorAdapter mv()
	{
		return this.mv;
	}


	void compile() throws CompilerException
	{
		beginCompilation();
		compileBody();
		endCompilation();
	}


	protected void beginCompilation()
	{
		mv().visitCode();
	}


	protected void endCompilation()
	{
		mv().visitInsn( getNumericType().getReturnOpcode() );
		mv().endMethod();
		mv().visitEnd();
	}


	protected abstract void compileBody() throws CompilerException;


	protected void compileRef( MethodVisitor _mv, ByteCodeCellComputation _cell )
	{
		compileRef( _mv, _cell.getMethodName() );
	}

	protected void compileRef( MethodVisitor _mv, String _getterName )
	{
		_mv.visitVarInsn( Opcodes.ALOAD, 0 );
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, getSection().engine.getInternalName(), _getterName, "()"
				+ getNumericType().getDescriptor() );
	}


	protected void pushConstParam( Object _constantValue )
	{
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}
		else if (_constantValue instanceof Double) {
			double val = (Double) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Integer) {
			int val = (Integer) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Long) {
			long val = (Long) _constantValue;
			mv().push( val );
		}
		else if (_constantValue instanceof Boolean) {
			boolean val = (Boolean) _constantValue;
			mv().push( val );
		}
		else {
			mv().visitLdcInsn( _constantValue );
		}
	}


	protected void compileConst( Object _constantValue ) throws CompilerException
	{
		getNumericType().compileConst( mv(), _constantValue );
	}


	protected void compileInput( CallFrame _callChainToCall ) throws CompilerException
	{
		final CallFrame[] frames = _callChainToCall.getFrames();
		final boolean isStatic = Modifier.isStatic( frames[ 0 ].getMethod().getModifiers() );

		if (!isStatic) {
			mv().loadThis();
			mv().getField( getSection().engine, ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, getSection().inputs );
		}

		Class contextClass = getSection().getInputClass();
		for (CallFrame frame : frames) {
			final Method method = frame.getMethod();
			if (null != frame.getArgs()) {
				for (Object arg : frame.getArgs()) {
					pushConstParam( arg );
				}
			}
			int opcode = Opcodes.INVOKEVIRTUAL;
			if (contextClass.isInterface()) opcode = Opcodes.INVOKEINTERFACE;
			else if (isStatic) opcode = Opcodes.INVOKESTATIC;

			mv().visitMethodInsn( opcode, Type.getType( contextClass ).getInternalName(), method.getName(),
					Type.getMethodDescriptor( method ) );

			contextClass = method.getReturnType();
		}

		getNumericType().compileToNum( mv(), _callChainToCall.getMethod() );
	}


	protected void compileExpr( ExpressionNode _node ) throws CompilerException
	{
		if (null == _node) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}

		else if (_node instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue node = (ExpressionNodeForConstantValue) _node;
			compileConst( node.getValue() );
		}

		else if (_node instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel node = (ExpressionNodeForCellModel) _node;
			CellModel cell = (node).getCellModel();
			compileRef( cell );
		}

		else if (_node instanceof ExpressionNodeForParentSectionModel) {
			final ExpressionNodeForParentSectionModel node = (ExpressionNodeForParentSectionModel) _node;
			compileRef( node );
		}

		else if (_node instanceof ExpressionNodeForSubSectionModel) {
			final ExpressionNodeForSubSectionModel node = (ExpressionNodeForSubSectionModel) _node;
			compileRef( node );
		}

		else if (_node instanceof ExpressionNodeForOperator) {
			final ExpressionNodeForOperator node = (ExpressionNodeForOperator) _node;
			if (needsIf( node.getOperator() )) {
				compileIf( node, TRUENODE, FALSENODE );
			}
			else {
				compileOperator( node );
			}
		}

		else if (_node instanceof ExpressionNodeForFunction) {
			final ExpressionNodeForFunction node = (ExpressionNodeForFunction) _node;
			switch (node.getFunction()) {

				case IF:
					compileIf( node );
					break;

				case NOT:
					compileIf( node, TRUENODE, FALSENODE );
					break;

				default:
					compileFunction( node );
			}
		}

		else if (_node instanceof ExpressionNodeForAggregator) {
			final ExpressionNodeForAggregator node = (ExpressionNodeForAggregator) _node;
			switch (node.getAggregator()) {

				case AND:
				case OR:
					compileIf( node, TRUENODE, FALSENODE );
					break;

				default:
					compileAggregation( node );
			}
		}

		else {
			unsupported( _node );
		}

	}


	private boolean needsIf( Operator _operator )
	{
		switch (_operator) {
			case AND:
			case OR:
			case EQUAL:
			case NOTEQUAL:
			case LESS:
			case LESSOREQUAL:
			case GREATER:
			case GREATEROREQUAL:
				return true;
			default:
				return false;
		}
	}


	private void compileOperator( ExpressionNodeForOperator _node ) throws CompilerException
	{
		for (ExpressionNode arg : _node.getArguments()) {
			compileExpr( arg );
		}
		compileOperator( _node.getOperator(), _node.getArguments().size() );
	}


	private void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		getNumericType().compile( mv(), _operator, _numberOfArguments );
	}


	private void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		switch (_node.getFunction()) {

			case ROUND:
				compileStdFunction( _node );
				break;

			// TODO case INDEX:
			// compileHelpedExpr( new ByteCodeHelperCompilerForIndex( getSection(), _node ) );
			// break;

			default:
				unsupported( _node );
		}
	}


	private void compileStdFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final StringBuilder argTypeBuilder = new StringBuilder();
		for (ExpressionNode arg : _node.getArguments()) {
			compileExpr( arg );
			argTypeBuilder.append( getNumericType().getDescriptor() );
		}
		getNumericType().compileStdFunction( mv(), _node.getFunction(), argTypeBuilder.toString() );
	}


	private void compileIf( ExpressionNodeForFunction _node ) throws CompilerException
	{
		compileIf( _node.getArguments().get( 0 ), _node.getArguments().get( 1 ), _node.getArguments().get( 2 ) );
	}


	private void compileIf( ExpressionNode _test, ExpressionNode _ifTrue, ExpressionNode _ifFalse )
			throws CompilerException
	{
		final Label notMet = mv().newLabel();
		final Label done = mv().newLabel();

		new TestCompilerBranchingWhenFalse( _test, notMet ).compile();

		compileExpr( _ifTrue );

		mv().visitJumpInsn( Opcodes.GOTO, done );
		mv().mark( notMet );

		compileExpr( _ifFalse );

		mv().mark( done );
	}


	private abstract class TestCompiler
	{
		protected ExpressionNode node;
		protected Label branchTo;

		TestCompiler(ExpressionNode _node, Label _branchTo)
		{
			super();
			this.node = _node;
			this.branchTo = _branchTo;
		}

		void compile() throws CompilerException
		{
			if (this.node instanceof ExpressionNodeForOperator) {
				final ExpressionNodeForOperator opNode = (ExpressionNodeForOperator) this.node;
				final Operator operator = opNode.getOperator();

				switch (operator) {

					case AND:
						compileAnd();
						return;

					case OR:
						compileOr();
						return;

					case EQUAL:
					case NOTEQUAL:
					case GREATER:
					case GREATEROREQUAL:
					case LESS:
					case LESSOREQUAL:
						compileExpr( this.node.getArguments().get( 0 ) );
						compileExpr( this.node.getArguments().get( 1 ) );
						compileComparison( operator );
						return;

				}
			}

			else if (this.node instanceof ExpressionNodeForAggregator) {
				final ExpressionNodeForAggregator aggNode = (ExpressionNodeForAggregator) this.node;
				final Aggregator aggregator = aggNode.getAggregator();

				switch (aggregator) {
					case AND:
						compileAnd();
						return;
					case OR:
						compileOr();
						return;
				}
			}

			if (this.node instanceof ExpressionNodeForFunction) {
				final ExpressionNodeForFunction fnNode = (ExpressionNodeForFunction) this.node;
				final Function fn = fnNode.getFunction();

				switch (fn) {

					case NOT:
						compileNot();
						return;

				}
			}

			compileValue();
		}

		protected abstract TestCompiler newInverseCompiler( ExpressionNode _node, Label _branchTo );
		protected abstract void compileAnd() throws CompilerException;
		protected abstract void compileOr() throws CompilerException;
		protected abstract void compileComparison( Operator _comparison ) throws CompilerException;

		protected void compileComparison( int _ifOpcode, int _comparisonOpcode )
		{
			getNumericType().compileComparison( mv(), _comparisonOpcode );
			mv().visitJumpInsn( _ifOpcode, this.branchTo );
		}

		private void compileNot() throws CompilerException
		{
			final List<ExpressionNode> args = this.node.getArguments();
			if (0 < args.size()) {
				newInverseCompiler( args.get( 0 ), this.branchTo ).compile();
			}
			else {
				unsupported( this.node );
			}
		}

		void compileValue() throws CompilerException
		{
			compileExpr( this.node );
			getNumericType().compileZero( mv() );
			compileComparison( Operator.NOTEQUAL );
		}

		protected abstract void compileBooleanTest() throws CompilerException;
	}


	private class TestCompilerBranchingWhenFalse extends TestCompiler
	{

		TestCompilerBranchingWhenFalse(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( Opcodes.IFLE, Opcodes.DCMPL );
					return;

				case GREATEROREQUAL:
					compileComparison( Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESS:
					compileComparison( Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESSOREQUAL:
					compileComparison( Opcodes.IFGT, Opcodes.DCMPG );
					return;

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
			final int nArg = this.node.getArguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.getArguments().get( iArg );
				new TestCompilerBranchingWhenTrue( arg, met ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.getArguments().get( iArg );
			new TestCompilerBranchingWhenFalse( lastArg, this.branchTo ).compile();
			mv().mark( met );
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			for (ExpressionNode arg : this.node.getArguments()) {
				new TestCompilerBranchingWhenFalse( arg, this.branchTo ).compile();
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

		TestCompilerBranchingWhenTrue(ExpressionNode _node, Label _branchTo)
		{
			super( _node, _branchTo );
		}

		@Override
		protected void compileComparison( Operator _comparison ) throws CompilerException
		{
			switch (_comparison) {

				case EQUAL:
					compileComparison( Opcodes.IFEQ, Opcodes.DCMPL );
					return;

				case NOTEQUAL:
					compileComparison( Opcodes.IFNE, Opcodes.DCMPL );
					return;

				case GREATER:
					compileComparison( Opcodes.IFGT, Opcodes.DCMPG );
					return;

				case GREATEROREQUAL:
					compileComparison( Opcodes.IFGE, Opcodes.DCMPG );
					return;

				case LESS:
					compileComparison( Opcodes.IFLT, Opcodes.DCMPL );
					return;

				case LESSOREQUAL:
					compileComparison( Opcodes.IFLE, Opcodes.DCMPL );
					return;

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
			for (ExpressionNode arg : this.node.getArguments()) {
				new TestCompilerBranchingWhenTrue( arg, this.branchTo ).compile();
			}
		}

		@Override
		protected void compileAnd() throws CompilerException
		{
			final Label notMet = mv().newLabel();
			final int nArg = this.node.getArguments().size();
			int iArg = 0;
			while (iArg < nArg - 1) {
				final ExpressionNode arg = this.node.getArguments().get( iArg );
				new TestCompilerBranchingWhenFalse( arg, notMet ).compile();
				iArg++;
			}
			final ExpressionNode lastArg = this.node.getArguments().get( iArg );
			new TestCompilerBranchingWhenTrue( lastArg, this.branchTo ).compile();
			mv().mark( notMet );
		}

		@Override
		protected void compileBooleanTest() throws CompilerException
		{
			mv().visitJumpInsn( Opcodes.IFNE, this.branchTo );
		}
	}


	private void compileAggregation( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final Aggregator aggregator = _node.getAggregator();

		switch (aggregator) {

			case AVERAGE:
				compileHelpedExpr( new ByteCodeHelperCompilerForAverage( getSection(), _node ) );
				break;

			default:
				compileMapReduceAggregator( _node );

		}
	}


	private void compileMapReduceAggregator( ExpressionNodeForAggregator _node ) throws CompilerException
	{
		final Aggregator aggregator = _node.getAggregator();
		final Operator reductor = aggregator.getReductor();
		compileMapReduceAggregator( _node, reductor );
	}


	protected void compileMapReduceAggregator( ExpressionNodeForAggregator _node, final Operator _reductor )
			throws CompilerException
	{
		if (null == _reductor) unsupported( _node );
		boolean first = true;
		for (ExpressionNode arg : _node.getArguments()) {
			if (arg instanceof ExpressionNodeForSubSectionModel) {
				ExpressionNodeForSubSectionModel subArg = (ExpressionNodeForSubSectionModel) arg;
				compileIteration( _reductor, subArg );
			}
			else {
				compileExpr( arg );
			}
			if (first) first = false;
			else compileOperator( _reductor, 2 );
		}

		if (_node instanceof ExpressionNodeForPartialAggregation) {
			ExpressionNodeForPartialAggregation partialAggNode = (ExpressionNodeForPartialAggregation) _node;
			compileConst( partialAggNode.getPartialAggregation().accumulator );
			compileOperator( _reductor, 2 );
		}
	}


	private void compileIteration( Operator _reductor, ExpressionNodeForSubSectionModel _node ) throws CompilerException
	{
		// TODO compileIteration
		unsupported( _node );
	}


	private void compileRef( CellModel _cell )
	{
		compileRef( mv(), getSection().getCellComputation( _cell ) );
	}


	private void compileRef( ExpressionNodeForParentSectionModel _node ) throws CompilerException
	{
		// TODO compileRef
		unsupported( _node );
	}


	private void compileRef( ExpressionNodeForSubSectionModel _node ) throws CompilerException
	{
		// TODO compileRef
		unsupported( _node );
	}


	private void compileHelpedExpr( ByteCodeHelperCompiler _compiler ) throws CompilerException
	{
		_compiler.compile();
		compileRef( mv(), _compiler.getMethodName() );
	}


	protected void compileLog( String _message )
	{
		mv().dup2();
		mv().push( _message );
		mv().visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "logDouble",
				"(DLjava/lang/String;)V" );
	}


	protected void unsupported( ExpressionNode _node ) throws CompilerException
	{
		throw new CompilerException.UnsupportedExpression( "The expression " + _node.describe() + " is not supported." );
	}


	protected boolean isNull( ExpressionNode _node )
	{
		if (null == _node) return true;
		if (_node instanceof ExpressionNodeForConstantValue) {
			ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			return (null == constNode.getValue());
		}
		return false;
	}

}