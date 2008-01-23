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
package org.formulacompiler.spreadsheet.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public final class MultiCellRange extends CellRange
{
	private final CellIndex from;
	private final CellIndex to;


	MultiCellRange( CellIndex _from, CellIndex _to )
	{
		super();
		if (_from.spreadsheet != _to.spreadsheet)
			throw new IllegalArgumentException( "From and to not from same spreadsheet for range" );
		assert _from.sheetIndex <= _to.sheetIndex;
		assert _from.rowIndex <= _to.rowIndex;
		assert _from.columnIndex <= _to.columnIndex;
		this.from = _from;
		this.to = _to;
	}


	public CellIndex getFrom()
	{
		return this.from;
	}


	public CellIndex getTo()
	{
		return this.to;
	}


	public Iterator<CellIndex> iterator()
	{
		return new CellIndexRangeIterator();
	}


	private class CellIndexRangeIterator implements Iterator<CellIndex>
	{
		private int iSheet, lastSheet;
		private int firstRow, iRow, lastRow;
		private int firstColumn, iColumn, lastColumn;


		CellIndexRangeIterator()
		{
			int firstSheet = getFrom().sheetIndex;
			this.lastSheet = getTo().sheetIndex;
			this.firstRow = getFrom().rowIndex;
			this.lastRow = getTo().rowIndex;
			this.firstColumn = getFrom().columnIndex;
			this.lastColumn = getTo().columnIndex;

			this.iSheet = firstSheet - 1;
			this.iRow = this.lastRow;
			this.iColumn = this.lastColumn;
		}


		public boolean hasNext()
		{
			return (this.iSheet < this.lastSheet) || (this.iRow < this.lastRow) || (this.iColumn < this.lastColumn);
		}


		public CellIndex next()
		{
			this.iColumn++;
			if (this.iColumn > this.lastColumn) {
				this.iColumn = this.firstColumn;
				this.iRow++;
				if (this.iRow > this.lastRow) {
					this.iRow = this.firstRow;
					this.iSheet++;
				}
			}
			if ((this.iColumn <= this.lastColumn) && (this.iRow <= this.lastRow) && (this.iSheet <= this.lastSheet)) {
				return new CellIndex( MultiCellRange.this.from.spreadsheet, this.iSheet, this.iColumn, this.iRow );
			}
			else {
				throw new NoSuchElementException();
			}
		}


		public void remove()
		{
			assert false;
		}
	}


	public CellIndex getCellIndexRelativeTo( CellIndex _cell ) throws SpreadsheetException
	{
		if (this.from.columnIndex == this.to.columnIndex) {
			return new CellIndex( this.from.spreadsheet, _cell.sheetIndex, this.from.columnIndex, _cell.rowIndex );
		}
		else if (this.from.rowIndex == this.to.rowIndex) {
			return new CellIndex( this.from.spreadsheet, _cell.sheetIndex, _cell.columnIndex, this.from.rowIndex );
		}
		throw new SpreadsheetException.CellRangeNotUniDimensional( "Range "
				+ this + " cannot be used to specify a relative cell for " + _cell );
	}


	public final boolean contains( Spreadsheet.Range _other )
	{
		final CellRange range = (CellRange) _other;
		return this.from.sheetIndex <= range.getFrom().sheetIndex
				&& this.from.rowIndex <= range.getFrom().rowIndex
				&& this.from.columnIndex <= range.getFrom().columnIndex
				&& this.to.sheetIndex >= range.getTo().sheetIndex
				&& this.to.rowIndex >= range.getTo().rowIndex
				&& this.to.columnIndex >= range.getTo().columnIndex;
	}


	public Spreadsheet.Cell getTopLeft()
	{
		return this.from;
	}

	public Spreadsheet.Cell getBottomRight()
	{
		return this.to;
	}

	public Iterable<Spreadsheet.Cell> cells()
	{
		final Iterator<CellIndex> baseIterator = iterator();
		return new Iterable<Spreadsheet.Cell>()
		{

			public Iterator<Spreadsheet.Cell> iterator()
			{
				return new Iterator<Spreadsheet.Cell>()
				{

					public boolean hasNext()
					{
						return baseIterator.hasNext();
					}

					public Spreadsheet.Cell next()
					{
						return baseIterator.next();
					}

					public void remove()
					{
						baseIterator.remove();
					}

				};
			}
		};
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		this.from.describeTo( _to );
		_to.append( ':' );
		this.to.describeTo( _to );
	}


}
