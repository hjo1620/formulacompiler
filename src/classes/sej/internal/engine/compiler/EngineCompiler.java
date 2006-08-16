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
package sej.internal.engine.compiler;

import java.lang.reflect.Method;

import sej.CompilerException;
import sej.NumericType;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.Util;
import sej.internal.model.ComputationModel;
import sej.runtime.EngineException;

public interface EngineCompiler
{

	public static class Config
	{
		public ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
		public ComputationModel model;
		public NumericType numericType = SEJ.DEFAULT_NUMERIC_TYPE;
		public Class factoryClass;
		public Method factoryMethod;

		public void validate()
		{
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );
			if (this.model == null) throw new IllegalArgumentException( "model is null" );
			if (this.model.getInputClass() == null) throw new IllegalArgumentException( "model.inputClass is null" );
			if (this.model.getOutputClass() == null) throw new IllegalArgumentException( "model.outputClass is null" );

			Util.validateFactory( this.factoryClass, this.factoryMethod, this.model.getInputClass(), this.model
					.getOutputClass() );
		}
	}


	public abstract SaveableEngine compile() throws CompilerException, EngineException;

}
