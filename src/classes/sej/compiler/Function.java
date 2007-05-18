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
package sej.compiler;


/**
 * Lists all the functions supported by SEJ.
 * 
 * @author peo
 * 
 * @see sej.spreadsheet.SpreadsheetBuilder#fun(Function, sej.spreadsheet.SpreadsheetBuilder.ExprNode[])
 */
public enum Function {

	// Logic

	IF, NOT,

	// Math

	ABS, ROUND,

	// Combinatorics

	FACT, COMBIN,

	// Financials

	NPV, MIRR, IRR,

	// Dates

	TODAY {
		@Override
		public boolean isVolatile()
		{
			return true;
		}
	},

	// Lookup

	MATCH, INDEX,

	// String

	CONCATENATE, LEN, MID, LEFT, RIGHT, SUBSTITUTE, REPLACE, SEARCH, FIND, EXACT, LOWER, UPPER, PROPER,

	// Aggregators
	// Don't forget to update AGGREGATORS below!

	SUM, PRODUCT, MIN, MAX, COUNT, AVERAGE, VAR, VARP, AND, OR,

	// Database aggregators
	DSUM, DPRODUCT, DCOUNT, DMIN, DMAX;


	private static final Function[] AGGREGATORS = { SUM, PRODUCT, MIN, MAX, COUNT, AVERAGE, VAR, VARP, AND, OR };


	public String getName()
	{
		return toString();
	}

	public boolean isVolatile()
	{
		return false;
	}

	public static Function[] aggregators()
	{
		return AGGREGATORS;
	}

}