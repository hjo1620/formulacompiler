/*
 * Copyright (c) 2006 Peter Arrenbrecht
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - The names of the contributors may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contact information:
 * Peter Arrenbrecht
 * http://www.arrenbrecht.ch/jcite
 */
package org.formulacompiler.compiler.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;


/**
 * Utility methods.
 * 
 * @author peo
 */
public class IOUtil
{


	public static final String jdkVersionSuffix()
	{
		final String ver = System.getProperty( "java.version" );
		return ver.substring( 0, 3 );
	}


	public static final String readStringFrom( File _source ) throws IOException
	{
		return readStringFrom( new FileInputStream( _source ) );
	}


	public static final String readStringFrom( InputStream _source ) throws IOException
	{
		final StringBuffer sb = new StringBuffer( 1024 );
		final BufferedReader reader = new BufferedReader( new InputStreamReader( _source, "UTF-8" ) );
		try {
			char[] chars = new char[ 1024 ];
			int red;
			while ((red = reader.read( chars )) > -1) {
				sb.append( String.valueOf( chars, 0, red ) );
			}
		}
		finally {
			reader.close();
		}
		return sb.toString();
	}


	public static final void writeStringTo( String _value, File _target ) throws IOException
	{
		final BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( _target ),
				"UTF-8" ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}


	public static final boolean writeStringToIfNotUpToDate( String _value, File _target ) throws IOException
	{
		if (_target.exists() && _value.equals( readStringFrom( _target ) )) {
			return false;
		}
		writeStringTo( _value, _target );
		return true;
	}


	public static final boolean writeStringToIfNotUpToDateWithMessage( String _value, File _target ) throws IOException
	{
		if (writeStringToIfNotUpToDate( _value, _target )) {
			System.out.println( _target.getPath() + " written." );
			return true;
		}
		return false;
	}


	public static void writeStreamToFile( InputStream _actual, File _file ) throws FileNotFoundException, IOException
	{
		final OutputStream expected = new BufferedOutputStream( new FileOutputStream( _file ) );
		try {
			int red;
			while ((red = _actual.read()) >= 0)
				expected.write( red );
		}
		finally {
			expected.close();
		}
	}


	public static String normalizeLineEndings( String _s )
	{
		return _s.replace( "\r\n", "\n" ).replace( '\r', '\n' );
	}


	public static interface FileVisitor
	{
		void visit( File _inputFile, File _outputFile ) throws IOException;
	}


	public static final void iterateFiles( File _inputFolder, String _pattern, File _outputFolder, boolean _recurse,
			FileVisitor _visitor ) throws IOException
	{
		final StringBuilder src = new StringBuilder();
		for (int i = 0; i < _pattern.length(); i++) {
			char c = _pattern.charAt( i );
			switch (c) {
				case '*':
					src.append( ".*" );
					break;
				case '?':
					src.append( "." );
					break;
				default:
					src.append( "\\x" );
					src.append( Integer.toHexString( c ) );
			}
		}
		final Pattern pattern = Pattern.compile( src.toString() );

		final File[] inputFiles = _inputFolder.listFiles( new FilenameFilter()
		{

			public boolean accept( File _dir, String _name )
			{
				return pattern.matcher( _name ).matches();
			}

		} );

		for (File inputFile : inputFiles) {
			if (inputFile.isFile()) {
				final File outputFile = new File( _outputFolder, inputFile.getName() );
				_visitor.visit( inputFile, outputFile );
			}
		}

		if (_recurse) {
			for (File dirOrFile : _inputFolder.listFiles()) {
				if (dirOrFile.isDirectory() && dirOrFile.getName() != "." && dirOrFile.getName() != "..") {
					final File subInputFolder = dirOrFile;
					final File subOutputFolder = new File( _outputFolder, subInputFolder.getName() );
					iterateFiles( subInputFolder, _pattern, subOutputFolder, _recurse, _visitor );
				}
			}
		}
	}


	public static final String exec( String... _command ) throws IOException, InterruptedException
	{
		final ProcessBuilder pb = new ProcessBuilder( _command );
		final Process p = pb.start();
		p.waitFor();
		final StringWriter writer = new StringWriter();
		writeStream( p.getInputStream(), writer );
		return writer.toString();
	}

	private static final void writeStream( InputStream _from, Writer _printTo ) throws IOException
	{
		final Reader in = new BufferedReader( new InputStreamReader( new BufferedInputStream( _from ) ) );
		while (in.ready())
			_printTo.write( in.read() );
	}


}
