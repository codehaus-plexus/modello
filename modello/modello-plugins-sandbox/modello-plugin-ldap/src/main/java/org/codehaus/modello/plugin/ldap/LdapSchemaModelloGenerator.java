package org.codehaus.modello.plugin.ldap;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.StringUtils;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.ldap.model.AttributeType;
import org.codehaus.modello.plugin.ldap.model.ObjectClass;
import org.codehaus.modello.plugin.ldap.metadata.LdapFieldMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LdapSchemaModelloGenerator
    extends AbstractModelloGenerator
{
    public final static String FILENAME_PARAMETER = "schemaFilename";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Map objectClasses = new HashMap();

    private Map attributeTypes = new HashMap();

    // ----------------------------------------------------------------------
    // AbstractModelloGenerator Implementation
    // ----------------------------------------------------------------------

    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        String filename = getParameter( properties, FILENAME_PARAMETER, null );

        if ( filename == null )
        {
            filename = model.getId() + ".schema";
        }

        File file = new File( getOutputDirectory(), filename );

        System.out.println( "Generating LDAP Schema to '" + file.getAbsolutePath() + "'." );

        try
        {
            FileWriter output = new FileWriter( file );

            PrintWriter printer = new PrintWriter( output );

            makeLdapObjects( model );

            writeSchema( printer );

            printer.flush();

            output.close();
        }
        catch( IOException ex )
        {
            System.out.println( "IO error while generating LDAP schema." );

            ex.printStackTrace( System.out );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void makeLdapObjects( Model model )
    {
        List classes = model.getClasses( getGeneratedVersion() );

        for ( Iterator it = classes.iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            ObjectClass objectClass = makeObjectClass( modelClass );

            if ( objectClass == null )
            {
                continue;
            }

            objectClasses.put( objectClass.getName(), objectClass );

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            List fields = modelClass.getFields( getGeneratedVersion() );

            for ( Iterator it2 = fields.iterator(); it2.hasNext(); )
            {
                ModelField modelField = (ModelField) it2.next();

                AttributeType attributeType = makeAttribute( modelField );

                if ( attributeType == null )
                {
                    continue;
                }

                attributeTypes.put( attributeType.getName(), attributeType );

                if ( modelField.isRequired() )
                {
                    objectClass.getRequiredAttributes().add( attributeType );
                }
                else
                {
                    objectClass.getNotRequiredAttributes().add( attributeType );
                }
            }
        }

        // At this point all objectClasses should have been generated and give a OID
        // TODO: when a ModelClass has a reference to another ModelClass make
        //       sure that the reference has the OID of the referenced object
    }

    private void writeSchema( PrintWriter printer )
    {
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd HH:mm" );

        printer.println( "# ----------------------------------------------------------------------" );
        printer.println( "# LDAP Schema generated " + formatter.format( new Date() ) + "." );
        printer.println( "# ----------------------------------------------------------------------" );

        printer.println( "" );
        printer.println( "# ----------------------------------------------------------------------" );
        printer.println( "# Attribute Types" );
        printer.println( "# ----------------------------------------------------------------------" );
        printer.println( "" );

        for ( Iterator it = attributeTypes.values().iterator(); it.hasNext(); )
        {
            AttributeType attributeType = (AttributeType) it.next();

            printer.println( "attributetype (" );
            printer.println( "    " + attributeType.getOid() );
            printer.println( "    NAME '" + attributeType.getName() + "'" );

            if ( !StringUtils.isEmpty( attributeType.getDescription() ) )
            {
                printer.println( "    DESC '" + attributeType.getDescription() + "'" );
            }

            if ( !StringUtils.isEmpty( attributeType.getEquality() ) )
            {
                printer.println( "    EQUALITY '" + attributeType.getEquality() + "'" );
            }

            if ( !StringUtils.isEmpty( attributeType.getSubString() ) )
            {
                printer.println( "    SUBSTR '" + attributeType.getSubString() + "'" );
            }

            if ( attributeType.getLength() == 0 )
            {
                printer.println( "    SYNTAX " + attributeType.getSyntax() );
            }
            else
            {
                printer.println( "    SYNTAX " + attributeType.getSyntax() + "{" + attributeType.getLength() + "}" );
            }

            printer.println( ")" );
            printer.println( "" );
        }

        printer.println( "# ----------------------------------------------------------------------" );
        printer.println( "# Object Classes" );
        printer.println( "# ----------------------------------------------------------------------" );
        printer.println( "" );

        for ( Iterator it = objectClasses.values().iterator(); it.hasNext(); )
        {
            ObjectClass objectClass = (ObjectClass) it.next();

            printer.println( "objectclass (" );
            printer.println( "    " + objectClass.getOid() );
            printer.println( "    NAME '" + objectClass.getName() + "'" );

            if ( !StringUtils.isEmpty( objectClass.getDescription() ) )
            {
                printer.println( "    DESC '" + objectClass.getDescription() + "'" );
            }

            printer.println( "    SUP " + objectClass.getSuperObjectClass() );

            if ( objectClass.isAuxiliary() )
            {
                printer.println( "    AUXILIARY" );
            }

            printAttributeList( printer, "MUST", objectClass.getRequiredAttributes() );

            printAttributeList( printer, "MAY", objectClass.getNotRequiredAttributes() );

            printer.println( ")" );
            printer.println( "" );
        }
    }

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    private ObjectClass makeObjectClass( ModelClass modelClass )
    {
        String name = makeObjectClassName( modelClass );

        // TODO: Encode this string somehow. Just strip out any illegal characters?
        String description = modelClass.getDescription();

        String superModelClassName = modelClass.getSuperClass();

        String superObjectClass = "top";

        if ( !StringUtils.isEmpty( superModelClassName ) )
        {
            ModelClass superModelClass = modelClass.getModel().getClass( superModelClassName, getGeneratedVersion() );

            superObjectClass = makeObjectClassName( superModelClass );
        }

        boolean auxiliary = true;

        String oid = "1.2.3.4.5.6.7.8.9";

        return new ObjectClass( oid, name, description, superObjectClass, auxiliary );
    }

    private AttributeType makeAttribute( ModelField modelField )
    {
        String name = modelField.getName();

        String description = modelField.getDescription();

        String equality;

        String subString = null;

        String syntax;

        int length;

        String type = modelField.getType();

        if ( type.equals( "String" ) )
        {
            syntax = "1.3.6.1.4.1.1466.115.121.1.26";

            equality = "caseIgnoreA5Match";

            subString = "caseIgnoreA5SubstringsMatch";

            length = 255;
        }
        else if ( type.equals( "int" ) )
        {
            syntax = "1.3.6.1.4.1.1466.115.121.1.27";

            equality = "integerMatch";

            length = 0;
        }
        else
        {
            System.err.println( "Unkown type: '" + type + "' for field '" + modelField.getName() + "' in class '" + modelField.getModelClass().getName() + "'." );

            return null;
        }

        LdapFieldMetadata metadata = (LdapFieldMetadata) modelField.getMetadata( LdapFieldMetadata.ID );

        String oid = metadata.getOid();

        return new AttributeType( oid, name, description, equality, subString, syntax, length );
    }

    // ----------------------------------------------------------------------
    // Printing Utils
    // ----------------------------------------------------------------------

    private void printAttributeList( PrintWriter printer, String type, List attributes )
    {
        if ( attributes.size() > 0 )
        {
            printer.println( "    " + type + " ( " );

            Iterator it2;

            int i;

            for ( it2 = attributes.iterator(), i = 0; it2.hasNext(); i++ )
            {
                AttributeType attributeType = (AttributeType) it2.next();

                printer.print( "        " + attributeType.getName() );

                if ( i != attributes.size() - 1 )
                {
                    printer.println( " $" );
                }
                else
                {
                    printer.println( " " );
                }
            }

            printer.println( "    )" );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String makeObjectClassName( ModelClass modelClass )
    {
        return uncapitalise( modelClass.getName() );
    }
}
