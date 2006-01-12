package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

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

        XMLWriter w = new PrettyPrintXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" );
        writer.write( "\n" );
        writer.write( "<!DOCTYPE hibernate-mapping PUBLIC\n" );
        writer.write( "  \"-//Hibernate/Hibernate Mapping DTD//EN\"\n" );
        writer.write( "  \"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\">\n" );
        writer.write( "\n" );

        w.startElement( "hibernate-mapping" );

        for ( Iterator it = model.getClasses( getGeneratedVersion() ).iterator(); it.hasNext(); )
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
        String fqcn = getFullyQualifiedClassName( modelClass );

        w.startElement( "class" );

        w.addAttribute( "name", fqcn );

        w.addAttribute( "table", modelClass.getName() );

        ModelField idField = getIdField( modelClass );

        HibernateFieldMetadata metadata = (HibernateFieldMetadata) idField.getMetadata( HibernateFieldMetadata.ID );

        if ( isEmpty( metadata.getGenerator() ) )
        {
            throw new ModelloRuntimeException( "Error while generating hibernate mapping for '" + modelClass.getName() + "': The id field must set a 'generator' metadata element." );
        }

        writeHibernateId( w, idField );

        for ( Iterator it = modelClass.getFields( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            if ( modelField instanceof ModelAssociation )
            {
                ModelAssociation modelAssociation = (ModelAssociation) it.next();

                writeAssociation( w, modelAssociation );
            }
            else
            {
                metadata = (HibernateFieldMetadata)modelField.getMetadata( HibernateFieldMetadata.ID );

                if ( metadata.isId() )
                {
                    // the id fields are already mapped
                    continue;
                }

                writeField( w, modelField );
            }
        }

        w.endElement();
    }

    private void writeHibernateId( XMLWriter w, ModelField field )
    {
        HibernateFieldMetadata metadata = (HibernateFieldMetadata) field.getMetadata( HibernateFieldMetadata.ID );

        w.startElement( "id" );

        w.addAttribute( "name", field.getName() );

        w.addAttribute( "type", toHibernateType( field ) );

//        w.addAttribute( "unsaved-value", "null" );

/*
        w.startElement( "column" );

        w.addAttribute( "name", field.getName() );

//        w.addAttribute( "type", toHibernateType( field ) );

//        w.addAttribute( "non-null", "true" );

        w.endElement();
*/

        w.startElement( "generator" );

        w.addAttribute( "class", metadata.getGenerator() );

        w.endElement();

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

    private void writeAssociation( XMLWriter w, ModelAssociation modelAssociation )
    {
        writeHibernateOneToMany( w, modelAssociation );
    }

    private void writeHibernateProperty( XMLWriter w, ModelField modelField )
    {
        HibernateFieldMetadata metadata = (HibernateFieldMetadata) modelField.getMetadata( HibernateFieldMetadata.ID );

        w.startElement( "property" );

        w.addAttribute( "name", modelField.getName() );

        String type = toHibernateType( modelField );

        if( type.equals( "string" ) )
        {
            String length = metadata.getLength();

            if ( isEmpty( length ) )
            {
                throw new ModelloRuntimeException( "Missing metadata element 'length' for field with type='string' for the field " + modelField.getName() + " in class " + modelField.getModelClass().getName() );
            }

            if ( length.equals( "infinite" ) )
            {
                // TODO: find out what hibernate type that will yield a TEXT/CLOB sql type
                w.addAttribute( "type", "string" );
            }
            else
            {
                int num;

                try
                {
                    num = Integer.parseInt( length );

                    if ( num <= 0 )
                    {
                        throw new ModelloRuntimeException( "Invalid value for 'length' for the field " + modelField.getName() + " in class " + modelField.getModelClass().getName() );
                    }
                }
                catch( NumberFormatException ex )
                {
                    throw new ModelloRuntimeException( "Invalid value for metadata field 'length' in the field " + modelField.getName() + " in class " + modelField.getModelClass().getName() );
                }

                w.addAttribute( "type", "string" );

                w.addAttribute( "length", length );
            }
        }
        else if("Boolean".equals(type))
        {
            w.addAttribute( "type", "boolean" );
        }
        else
        {
            w.addAttribute( "type", type );
        }

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

        ModelClass clazz = getModel().getClass( modelField.getType(), getGeneratedVersion() );

        if ( clazz == null )
        {
            throw new ModelloRuntimeException( "Can't find class '" + modelField.getType() + "'." );
        }

        w.addAttribute( "type", getFullyQualifiedClassName( clazz ) );

        if ( modelField.isRequired() )
        {
            w.addAttribute( "not-null", "true" );
        }

        w.endElement();
    }
/*
    private void writeHibernateManyToOne( XMLWriter w, ModelAssociation modelAssociation )
    {
        w.startElement( "many-to-one" );

        w.addAttribute( "name", modelAssociation.getFromRole() );

        w.addAttribute( "type", modelAssociation.getToClass().getName() );

        // TODO: When is it useful to have not-null associations? Isn't that just a empty list?
        w.addAttribute( "not-null", "true" );

        w.endElement();
    }
*/
    private void writeHibernateOneToMany( XMLWriter w, ModelAssociation modelAssociation )
    {
        w.startElement( "list" );

        w.addAttribute( "name", modelAssociation.getName() );

        w.startElement( "key" );

        ModelField idField = getIdField( modelAssociation.getToClass() );

        w.addAttribute( "column", toHibernateType( idField ) );

        w.endElement();

        w.startElement( "index" );

        w.addAttribute( "column", "index" );

        w.endElement();

        w.startElement( "one-to-many" );

        w.addAttribute( "class", getFullyQualifiedClassName( modelAssociation.getToClass() ) );

        w.endElement();

        w.endElement();
    }

    private String toHibernateType( ModelField field )
    {
        String fieldType = field.getType();

        Map map = new HashMap();

        map.put( "float", "float" );

        map.put( "int", "int" );

        map.put( "char", "character" );

        map.put( "String", "string" );

        map.put( "Date", "timestamp" );

        String hibernateType = (String) map.get( fieldType );

        if ( hibernateType == null )
        {
            throw new ModelloRuntimeException( "Unknonwn field type: '" + fieldType + "'." );
        }

        return hibernateType;
    }

    private ModelField getIdField( ModelClass modelClass )
    {
        ModelField idField = null;

        for ( Iterator it = modelClass.getFields( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            HibernateFieldMetadata metadata = (HibernateFieldMetadata)modelField.getMetadata( HibernateFieldMetadata.ID );

            System.err.println( modelClass.getName() + ":" + modelField.getName() + ":" + metadata.isId() );

            if ( metadata.isId() )
            {
                if ( idField != null )
                {
                    throw new ModelloRuntimeException( "Error while generating hibernate mapping for '" + modelClass.getName() + "': There can only be a single id field. Had: " + idField.getName() + ", found: " + modelField.getName() + "." );
                }

                idField = modelField;
            }
/*
            if ( true )
            {
                throw new RuntimeException( "fuck" );
            }*/
        }

        if ( idField == null )
        {
            throw new ModelloRuntimeException( "Error while generating hibernate mapping for class: " + modelClass.getName() + ". There must be one id per model class." );
        }

        return idField;
    }

    private String getFullyQualifiedClassName( ModelClass modelClass )
    {
        if ( !isEmpty( modelClass.getModel().getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) ) )
        {
            return modelClass.getModel().getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) + "." + modelClass.getName();
        }
        else
        {
            return modelClass.getName();
        }
    }
}
