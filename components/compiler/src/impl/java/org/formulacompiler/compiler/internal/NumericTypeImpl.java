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
package org.formulacompiler.compiler.internal;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v1;
import org.formulacompiler.runtime.internal.RuntimeDouble_v1;
import org.formulacompiler.runtime.internal.RuntimeLong_v1;


/**
 * Immutable class representing the type to be used by the numeric computations of generated
 * engines.
 * 
 * @author peo
 */
public abstract class NumericTypeImpl implements NumericType
{
	private final Class valueType;
	private final int scale;
	private final int roundingMode;

	// LATER Switch to MathContext

	/**
	 * To ensure compatibility with JRE 1.4 I cannot use a MathContext here.
	 */
	protected NumericTypeImpl( Class _valueType, int _scale, int _roundingMode )
	{
		super();
		this.valueType = _valueType;
		this.scale = _scale;
		this.roundingMode = _roundingMode;
	}


	/**
	 * Returns an instance with the specified attributes.
	 */
	public static NumericType getInstance( Class _valueType, int _scale, int _roundingMode )
	{
		if (_valueType == Double.TYPE) {
			if (_scale != UNDEFINED_SCALE) throw new IllegalArgumentException( "Scale is not supported for double" );
			if (_roundingMode != BigDecimal.ROUND_DOWN)
				throw new IllegalArgumentException( "Rounding is not supported for double" );
			return new DoubleType();
		}
		else if (_valueType == Long.TYPE) {
			if (_scale == 0) {
				if (_roundingMode != BigDecimal.ROUND_DOWN)
					throw new IllegalArgumentException( "Unscaled long can only be rounded down" );
				return new LongType();
			}
			else {
				if (_roundingMode != BigDecimal.ROUND_DOWN)
					throw new IllegalArgumentException( "Scaled long can only be rounded down" );
				return new ScaledLongType( _scale );
			}
		}
		else if (_valueType == BigDecimal.class) {
			return new BigDecimalType( _scale, _roundingMode );
		}
		throw new IllegalArgumentException( "Unsupported numeric type" );
	}


	public static final class Factory implements NumericType.Factory
	{
		public NumericType getInstance( Class _valueType, int _scale, int _roundingMode )
		{
			return NumericTypeImpl.getInstance( _valueType, _scale, _roundingMode );
		}
	}


	public Class getValueType()
	{
		return this.valueType;
	}

	public int getScale()
	{
		return this.scale;
	}

	public int getRoundingMode()
	{
		return this.roundingMode;
	}


	public abstract Number getZero();

	public abstract Number getOne();


	public Number valueOf( Number _value )
	{
		if (null == _value) return null;
		return assertProperNumberType( convertFromAnyNumber( _value ) );
	}

	public final Number valueOf( String _value ) throws ParseException
	{
		return assertProperNumberType( valueOf( _value, Locale.getDefault() ) );
	}

	public final Number valueOf( String _value, Locale _locale ) throws ParseException
	{
		if (null == _value) return getZero();
		if (0 == _value.length()) return getZero();
		return assertProperNumberType( convertFromString( _value, _locale ) );
	}

	public final String valueToString( Number _value )
	{
		return valueToString( _value, Locale.getDefault() );
	}

	public final String valueToString( Number _value, Locale _locale )
	{
		if (null == _value) return "";
		return convertToString( assertProperNumberType( _value ), _locale );
	}

	public final String valueToConciseString( Number _value )
	{
		return valueToConciseString( _value, Locale.getDefault() );
	}

	public final String valueToConciseString( Number _value, Locale _locale )
	{
		if (null == _value) return "";
		return convertToConciseString( assertProperNumberType( _value ), _locale );
	}


	protected abstract Number convertFromAnyNumber( Number _value );
	protected abstract Number assertProperNumberType( Number _value );

	protected Number convertFromString( String _value, Locale _locale ) throws ParseException
	{
		return convertFromAnyNumber( NumberFormat.getNumberInstance( _locale ).parse( _value ) );
	}

	protected String convertToString( Number _value, Locale _locale )
	{
		return NumberFormat.getNumberInstance( _locale ).format( _value );
	}

	protected String convertToConciseString( Number _value, Locale _locale )
	{
		return convertToString( _value, _locale );
	}


	public void validateReturnTypeForCell( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();

		if (Byte.TYPE == returnType || Byte.class == returnType) return;
		if (Short.TYPE == returnType || Short.class == returnType) return;
		if (Integer.TYPE == returnType || Integer.class == returnType) return;
		if (Long.TYPE == returnType || Long.class == returnType) return;

		if (Float.TYPE == returnType || Float.class == returnType) return;
		if (Double.TYPE == returnType || Double.class == returnType) return;

		if (Boolean.TYPE == returnType || Boolean.class == returnType) return;
		if (Date.class == returnType) return;

		if (BigInteger.class == returnType) return;
		if (BigDecimal.class == returnType) return;

		if (String.class == returnType) return;

		throw new CompilerException.UnsupportedDataType( "The method " + _method + " has an unsupported return type" );
	}


	@Override
	public String toString()
	{
		return getValueType().getName() + ((UNDEFINED_SCALE != getScale())? "." + Integer.toString( getScale() ) : "");
	}


	public static final class DoubleType extends NumericTypeImpl
	{

		protected DoubleType()
		{
			super( Double.TYPE, UNDEFINED_SCALE, BigDecimal.ROUND_UNNECESSARY );
		}

		@Override
		public Number getZero()
		{
			return Double.valueOf( 0.0 );
		}

		@Override
		public Number getOne()
		{
			return Double.valueOf( 1.0 );
		}

		@Override
		protected Double assertProperNumberType( Number _value )
		{
			return (Double) _value;
		}

		@Override
		protected Number convertFromAnyNumber( Number _value )
		{
			if (_value instanceof Double) return _value;
			return _value.doubleValue();
		}

		@Override
		protected String convertToConciseString( Number _value, Locale _locale )
		{
			// We want to be sure this is a double here.
			return RuntimeDouble_v1.toExcelString( _value.doubleValue(), _locale );
		}

	}


	public static final class BigDecimalType extends NumericTypeImpl
	{
		private final BigDecimal zero = adjustScale( BigDecimal.ZERO );
		private final BigDecimal one = adjustScale( BigDecimal.ONE );

		protected BigDecimalType( int _scale, int _roundingMode )
		{
			super( BigDecimal.class, _scale, _roundingMode );
		}

		@Override
		public Number getZero()
		{
			return this.zero;
		}

		@Override
		public Number getOne()
		{
			return this.one;
		}

		@Override
		protected BigDecimal assertProperNumberType( Number _value )
		{
			return (BigDecimal) _value;
		}

		@Override
		protected BigDecimal convertFromAnyNumber( Number _value )
		{
			BigDecimal v;
			if (_value instanceof BigDecimal) {
				v = (BigDecimal) _value;
			}
			else if (_value instanceof Long) {
				v = BigDecimal.valueOf( _value.longValue() );
			}
			else if (_value instanceof Integer) {
				v = BigDecimal.valueOf( _value.longValue() );
			}
			else if (_value instanceof Byte) {
				v = BigDecimal.valueOf( _value.longValue() );
			}
			else {
				v = BigDecimal.valueOf( _value.doubleValue() );
			}
			return adjustScale( v );
		}

		@Override
		protected String convertToConciseString( Number _value, Locale _locale )
		{
			return RuntimeBigDecimal_v1.toExcelString( (BigDecimal) _value, _locale );
		}

		private BigDecimal adjustScale( BigDecimal _value )
		{
			if (NumericType.UNDEFINED_SCALE != getScale()) {
				return _value.setScale( getScale(), getRoundingMode() );
			}
			return _value;
		}

	}


	public static abstract class AbstractLongType extends NumericTypeImpl
	{

		protected AbstractLongType( int _scale, int _roundingMode )
		{
			super( Long.TYPE, _scale, _roundingMode );
		}

		@Override
		public Number getZero()
		{
			return Long.valueOf( zero() );
		}

		@Override
		public Number getOne()
		{
			return Long.valueOf( one() );
		}

		@Override
		protected Long assertProperNumberType( Number _value )
		{
			return (Long) _value;
		}

		public long zero()
		{
			return 0L;
		}

		public abstract long one();

	}


	public static final class LongType extends AbstractLongType
	{

		protected LongType()
		{
			super( 0, BigDecimal.ROUND_DOWN );
		}

		@Override
		public long one()
		{
			return 1;
		}

		@Override
		protected Number convertFromAnyNumber( Number _value )
		{
			if (_value instanceof Long) return _value;
			return _value.longValue();
		}

	}


	public static final class ScaledLongType extends AbstractLongType
	{
		private static long[] SCALING_FACTORS = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
				1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
				1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L };

		private final long scalingFactor;

		protected ScaledLongType( int _scale )
		{
			super( _scale, BigDecimal.ROUND_DOWN );
			if (_scale < 1 || _scale >= SCALING_FACTORS.length) {
				throw new IllegalArgumentException( "Scale is out of range" );
			}
			this.scalingFactor = SCALING_FACTORS[ _scale ];
		}

		@Override
		public long one()
		{
			return this.scalingFactor;
		}

		@Override
		protected final Number convertFromString( String _value, Locale _locale )
		{
			return parse( _value, _locale );
		}

		public long parse( String _value, Locale _locale )
		{
			final DecimalFormatSymbols syms = ((DecimalFormat) NumberFormat.getNumberInstance( _locale ))
					.getDecimalFormatSymbols();
			final char decSep = syms.getDecimalSeparator();
			final char minusSign = syms.getMinusSign();

			String value = _value;
			if (value.indexOf( 'E' ) >= 0 || value.indexOf( 'e' ) >= 0) {
				value = new BigDecimal( value ).toPlainString();
			}
			final int posOfDecPoint = value.indexOf( decSep );
			if (posOfDecPoint < 0) {
				return Long.parseLong( value ) * this.scalingFactor;
			}
			else {
				final int scaleOfResult = getScale();
				final int scaleOfValue = value.length() - posOfDecPoint - 1;
				final int scaleOfDigits = (scaleOfValue > scaleOfResult)? scaleOfResult : scaleOfValue;
				final String digits = value.substring( 0, posOfDecPoint )
						+ value.substring( posOfDecPoint + 1, posOfDecPoint + 1 + scaleOfDigits );
				final boolean roundUp;
				if (scaleOfValue > scaleOfDigits) {
					final char nextDigit = value.charAt( posOfDecPoint + 1 + scaleOfDigits );
					roundUp = nextDigit >= '5';
				}
				else {
					roundUp = false;
				}
				long unscaled = Long.parseLong( digits );
				if (roundUp) {
					final boolean negative = value.charAt( 0 ) == minusSign;
					unscaled += negative? -1 : 1;
				}
				if (scaleOfDigits == scaleOfResult) {
					return unscaled;
				}
				else {
					assert scaleOfDigits < scaleOfResult;
					final long rescalingFactor = SCALING_FACTORS[ scaleOfResult - scaleOfDigits ];
					return unscaled * rescalingFactor;
				}
			}
		}

		@Override
		protected Number convertFromAnyNumber( Number _value )
		{
			if (_value instanceof Long) return _value.longValue() * one();
			return Math.round( RuntimeDouble_v1.round( _value.doubleValue(), getScale() ) * one() );
		}

		@Override
		protected String convertToString( Number _value, Locale _locale )
		{
			return RuntimeLong_v1.toExcelString( (Long) _value, getScale(), _locale );
		}

	}

}
