package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.metadata.AbstractMetaDataPlugin;
import org.codehaus.modello.metadata.MetaData;
import org.codehaus.modello.metadata.MetaDataPlugin;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlMetaDataPlugin
    extends AbstractMetaDataPlugin
    implements MetaDataPlugin
{
    public MetaData getModelMetaData( Model model, Map data )
    {
        return null;
    }

    public MetaData getClassMetaData( ModelClass clazz, Map data )
    {
        return null;
    }

    public MetaData getFieldMetaData( ModelField field, Map data )
    {
        XmlMetaData metaData = new XmlMetaData();

        String attribute = (String) data.get( "attribute" );

        metaData.setAttribute( Boolean.valueOf( attribute ).booleanValue() );

        return metaData;
    }
/*
    public Class initializeXStream( XStream xstream )
        throws ModelloRuntimeException
    {
        xstream.alias( ID, XmlMetaData.class );

        return XmlMetaData.class;
    }

    public void generate( Model model )
        throws ModelloRuntimeException
    {
        for ( Iterator i = model.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            processClass( modelClass );
        }
    }

    private void processClass( ModelClass modelClass )
        throws ModelloRuntimeException
    {
        getLogger().info( "Processing class: " + modelClass.getName() );

        for ( Iterator i = modelClass.getFields().iterator(); i.hasNext(); )
        {
            ModelField modelField = (ModelField) i.next();

            processField( modelField );
        }
    }

    private void processField( ModelField modelField )
        throws ModelloRuntimeException
    {
        getLogger().info( "Processing field: " + modelField.getName() );

        if ( !modelField.hasMetaData( ID ) )
        {
            return;
        }

        XmlMetaData meta = (XmlMetaData) modelField.getMetaData( ID );

        if ( meta != null )
        {
            getLogger().info( "attribute: " + meta.isAttribute() );
        }
    }
*/
}
