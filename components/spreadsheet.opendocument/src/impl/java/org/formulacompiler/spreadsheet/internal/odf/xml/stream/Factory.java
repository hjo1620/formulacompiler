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

package org.formulacompiler.spreadsheet.internal.odf.xml.stream;

import java.io.OutputStream;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * @author Vladimir Korenev
 */
class Factory
{
	public static XMLEventWriter createXMLEventWriter( final OutputStream _outputStream ) throws XMLStreamException
	{
		final XMLEventWriter writer;
		if (Boolean.getBoolean( "org.formulacompiler.xml.stream.XMLEventWriter.useDefault" )) {
			writer = XMLOutputFactory.newInstance().createXMLEventWriter( _outputStream, "UTF-8" );
		}
		else {
			writer = new XMLStreamEventWriter( XMLOutputFactory.newInstance().createXMLStreamWriter( _outputStream, "UTF-8" ) );
		}
		return writer;
	}
}
