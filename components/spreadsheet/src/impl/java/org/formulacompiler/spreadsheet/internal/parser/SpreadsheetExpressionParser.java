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
package org.formulacompiler.spreadsheet.internal.parser;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.Token;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellRefParser;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeIntersection;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeShape;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeUnion;
import org.formulacompiler.spreadsheet.internal.MultiCellRange;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public abstract class SpreadsheetExpressionParser extends ExpressionParser
{
	protected final SpreadsheetImpl workbook;
	protected final SheetImpl sheet;
	protected final CellInstance cell;
	protected final CellIndex cellIndex;

	public SpreadsheetExpressionParser( String _exprText, CellInstance _parseRelativeTo )
	{
		super( _exprText );
		this.cell = _parseRelativeTo;
		this.sheet = (this.cell == null) ? null : this.cell.getRow().getSheet();
		this.workbook = (this.sheet == null) ? null : this.sheet.getSpreadsheet();
		this.cellIndex = (this.cell == null) ? null : this.cell.getCellIndex();
	}

	protected SpreadsheetExpressionParser( String _exprText, SpreadsheetImpl _workbook )
	{
		super( _exprText );
		this.workbook = _workbook;
		this.sheet = null;
		this.cell = null;
		this.cellIndex = null;
	}

	public static SpreadsheetExpressionParser newParser( String _exprText, CellInstance _parseRelativeTo,
			CellRefFormat _format )
	{
		final SpreadsheetExpressionParser parser;
		switch (_format) {
			case A1:
				parser = new SpreadsheetExpressionParserA1( _exprText, _parseRelativeTo );
				break;
			case A1_ODF:
				parser = new SpreadsheetExpressionParserA1ODF( _exprText, _parseRelativeTo );
				break;
			case R1C1:
				parser = new SpreadsheetExpressionParserR1C1( _exprText, _parseRelativeTo );
				break;
			default:
				throw new IllegalArgumentException( _format + " format is not supported" );
		}
		return parser;
	}


	@Override
	protected final ExpressionNode makeCellA1( Token _cell )
	{
		return makeCellA1( _cell.image, this.sheet );
	}

	@Override
	protected final ExpressionNode makeCellA1( Token _cell, Token _sheet )
	{
		return makeCellA1( _cell.image, getSheetByName( _sheet.image ) );
	}

	protected final ExpressionNode makeCellA1( String _ref, SheetImpl _sheet )
	{
		final CellRefParser parser = CellRefParser.getInstance( CellRefFormat.A1 );
		return new ExpressionNodeForCell( parser.getCellIndexForCanonicalName( _ref, _sheet, this.cellIndex ) );
	}

	@Override
	protected final ExpressionNode makeCellA1ODF( Token _cell, ExpressionNode _node )
	{
		final SheetImpl sheet;
		if (_node != null) {
			final ExpressionNodeForCell nodeForCell = (ExpressionNodeForCell) _node;
			final CellIndex cellIndex = nodeForCell.getCellIndex();
			sheet = cellIndex.getSheet();
		}
		else {
			assert this.sheet != null;
			sheet = this.sheet;
		}
		return makeCellA1ODF( _cell.image, sheet );
	}

	@Override
	protected final ExpressionNode makeCellA1ODF( Token _cell, Token _sheet )
	{
		final SheetImpl sheet = getSheetByName( _sheet.image );
		return makeCellA1ODF( _cell.image, sheet );
	}

	private ExpressionNode makeCellA1ODF( String _ref, SheetImpl _sheet )
	{
		final CellRefParser parser = CellRefParser.getInstance( CellRefFormat.A1_ODF );
		return new ExpressionNodeForCell( parser.getCellIndexForCanonicalName( _ref, _sheet, this.cellIndex ) );
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell )
	{
		return makeCellR1C1( _cell.image, this.sheet );
	}

	@Override
	protected ExpressionNode makeCellR1C1( Token _cell, Token _sheet )
	{
		return makeCellR1C1( _cell.image, getSheetByName( _sheet.image ) );
	}

	protected final ExpressionNode makeCellR1C1( String _ref, SheetImpl _sheet )
	{
		final CellRefParser parser = CellRefParser.getInstance( CellRefFormat.R1C1 );
		return new ExpressionNodeForCell( parser.getCellIndexForCanonicalName( _ref, _sheet, this.cellIndex ) );
	}

	protected final SheetImpl getSheetByName( String _sheet )
	{
		final String sheetName = stripSheetNameDecorationFrom( _sheet );
		final SheetImpl namedSheet = this.workbook.getSheet( sheetName );
		if (null == namedSheet) {
			throw new CompilerException.NameNotFound( "Sheet '" + sheetName + "' is not defined." );
		}
		return namedSheet;
	}

	private final String stripSheetNameDecorationFrom( String _sheet )
	{
		int startPos = 0;
		int endPos = _sheet.length();
		if ('\'' == _sheet.charAt( 0 )) startPos++;
		if ('!' == _sheet.charAt( endPos - 1 )) endPos--;
		if ('\'' == _sheet.charAt( endPos - 1 )) endPos--;
		return _sheet.substring( startPos, endPos );
	}


	private final CellRange parseNamedRef( String _ident )
	{
		final CellRange ref = this.workbook.getNamedRef( _ident );
		if (null == ref) {
			throw new InnerParserException( new CompilerException.UnsupportedExpression( "The name '"
					+ _ident + "' is not defined in this spreadsheet." ) );
		}
		return ref;
	}

	@Override
	protected final boolean isRangeName( Token _name )
	{
		return this.workbook.getNamedRef( _name.image ) instanceof MultiCellRange;
	}

	@Override
	protected final ExpressionNode makeNamedCellRef( Token _name )
	{
		final CellRange range = parseNamedRef( _name.image );
		try {
			final CellIndex cell = range.getCellIndexRelativeTo( this.cellIndex );
			return new ExpressionNodeForCell( cell );
		}
		catch (SpreadsheetException e) {
			throw new InnerParserException( e );
		}
	}

	@Override
	protected final ExpressionNode makeNamedRangeRef( Token _name )
	{
		final CellRange range = parseNamedRef( _name.image );
		return new ExpressionNodeForRange( range );
	}


	@Override
	protected final ExpressionNode makeNodeForReference( Object _reference )
	{
		if (_reference instanceof CellIndex) {
			final CellIndex cell = (CellIndex) _reference;
			return new ExpressionNodeForCell( cell );
		}
		else if (_reference instanceof CellRange) {
			final CellRange range = (CellRange) _reference;
			return new ExpressionNodeForRange( range );
		}
		throw new IllegalArgumentException( "Reference must be a cell or range" );
	}


	@Override
	protected final Object makeCellRange( Collection<ExpressionNode> _nodes )
	{
		final Iterator<ExpressionNode> nodes = _nodes.iterator();
		final ExpressionNodeForCell a = (ExpressionNodeForCell) nodes.next();
		final ExpressionNodeForCell b = (ExpressionNodeForCell) nodes.next();
		return CellRange.getCellRange( a.getCellIndex(), b.getCellIndex() );
	}

	@Override
	protected final Object makeCellIndex( ExpressionNode _node)
	{
		final ExpressionNodeForCell nodeForCell = (ExpressionNodeForCell) _node;
		return nodeForCell.getCellIndex();
	}


	@Override
	protected final ExpressionNode makeRangeIntersection( Collection<ExpressionNode> _firstTwoElements )
	{
		return new ExpressionNodeForRangeIntersection( _firstTwoElements );
	}

	@Override
	protected final ExpressionNode makeRangeUnion( Collection<ExpressionNode> _firstTwoElements )
	{
		return new ExpressionNodeForRangeUnion( _firstTwoElements );
	}

	@Override
	protected final ExpressionNode makeShapedRange( ExpressionNode _range )
	{
		return new ExpressionNodeForRangeShape( _range );
	}

}
