package org.codehaus.modello;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class Model
    extends BaseElement
{
    private String id;

    private List classes;

    private Set classNames;

    private Map classMap;

    private String packageName;

    private String root;

    public Model()
    {
        classNames = new HashSet();

        classMap = new HashMap();
    }

    public String getId()
    {
        return id;
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

    public String getPackageName()
    {
        return packageName;
    }

    public void initialize()
    {
        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            classNames.add( modelClass.getName() );

            classMap.put( modelClass.getName(), modelClass );

            modelClass.initialize( this );
        }
    }

    public void validate()
    {
    }
}
