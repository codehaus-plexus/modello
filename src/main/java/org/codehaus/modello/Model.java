package org.codehaus.modello;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class Model
    extends BaseElement
{
    List classes;

    Set classNames;

    Map classMap;

    String packageName;

    String root;

    public Model()
    {
        classNames = new HashSet();

        classMap = new HashMap();
    }

    public List getClasses()
    {
        return classes;
    }

    public Set getClassNames()
    {
        return classNames;
    }

    public String getRoot()
    {
        return root;
    }

    public ModelClass getClass( String type )
    {
        return (ModelClass) classMap.get( type );
    }

    public void initialize()
    {
        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            classNames.add( modelClass.getName() );

            classMap.put( modelClass.getName(), modelClass );

            modelClass.initialize();
        }
    }

    public String getPackageName()
    {
        return packageName;
    }

    public List getAllFields( ModelClass modelClass )
    {
        List allFields = new ArrayList();

        allFields.addAll( modelClass.getFields() );

        ModelClass c = modelClass;

        while ( c.getSuperClass() != null )
        {
            ModelClass parent = (ModelClass) classMap.get( c.getSuperClass() );

            allFields.addAll( parent.getFields() );

            c = parent;
        }

        return allFields;
    }
}