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
package sej.decompiler;

import java.io.IOException;

import sej.runtime.Engine;

/**
 * Decompiles a JVM byte code engine back to Java source using the <a
 * href="jode.sourceforge.net">JODE</a> library.
 * 
 * @author peo
 */
public interface ByteCodeEngineDecompiler
{

	/**
	 * Configuration data for new instances of {@link sej.decompiler.ByteCodeEngineDecompiler}.
	 * 
	 * @author peo
	 * 
	 * @see SEJByteCode#decompile(Engine)
	 */
	public static class Config
	{
		
		/**
		 * The engine to decompile.
		 */
		public Engine engine;

		/**
		 * Validates the configuration for missing or improperly set values.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.engine == null) throw new IllegalArgumentException( "engine is null" );
		}
	}


	/**
	 * Decompiles the engine and returns a source code description object.
	 * 
	 * @throws IOException
	 */
	public abstract ByteCodeEngineSource decompile() throws IOException;

	/**
	 * Factory interface for {@link sej.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		public ByteCodeEngineDecompiler newInstance( Config _config );
	}

}