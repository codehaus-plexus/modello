package org.codehaus.modello.generator.xml;

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
