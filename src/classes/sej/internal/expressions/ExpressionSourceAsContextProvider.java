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
package sej.internal.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;

public class ExpressionSourceAsContextProvider implements ExpressionContextProvider
{
	private final ExpressionNode expr;

	public ExpressionSourceAsContextProvider(ExpressionNode _expr)
	{
		super();
		this.expr = _expr.getOrigin();
	}

	public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
	{
		_result.append( "\nIn expression " );
		
		try {
			final ExpressionNode focus = (_focusedNode == null)? null : _focusedNode.getOrigin();
			this.expr.describeTo( _result, new ExpressionDescriptionConfig( focus, " >> ", " << ") );
		}
		catch (IOException e) {
			_result.append( " >> ERROR describing expression:" ).append( e.getMessage() );
		}
		
		_result.append( "; error location indicated by >>..<<." );
	}

}