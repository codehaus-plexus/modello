package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import com.thoughtworks.xstream.XStream;

import java.util.Iterator;

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.ModelloPlugin;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlModelloPlugin
    extends AbstractLogEnabled
    implements ModelloPlugin
{
    public String getId()
    {
        return "xml";
    }

    public Class initializeXStream( XStream xstream )
        throws ModelloRuntimeException
    {
        xstream.alias( "xml", XmlMetaData.class );

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

        XmlMetaData meta = (XmlMetaData) modelField.getMetaData( "xml" );

        if ( meta != null )
        {
            getLogger().info( "attribute: " + meta.isAttribute() );
        }
    }
}
