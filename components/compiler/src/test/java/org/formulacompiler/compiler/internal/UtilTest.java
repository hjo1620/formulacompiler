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

package org.formulacompiler.compiler.internal;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class UtilTest extends TestCase
{
	private static final Method intfMethod = getMethod( TestIntf.class, "sth" );
	private static final Method moreMethod = getMethod( TestClass.class, "more" );
	private static final Method otherMethod = getMethod( TestDescendant.class, "other" );

	private static Method getMethod( Class<?> _class, String _name )
	{
		try {
			return _class.getMethod( _name );
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}


	public void testValidateCallable() throws Exception
	{
		Util.validateCallable( TestIntf.class, intfMethod );
		Util.validateCallable( TestClass.class, intfMethod );
		Util.validateCallable( TestClass.class, moreMethod );
		Util.validateCallable( TestDescendant.class, intfMethod );
		Util.validateCallable( TestDescendant.class, moreMethod );
		Util.validateCallable( TestDescendant.class, otherMethod );

		try {
			Util.validateCallable( TestIntf.class, moreMethod );
			fail();
		}
		catch (IllegalArgumentException e) {
			//
		}

		try {
			Util.validateCallable( TestClass.class, otherMethod );
			fail();
		}
		catch (IllegalArgumentException e) {
			//
		}
	}

	static interface TestIntf
	{
		void sth();
	}

	static class TestClass implements TestIntf
	{
		public void sth()
		{
			//
		}
		public void more()
		{
			//
		}
	}

	static class TestDescendant extends TestClass
	{
		public void other()
		{
			//
		}
	}


	private static final Method[] sigIntfMethods = SigIntf.class.getMethods();

	public void testSignatureOf() throws Exception
	{
		assertSig( "()V", "none" );
		assertSig( "()I", "anInt" );
		assertSig( "(I)I", "withInt" );
		assertSig( "(II)I", "withInts" );
		assertSig( "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", "withObjStr" );
	}

	static interface SigIntf
	{
		void none();
		int anInt();
		int withInt( int a );
		int withInts( int a, int b );
		Object withObjStr( Object a, String b );
	}

	private void assertSig( String _sig, String _mtd )
	{
		assertEquals( _sig, Util.signatureOf( getSigMethod( _mtd ) ) );
	}

	private Method getSigMethod( String _name )
	{
		for (Method m : sigIntfMethods) {
			if (m.getName().equals( _name )) return m;
		}
		return null;
	}


	public void testAbstractMethodsOf() throws Exception
	{
		assertEquals( "AbstractTestClass", 3, Util.abstractMethodsOf( AbstractTestClass.class ).size() );
		assertEquals( "NonAbstractTestClass", 0, Util.abstractMethodsOf( NonAbstractTestClass.class ).size() );
	}

	static abstract class AbstractTestBase
	{
		abstract void notTangible();
	}

	static interface AbstractTestIntf extends TestIntf
	{
		void other();
	}

	static abstract class AbstractTestClass extends AbstractTestBase implements AbstractTestIntf
	{
		//
	}

	static class NonAbstractTestClass extends AbstractTestClass
	{
		@Override
		void notTangible()
		{
			// 
		}
		public void sth()
		{
			// 
		}
		public void other()
		{
			// 
		}
	}

}
