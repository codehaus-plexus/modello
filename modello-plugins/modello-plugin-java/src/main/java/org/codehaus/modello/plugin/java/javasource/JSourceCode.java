/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999-2001 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.codehaus.modello.plugin.java.javasource;

import java.util.ArrayList;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.List;
import org.codehaus.modello.ModelloRuntimeException;

/**
 * A class for holding in-memory Java source code.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JSourceCode
{


    /**
     * A list of JCodeStatements
     **/
    private List<JCodeStatement> source;

    /**
     * The indent size
     **/
    private short indentSize = 4;

    /**
     * The current indent size
     **/
    private short currentIndent = indentSize;

    /**
     * Creates an empty JSourceCode
     **/
    public JSourceCode()
    {
        super();
        source = new ArrayList<>();
    } //-- JSourceCode

    /**
     * Creates a JSourceCode and adds the given String
     * to it's contents
     * @param sourceCode the source to add
     **/
    public JSourceCode( String sourceCode )
    {
        this();
        this.source.add( new JCodeStatement( sourceCode, currentIndent ) );
    } //-- JSourceCode

    /**
     * Adds the given statement to this JSourceCode. The statement
     * will be added on a new line.
     * @param statement the statement to add
     **/
    public void add( String statement )
    {
        JCodeStatement jcs = new JCodeStatement( statement, currentIndent );
        source.add( jcs );
    } //-- add

    /**
     * Adds the given statement to this JSourceCode. The statement
     * will be added on a new line.
     * @param statement the statement to add
     * @param indentSize the indentSize is the size of the indentation to use
     * when printing this JSourceCode
     * @see #print
     * @deprecated this method is not here any mode in castor codegen 1.3rc1
     **/
    public void add( String statement, short indentSize )
    {
        JCodeStatement jcs = new JCodeStatement( statement, indentSize );
        source.add( jcs );
    } //-- add

    /**
     * Adds the given statement to this JSourceCode. The statement
     * will be added on a new line and added with increased indent.
     * This is a convenience method for the sequence
     * <code>
     * indent();
     * add(statement);
     * unindent();
     * </code>
     * @param statement the statement to add
     **/
    public void addIndented( String statement )
    {
        indent();
        JCodeStatement jcs = new JCodeStatement( statement, currentIndent );
        source.add( jcs );
        unindent();
    } //-- add

    /**
     * Appends the given String to the last line in this
     * JSourceCode
     * @param segment the String to append
     **/
    public void append( String segment )
    {

        if ( source.isEmpty() )
            add( segment );
        else
        {
            JCodeStatement jcs = source.get( source.size() - 1 );
            jcs.append( segment );
        }
    } //-- append(String)

    /**
     * Clears all the code statements from this JSourceCode
     **/
    public void clear()
    {
        source.clear();
    } //-- clear();

    /**
     * Copies the contents of this JSourceCode into the given JSourceCode
     * @param jsc the JSourceCode to copy this JSourceCode into
     **/
    public void copyInto( JSourceCode jsc )
    {
        for ( JCodeStatement jCodeStatement : source )
        {
            jsc.addCodeStatement( jCodeStatement );
        }
    } //-- copyInto

    /**
     * Increases the current indent level by 1
     **/
    public void indent()
    {
        currentIndent += indentSize;
    } //-- indent();

    /**
     * Returns true if this JSourceCode is empty (ie. no source).
     * @return true if this JSourceCode is empty.
     **/
    public boolean isEmpty()
    {
        return source.isEmpty();
    } //-- isEmpty

    /**
     * Prints this JSourceCode to the given JSourceWriter
     * @param jsw the JSourceWriter to print to
     **/
    public void print( JSourceWriter jsw )
    {
        for ( JCodeStatement jCodeStatement : source ) jsw.writeln( jCodeStatement.toString() );
    } //-- print

    /**
     * Decreases the indent level by 1
     **/
    public void unindent()
    {
        if ( currentIndent == 0 )
        {
            throw new ModelloRuntimeException( "Cannot unindent: current indent is 0." );
        }
        currentIndent -= indentSize;
    } //-- unindent


    /**
     * Returns the String representation of this JSourceCode
     * @return the String representation of this JSourceCode
     **/
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.getProperty( "line.separator" );
        for ( JCodeStatement jCodeStatement : source )
        {
            sb.append( jCodeStatement.toString() );
            sb.append( lineSeparator );
        }
        return sb.toString();
    } //-- toString

    /**
     * Adds the given JCodeStatement to this JSourceCode
     * @param jcs the JCodeStatement to add
     **/
    private void addCodeStatement( JCodeStatement jcs )
    {
        short indent = (short) ( jcs.getIndent() + currentIndent - JCodeStatement.DEFAULT_INDENTSIZE );
        source.add( new JCodeStatement( jcs.getStatement(), indent ) );
    } //-- addCodeStatement(JCodeStatement)

} //-- JSourceCode

/**
 * Represents a line of code, used by JSourceCode class
 * @author <a href="kvisco@intalio.com">Keith Visco</a>
 **/
class JCodeStatement
{

    private StringBuilder value;
    static public short DEFAULT_INDENTSIZE = 4;
    private short indentSize = DEFAULT_INDENTSIZE;

    JCodeStatement()
    {
        super();
        value = new StringBuilder();
    } //-- JCodeStatement

    JCodeStatement( String statement )
    {
        this();
        this.value.append( statement );
    } //-- JCodeStatement

    JCodeStatement( String statement, short indentSize )
    {
        this( statement );
        this.indentSize = indentSize;
    } //-- JCodeStatement


    void append( String segment )
    {
        value.append( segment );
    }

    short getIndent()
    {
        return indentSize;
    } //-- getIndent

    String getStatement()
    {
        return value.toString();
    } //-- getStatement

    public String toString()
    {
        if ( value.length() == 0 )
        {
            return "";
        }

        StringBuilder sb = new StringBuilder( indentSize + value.length() );
        for ( int i = 0; i < indentSize; i++ ) sb.append( ' ' );
        sb.append( value );
        return sb.toString();
    }
} //-- JCodeStatement
