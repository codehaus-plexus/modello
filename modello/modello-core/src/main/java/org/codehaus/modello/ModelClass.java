package org.codehaus.modello;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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
    String superClass;

    List fields;

    Map fieldMap;

    String code;

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

    public String getCode()
    {
        return code;
    }

    public boolean hasSuperClass()
    {
        return ( superClass != null );
    }
}