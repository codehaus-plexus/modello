package org.codehaus.modello.generator.xml;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;

public class DefaultXMLWriter
    implements XMLWriter
{
    private PrintWriter writer;
    private LinkedList elementStack = new LinkedList();
    private boolean tagInProgress;
    private int depth;
    private String lineIndenter = "  ";
    private boolean readyForNewLine;
    private boolean tagIsEmpty;

    public DefaultXMLWriter( Writer writer )
    {
        this.writer = new PrintWriter( writer );
    }

    public void startElement( String name )
    {
        tagIsEmpty = false;
        finishTag();
        write( "<" );
        write( name );
        elementStack.addLast( name );
        tagInProgress = true;
        depth++;
        readyForNewLine = true;
        tagIsEmpty = true;
    }

    public void writeText( String text )
    {
        readyForNewLine = false;
        tagIsEmpty = false;
        finishTag();

        // We might have to revisit this but for elements with embedded html we
        // don't want to escape the characters.

        //text = text.replaceAll( "&", "&amp;" );
        //text = text.replaceAll( "<", "&lt;" );
        //text = text.replaceAll( ">", "&gt;" );

        write( text );
    }

    public void addAttribute( String key, String value )
    {
        write( " " );
        write( key );
        write( "=\"" );
        write( value );
        write( "\"" );
    }

    public void endElement()
    {
        depth--;
        if ( tagIsEmpty )
        {
            write( "/" );
            readyForNewLine = false;
            finishTag();
            elementStack.removeLast();
        }
        else
        {
            finishTag();
            write( "</" + elementStack.removeLast() + ">" );
        }
        readyForNewLine = true;
    }
    
    public void addCData( String cdata )
    {
        tagIsEmpty = false;
        finishTag();
        write( "<![CDATA[" + cdata + "]]>" );
    }

    private void write( String str )
    {
        writer.write( str );
    }

    private void finishTag()
    {
        if ( tagInProgress )
        {
            write( ">" );
        }
        tagInProgress = false;
        if ( readyForNewLine )
        {
            endOfLine();
        }
        readyForNewLine = false;
        tagIsEmpty = false;
    }

    protected void endOfLine()
    {
        write( "\n" );
        for ( int i = 0; i < depth; i++ )
        {
            write( lineIndenter );
        }
    }
}
