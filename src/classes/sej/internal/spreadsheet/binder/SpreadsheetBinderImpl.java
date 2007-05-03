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
package sej.internal.spreadsheet.binder;

import sej.compiler.CallFrame;
import sej.compiler.CompilerException;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.internal.spreadsheet.binding.SectionBinding;
import sej.internal.spreadsheet.binding.WorkbookBinding;
import sej.spreadsheet.Orientation;
import sej.spreadsheet.Spreadsheet;
import sej.spreadsheet.SpreadsheetBinder;
import sej.spreadsheet.SpreadsheetBinding;
import sej.spreadsheet.Spreadsheet.Cell;
import sej.spreadsheet.Spreadsheet.Range;

public final class SpreadsheetBinderImpl implements SpreadsheetBinder
{
	private final Spreadsheet spreadsheet;
	private final WorkbookBinding binding;
	private final SectionBinderImpl root;

	public SpreadsheetBinderImpl(Config _config)
	{
		super();
		_config.validate();
		this.spreadsheet = _config.spreadsheet;
		this.binding = new WorkbookBinding( (SpreadsheetImpl) _config.spreadsheet, _config.inputClass,
				_config.outputClass );
		this.root = new SectionBinderImpl( this.binding.getRoot() );
	}

	public static final class Factory implements SpreadsheetBinder.Factory
	{
		public SpreadsheetBinder newInstance( Config _config )
		{
			return new SpreadsheetBinderImpl( _config );
		}
	}


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public SpreadsheetBinding getBinding() throws CompilerException
	{
		this.binding.validate();
		return this.binding;
	}


	// ------------------------------------------------ Operations


	public Section getRoot()
	{
		return this.root;
	}


	private class SectionBinderImpl implements SpreadsheetBinder.Section
	{
		private final SectionBinding sectionBinding;

		public SectionBinderImpl(SectionBinding _binding)
		{
			super();
			this.sectionBinding = _binding;
		}

		public void defineInputCell( Cell _cell, CallFrame _callChainToCall ) throws CompilerException
		{
			this.sectionBinding.defineInputCell( _cell, _callChainToCall );
		}

		public void defineOutputCell( Cell _cell, CallFrame _call ) throws CompilerException
		{
			this.sectionBinding.defineOutputCell( _cell, _call );
		}

		public SpreadsheetBinder.Section defineRepeatingSection( Range _range, Orientation _orientation,
				CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
				Class _outputClass ) throws CompilerException
		{
			return new SectionBinderImpl( this.sectionBinding.defineRepeatingSection( _range, _orientation,
					_inputCallChainReturningIterable, _inputClass, _outputCallToImplementIterable, _outputClass ) );
		}

		public Class getInputClass()
		{
			return this.sectionBinding.getInputClass();
		}

		public Class getOutputClass()
		{
			return this.sectionBinding.getOutputClass();
		}

	}


}
