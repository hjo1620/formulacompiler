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
package org.formulacompiler.tests.regression;

import java.io.File;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith( MultiFormat.class )
public class SumRangeOnDifferentSheet
{
	private static final String DATA_FILE_PATH = "src/test/data/org/formulacompiler/tests/regression/SumRangeOnDifferentSheet";

	private final String spreadsheetExtension;

	public SumRangeOnDifferentSheet( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Test
	public void testSheet1() throws Exception
	{
		EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( new File( DATA_FILE_PATH + getSpreadsheetExtension() ) );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.getRootBinder().defineOutputCell( b.getSpreadsheet().getCellA1( "A1" ),
				b.newCallFrame( Outputs.class.getMethod( "result" ) ) );
		SaveableEngine e = b.compile();
		ComputationFactory f = e.getComputationFactory();
		Outputs c = (Outputs) f.newComputation( new Inputs() );
		assertEquals( 6.0, c.result(), 0.0001 );
	}

	@Test
	public void testSheet2() throws Exception
	{
		EngineBuilder b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( new File( DATA_FILE_PATH + getSpreadsheetExtension() ) );
		b.setInputClass( Inputs.class );
		b.setOutputClass( Outputs.class );
		b.getRootBinder().defineOutputCell( b.getSpreadsheet().getCellA1( "Sheet2!A1" ),
				b.newCallFrame( Outputs.class.getMethod( "result" ) ) );
		SaveableEngine e = b.compile();
		ComputationFactory f = e.getComputationFactory();
		Outputs c = (Outputs) f.newComputation( new Inputs() );
		assertEquals( 15.0, c.result(), 0.0001 );
	}

	public static class Inputs
	{
		// Dummy
	}

	public static interface Outputs
	{
		double result();
	}

}
