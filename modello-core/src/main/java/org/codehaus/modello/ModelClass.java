package org.codehaus.modello;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class ModelClass
    extends BaseElement
{
    private String superClass;

    private List fields;

    private Map fieldMap;

    private List codeSegments;

    private Model model;

    public ModelClass()
    {
        fieldMap = new HashMap();
    }

    public void initialize( Model model )
    {
        this.model = model;

        for ( Iterator i = fields.iterator(); i.hasNext(); )
        {
            ModelField modelField = (ModelField) i.next();

            modelField.initialize( this );

            fieldMap.put( modelField.getName(), modelField );
        }
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public List getFields()
    {
        return fields;
    }

    public List getCodeSegments()
    {
        return codeSegments;
    }

    public boolean hasSuperClass()
    {
        return ( superClass != null );
    }

    public ModelField getField( String fieldName )
    {
        ModelField field = (ModelField) fieldMap.get( fieldName );

        if ( field == null )
        {
            throw new ModelloRuntimeException( "No such field: '" + fieldName + "'." );
        }

        return field;
    }

    public Model getModel()
    {
        return model;
    }
}
