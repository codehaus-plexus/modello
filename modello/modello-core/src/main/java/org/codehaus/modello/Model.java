package org.codehaus.modello;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class Model
    extends Base
{
    List classes;

    Set classNames;

    String packageName;

    public Model()
    {
        classNames = new HashSet();
    }

    public List getClasses()
    {
        return classes;
    }

    public Set getClassNames()
    {
        return classNames;
    }

    public void registerClassNames()
    {
        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            classNames.add( modelClass.getName() );
        }
    }

    public String getPackageName()
    {
        return packageName;
    }
}