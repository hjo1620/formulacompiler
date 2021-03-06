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

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.tests.utils.AbstractIOTestCase;


/**
 * This is the prototype of how AFC implements sums over variably sized sections.
 * 
 * @author peo
 */
public class SectionSumTest extends AbstractIOTestCase
{

	public void testSums() throws Exception
	{
		RootInput input = new RootInput( 10, 2, 3, 4, 5 );
		int expected = 10 + 4 * (2 + 3 + 4 + 5);
		assertSums( expected, -20, input );
	}

	public void testEmptySums() throws Exception
	{
		RootInput input = new RootInput( 20 );
		assertSums( 20, 0, input );
	}

	private void assertSums( int _expected, int _expectedDiffWithoutLast, RootInput _input ) throws Exception
	{
		assertSums( _expected, _expectedDiffWithoutLast, new RootPrototype( _input ), _input, "Proto" );
		assertSums( _expected, _expectedDiffWithoutLast, newRootEngine( _input ), _input, "AFC" );
	}

	private void assertSums( int _expected, int _expectedDiffWithoutLast, RootOutput _output, RootInput _input,
			String _engName )
	{
		assertSums( _expected, _output, _engName );
		if (_expectedDiffWithoutLast != 0) {
			_input.detailLen--;
			try {
				_output.reset();
				assertSums( _expected + _expectedDiffWithoutLast, _output, _engName );
			}
			finally {
				_input.detailLen++;
			}
		}
	}

	private void assertSums( int _expected, RootOutput _output, String _engName )
	{
		assertEquals( _engName + ": array", _expected, _output.arraySums() );
		assertEquals( _engName + ": iterator", _expected, _output.iteratorSums() );
		assertEquals( _engName + ": iterable", _expected, _output.iterableSums() );
		assertEquals( _engName + ": collection", _expected, _output.collectionSums() );
	}

	private RootOutput newRootEngine( RootInput _input ) throws Exception
	{
		SpreadsheetBuilder bld = SpreadsheetCompiler.newSpreadsheetBuilder();
		bld.newCell( bld.cst( 1 ) );
		bld.nameCell( "rootValue" );
		CellRef rootCell = bld.currentCell();

		buildSectionSum( bld, rootCell, "array" );
		buildSectionSum( bld, rootCell, "iterator" );
		buildSectionSum( bld, rootCell, "iterable" );
		buildSectionSum( bld, rootCell, "collection" );

		Spreadsheet sht = bld.getSpreadsheet();

		EngineBuilder cmp = SpreadsheetCompiler.newEngineBuilder();
		cmp.setSpreadsheet( sht );
		cmp.setInputClass( RootInput.class );
		cmp.setOutputClass( RootOutput.class );
		cmp.setNumericType( SpreadsheetCompiler.LONG );
		Section bnd = cmp.getRootBinder();

		bnd.defineInputCell( sht.getCell( "rootValue" ), call( RootInput.class, "getRootValue" ) );

		bindSectionSum( sht, bnd, "array" );
		bindSectionSum( sht, bnd, "iterator" );
		bindSectionSum( sht, bnd, "iterable" );
		bindSectionSum( sht, bnd, "collection" );

		SaveableEngine engine = cmp.compile();
		checkEngine( engine );

		return (RootOutput) engine.getComputationFactory().newComputation( _input );
	}

	private void buildSectionSum( SpreadsheetBuilder _bld, CellRef _rootCell, String _prefix )
	{
		_bld.newRow();
		_bld.newCell( _bld.cst( 2 ) );
		_bld.nameCell( _prefix + "Det" );
		CellRef detCell = _bld.currentCell();
		_bld.newCell( _bld.op( Operator.TIMES, _bld.ref( detCell ), _bld.ref( _bld.cst( 2 ) ) ) );
		CellRef detTwice = _bld.currentCell();
		_bld.nameRange( _bld.range( detCell, detTwice ), _prefix + "Dets" );
		_bld.newRow();
		_bld.newCell( _bld.fun( Function.SUM, _bld.ref( detTwice ) ) );
		CellRef detSum = _bld.currentCell();
		_bld.newCell( _bld.fun( Function.SUM, _bld.ref( _rootCell ), _bld.ref( detSum ), _bld.ref( detSum ) ) );
		_bld.nameCell( _prefix + "Sums" );
	}

	private void bindSectionSum( Spreadsheet _sht, Section _bnd, String _prefix ) throws CompilerException, Exception
	{
		Range rng = _sht.getRange( _prefix + "Dets" );
		CallFrame iter = call( RootInput.class, "get" + _prefix );
		Section dets = _bnd.defineRepeatingSection( rng, Orientation.VERTICAL, iter, DetailInput.class, null, null );
		dets.defineInputCell( _sht.getCell( _prefix + "Det" ), call( DetailInput.class, "getDetailValue" ) );
		_bnd.defineOutputCell( _sht.getCell( _prefix + "Sums" ), call( RootOutput.class, _prefix + "Sums" ) );
	}

	private CallFrame call( Class _cls, String _mtd ) throws Exception
	{
		return FormulaCompiler.newCallFrame( _cls.getMethod( _mtd ) );
	}


	public static abstract class RootOutput implements Resettable
	{
		public abstract long arraySums();
		public abstract long iteratorSums();
		public long iterableSums()
		{
			return 0;
		}
		public abstract long collectionSums();
	}


	public static final class RootPrototype extends RootOutput implements Computation
	{
		private final RootInput inputs;

		public RootPrototype( RootInput _inputs )
		{
			this.inputs = _inputs;
		}

		@Override
		public long arraySums()
		{
			return this.inputs.getRootValue() + arraySum() + arraySum();
		}

		long arraySum()
		{
			final DetailPrototype[] ds = arrayDets();
			final int dl = ds.length;
			if (dl > 0) {
				long result = ds[ 0 ].detailValue();
				for (int i = 1; i < dl; i++) {
					result += ds[ i ].detailValue();
				}
				return result;
			}
			else {
				return 0;
			}
		}

		private DetailPrototype[] arrayDets;

		/**
		 * The simplest case: build an array from an array.
		 */
		DetailPrototype[] arrayDets()
		{
			if (this.arrayDets == null) {
				final DetailInput[] ds = this.inputs.getarray();
				if (ds != null) {
					final int dl = ds.length;

					final DetailPrototype[] di = new DetailPrototype[ dl ];
					for (int i = 0; i < dl; i++) {
						di[ i ] = new DetailPrototype( ds[ i ], this );
					}

					this.arrayDets = di;
				}
				else {
					this.iteratorDets = new DetailPrototype[ 0 ];
				}
			}
			return this.arrayDets;
		}


		@Override
		public long iteratorSums()
		{
			return this.inputs.getRootValue() + iteratorSum() + iteratorSum();
		}

		long iteratorSum()
		{
			long result = 0;
			final DetailPrototype[] ds = iteratorDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] iteratorDets;

		/**
		 * Build an array from an iterator.
		 */
		DetailPrototype[] iteratorDets()
		{
			if (this.iteratorDets == null) {
				final Iterator<DetailInput> ds = this.inputs.getiterator();
				if (ds != null) {

					final Collection<DetailPrototype> coll = New.list();
					while (ds.hasNext()) {
						coll.add( new DetailPrototype( ds.next(), this ) );
					}
					final DetailPrototype[] di = coll.toArray( new DetailPrototype[ coll.size() ] );

					this.iteratorDets = di;
				}
				else {
					this.iteratorDets = new DetailPrototype[ 0 ];
				}
			}
			return this.iteratorDets;
		}


		@Override
		public long iterableSums()
		{
			return this.inputs.getRootValue() + iterableSum() + iterableSum();
		}

		long iterableSum()
		{
			long result = 0;
			final DetailPrototype[] ds = iterableDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] iterableDets;

		/**
		 * Build an array from an iterable's iterator.
		 */
		DetailPrototype[] iterableDets()
		{
			if (this.iterableDets == null) {
				final Iterator<DetailInput> ds = this.inputs.getiterable().iterator();
				if (ds != null) {

					final Collection<DetailPrototype> coll = New.list();
					while (ds.hasNext()) {
						coll.add( new DetailPrototype( ds.next(), this ) );
					}
					final DetailPrototype[] di = coll.toArray( new DetailPrototype[ coll.size() ] );

					this.iterableDets = di;
				}
				else {
					this.iterableDets = new DetailPrototype[ 0 ];
				}
			}
			return this.iterableDets;
		}


		@Override
		public long collectionSums()
		{
			return this.inputs.getRootValue() + collectionSum() + collectionSum();
		}

		long collectionSum()
		{
			long result = 0;
			final DetailPrototype[] ds = collectionDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] collectionDets;

		/**
		 * Build an array from a collection. Since we know the size of the collection, pre-allocate
		 * the array.
		 */
		DetailPrototype[] collectionDets()
		{
			if (this.collectionDets == null) {
				final Collection<DetailInput> dc = this.inputs.getcollection();
				if (dc != null) {

					final int dl = dc.size();
					final Iterator<DetailInput> ds = dc.iterator();
					final DetailPrototype[] di = new DetailPrototype[ dl ];
					for (int i = 0; i < dl; i++) {
						di[ i ] = new DetailPrototype( ds.next(), this );
					}

					this.collectionDets = di;
				}
				else {
					this.collectionDets = new DetailPrototype[ 0 ];
				}
			}
			return this.collectionDets;
		}

		public void reset()
		{
			this.arrayDets = null;
			this.collectionDets = null;
			this.iterableDets = null;
			this.iteratorDets = null;
		}

	}

	public static final class DetailPrototype
	{
		private final RootPrototype parent;
		private final DetailInput inputs;

		public DetailPrototype( DetailInput _inputs, RootPrototype _parent )
		{
			this.parent = _parent;
			this.inputs = _inputs;
		}

		long detailValue()
		{
			return this.inputs.getDetailValue() * 2;
		}

		void parentRef() // make warning go away
		{
			this.parent.toString();
		}

	}


	public static final class RootInput
	{
		private final long value;
		private DetailInput[] details;
		public int detailLen;

		public RootInput( long _rootValue, long... _detailValues )
		{
			this.value = _rootValue;
			this.details = new DetailInput[ _detailValues.length ];
			for (int i = 0; i < _detailValues.length; i++) {
				this.details[ i ] = new DetailInput( _detailValues[ i ] );
			}
			this.detailLen = this.details.length;
		}

		public long getRootValue()
		{
			return this.value;
		}

		public DetailInput[] getarray()
		{
			DetailInput[] r = new DetailInput[ this.detailLen ];
			System.arraycopy( this.details, 0, r, 0, r.length );
			return r;
		}

		public Collection<DetailInput> getcollection()
		{
			final Collection<DetailInput> r = New.list( this.detailLen );
			for (int i = 0; i < this.detailLen; i++) {
				r.add( this.details[ i ] );
			}
			return r;
		}

		public Iterable<DetailInput> getiterable()
		{
			return getcollection();
		}

		public Iterator<DetailInput> getiterator()
		{
			return getcollection().iterator();
		}

	}

	public static final class DetailInput
	{
		private final long value;

		public DetailInput( long _value )
		{
			this.value = _value;
		}

		public long getDetailValue()
		{
			return this.value;
		}
	}

}
