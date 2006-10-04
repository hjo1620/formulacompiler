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
package sej.internal.templates;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import sej.internal.runtime.RuntimeBigDecimal_v1;


@SuppressWarnings("unqualified-field-access")
public class ExpressionTemplatesForBigDecimals
{
	boolean isScaled = false;
	int fixedScale = 0;
	int roundingMode = 0;


	// ------------------------------------------------ Utils


	@ReturnsAdjustedValue
	BigDecimal util_adjustValue( BigDecimal a )
	{
		return a.setScale( fixedScale, roundingMode );
	}


	BigDecimal util_round( BigDecimal a, int _maxFrac )
	{
		return RuntimeBigDecimal_v1.round( a, _maxFrac );
	}


	BigDecimal util_fromInt( int a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromLong( long a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromDouble( double a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromFloat( float a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromBigDecimal( BigDecimal a )
	{
		return a == null ? BigDecimal.ZERO : a;
	}

	BigDecimal util_fromBigInteger( BigInteger a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a );
	}

	BigDecimal util_fromNumber( Number a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a.toString() );
	}

	BigDecimal util_fromBoolean( boolean a )
	{
		return a ? BigDecimal.ONE : BigDecimal.ZERO;
	}

	BigDecimal util_fromDate( Date a )
	{
		return RuntimeBigDecimal_v1.dateToNum( a );
	}


	byte util_toByte( BigDecimal a )
	{
		return a.byteValue();
	}

	short util_toShort( BigDecimal a )
	{
		return a.shortValue();
	}

	int util_toInt( BigDecimal a )
	{
		return a.intValue();
	}

	long util_toLong( BigDecimal a )
	{
		return a.longValue();
	}

	double util_toDouble( BigDecimal a )
	{
		return a.doubleValue();
	}

	float util_toFloat( BigDecimal a )
	{
		return a.floatValue();
	}

	BigInteger util_toBigInteger( BigDecimal a )
	{
		return a.toBigInteger();
	}

	BigDecimal util_toBigDecimal( BigDecimal a )
	{
		return a;
	}

	boolean util_toBoolean( BigDecimal a )
	{
		return !a.equals( BigDecimal.ZERO );
	}

	char util_toCharacter( BigDecimal a )
	{
		return (char) a.intValue();
	}

	Date util_toDate( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.dateFromNum( a );
	}

	String util_toString( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.toExcelString( a );
	}


	BigDecimal util_fromScaledLong( long a, int _scale )
	{
		return RuntimeBigDecimal_v1.fromScaledLong( a, _scale );
	}

	long util_toScaledLong( BigDecimal a, int _scale )
	{
		return RuntimeBigDecimal_v1.toScaledLong( a, _scale );
	}


	// ------------------------------------------------ Operators


	@ReturnsAdjustedValue
	BigDecimal op_NOOP( BigDecimal a )
	{
		return a;
	}

	@ReturnsAdjustedValue
	BigDecimal op_PLUS( BigDecimal a, BigDecimal b )
	{
		return a.add( b );
	}

	@ReturnsAdjustedValue
	BigDecimal op_MINUS( BigDecimal a, BigDecimal b )
	{
		return a.subtract( b );
	}

	@ReturnsAdjustedValue
	BigDecimal op_MINUS( BigDecimal a )
	{
		return a.negate();
	}

	BigDecimal op_TIMES( BigDecimal a, BigDecimal b )
	{
		return a.multiply( b );
	}

	@ReturnsAdjustedValue
	BigDecimal op_DIV__if_needsValueAdjustment( BigDecimal a, BigDecimal b )
	{
		return a.divide( b, fixedScale, roundingMode );
	}

	@ReturnsAdjustedValue
	BigDecimal op_DIV( BigDecimal a, BigDecimal b )
	{
		return a.divide( b );
	}

	BigDecimal op_EXP( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.pow( a, b );
	}

	BigDecimal op_PERCENT( BigDecimal a )
	{
		return a.movePointLeft( 2 );
	}

	@ReturnsAdjustedValue
	BigDecimal op_MIN( BigDecimal a, BigDecimal b )
	{
		BigDecimal a__1, b__2;
		return ((a__1 = a).compareTo( (b__2 = b) ) < 0) ? a__1 : b__2;
	}

	@ReturnsAdjustedValue
	BigDecimal op_MAX( BigDecimal a, BigDecimal b )
	{
		BigDecimal a__1, b__2;
		return ((a__1 = a).compareTo( (b__2 = b) ) > 0) ? a__1 : b__2;
	}

	BigDecimal op_AND( BigDecimal a, BigDecimal b )
	{
		return (a.compareTo( BigDecimal.ZERO ) != 0) && (b.compareTo( BigDecimal.ZERO ) != 0) ? BigDecimal.ONE
				: BigDecimal.ZERO;
	}

	BigDecimal op_OR( BigDecimal a, BigDecimal b )
	{
		return (a.compareTo( BigDecimal.ZERO ) != 0) || (b.compareTo( BigDecimal.ZERO ) != 0) ? BigDecimal.ONE
				: BigDecimal.ZERO;
	}


	// ------------------------------------------------ Numeric Functions


	@ReturnsAdjustedValue
	BigDecimal fun_ABS( BigDecimal a )
	{
		return a.abs();
	}

	BigDecimal fun_ROUND( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.fun_ROUND( a, b );
	}


	// ------------------------------------------------ Date Functions


	BigDecimal fun_TODAY()
	{
		return RuntimeBigDecimal_v1.fun_TODAY();
	}


}