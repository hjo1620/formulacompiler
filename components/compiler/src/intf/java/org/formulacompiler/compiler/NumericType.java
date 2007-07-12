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
package org.formulacompiler.compiler;

import java.text.ParseException;
import java.util.Locale;

import org.formulacompiler.runtime.FormulaRuntime;

/**
 * Immutable class representing the type to be used by the numeric computations of generated
 * engines.
 * 
 * @author peo
 */
public interface NumericType
{

	/**
	 * For BigDecimal types, indicates that no explicit scaling should be performed by the engine.
	 */
	public static final int UNDEFINED_SCALE = FormulaRuntime.UNDEFINED_SCALE;

	/**
	 * Returns the Java class of the base type.
	 */
	public Class getValueType();

	/**
	 * Returns the fixed scale, or else {@link #UNDEFINED_SCALE}.
	 */
	public int getScale();

	/**
	 * Returns the rounding mode.
	 */
	public int getRoundingMode();

	/**
	 * Returns the number 0.
	 */
	public Number getZero();

	/**
	 * Returns the number 1.
	 */
	public Number getOne();

	/**
	 * Converts a number to this type. Null is returned as null.
	 * 
	 * @return an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #getValueType()
	 */
	public Number valueOf( Number _value );

	/**
	 * Parses a string into a value using the default locale. Null and the empty string return zero
	 * (see {@link #getZero()}).
	 * 
	 * @return an instance of the corresponding (boxed) Java number type.
	 * 
	 * @throws ParseException
	 * 
	 * @see #valueOf(String, Locale)
	 * @see #getValueType()
	 */
	public Number valueOf( String _value ) throws ParseException;

	/**
	 * Parses a string into a value using the given locale. Null and the empty string return zero
	 * (see {@link #getZero()}).
	 * 
	 * @param _locale determines formatting options; must not be {@code null}.
	 * @return an instance of the corresponding (boxed) Java number type.
	 * @throws ParseException
	 * 
	 * @see #getValueType()
	 */
	public Number valueOf( String _value, Locale _locale ) throws ParseException;

	/**
	 * Returns the value as a string in its canonical representation using the default locale. Null
	 * returns the empty string.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #valueToString(Number, Locale)
	 * @see #getValueType()
	 */
	public String valueToString( Number _value );

	/**
	 * Returns the value as a string in its canonical representation using the given locale. Null
	 * returns the empty string.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * @param _locale determines formatting options; must not be {@code null}.
	 * 
	 * @see #getValueType()
	 */
	public String valueToString( Number _value, Locale _locale );

	/**
	 * Returns the value as a string with no superfluous leading or trailing zeroes and decimal point
	 * using the default locale. Null returns the empty string. Uses scientific display the way Excel
	 * does.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #valueToConciseString(Number, Locale)
	 * @see #getValueType()
	 */
	public String valueToConciseString( Number _value );

	/**
	 * Returns the value as a string with no superfluous leading or trailing zeroes and decimal point
	 * using the given locale. Null returns the empty string. Uses scientific display the way Excel
	 * does.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * @param _locale determines formatting options; must not be {@code null}.
	 * 
	 * @see #getValueType()
	 */
	public String valueToConciseString( Number _value, Locale _locale );


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		NumericType getInstance( Class _valueType, int _scale, int _roundingMode );
	}

}
