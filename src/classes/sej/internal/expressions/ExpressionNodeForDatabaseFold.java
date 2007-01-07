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
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForDatabaseFold extends ExpressionNodeForAbstractFold
{
	private final String filterColumnNamePrefix;
	private final int staticFoldedColumnIndex;
	private final int[] foldableColumnKeys;
	private final String[] filterColumnNames;
	private final DataType[] filterColumnTypes;
	private final boolean isReduce;
	private final boolean isZeroForEmptySelection;

	public ExpressionNodeForDatabaseFold(ArrayDescriptor _tableDescriptor, String _filterColNamePrefix,
			String _accumulatorName, String _elementName, int _staticFoldedColumnIndex, int[] _foldableColumnKeys,
			DataType[] _filterColumnTypes, boolean _isReduce, boolean _isZeroForEmptySelection)
	{
		super( _accumulatorName, _elementName, false );
		this.filterColumnNamePrefix = _filterColNamePrefix;
		this.staticFoldedColumnIndex = _staticFoldedColumnIndex;
		this.foldableColumnKeys = _foldableColumnKeys;
		this.isReduce = _isReduce;
		this.isZeroForEmptySelection = _isZeroForEmptySelection;

		final int nCol = _tableDescriptor.getNumberOfColumns();
		this.filterColumnNames = new String[ nCol ];
		for (int iCol = 0; iCol < nCol; iCol++) {
			this.filterColumnNames[ iCol ] = filterColumnNamePrefix() + iCol;
		}
		this.filterColumnTypes = _filterColumnTypes;
	}

	public ExpressionNodeForDatabaseFold(ArrayDescriptor _tableDescriptor, String _filterColNamePrefix,
			ExpressionNode _filter, String _accumulatorName, ExpressionNode _initialValue, String _elementName,
			ExpressionNode _foldingStep, int _staticFoldedColumnIndex, int[] _foldableColumnKeys,
			ExpressionNode _foldedColumnIndex, DataType[] _filterColumnTypes, boolean _isReduce,
			boolean _isZeroForEmptySelection, ExpressionNode _arrayRef)
	{
		this( _tableDescriptor, _filterColNamePrefix, _accumulatorName, _elementName, _staticFoldedColumnIndex,
				_foldableColumnKeys, _filterColumnTypes, _isReduce, _isZeroForEmptySelection );
		addArgument( _initialValue );
		addArgument( _foldingStep );
		addArgument( _filter );
		addArgument( _foldedColumnIndex );
		addArgument( _arrayRef );
	}


	public final String filterColumnNamePrefix()
	{
		return this.filterColumnNamePrefix;
	}

	public final ExpressionNode filter()
	{
		return argument( 2 );
	}

	public final int staticFoldedColumnIndex()
	{
		return this.staticFoldedColumnIndex;
	}

	public final int[] foldableColumnKeys()
	{
		return this.foldableColumnKeys;
	}

	public final ExpressionNode foldedColumnIndex()
	{
		return argument( 3 );
	}

	public final ExpressionNodeForArrayReference table()
	{
		return (ExpressionNodeForArrayReference) argument( 4 );
	}

	public final String[] filterColumnNames()
	{
		return this.filterColumnNames;
	}

	public DataType[] filterColumnTypes()
	{
		return this.filterColumnTypes;
	}

	public final boolean isReduce()
	{
		return this.isReduce;
	}

	public final boolean isZeroForEmptySelection()
	{
		return this.isZeroForEmptySelection;
	}


	@Override
	protected void skipToElements( Iterator<ExpressionNode> _iterator )
	{
		super.skipToElements( _iterator );
		_iterator.next(); // filter
		_iterator.next(); // column index
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (isReduce()) _to.append( "_DREDUCE( " );
		else if (isZeroForEmptySelection()) _to.append( "_DFOLD0( " );
		else _to.append( "_DFOLD( " );
		_to.append( filterColumnNamePrefix() ).append( ": " );
		filter().describeTo( _to, _cfg );
		_to.append( "; " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		if (staticFoldedColumnIndex() >= 0) {
			_to.append( '#' ).append( staticFoldedColumnIndex() );
		}
		else {
			foldedColumnIndex().describeTo( _to, _cfg );
		}
		_to.append( "; " );
		describeElements( _to, _cfg );
	}


	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForDatabaseFold( table().arrayDescriptor(), filterColumnNamePrefix(), accumulatorName(),
				elementName(), staticFoldedColumnIndex(), foldableColumnKeys(), filterColumnTypes(), isReduce(),
				isZeroForEmptySelection() );
	}


}