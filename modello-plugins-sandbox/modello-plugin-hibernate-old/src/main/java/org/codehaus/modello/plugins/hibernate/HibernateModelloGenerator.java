package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.xml.DefaultXMLWriter;
import org.codehaus.modello.generator.xml.XMLWriter;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class HibernateModelloGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateHibernateMapping();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error while generating hibernate mapping.", ex );
        }
    }

    private void generateHibernateMapping()
        throws IOException
    {
        Model model = getModel();

        File f = new File( getOutputDirectory(), model.getId() + ".hbm.xml" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new DefaultXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" );

        writer.write( "\n" );

        writer.write( "<!DOCTYPE hibernate-mapping PUBLIC\n" );
        writer.write( "    \"-//Hibernate/Hibernate Mapping DTD//EN\"\n" );
        writer.write( "    \"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\">\n" );

        writer.write( "\n" );

        w.startElement( "hibernate-mapping" );

        for ( Iterator it = model.getClasses().iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            writeClass( w, modelClass );
        }

        w.endElement();

        writer.write( "\n" );

        writer.flush();

        writer.close();
    }

    private void writeClass( XMLWriter w, ModelClass modelClass )
    {
        w.startElement( "class" );

        String fqcn;

        if ( !isEmpty( modelClass.getModel().getPackageName() ) )
        {
            fqcn = modelClass.getModel().getPackageName() + "." + modelClass.getName();
        }
        else
        {
            fqcn = modelClass.getName();
        }

        w.addAttribute( "name", fqcn );

        w.addAttribute( "table", modelClass.getName() );

        for ( Iterator it = modelClass.getFields().iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            writeField( w, modelField );
        }

        w.endElement();
    }

    private void writeField( XMLWriter w, ModelField modelField )
    {
        if ( modelField.isPrimitive() )
        {
            writeHibernateProperty( w, modelField );
        }
        else
        {
            writeHibernateOneToOne( w, modelField );
        }
    }

    private void writeHibernateProperty( XMLWriter w, ModelField modelField )
    {
        w.startElement( "property" );

        w.addAttribute( "name", modelField.getName() );

        w.addAttribute( "type", modelField.getType() );

        if ( modelField.isRequired() )
        {
            w.addAttribute( "not-null", "true" );
        }

        w.endElement();
    }

    private void writeHibernateOneToOne( XMLWriter w, ModelField modelField )
    {
        w.startElement( "one-to-one" );

        w.addAttribute( "name", modelField.getName() );

        w.addAttribute( "type", modelField.getType() );

        if ( modelField.isRequired() )
        {
            w.addAttribute( "not-null", "true" );
        }

        w.endElement();
    }
}
