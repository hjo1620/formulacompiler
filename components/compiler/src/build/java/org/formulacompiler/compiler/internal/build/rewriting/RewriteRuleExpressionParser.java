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
package org.formulacompiler.compiler.internal.build.rewriting;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.build.rewriting.AbstractDef.Param;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.ParseException;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.runtime.New;

final class RewriteRuleExpressionParser extends ExpressionParser
{
	private final Collection<RuleDef> rules;
	private final Map<String, FoldDef> folds;

	public RewriteRuleExpressionParser( String _exprText, Collection<RuleDef> _rules, Map<String, FoldDef> _folds )
	{
		super( _exprText );
		this.rules = _rules;
		this.folds = _folds;
	}


	public void parseFile() throws CompilerException
	{
		try {
			parseRules();
		}
		catch (InnerParserException e) {
			throw adorn( e.getCause() );
		}
		catch (ParseException e) {
			throw adorn( e );
		}
	}


	private AbstractDef currentDef = null;

	@Override
	protected void makeNewRuleDef( Token _name )
	{
		final String name = _name.image.toUpperCase();
		this.currentDef = new RuleDef( Function.valueOf( name ) );
	}

	@Override
	protected void finalizeLastRuleDef()
	{
		checkInRewrite();
		this.rules.add( (RuleDef) this.currentDef );
		this.currentDef = null;
	}


	@Override
	protected void makeNewFoldDef( Token _name )
	{
		this.currentDef = new FoldDef( _name.image );
	}

	@Override
	protected void finalizeLastFoldDef()
	{
		checkInRewrite();
		final FoldDef foldDef = (FoldDef) this.currentDef;
		this.folds.put( foldDef.name, foldDef );
		this.currentDef = null;
	}


	@Override
	protected void checkInRewrite()
	{
		if (null == this.currentDef) super.checkInRewrite();
	}

	@Override
	protected void makeNewParam( Token _name, char _suffix )
	{
		checkInRewrite();
		final Param.Type type;
		switch (_suffix) {
			case 0:
				type = Param.Type.VALUE;
				break;
			case '#':
				type = Param.Type.ARRAY;
				break;
			case '*':
				type = Param.Type.LIST;
				break;
			case '+':
				type = Param.Type.SYMBOLIC;
				break;
			default:
				throw new IllegalArgumentException( "Unexpected param suffix " + _suffix );
		}
		this.currentDef.addParam( _name.image, type );
	}

	@Override
	protected void makeBody()
	{
		checkInRewrite();
		this.currentDef.setBody( popNode() );
	}


	private final LetDictionary letDict = new LetDictionary();

	@Override
	protected void let( Token... _names )
	{
		checkInRewrite();
		for (int i = 0; i < _names.length; i++)
			this.letDict.let( _names[ i ].image, null, null );
	}

	@Override
	protected void unlet( Token... _names )
	{
		checkInRewrite();
		for (int i = _names.length - 1; i >= 0; i--)
			this.letDict.unlet( _names[ i ].image );
	}

	@Override
	protected void letParams()
	{
		for (Param p : this.currentDef.params)
			this.letDict.let( p.name, null, null );
	}

	@Override
	protected void unletParams()
	{
		this.letDict.unlet( this.currentDef.params.size() );
	}

	@Override
	protected ExpressionNode makeLetVar( Token _name )
	{
		checkInRewrite();
		final String name = _name.image;
		if (null == this.letDict.find( name )) {
			throw new IllegalArgumentException( "Let var " + name + " is not defined here." );
		}
		return new ExpressionNodeForLetVar( name );
	}

	@Override
	protected ExpressionNode makeNamedCellRef( Token _name )
	{
		return makeLetVar( _name );
	}

	@Override
	protected ExpressionNode makeShapedRange( ExpressionNode _range )
	{
		if (_range instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar var = (ExpressionNodeForLetVar) _range;
			return var;
		}
		return super.makeShapedRange( _range );
	}


	private static final class Accu
	{
		String name;
		ExpressionNode init;
		ExpressionNode step;

		public Accu( String _name, ExpressionNode _init )
		{
			super();
			this.name = _name;
			this.init = _init;
		}
	}

	private static final class FoldContext
	{
		Collection<Accu> foldAccus = New.collection();
		Map<String, Accu> foldAccuMap = New.map();
		Collection<String> foldEltNames = New.collection();
		String foldIdxName;
		String foldCountName;
		ExpressionNode foldInto;
		ExpressionNode foldWhenEmpty;
	}

	private final Stack<FoldContext> outerFoldContexts = New.stack();
	private FoldContext foldContext = null;

	@Override
	protected void initFold()
	{
		this.outerFoldContexts.push( this.foldContext );
		this.foldContext = new FoldContext();
	}

	@Override
	protected void addFoldAccuInit( Token _name, ExpressionNode _init )
	{
		final FoldContext cx = this.foldContext;
		final Accu accu = new Accu( _name.image, _init );
		cx.foldAccus.add( accu );
		cx.foldAccuMap.put( accu.name, accu );
	}

	@Override
	protected void addFoldEltName( Token _name )
	{
		final FoldContext cx = this.foldContext;
		cx.foldEltNames.add( _name.image );
	}

	@Override
	protected void setFoldIdxName( Token _name )
	{
		final FoldContext cx = this.foldContext;
		cx.foldIdxName = _name.image;
	}

	@Override
	protected void letFoldAccus()
	{
		final FoldContext cx = this.foldContext;
		for (Accu a : cx.foldAccus)
			this.letDict.let( a.name, null, null );
	}

	@Override
	protected void unletFoldAccus()
	{
		final FoldContext cx = this.foldContext;
		this.letDict.unlet( cx.foldAccus.size() );
	}

	@Override
	protected void letFoldElts()
	{
		final FoldContext cx = this.foldContext;
		for (String n : cx.foldEltNames)
			this.letDict.let( n, null, null );
		if (null != cx.foldIdxName) this.letDict.let( cx.foldIdxName, null, null );
	}

	@Override
	protected void unletFoldElts()
	{
		final FoldContext cx = this.foldContext;
		if (null != cx.foldIdxName) this.letDict.unlet( cx.foldIdxName );
		this.letDict.unlet( cx.foldEltNames.size() );
	}

	@Override
	protected void letFoldCount()
	{
		final FoldContext cx = this.foldContext;
		if (null != cx.foldCountName) this.letDict.let( cx.foldCountName, null, null );
	}

	@Override
	protected void unletFoldCount()
	{
		final FoldContext cx = this.foldContext;
		if (null != cx.foldCountName) this.letDict.unlet( cx.foldCountName );
	}

	@Override
	protected void addFoldStep( Token _name, ExpressionNode _step )
	{
		final FoldContext cx = this.foldContext;
		cx.foldAccuMap.get( _name.image ).step = _step;
	}

	@Override
	protected void setFoldCountName( Token _name )
	{
		final FoldContext cx = this.foldContext;
		cx.foldCountName = _name.image;
	}

	@Override
	protected void setFoldInto( ExpressionNode _node )
	{
		final FoldContext cx = this.foldContext;
		cx.foldInto = _node;
	}

	@Override
	protected void setFoldWhenEmpty( ExpressionNode _node )
	{
		final FoldContext cx = this.foldContext;
		cx.foldWhenEmpty = _node;
	}

	@Override
	protected void pushFold( boolean _mayRearrange, boolean _mayReduce )
	{
		final FoldContext cx = this.foldContext;
		final int nAccu = cx.foldAccus.size();
		final String[] accuNames = new String[ nAccu ];
		final ExpressionNode[] accuInits = new ExpressionNode[ nAccu ];
		final ExpressionNode[] accuSteps = new ExpressionNode[ nAccu ];
		int iAccu = 0;
		for (Accu a : cx.foldAccus) {
			accuNames[ iAccu ] = a.name;
			accuInits[ iAccu ] = a.init;
			accuSteps[ iAccu ] = a.step;
			iAccu++;
		}

		final int nElt = cx.foldEltNames.size();
		final String[] eltNames = new String[ nElt ];
		int iElt = 0;
		for (String n : cx.foldEltNames) {
			eltNames[ iElt++ ] = n;
		}

		pushNode( new ExpressionNodeForFoldDefinition( accuNames, accuInits, cx.foldIdxName, eltNames, accuSteps,
				cx.foldCountName, cx.foldInto, cx.foldWhenEmpty, _mayRearrange, _mayReduce ) );

		this.foldContext = this.outerFoldContexts.empty() ? null : this.outerFoldContexts.pop();
	}

	@Override
	protected void pushApplyList( Token _def, Token _elts )
	{
		final ExpressionNode def = (_def == null) ? popNode() : var( _def.image );
		pushNode( new ExpressionNodeForFoldList( def, var( _elts.image ) ) );
	}

	@Override
	protected void pushApplyVectors( Token _def, Collection<Token> _vecs )
	{
		final ExpressionNode def = (_def == null) ? popNode() : var( _def.image );
		final Collection<ExpressionNode> vecs = New.collection( _vecs.size() );
		for (Token vec : _vecs)
			vecs.add( var( vec.image ) );
		pushNode( new ExpressionNodeForFoldVectors( def, vecs ) );
	}

}