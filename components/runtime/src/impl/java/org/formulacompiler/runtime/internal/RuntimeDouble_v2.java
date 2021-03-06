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

package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.NotAvailableException;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;


public final class RuntimeDouble_v2 extends Runtime_v2
{
	private static final double EXCEL_EPSILON = 0.0000001;
	private static final double[] POW10 = { 1E-10, 1E-9, 1E-8, 1E-7, 1E-6, 1E-5, 1E-4, 1E-3, 1E-2, 1E-1, 1, 1E+1, 1E+2,
			1E+3, 1E+4, 1E+5, 1E+6, 1E+7, 1E+8, 1E+9, 1E+10 };


	public static double max( final double a, final double b )
	{
		return a >= b ? a : b;
	}

	public static double min( final double a, final double b )
	{
		return a <= b ? a : b;
	}

	public static double fun_CEILING( final double _number, final double _significance )
	{
		final double a = _number / _significance;
		if (a < 0) {
			err_CEILING();
		}
		return roundUp( a ) * _significance;
	}

	public static double fun_CEILING_OOo( final double _number, final double _significance )
	{
		if ((_number > 0 && _significance < 0) || (_number < 0 && _significance > 0)) {
			err_CEILING();
		}
		final double s = Math.abs( _significance );
		final double a = _number / s;
		return Math.ceil( a ) * s;
	}

	public static double fun_FLOOR( final double _number, final double _significance )
	{
		final double a = _number / _significance;
		if (a < 0) {
			err_FLOOR();
		}
		return roundDown( a ) * _significance;
	}

	public static double fun_FLOOR_OOo( final double _number, final double _significance )
	{
		if ((_number > 0 && _significance < 0) || (_number < 0 && _significance > 0)) {
			err_CEILING();
		}
		final double s = Math.abs( _significance );
		final double a = _number / s;
		return Math.floor( a ) * s;
	}

	public static double fun_RAND()
	{
		return generator.nextDouble();
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- round
	public static double round( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		if (0 > _val) {
			return Math.ceil( _val * shift - 0.5 ) / shift;
		}
		else {
			return Math.floor( _val * shift + 0.5 ) / shift;
		}
	}
	// ---- round

	public static double fun_ROUNDDOWN( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundDown( _val * shift ) / shift;
	}

	public static double fun_ROUNDUP( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundUp( _val * shift ) / shift;
	}

	public static double fun_SINH( final double a )
	{
		return Math.sinh( a );
	}

	public static double fun_ACOSH( final double a )
	{
		return Math.log( a + Math.sqrt( a * a - 1 ) );
	}

	public static double fun_ASINH( final double a )
	{
		return Math.log( a + Math.sqrt( a * a + 1 ) );
	}

	public static double fun_ATANH( final double a )
	{
		return Math.log( (1 + a) / (1 - a) ) / 2;
	}

	public static double trunc( final double _val, final int _maxFrac )
	{
		final double shift = pow10( _maxFrac );
		return roundDown( _val * shift ) / shift;
	}

	private static double roundDown( final double _val )
	{
		return 0 > _val ? Math.ceil( _val ) : Math.floor( _val );
	}

	private static double roundUp( final double _val )
	{
		return 0 > _val ? Math.floor( _val ) : Math.ceil( _val );
	}

	private static double pow10( final int _exp )
	{
		return (_exp >= -10 && _exp <= 10) ? POW10[ _exp + 10 ] : Math.pow( 10, _exp );
	}

	public static boolean booleanFromNum( final double _val )
	{
		return (checkDouble( _val ) != 0);
	}

	public static double booleanToNum( final boolean _val )
	{
		return _val ? 1.0 : 0.0;
	}

	public static double numberToNum( final Number _num )
	{
		return (_num == null) ? 0.0 : _num.doubleValue();
	}


	public static double fromScaledLong( long _scaled, long _scalingFactor )
	{
		return ((double) _scaled) / ((double) _scalingFactor);
	}

	public static long toScaledLong( double _value, long _scalingFactor )
	{
		return (long) (_value * _scalingFactor);
	}


	public static String toExcelString( double _value, Environment _environment )
	{
		return stringFromBigDecimal( BigDecimal.valueOf( _value ), _environment );
	}

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	/**
	 * @deprecated replaced by {@link #dateFromNum(double,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static Date dateFromNum( final double _excel, TimeZone _timeZone )
	{
		return dateFromNum( _excel, _timeZone, ComputationMode.EXCEL );
	}

	public static Date dateFromNum( final double _date, TimeZone _timeZone, ComputationMode _mode )
	{
		return dateFromDouble( _date, _timeZone, _mode == ComputationMode.EXCEL );
	}

	/**
	 * @deprecated replaced by {@link #msSinceUTC1970FromNum(double,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static long msSinceUTC1970FromNum( double _msSinceUTC1970, TimeZone _timeZone )
	{
		return msSinceUTC1970FromNum( _msSinceUTC1970, _timeZone, ComputationMode.EXCEL );
	}

	public static long msSinceUTC1970FromNum( double _msSinceUTC1970, TimeZone _timeZone, ComputationMode _mode )
	{
		return msSinceUTC1970FromDouble( _msSinceUTC1970, _timeZone, _mode == ComputationMode.EXCEL );
	}

	public static long msFromNum( double _msSinceUTC1970 )
	{
		return msFromDouble( _msSinceUTC1970 );
	}

	/**
	 * @deprecated replaced by {@link #dateToNum(Date,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static double dateToNum( final Date _date, TimeZone _timeZone )
	{
		return dateToNum( _date, _timeZone, ComputationMode.EXCEL );
	}

	public static double dateToNum( final Date _date, TimeZone _timeZone, ComputationMode _mode )
	{
		return dateToDouble( _date, _timeZone, _mode == ComputationMode.EXCEL );
	}

	/**
	 * @deprecated replaced by {@link #msSinceUTC1970ToNum(long,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static double msSinceUTC1970ToNum( long _msSinceUTC1970, TimeZone _timeZone )
	{
		return msSinceUTC1970ToNum( _msSinceUTC1970, _timeZone, ComputationMode.EXCEL );
	}

	public static double msSinceUTC1970ToNum( long _msSinceUTC1970, TimeZone _timeZone, ComputationMode _mode )
	{
		return msSinceUTC1970ToDouble( _msSinceUTC1970, _timeZone, _mode == ComputationMode.EXCEL );
	}

	public static double msToNum( long _msSinceUTC1970 )
	{
		return msToDouble( _msSinceUTC1970 );
	}

	/**
	 * @deprecated replaced by {@link #fun_DATE(int,int,int,ComputationMode)}
	 */
	@Deprecated
	public static double fun_DATE( final int _year, final int _month, final int _day )
	{
		return fun_DATE( _year, _month, _day, ComputationMode.EXCEL );
	}

	public static double fun_DATE( final int _year, final int _month, final int _day, ComputationMode _mode )
	{
		final int year;
		switch (_mode) {
			case EXCEL:
				year = _year < 1899 ? _year + 1900 : _year;
				break;
			default:
				year = _year >= 100 ? _year : _year >= 30 ? _year + 1900 : _year + 2000;
		}
		return dateToNum( year, _month, _day, false );
	}

	static double dateToNum( final int _year, final int _month, final int _day, boolean _excelCompatible )
	{
		final Calendar calendar = new GregorianCalendar( GMT_TIME_ZONE );
		calendar.clear();
		calendar.setLenient( true );
		calendar.set( Calendar.YEAR, _year );
		calendar.set( Calendar.MONTH, _month - 1 );
		calendar.set( Calendar.DAY_OF_MONTH, _day );
		final Date date = calendar.getTime();
		final TimeZone timeZone = calendar.getTimeZone();
		return dateToDouble( date, timeZone, _excelCompatible );
	}

	/**
	 * @deprecated replaced by {@link #fun_WEEKDAY(double,int,ComputationMode)}
	 */
	@Deprecated
	public static int fun_WEEKDAY( final double _date, int _type )
	{
		return fun_WEEKDAY( _date, _type, ComputationMode.EXCEL );
	}

	public static int fun_WEEKDAY( final double _date, int _type, ComputationMode _mode )
	{
		final int dayOfWeek = getCalendarValueFromNum( _date, Calendar.DAY_OF_WEEK, _mode );
		int type = (_type < 1 || _type > 3) && _mode == ComputationMode.OPEN_OFFICE_CALC ? 3 : _type;
		switch (type) {
			case 1:
				return dayOfWeek;
			case 2:
				return dayOfWeek > 1 ? dayOfWeek - 1 : 7;
			case 3:
				return dayOfWeek > 1 ? dayOfWeek - 2 : 6;
			default:
				throw new FormulaException( "#NUM! because of illegal argument _type in WEEKDAY" );
		}
	}

	public static int fun_WORKDAY( double _startDate, double _days, double[] _holidays, ComputationMode _mode )
	{
		int days = (int) _days;
		int actDate = (int) _startDate;
		if (days == 0) {
			return actDate;
		}
		final Set<Integer> holidays = New.set();
		for (double _holiday : _holidays) {
			final int holiday = (int) _holiday;
			if (holiday != 0) {
				holidays.add( holiday );
			}
		}

		if (days > 0) {
			if (fun_WEEKDAY( actDate, 3, _mode ) == 5) {
				// when starting on Saturday, assuming we're starting on Sunday to get the jump over the weekend
				actDate++;
			}
			while (days != 0) {
				actDate++;

				if (fun_WEEKDAY( actDate, 3, _mode ) < 5) {
					if (!holidays.contains( actDate ))
						days--;
				}
				else {
					actDate++; // jump over weekend
				}
			}
		}
		else {
			if (fun_WEEKDAY( actDate, 3, _mode ) == 6)
				// when starting on Sunday, assuming we're starting on Saturday to get the jump over the weekend
				actDate--;

			while (days != 0) {
				actDate--;

				if (fun_WEEKDAY( actDate, 3, _mode ) < 5) {
					if (!holidays.contains( actDate ))
						days++;
				}
				else {
					actDate--; // jump over weekend
				}
			}
		}
		return actDate;
	}

	static long getDaySecondsFromNum( final double _time )
	{
		final double time = _time % 1;
		return Math.round( time * SECS_PER_DAY );
	}

	/**
	 * @deprecated replaced by {@link #fun_DAY(double,ComputationMode)}
	 */
	@Deprecated
	public static int fun_DAY( final double _date )
	{
		return fun_DAY( _date, ComputationMode.EXCEL );
	}

	public static int fun_DAY( final double _date, ComputationMode _mode )
	{
		return getCalendarValueFromNum( _date, Calendar.DAY_OF_MONTH, _mode );
	}

	/**
	 * @deprecated replaced by {@link #fun_DAYS360(double,double,boolean,ComputationMode)}
	 */
	@Deprecated
	public static double fun_DAYS360( double _start_date, double _end_date, boolean _method )
	{
		return fun_DAYS360( _start_date, _end_date, _method, ComputationMode.EXCEL );
	}

	public static double fun_DAYS360( double _start_date, double _end_date, boolean _method, ComputationMode _mode )
	{
		final int date_days1, date_days2, sign;
		if (_start_date <= _end_date) {
			date_days1 = (int) _start_date;
			date_days2 = (int) _end_date;
			sign = 1;
		}
		else {
			date_days1 = (int) _end_date;
			date_days2 = (int) _start_date;
			sign = -1;
		}
		final GregorianCalendar date1 = getGregorianCalendarInstanceFromNum( date_days1, _mode );
		final GregorianCalendar date2 = getGregorianCalendarInstanceFromNum( date_days2, _mode );
		int day1 = date1.get( Calendar.DAY_OF_MONTH );
		int day2 = date2.get( Calendar.DAY_OF_MONTH );
		if (day1 == 31) {
			day1 -= 1;
		}
		else if (!_method && date1.get( Calendar.MONTH ) == Calendar.FEBRUARY) {
			if (day1 == 29 || (day1 == 28 && !date1.isLeapYear( date1.get( Calendar.YEAR ) ))) {
				day1 = 30;
			}
		}
		if (day2 == 31) {
			if (!_method) {
				if (day1 == 30) {
					day2 -= 1;
				}
			}
			else {
				day2 = 30;
			}
		}
		return sign
				* ((date2.get( Calendar.YEAR ) - date1.get( Calendar.YEAR ))
						* 360 + (date2.get( Calendar.MONTH ) - date1.get( Calendar.MONTH )) * 30 + day2 - day1);
	}

	/**
	 * @deprecated replaced by {@link #fun_MONTH(double,ComputationMode)}
	 */
	@Deprecated
	public static int fun_MONTH( final double _date )
	{
		return fun_MONTH( _date, ComputationMode.EXCEL );
	}

	public static int fun_MONTH( final double _date, ComputationMode _mode )
	{
		return getCalendarValueFromNum( _date, Calendar.MONTH, _mode ) + 1;
	}

	/**
	 * @deprecated replaced by {@link #fun_YEAR(double,ComputationMode)}
	 */
	@Deprecated
	public static int fun_YEAR( final double _date )
	{
		return fun_YEAR( _date, ComputationMode.EXCEL );
	}

	public static int fun_YEAR( final double _date, ComputationMode _mode )
	{
		return getCalendarValueFromNum( _date, Calendar.YEAR, _mode );
	}

	public static double fun_YEARFRAC( double _start_date, double _end_date, int _basis, ComputationMode _mode )
	{
		final int date_days1;
		final int date_days2;
		if (_start_date <= _end_date) {
			date_days1 = (int) _start_date;
			date_days2 = (int) _end_date;
		}
		else {
			date_days1 = (int) _end_date;
			date_days2 = (int) _start_date;
		}
		final GregorianCalendar date1 = getGregorianCalendarInstanceFromNum( date_days1, _mode );
		final GregorianCalendar date2 = getGregorianCalendarInstanceFromNum( date_days2, _mode );
		switch (_basis) {
			case 0: { // 0=USA (NASD) 30/360
				double daysCount = fun_DAYS360( date_days1, date_days2, false, _mode );
				// FIX different work of YEARFRAC and DAYS360 functions for USA (NASD) basis
				if (_mode == ComputationMode.EXCEL && date2.get( Calendar.DAY_OF_MONTH ) == 31) {
					final int day = date1.get( Calendar.DAY_OF_MONTH );
					if (date1.get( Calendar.MONTH ) == Calendar.FEBRUARY
							&& (day == 29 || (day == 28 && !date1.isLeapYear( date1.get( Calendar.YEAR ) )))) {
						daysCount++;
					}
				}
				return daysCount / 360;
			}
			case 4: // 4=Europe 30/360
				return fun_DAYS360( date_days1, date_days2, true, _mode ) / 360;

			case 1: { // 1=exact/exact
				final int year1 = date1.get( Calendar.YEAR );
				final int year2 = date2.get( Calendar.YEAR );
				int years = year2 - year1;

				if (_mode == ComputationMode.EXCEL) {
					int leapYears = 0;
					for (int year = year1; year <= year2; year++) {
						if (date1.isLeapYear( year )) {
							leapYears++;
						}
					}
					final double yearCoef = (double) leapYears / (years + 1);
					final double daysInYearAverage = 365 + yearCoef;
					final double daysCount = date_days2 - date_days1;
					return daysCount / daysInYearAverage;
				}
				else {
					final int month1 = date1.get( Calendar.MONTH );
					final int month2 = date2.get( Calendar.MONTH );
					final int day1 = date1.get( Calendar.DAY_OF_MONTH );
					final int day2 = date2.get( Calendar.DAY_OF_MONTH );
					final double daysInYear = date1.isLeapYear( year1 ) ? 366 : 365;
					if (years > 0 && (month1 > month2 || (month1 == month2 && day1 > day2))) {
						years--;
					}
					double dayDiff;
					if (years > 0) {
						dayDiff = (int) (date_days2 - fun_DATE( year2, month1 + 1, day1, _mode ));
					}
					else {
						dayDiff = date_days2 - date_days1;
					}
					if (dayDiff < 0) {
						dayDiff += daysInYear;
					}
					return years + dayDiff / daysInYear;
				}
			}
			case 2: { // 2=exact/360
				final double daysCount = date_days2 - date_days1;
				return daysCount / 360;
			}
			case 3: { //3=exact/365
				final double daysCount = date_days2 - date_days1;
				return daysCount / 365;
			}
			default:
				fun_ERROR( "#NUM! Invalid basis in YEARFRAC" );
				// Can not be accessed. Only for compiler.
				return 0;
		}
	}

	private static int getCalendarValueFromNum( double _date, int _field, ComputationMode _mode )
	{
		final Calendar calendar = getGregorianCalendarInstanceFromNum( _date, _mode );
		return calendar.get( _field );
	}

	private static GregorianCalendar getGregorianCalendarInstanceFromNum( double _date, ComputationMode _mode )
	{
		final GregorianCalendar calendar = new GregorianCalendar( GMT_TIME_ZONE );
		final TimeZone timeZone = calendar.getTimeZone();
		final Date date = dateFromNum( _date, timeZone, _mode );
		calendar.setTime( date );
		return calendar;
	}

	/**
	 * @deprecated replaced by {@link #fun_NOW(Environment,ComputationTime,ComputationMode)}
	 */
	@Deprecated
	public static double fun_NOW( final Environment _environment, final ComputationTime _computationTime )
	{
		return fun_NOW( _environment, _computationTime, ComputationMode.EXCEL );
	}

	public static double fun_NOW( Environment _environment, ComputationTime _computationTime, ComputationMode _mode )
	{
		return dateToNum( now( _computationTime ), _environment.timeZone(), _mode );
	}

	/**
	 * @deprecated replaced by {@link #fun_TODAY(Environment,ComputationTime,ComputationMode)}
	 */
	@Deprecated
	public static double fun_TODAY( final Environment _environment, final ComputationTime _computationTime )
	{
		return fun_TODAY( _environment, _computationTime, ComputationMode.EXCEL );
	}

	public static double fun_TODAY( Environment _environment, ComputationTime _computationTime, ComputationMode _mode )
	{
		final TimeZone timeZone = _environment.timeZone();
		return dateToNum( today( timeZone, _computationTime ), timeZone, _mode );
	}

	public static double fun_TIME( double _hour, double _minute, double _second )
	{
		final long seconds = ((long) _hour * SECS_PER_HOUR + (long) _minute * 60 + (long) _second) % SECS_PER_DAY;
		return (double) seconds / SECS_PER_DAY;
	}

	public static double fun_TIME_OOo( double _hour, double _minute, double _second )
	{
		final long seconds = ((long) _hour * SECS_PER_HOUR + (long) _minute * 60 + (long) _second);
		return (double) seconds / SECS_PER_DAY;
	}

	public static double fun_SECOND( double _date )
	{
		final long seconds = getDaySecondsFromNum( _date ) % 60;
		return seconds;
	}

	public static double fun_MINUTE( double _date )
	{
		final long minutes = getDaySecondsFromNum( _date ) / 60 % 60;
		return minutes;
	}

	public static double fun_HOUR( double _date )
	{
		final long hours = getDaySecondsFromNum( _date ) / SECS_PER_HOUR % 24;
		return hours;
	}

	private static double mulRange( int m, int n )
	{
		double res = 1;
		for (int i = m + 1; i <= n; i++) {
			res *= i;
		}
		return res;
	}

	private static class Segment
	{
		int start;
		int end;

		private Segment( final int _start, final int _end )
		{
			this.start = _start;
			this.end = _end;
		}
	}

	public static double fun_HYPGEOMDIST( int _x, int _n, int _M, int _N )
	{
		if ((_x < 0) || (_n < _x) || (_M < _x) || (_N < _n) || (_N < _M) || (_x < _n - _N + _M)) {
			// Illegal Argument
			fun_ERROR( "#NUM! because of illegal arguments in BETADIST" );
		}
		if (_N < 100) {
			// simple method which works for small numbers
			return mulRange( _M - _x, _M )
					* mulRange( _n - _x, _n ) / mulRange( _N - _M, _N ) * mulRange( _N - _n - _M + _x, _N - _n )
					/ mulRange( 1, _x );
		}
		else {
			// algorithm for large numbers
			LinkedList<Segment> numerator = new LinkedList<Segment>();
			LinkedList<Segment> denominator = new LinkedList<Segment>();
			numerator.add( new Segment( _M - _x + 1, _M ) );
			numerator.add( new Segment( _n - _x + 1, _n ) );
			numerator.add( new Segment( _N - _n - _M + _x + 1, _N - _n ) );
			denominator.add( new Segment( 1, _x ) );
			denominator.add( new Segment( _N - _M + 1, _N ) );
			for (int i = 0; i < numerator.size(); i++) {
				for (int j = 0; j < denominator.size(); j++) {
					if (i < 0) i++;
					Segment numeratorElement = numerator.get( i );
					Segment denominatorElement = denominator.get( j );
					if (numeratorElement.start > denominatorElement.start) {
						if (numeratorElement.start <= denominatorElement.end) {
							if (numeratorElement.end > denominatorElement.end) {
								int tmp = numeratorElement.start;
								numeratorElement.start = denominatorElement.end + 1;
								denominatorElement.end = tmp - 1;
							}
							else {
								if (numeratorElement.end < denominatorElement.end) {
									denominator.add( new Segment( numeratorElement.end + 1, denominatorElement.end ) );
								}
								denominatorElement.end = numeratorElement.start - 1;
								numerator.remove( i );
								i--;
							}
						}
					}
					else {
						if (numeratorElement.start < denominatorElement.start) {
							if (numeratorElement.end >= denominatorElement.start) {
								if (numeratorElement.end < denominatorElement.end) {
									int tmp = denominatorElement.start;
									denominatorElement.start = numeratorElement.end + 1;
									numeratorElement.end = tmp - 1;
								}
								else {
									if (numeratorElement.end > denominatorElement.end) {
										numerator.add( new Segment( denominatorElement.end + 1, numeratorElement.end ) );
									}
									numeratorElement.end = denominatorElement.start - 1;
									denominator.remove( j );
									j--;
								}
							}
						}
						else {
							if (numeratorElement.end < denominatorElement.end) {
								denominatorElement.start = numeratorElement.end + 1;
								numerator.remove( i );
								i--;
							}
							else {
								if (numeratorElement.end > denominatorElement.end) {
									numeratorElement.start = denominatorElement.end + 1;
									denominator.remove( j );
									j--;
								}
								else {
									denominator.remove( j );
									j--;
									numerator.remove( i );
									i--;
								}
							}
						}
					}
				}
			}
			double res = 1;
			int numeratorIndex = 0;
			int numeratorCurrMult = 0;
			int denominatorIndex = 0;
			int denominatorCurrMult = 0;
			double upperLimit = 1E+250;
			double lowerLimit = 1E-250;
			while (numeratorIndex < numerator.size() || denominatorIndex < denominator.size()) {
				if ((res >= upperLimit & denominatorIndex >= denominator.size())
						|| (res <= lowerLimit & numeratorIndex >= numerator.size())) {
					res = 0;
					numeratorIndex = numerator.size();
					denominatorIndex = denominator.size();
				}
				while (numeratorIndex < numerator.size() & res < upperLimit) {
					Segment numeratorElement = numerator.get( numeratorIndex );
					if (numeratorCurrMult == 0) {
						numeratorCurrMult = numeratorElement.start;
					}
					while (numeratorCurrMult <= numeratorElement.end & res < upperLimit) {
						res *= numeratorCurrMult;
						numeratorCurrMult++;
					}
					if (numeratorCurrMult > numeratorElement.end) {
						numeratorIndex++;
						numeratorCurrMult = 0;
					}
				}
				while (denominatorIndex < denominator.size() & res > lowerLimit) {
					Segment denominatorElement = denominator.get( denominatorIndex );
					if (denominatorCurrMult == 0) {
						denominatorCurrMult = denominatorElement.start;
					}
					while (denominatorCurrMult <= denominatorElement.end & res > lowerLimit) {
						res /= denominatorCurrMult;
						denominatorCurrMult++;
					}
					if (denominatorCurrMult > denominatorElement.end) {
						denominatorIndex++;
						denominatorCurrMult = 0;
					}
				}
			}
			return res;
		}
	}

	public static double fun_ACOS( double _a )
	{
		return Math.acos( _a );
	}

	public static double fun_TRUNC( final double _val )
	{
		return roundDown( _val );
	}

	public static double fun_EVEN( final double _val )
	{
		return roundUp( _val / 2 ) * 2;
	}

	public static double fun_ODD( final double _val )
	{
		if (0 > _val) {
			return Math.floor( (_val - 1) / 2 ) * 2 + 1;
		}
		else {
			return Math.ceil( (_val + 1) / 2 ) * 2 - 1;
		}
	}

	public static double fun_POWER( final double _n, final double _p )
	{
		return Math.pow( _n, _p );
	}

	public static double fun_LN( final double _p )
	{
		return Math.log( _p );
	}

	public static double fun_LOG( final double _n, final double _x )
	{
		return Math.log( _n ) / Math.log( _x );
	}

	public static double fun_LOG10( final double _p )
	{
		return Math.log10( _p );
	}

	public static double fun_ERF( double x )
	{
		return Probability.errorFunction( x );
	}

	public static double fun_ERFC( double a )
	{
		return Probability.errorFunctionComplemented( a );
	}

	public static double fun_BETADIST( double _x, double _alpha, double _beta )
	{
		if (_alpha <= 0 || _beta <= 0 || _x < 0 || _x > 1) {
			fun_ERROR( "#NUM! because of not alpha > 0, beta > 0, 0 <= x <= 1 in BETADIST" );
		}

		return Probability.beta( _alpha, _beta, _x );
	}

	public static double fun_BINOMDIST( int _successes, int _trials, double _probability, boolean _cumulative )
	{
		if (_successes < 0 || _successes > _trials || _probability < 0 || _probability > 1) {
			fun_ERROR( "#NUM! because of not 0 <= s <= t, 0 <= p <= 1 in BINOMDIST" );
		}

		if (_cumulative) {
			return binomialCumulative( _successes, _trials, _probability );
		}
		else {
			return binomialDensity( _successes, _trials, _probability );
		}
	}

	private static double binomialCumulative( final int _successes, final int _trials, final double _probability )
	{
		return Probability.binomial( _successes, _trials, _probability );
	}

	private static double binomialDensity( final int _successes, final int _trials, final double _probability )
	{
		final double q = 1.0 - _probability;
		double factor = Math.pow( q, _trials );
		if (factor == 0.0) {
			factor = Math.pow( _probability, _trials );
			if (factor == 0.0) {
				throw new FormulaException( "#NUM! because both factors = 0 in binomialDensity" );
			}
			else {
				final int max = _trials - _successes;
				for (int i = 0; i < max && factor > 0.0; i++) {
					factor *= ((double) (_trials - i)) / ((double) (i + 1)) * q / _probability;
				}
				return factor;

			}
		}
		else {
			for (int i = 0; i < _successes && factor > 0.0; i++) {
				factor *= ((double) (_trials - i)) / ((double) (i + 1)) * _probability / q;
			}
			return factor;
		}
	}

	private interface StatisticDistFunc
	{
		double apply( double x );
	}

	private static class BetaDistFunction implements StatisticDistFunc
	{
		private double x0;
		private double alpha;
		private double beta;

		BetaDistFunction( double x0, double alpha, double beta )
		{
			this.x0 = x0;
			this.alpha = alpha;
			this.beta = beta;
		}

		public double apply( double x )
		{
			return this.x0 - fun_BETADIST( x, this.alpha, this.beta );
		}
	}

	/**
	 * Iteration for inverse distributions.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr3_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 *
	 * @param func distribution function.
	 * @param _x0  lower bound.
	 * @param _x1  upper bound.
	 */
	private static double iterateInverse( StatisticDistFunc func, double _x0, double _x1 )
			throws IllegalArgumentException, ArithmeticException
	{
		double x0 = _x0;
		double x1 = _x1;
		if (x0 >= x1) {
			throw new IllegalArgumentException( "Wrong interval" );
		}
		double fEps = 1E-7;

		// find enclosing interval
		double f0 = func.apply( x0 );
		double f1 = func.apply( x1 );
		double xs;
		int i;
		for (i = 0; i < 1000 & f0 * f1 > 0; i++) {
			if (Math.abs( f0 ) <= Math.abs( f1 )) {
				xs = x0;
				x0 += 2 * (x0 - x1);
				if (x0 < 0) x0 = 0;
				x1 = xs;
				f1 = f0;
				f0 = func.apply( x0 );
			}
			else {
				xs = x1;
				x1 += 2 * (x1 - x0);
				x0 = xs;
				f0 = f1;
				f1 = func.apply( x1 );
			}
		}
		if (f0 == 0) return x0;
		if (f1 == 0) return x1;
		// simple iteration
		double x00 = x0;
		double x11 = x1;
		double fs = func.apply( 0.5 * (x0 + x1) );
		for (i = 0; i < 100; i++) {
			xs = 0.5 * (x0 + x1);
			if (Math.abs( f1 - f0 ) >= fEps) {
				fs = func.apply( xs );
				if (f0 * fs <= 0) {
					x1 = xs;
					f1 = fs;
				}
				else {
					x0 = xs;
					f0 = fs;
				}
			}
			else {
				// add one step of regula falsi to improve precision
				if (x0 != x1) {
					double regxs = (f1 - f0) / (x1 - x0);
					if (regxs != 0) {
						double regx = x1 - f1 / regxs;
						if (regx >= x00 && regx <= x11) {
							double regfs = func.apply( regx );
							if (Math.abs( regfs ) < Math.abs( fs )) xs = regx;
						}
					}
				}
				return xs;
			}
		}
		throw new ArithmeticException();
	}

	public static double fun_BETAINV( double _x, double _alpha, double _beta )
	{
		if (_x <= 0 || _x >= 1 || _alpha <= 0 || _beta <= 0) {
			fun_ERROR( "#NUM! because not 0 < x < 1, alpha > 0, beta > 0 in BETAINV" );
		}
		final BetaDistFunction func = new BetaDistFunction( _x, _alpha, _beta );
		return iterateInverse( func, 0, 1 );
	}

	public static double fun_CHIDIST( double _x, double _degFreedom )
	{
		if (_x < 0 || _degFreedom < 1) {
			fun_ERROR( "#NUM! because x < 0 or degree < 1 in CHIDIST" );
		}

		return Probability.chiSquareComplemented( Math.floor( _degFreedom ), _x );
	}

	private static class ChiDistFunction implements StatisticDistFunc
	{
		private double x0;
		private double degrees;

		ChiDistFunction( double x0, double degrees )
		{
			this.x0 = x0;
			this.degrees = degrees;
		}

		public double apply( double x )
		{
			return this.x0 - fun_CHIDIST( x, this.degrees );
		}
	}

	public static double fun_CHIINV( double _x, double _degFreedom )
	{
		if (_x <= 0 || _x > 1 || _degFreedom < 1 || _degFreedom > 10000000000.0) {
			fun_ERROR( "#NUM! because not 0 < x <= 1, 1 <= degrees <= 10000000000 in CHIINV" );
		}
		ChiDistFunction func = new ChiDistFunction( _x, _degFreedom );
		double res = iterateInverse( func, _degFreedom / 2, _degFreedom );
		return res;
	}

	/**
	 * Computes CRITBINOM function.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr3_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	public static double fun_CRITBINOM( double _n, double _p, double _alpha )
	{
		// p <= 0 is contrary to Excel's docs where it says p < 0; but the test case says otherwise.
		if (_n < 0 || _p < 0 || _p > 1 || _alpha <= 0 || _alpha >= 1) {
			fun_ERROR( "#NUM! because not n >= 0, 0 <= p <= 1, 0 < alpha < 1 in CRITBINOM" );
		}
		double n = Math.floor( _n );
		double q = 1 - _p;
		double factor = Math.pow( q, n );
		if (factor == 0) {
			factor = Math.pow( _p, n );
			if (factor == 0) {
				throw new FormulaException( "#NUM! because factor = 0 in CRITBINOM" );
			}
			else {
				double sum = 1 - factor;
				int i;
				for (i = 0; i < n && sum >= _alpha; i++) {
					factor *= (n - i) / (i + 1) * q / _p;
					sum -= factor;
				}
				return n - i;
			}
		}
		else {
			double sum = factor;
			int i;
			for (i = 0; i < n && sum < _alpha; i++) {
				factor *= (n - i) / (i + 1) * _p / q;
				sum += factor;
			}
			return i;
		}
	}

	public static double getFDist( double x, double f1, double f2 )
	{
		double arg = f2 / (f2 + f1 * x);
		double alpha = f2 / 2.0;
		double beta = f1 / 2.0;
		return (fun_BETADIST( arg, alpha, beta ));
	}

	private static class FDistFunction implements StatisticDistFunc
	{
		private double p;
		private double f1;
		private double f2;

		FDistFunction( double p, double f1, double f2 )
		{
			this.p = p;
			this.f1 = f1;
			this.f2 = f2;
		}

		public double apply( double x )
		{
			return this.p - getFDist( x, this.f1, this.f2 );
		}
	}

	public static double fun_FINV( double _p, double _f1, double _f2 )
	{
		if (_p < 0 || _f1 < 1 || _f2 < 1 || _f1 >= 1.0E10 || _f2 >= 1.0E10 || _p > 1) {
			fun_ERROR( "#NUM! because p < 0 or not 1 <= f{1,2} < 1e10  in CHIINV" );
		}
		if (_p == 0) {
			return 1000000000;
		}
		double f1 = Math.floor( _f1 );
		double f2 = Math.floor( _f2 );
		FDistFunction func = new FDistFunction( _p, f1, f2 );
		return iterateInverse( func, f1 / 2, f1 );
	}

	private static class GammaDistFunction implements StatisticDistFunc
	{
		private double p;
		private double alpha;
		private double beta;

		GammaDistFunction( double p, double alpha, double beta )
		{
			this.p = p;
			this.alpha = alpha;
			this.beta = beta;
		}

		public double apply( double x )
		{
			return this.p - gammaCumulative( x, this.alpha, this.beta );
		}
	}

	public static double fun_GAMMAINV( double _p, double _alpha, double _beta )
	{
		if (_p < 0 || _p > 1 || _alpha <= 0 || _beta <= 0) {
			fun_ERROR( "#NUM! because not 0 <= p <= 1, alpha > 0, beta >0 in GAMMAINV" );
		}
		if (_p == 0) {
			return 0; // This is a correct result, not an error.
			// LATER Remove the above comment when the check for #NUM comments is done.
		}
		GammaDistFunction func = new GammaDistFunction( _p, _alpha, _beta );
		double start = _alpha * _beta;
		return iterateInverse( func, start / 2, start );
	}


	public static double fun_GAMMALN( double _x )
	{
		double x = _x;
		if (x <= 0) {
			fun_ERROR( "#NUM! because x <= 0 in GAMMALN" );
		}
		boolean bReflect;
		double[] c = { 76.18009173, -86.50532033, 24.01409822, -1.231739516, 0.120858003E-2, -0.536382E-5 };
		if (x >= 1) {
			bReflect = false;
			x -= 1;
		}
		else {
			bReflect = true;
			x = 1 - x;
		}
		double g = 1.0;
		double anum = x;
		for (int i = 0; i < 6; i++) {
			anum += 1.0;
			g += c[ i ] / anum;
		}
		g *= 2.506628275; // sqrt(2*PI)
		g = (x + 0.5) * Math.log( x + 5.5 ) + Math.log( g ) - (x + 5.5);
		if (bReflect) g = Math.log( Math.PI * x ) - g - Math.log( Math.sin( Math.PI * x ) );
		return g;
	}

	public static double fun_GAMMADIST( double _x, double _alpha, double _beta, boolean _cumulative )
	{
		if (_x < 0 || _alpha <= 0 || _beta <= 0) {
			fun_ERROR( "#NUM! because x < 0 or alpha <= 0 or beta <= 0 in GAMMADIST" );
		}

		if (_cumulative) {
			return gammaCumulative( _x, _alpha, _beta );
		}
		else {
			return gammaDensity( _x, _alpha, _beta );
		}
	}

	private static double gammaCumulative( double _x, double _alpha, double _beta )
	{
		return Probability.gamma( 1 / _beta, _alpha, _x );
	}

	private static double gammaDensity( double _x, double _alpha, double _beta )
	{
		return Math.pow( _x, _alpha - 1 ) * Math.exp( -_x / _beta ) / (Math.pow( _beta, _alpha ) * Gamma.gamma( _alpha ));
	}

	public static double fun_POISSON( int _x, double _mean, boolean _cumulative )
	{
		if (_x < 0 || _mean < 0) {
			fun_ERROR( "#NUM! because x < 0 or mean < 0 in POISSON" );
		}

		if (_cumulative) {
			return poissonCumulative( _x, _mean );
		}
		else {
			return poissonDensity( _x, _mean );
		}
	}

	private static double poissonCumulative( final int _x, final double _mean )
	{
		return Probability.poisson( _x, _mean );
	}

	private static double poissonDensity( final int _x, final double _mean )
	{
		return Math.exp( -_mean ) * Math.pow( _mean, _x ) / factorial( _x );
	}

	public static double fun_TDIST( double _x, double _degFreedom, int _tails, boolean no_floor )
	{
		if (_x < 0 || _degFreedom < 1 || (_tails != 1 && _tails != 2)) {
			fun_ERROR( "#NUM! because x < 0 or degrees < 1 or not tails in {1, 2} in TDIST" );
		}

		if (no_floor) {
			return (1 - Probability.studentT( _degFreedom, _x )) * _tails;
		}
		else {
			return (1 - Probability.studentT( Math.floor( _degFreedom ), _x )) * _tails;
		}
	}

	private static double getTDist( double t, double f )
	{
		return 0.5 * fun_BETADIST( f / (f + t * t), f / 2, 0.5 );
	}

	private static class TDistFunction implements StatisticDistFunc
	{
		private double p;
		private double degFreedom;

		TDistFunction( double p, double degFreedom )
		{
			this.p = p;
			this.degFreedom = degFreedom;
		}

		public double apply( double x )
		{
			return this.p - getTDist( x, this.degFreedom ) * 2;
		}
	}

	public static double fun_TINV( double _x, double _degFreedom )
	{
		if (_degFreedom < 1 || _degFreedom >= 1.0E5 || _x < 0 || _x > 1) {
			fun_ERROR( "#NUM! because not 0 <= x <= 1, 1 <= degrees < 1e5 in TINV" );
		}
		StatisticDistFunc func = new TDistFunction( _x, _degFreedom );
		return iterateInverse( func, _degFreedom / 2, _degFreedom );
	}

	public static double fun_WEIBULL( double _x, double _alpha, double _beta, boolean _cumulative )
	{
		if (_x < 0 || _alpha <= 0 || _beta <= 0) {
			fun_ERROR( "#NUM! because x < 0 or alpha <= 0 or beta <= 0 in WEIBULL" );
		}

		if (_cumulative) {
			return 1.0 - Math.exp( -Math.pow( _x / _beta, _alpha ) );
		}
		else {
			return _alpha
					/ Math.pow( _beta, _alpha ) * Math.pow( _x, _alpha - 1 ) * Math.exp( -Math.pow( _x / _beta, _alpha ) );

		}
	}

	public static double fun_MOD( double _n, double _d )
	{
		final double remainder = _n % _d;
		if (remainder != 0 && Math.signum( remainder ) != Math.signum( _d )) {
			return remainder + _d;
		}
		else {
			return remainder;
		}
	}

	public static double fun_SQRT( double _n )
	{
		return Math.sqrt( _n );
	}

	public static double fun_FACT( double _a )
	{
		if (_a < 0.0) {
			err_FACT();
		}
		int a = (int) _a;
		return factorial( a );
	}

	private static double factorial( int _a )
	{
		if (_a < 0) {
			throw new IllegalArgumentException( "number < 0" );
		}

		if (_a < FACTORIALS.length) {
			return FACTORIALS[ _a ];
		}
		else {
			int i = FACTORIALS.length;
			double r = FACTORIALS[ i - 1 ];
			while (i <= _a) {
				r *= i++;
			}
			return r;
		}
	}


	public static double fun_MDETERM( double[] _squareMatrix, int _sideLength )
	{
		final DoubleFactory2D factory2D = DoubleFactory2D.dense;
		final DoubleMatrix2D matrix2D = factory2D.make( _squareMatrix, _sideLength );
		return Algebra.DEFAULT.det( matrix2D );
	}


	/**
	 * Computes IRR using Newton's method, where x[i+1] = x[i] - f( x[i] ) / f'( x[i] )
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr2_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	public static double fun_IRR( double[] _values, double _guess )
	{
		final int MAX_ITER = 20;

		double x = _guess;
		int iter = 0;
		while (iter++ < MAX_ITER) {

			final double x1 = 1.0 + x;
			double fx = 0.0;
			double dfx = 0.0;
			for (int i = 0; i < _values.length; i++) {
				final double v = _values[ i ];
				final double x1_i = Math.pow( x1, i );
				fx += v / x1_i;
				final double x1_i1 = x1_i * x1;
				dfx += -i * v / x1_i1;
			}
			final double new_x = x - fx / dfx;
			final double epsilon = Math.abs( new_x - x );

			if (epsilon <= EXCEL_EPSILON) {
				if (_guess == 0.0 && Math.abs( new_x ) <= EXCEL_EPSILON) {
					return 0.0; // OpenOffice calc does this
				}
				else {
					return new_x;
				}
			}
			x = new_x;
		}
		throw new FormulaException( "#NUM! because result not found in " + MAX_ITER + " tries in IRR" );
	}

	private static double xirrResultingAmount( double[] _values, int[] dates, double rate )
	{
		final double startDate = dates[ 0 ];
		final double r = rate + 1.0;
		double result = _values[ 0 ];
		for (int i = 1; i < _values.length; ++i) {
			result += _values[ i ] / Math.pow( r, (dates[ i ] - startDate) / 365 );
		}
		return result;
	}

	private static double xirrResultingAmountDerivation( double[] _values, int[] dates, double rate )
	{
		final double startDate = dates[ 0 ];
		final double r = rate + 1.0;
		double result = _values[ 0 ];
		for (int i = 1; i < _values.length; ++i) {
			final double datePeriod = (dates[ i ] - startDate) / 365;
			result -= datePeriod * _values[ i ] / Math.pow( r, datePeriod + 1 );
		}
		return result;
	}

	public static double fun_XIRR( double[] _values, double[] _dates, double _guess )
	{
		final int MAX_ITER = 50;
		final double MAX_EPS = 1E-10;

		final int[] dates = new int[_dates.length];
		for (int i = 0; i < _values.length; i++) {
			dates[ i ] = (int) _dates[ i ];
		}
		if (_values.length != dates.length) {
			fun_ERROR( "#NUM! because values and dates array sizes are different in XIRR" );
		}
		if (_values.length < 2) {
			fun_ERROR( "#N/A! because values and dates array are too short in XIRR" );
		}
		if (Math.abs( _guess ) >= 1) {
			fun_ERROR( "#NUM! incorrect guess value in XIRR" );
		}
		boolean negativeValue = false;
		boolean positiveValue = false;
		for (double value : _values) {
			if (value < 0) negativeValue = true;
			if (value > 0) positiveValue = true;
		}
		if (!(negativeValue && positiveValue)) {
			fun_ERROR( "#NUM! XIRR function expects at least one positive and one negative cash flow" );
		}

		double resultRate = _guess;
		int iter = 0;
		boolean continuousFlag;
		do {
			final double resultValue = xirrResultingAmount( _values, dates, resultRate );
			final double newRate = resultRate - resultValue / xirrResultingAmountDerivation( _values, dates, resultRate );
			final double rateEps = Math.abs( newRate - resultRate );
			resultRate = newRate;
			continuousFlag = (rateEps > MAX_EPS) && (Math.abs( resultValue ) > MAX_EPS);
		}
		while (continuousFlag && (++iter < MAX_ITER));
		if (continuousFlag) {
			throw new FormulaException( "#NUM! because result not found in " + MAX_ITER + " tries in XIRR" );
		}
		return resultRate;
	}

	/**
	 * Computes DB function.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr2_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	public static double fun_DB( double _cost, double _salvage, double _life, double _period, double _month )
	{
		final double month = Math.floor( _month );
		final double rate = round( 1 - Math.pow( _salvage / _cost, 1 / _life ), 3 );
		final double depreciation1 = _cost * rate * month / 12;
		double depreciation = depreciation1;
		if ((int) _period > 1) {
			double totalDepreciation = depreciation1;
			final int maxPeriod = (int) (_life > _period ? _period : _life);
			for (int i = 2; i <= maxPeriod; i++) {
				depreciation = (_cost - totalDepreciation) * rate;
				totalDepreciation += depreciation;
			}
			if (_period > _life) {
				depreciation = (_cost - totalDepreciation) * rate * (12 - month) / 12;
			}
		}
		return depreciation;
	}

	/**
	 * Computes DDB function.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr2_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	public static double fun_DDB( double _cost, double _salvage, double _life, double _period, double _factor )
	{
		final double remainingCost;
		final double newCost;
		final double k = 1 - _factor / _life;
		if (k <= 0) {
			remainingCost = _period == 1 ? _cost : 0;
			newCost = _period == 0 ? _cost : 0;
		}
		else {
			final double k_p1 = Math.pow( k, _period - 1 );
			final double k_p = k_p1 * k;
			remainingCost = _cost * k_p1;
			newCost = _cost * k_p;
		}

		double depreciation = remainingCost - (newCost < _salvage ? _salvage : newCost);
		if (depreciation < 0) {
			depreciation = 0;
		}
		return depreciation;
	}

	public static double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type, double _guess )
	{
		final int MAX_ITER = 50;
		final boolean type = _type != 0;
		double eps = 1;
		double rate0 = _guess;
		for (int count = 0; eps > EXCEL_EPSILON && count < MAX_ITER; count++) {
			final double rate1;
			if (rate0 == 0) {
				final double a = _pmt * _nper;
				final double b = a + (type ? _pmt : -_pmt);
				rate1 = rate0 - (_pv + _fv + a) / (_nper * (_pv + b / 2));
			}
			else {
				final double a = 1 + rate0;
				final double b = Math.pow( a, _nper - 1 );
				final double c = b * a;
				final double d = _pmt * (1 + (type ? rate0 : 0));
				final double e = rate0 * _nper * b;
				final double f = c - 1;
				final double g = rate0 * _pv;
				rate1 = rate0 * (1 - (g * c + d * f + rate0 * _fv) / (g * e - _pmt * f + d * e));
			}
			eps = Math.abs( rate1 - rate0 );
			rate0 = rate1;
		}
		if (eps >= EXCEL_EPSILON) {
			fun_ERROR( "#NUM! because of result do not converge to within "
					+ EXCEL_EPSILON + " after " + MAX_ITER + " iterations in RATE" );
		}
		return rate0;
	}

	/**
	 * Computes VDB function.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr2_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	public static double fun_VDB( double _cost, double _salvage, double _life, double _start_period, double _end_period,
			double _factor, boolean _no_switch )
	{
		double valVDB = 0;
		if (_start_period < 0 || _end_period < _start_period || _end_period > _life || _cost < 0 || _factor < 0) {
			fun_ERROR( "#NUM! because of illegal argument values in VDB" );
		}
		else {
			if (_salvage > _cost) {
				return 0; // correct result
			}
			int loopStart = (int) Math.floor( _start_period );
			int loopEnd = (int) Math.ceil( _end_period );
			if (_no_switch) {
				for (int i = loopStart + 1; i <= loopEnd; i++) {
					double valDDB = fun_DDB( _cost, _salvage, _life, i, _factor );
					if (i == loopStart + 1) {
						valDDB *= (_end_period < loopStart + 1 ? _end_period : loopStart + 1) - _start_period;
					}
					else if (i == loopEnd) {
						valDDB *= _end_period + 1 - loopEnd;
					}
					valVDB += valDDB;
				}
			}
			else {
				double _life2 = _life;
				double start = _start_period;
				double end = _end_period;
				double part;
				if (start != Math.floor( start )) {
					if (_factor > 1) {
						if (start >= _life / 2) {
							// this part works like in Open Office
							part = start - _life / 2;
							start = _life / 2;
							end -= part;
							_life2 += 1;
						}
					}
				}
				final double cost = _cost - interVDB( _cost, _salvage, _life, _life2, start, _factor );
				valVDB = interVDB( cost, _salvage, _life, _life - start, end - start, _factor );
			}
		}
		return valVDB;
	}

	/**
	 * Computes VDB helper function.
	 * Converted from <a href="http://docs.go-oo.org/sc/html/interpr2_8cxx-source.html">OpenOffice.org source</a>
	 * under the terms of the GNU Lesser General Public License version 3.
	 */
	private static double interVDB( double _cost, double _salvage, double _life, double _life2, double _period,
			double _factor )
	{
		double valVDB = 0;
		int loopEnd = (int) Math.ceil( _period );
		double salvageCost = _cost - _salvage;
		boolean flagSLN = false;
		double valTmpRes;
		double valSLN = 0;
		for (int i = 1; i <= loopEnd; i++) {
			if (!flagSLN) {
				double valDDB = fun_DDB( _cost, _salvage, _life, i, _factor );
				valSLN = salvageCost / (_life2 - i + 1);
				if (valSLN > valDDB) {
					valTmpRes = valSLN;
					flagSLN = true;
				}
				else {
					valTmpRes = valDDB;
					salvageCost -= valDDB;
				}
			}
			else {
				valTmpRes = valSLN;
			}
			if (i == loopEnd) valTmpRes *= (_period + 1 - loopEnd);

			valVDB += valTmpRes;
		}
		return valVDB;
	}

	/**
	 * @deprecated replaced by {@link #fun_VALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static double fun_VALUE( String _text, final Environment _environment )
	{
		return fun_VALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static double fun_VALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		return fromString( _text, _environment, _mode );
	}

	public static double fromString(  String _text,  Environment _environment,  ComputationMode _mode )
	{
		return stringToNumber( _text, _environment, _mode ).doubleValue();
	}

	/**
	 * @deprecated replaced by {@link #fun_DATEVALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static double fun_DATEVALUE( String _text, final Environment _environment )
	{
		return fun_DATEVALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static double fun_DATEVALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		final String text = _text.trim();
		try {
			final double date = parseDateAndOrTime( text, _environment, _mode == ComputationMode.EXCEL );
			return Math.floor( date );
		}
		catch (ParseException e) {
			throw new FormulaException( "#VALUE! because argument could not be interpreted properly in DATEVALUE" );
		}
	}

	/**
	 * @deprecated replaced by {@link #fun_TIMEVALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static double fun_TIMEVALUE( String _text, final Environment _environment )
	{
		return fun_TIMEVALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static double fun_TIMEVALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		final String text = _text.trim();
		try {
			double dataTime = parseDateAndOrTime( text, _environment, _mode == ComputationMode.EXCEL );
			return dataTime - Math.floor( dataTime );
		}
		catch (ParseException e) {
			throw new FormulaException( "#VALUE! because argument could not be interpreted properly in TIMEVALUE" );
		}
	}

	public static int fun_MATCH_Exact( double _x, double[] _xs )
	{
		for (int i = 0; i < _xs.length; i++) {
			if (_x == _xs[ i ]) return i + 1; // Excel is 1-based
		}
		throw new NotAvailableException();
	}

	public static int fun_MATCH_Ascending( double _x, double[] _xs )
	{
		if (_xs[ 0 ] > _x) throw new NotAvailableException();
		final int n = _xs.length;
		for (int i = 1; i <= n - 1; i++) {
			if (_xs[ i ] > _x) return i; // Excel is 1-based
		}
		return n; // Excel is 1-based
	}

	public static int fun_MATCH_Descending( double _x, double[] _xs )
	{
		if (_xs[ 0 ] < _x) throw new NotAvailableException();
		final int n = _xs.length;
		for (int i = 1; i <= n - 1; i++) {
			if (_xs[ i ] < _x) return i; // Excel is 1-based
		}
		return n; // Excel is 1-based
	}


}
