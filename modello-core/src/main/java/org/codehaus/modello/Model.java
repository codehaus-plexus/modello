package org.codehaus.modello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String id;

    private List classes;

    private Set classNames;

    private Map classMap;

    private String packageName;

    private String root;

//    private Map metaDataClasses;

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

    public void initialize()
    {
//        this.metaDataClasses = metaDataClasses;

        for ( Iterator i = classes.iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            classNames.add( modelClass.getName() );

            classMap.put( modelClass.getName(), modelClass );

            modelClass.initialize( this );
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
/*
    public Map getMetaDataClasses()
    {
        return metaDataClasses;
    }
*/
}
