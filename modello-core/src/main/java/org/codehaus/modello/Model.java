package org.codehaus.modello;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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
        }
    }

    public String getPackageName()
    {
        return packageName;
    }
}