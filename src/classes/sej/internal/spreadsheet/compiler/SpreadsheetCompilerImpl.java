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
package sej.internal.spreadsheet.compiler;

import java.lang.reflect.Method;

import sej.CompilerException;
import sej.NumericType;
import sej.SaveableEngine;
import sej.SpreadsheetBinding;
import sej.SpreadsheetCompiler;
import sej.internal.engine.compiler.AbstractEngineCompiler;
import sej.internal.engine.compiler.EngineCompiler;
import sej.internal.model.ComputationModel;
import sej.internal.model.compiler.ComputationModelCompiler;
import sej.runtime.EngineException;


public class SpreadsheetCompilerImpl implements SpreadsheetCompiler
{
	private final SpreadsheetBinding binding;
	private final NumericType numericType;
	private final Class factoryClass;
	private final Method factoryMethod;

	
	public SpreadsheetCompilerImpl(Config _config)
	{
		super();

		_config.validate();

		this.binding = _config.binding;
		this.numericType = _config.numericType;
		this.factoryClass = _config.factoryClass;
		this.factoryMethod = _config.factoryMethod;
	}


	public SaveableEngine compile() throws CompilerException, EngineException
	{
		ComputationModelCompiler cc = new ComputationModelCompiler( this.binding, this.numericType );
		ComputationModel cm = cc.compile();

		EngineCompiler.Config ecc = new EngineCompiler.Config();
		ecc.model = cm;
		ecc.numericType = this.numericType;
		ecc.factoryClass = this.factoryClass;
		ecc.factoryMethod = this.factoryMethod;
		EngineCompiler ec = AbstractEngineCompiler.newInstance( ecc );

		return ec.compile();
	}

}