package org.codehaus.modello.translator.dtd;

/*
 * Copyright (c) 2005, Codehaus.org
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

import com.wutka.dtd.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.codehaus.modello.model.Model;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.plugin.AbstractModelloTranslator;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DtdTranslator
    extends AbstractModelloTranslator
{
    public Model translate( Reader reader, Properties parameters )
        throws ModelloException
    {
        boolean trace = ( new Boolean( parameters.getProperty( "trace" ) ) ).booleanValue();

        DTDParser parser = new DTDParser( reader, trace );

        DTD dtd;

        try
        {
            dtd = parser.parse( true );
        }
        catch( IOException e )
        {
            throw new ModelloException( "Couldn't parse the dtd.", e );
        }

        String outputDir = parameters.getProperty( ModelloParameterConstants.OUTPUT_DIRECTORY );

        PrintWriter writer;

        try
        {
            FileOutputStream fos = new FileOutputStream( new File( outputDir, dtd.rootElement.name + ".mdo" ) );

            writer = new PrintWriter( fos );
        }
        catch( FileNotFoundException e )
        {
            throw new ModelloException( "Can't create modello config file.", e );
        }

        translate( writer, dtd, parameters );

        return null;
    }

    public void translate( Model model, Properties parameters )
        throws ModelloException
    {
    }

    private void translate( PrintWriter writer, DTD dtd, Properties parameters )
        throws ModelloException
    {
        String id = parameters.getProperty( "id" );

        String packageName = parameters.getProperty( "package" );

        String version = parameters.getProperty( ModelloParameterConstants.VERSION );

        if ( id == null )
        {
            throw new ModelloException( "You must specify a modello id for translation." );
        }

        if ( packageName == null )
        {
            throw new ModelloException( "You must specify a packageName for translation." );
        }

        if ( version == null )
        {
            throw new ModelloException( "You must specify a version for translation." );
        }

        writer.println( "<model>" );

        writer.println( "  <id>" + id + "</id>" );

        writer.println( "  <name>" + id + "</name>" );

        writer.println( "  <packageName>" + packageName + "</packageName>" );

        writer.println( "  <root>" + dtd.rootElement.name + "</root>" );

        writer.println( "  <classes>" );

        writeClassElement( writer, dtd, dtd.rootElement, version );

        Enumeration e = dtd.elements.elements();

        while ( e.hasMoreElements() )
        {
            DTDElement elem = (DTDElement) e.nextElement();

            if ( ! elem.equals( dtd.rootElement ) )
            {
                if ( isContainer( dtd, elem.name ) && ! isAssociation( dtd, elem.name ) )
                {
                    writeClassElement( writer, dtd, elem, version );
                }
            }
        }

        writer.println( "  </classes>" );

        writer.println( "</model>" );

        writer.flush();
    }

    private void writeClassElement( PrintWriter writer, DTD dtd, DTDElement element, String version )
        throws ModelloException
    {
        writer.println( "    <class>" );

        writer.println( "      <name>" + element.name + "</name>" );

        writer.println( "      <version>" + version + "</version>" );

        writer.println( "      <fields>" );

        writeAttributes( writer, element, version );

        Vector associations = new Vector();

        writeFields( writer, dtd, element.content, version, associations );

        writer.println( "      </fields>" );

        if ( ! associations.isEmpty() )
        {
            Enumeration e = associations.elements();

            writer.println( "      <associations>" );

            while ( e.hasMoreElements() )
            {
                String associationName = (String)e.nextElement();

                if ( isAssociation( dtd, associationName ) )
                {
                    writeAssociation( writer, dtd, associationName, version );
                }
                else
                {
                    throw new ModelloException( "The element " + associationName + " isn't a valid association." );
                }
            }

            writer.println( "      <associations>" );
        }

        writer.println( "    </class>" );

        writer.flush();
    }

    private void writeAttributes( PrintWriter writer, DTDElement element, String version )
    {
        if ( element.attributes != null && element.attributes.size() > 0 )
        {
            Enumeration attributes = element.attributes.elements();

            while ( attributes.hasMoreElements() )
            {
                DTDAttribute attribute = (DTDAttribute) attributes.nextElement();

                writer.println( "        <field attribute=\"true\">" );

                writer.println( "          <name>" + attribute.name + "</name>" );

                writer.println( "          <version>" + version + "</version>" );

                writer.println( "          <type>String</type>" );

                writer.println( "        </field>" );
            }
        }

        writer.flush();
    }

    private void writeFields( PrintWriter writer, DTD dtd, DTDItem item, String version, Vector associations )
        throws ModelloException
    {
        if ( item instanceof DTDAny )
        {
            throw new ModelloException( "The \"ANY\" type isn't supported." );
        }
        else if ( item instanceof DTDChoice )
        {
//            writer.println( "DTDChoice" );

            DTDItem[] items = ( (DTDChoice) item ).getItems();

            for ( int i=0; i < items.length; i++ )
            {
                writeFields( writer, dtd, items[i], version, associations );
            }
        }
        else if ( item instanceof DTDMixed )
        {
//            writer.println( "DTDMixed" );

            DTDItem[] items = ( (DTDMixed) item ).getItems();

            for ( int i=0; i < items.length; i++ )
            {
                writeFields( writer, dtd, items[i], version, associations );
            }
        }
        else if ( item instanceof DTDSequence )
        {
//            writer.println( "DTDSequence" );

            DTDItem[] items = ( (DTDSequence) item ).getItems();

            for ( int i=0; i < items.length; i++ )
            {
                writeFields( writer, dtd, items[i], version, associations );
            }
        }
        else if ( item instanceof DTDName )
        {
            String name = ( (DTDName) item).value;

//            writer.println( "DTDName" );

            if ( DTDCardinal.NONE.equals( item.getCardinal() ) ||
                 DTDCardinal.OPTIONAL.equals( item.getCardinal() ) )
            {
                if ( ! isAssociation( dtd, name ) )
                {
                    writer.println( "        <field>" );

                    writer.println( "          <name>" + name + "</name>" );

                    writer.println( "          <version>" + version + "</version>" );

                    if ( isContainer( dtd, name ) )
                    {
                        writer.println( "          <type>" + name + "</type>" );
                    }
                    else if ( isEmpty( dtd, name ) )
                    {
                        writer.println( "          <type>boolean</type>" );
                    }
                    else
                    {
                        writer.println( "          <type>String</type>" );
                    }
                    writer.println( "        </field>" );
                }
                else
                {
                    associations.add( name );
                }
            }
            else
            {
//                associations.add( name );
            }

            writer.flush();
        }
        else
        {
            throw new ModelloException( "The " + item.getClass().getName() + " insn't supported as field." );
        }
    }

    private void writeAssociation( PrintWriter writer, DTD dtd, String name, String version )
        throws ModelloException
    {
        DTDElement element = getElement( dtd, name );

        writer.println( "        <association>" );

        writer.println( "          <name>" + name + "</name>" );

        writer.println( "          <version>" + version + "</version>" );

        writer.println( "        </association>" );

        writer.flush();
    }

    private boolean isContainer( DTD dtd, String elementName )
    {
        DTDElement element = getElement( dtd, elementName );

        DTDItem content = element.content;

        if ( content instanceof DTDSequence )
        {
            return true;
        }

        return false;
    }

    private boolean isAssociation( DTD dtd, String elementName )
    {
        DTDElement element = getElement( dtd, elementName );

        DTDItem content = element.content;

        if ( content instanceof DTDSequence )
        {
            DTDItem[] items = ( (DTDSequence) content ).getItems();

            if ( items.length == 1 && items[0] instanceof DTDName )
            {
                DTDName item = ( DTDName )items[0];

                if ( DTDCardinal.ZEROMANY.equals( item.getCardinal() ) ||
                     DTDCardinal.ONEMANY.equals( item.getCardinal() ) )
                {
                    if ( element.name.equalsIgnoreCase( item.value + "s" ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isEmpty( DTD dtd, String elementName )
    {
        DTDElement element = getElement( dtd, elementName );

        DTDItem content = element.content;

        if ( content instanceof DTDEmpty )
        {
            return true;
        }

        return false;
    }

    private boolean isPCData( DTD dtd, String elementName )
    {
        DTDElement element = getElement( dtd, elementName );

        DTDItem content = element.content;

        if ( content instanceof DTDMixed )
        {
            DTDItem[] items = ( (DTDMixed) content ).getItems();

            if (items.length == 1 && items[0] instanceof DTDPCData )
            {
                return true;
            }
        }

        return false;
    }

    private DTDElement getElement( DTD dtd, String elementName )
    {
        return (DTDElement)dtd.elements.get( elementName );
    }
}
