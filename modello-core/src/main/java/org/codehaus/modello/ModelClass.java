package org.codehaus.modello;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 *
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

    public ModelClass()
    {
        fieldMap = new HashMap();
    }

    public void initialize()
    {
        for ( Iterator i = fields.iterator(); i.hasNext(); )
        {
            ModelField modelField = (ModelField) i.next();

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
        return (ModelField) fieldMap.get( fieldName );
    }
}