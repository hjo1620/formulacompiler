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
package sej.internal.model.rewriting;

import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.model.util.InterpretedNumericType;

final class FunctionRewriterForDCOUNT extends AbstractFunctionRewriterForDatabaseAggregator
{

	public FunctionRewriterForDCOUNT(ExpressionNodeForFunction _fun, InterpretedNumericType _type)
	{
		super( _fun, _type );
	}


	@Override
	protected ExpressionNode foldingStep( String _accumulatorName, String _elementName )
	{
		return op( foldingOperator(), var( _accumulatorName ), cst( 1.0 ) );
	}

	@Override
	protected Operator foldingOperator()
	{
		return Operator.PLUS;
	}


}