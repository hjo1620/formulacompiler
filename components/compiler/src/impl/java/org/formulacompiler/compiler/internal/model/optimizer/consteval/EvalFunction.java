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
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.EvalNotPossibleException;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;


public class EvalFunction extends EvalShadow
{

	EvalFunction(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected Object eval() throws CompilerException
	{
		final Function function = ((ExpressionNodeForFunction) node()).getFunction();
		switch (function) {

			case AND:
				return evalBooleanSequence( false );

			case OR:
				return evalBooleanSequence( true );

			case COUNT: {
				final Collection<ExpressionNode> uncountables = New.newCollection();
				final int staticValueCount = node().countArgumentValues( context().letDict, uncountables );
				final int subCount = uncountables.size();
				if (subCount == 0) {
					return staticValueCount;
				}
				else {
					final SectionModel[] subs = new SectionModel[ subCount ]; 
					final int[] subCounts = new int[ subCount ];
					int i = 0;
					for (ExpressionNode uncountable : uncountables) {
						final ExpressionNodeForSubSectionModel sub = (ExpressionNodeForSubSectionModel) uncountable;
						subs[i] = sub.getSectionModel();
						final Collection<ExpressionNode> subUncountables = New.newCollection();
						subCounts[i] = sub.countArgumentValues( context().letDict, subUncountables );
						if (subUncountables.size() > 0) {
							throw new CompilerException.UnsupportedExpression( "COUNT of nested sections not supported" );
						}
					}
					return new ExpressionNodeForCount( staticValueCount, subs, subCounts );
				}
			}

			default:
				return super.eval();

		}
	}


	private final Object evalBooleanSequence( boolean _returnThisIfFound ) throws CompilerException
	{
		final InterpretedNumericType type = type();
		final Collection<ExpressionNode> dynArgs = New.newCollection();
		final int n = cardinality();
		for (int i = 0; i < n; i++) {
			final Object arg = evaluateArgument( i );
			if (isConstant( arg )) {
				final boolean value = type.toBoolean( arg );
				if (value == _returnThisIfFound) {
					return _returnThisIfFound;
				}
			}
			else {
				dynArgs.add( (ExpressionNode) arg );
			}
		}
		if (dynArgs.size() > 0) {
			final ExpressionNode result = node().cloneWithoutArguments();
			result.arguments().addAll( dynArgs );
			return result;
		}
		else {
			return !_returnThisIfFound;
		}
	}


	@Override
	protected Object evaluateToConst( Object... _args )
	{
		final Function function = ((ExpressionNodeForFunction) node()).getFunction();
		if (function.isVolatile()) {
			return evaluateToNode( _args );
		}
		else {
			switch (function) {

				case COUNT:
					throw new IllegalStateException( "COUNT not expected in evaluateToConst" );

				default:
					try {
						return type().compute( function, _args );
					}
					catch (EvalNotPossibleException e) {
						return evaluateToNode( _args );
					}

			}
		}
	}


}
