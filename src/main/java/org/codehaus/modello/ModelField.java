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
public class ModelField
    extends BaseElement
{
    private String type;

    private String specification;

    private String defaultValue;

    private String typeValidator;

    private boolean required;

    private List metaData;

    private transient Map metaDataObjects = new HashMap();

    private ModelClass modelClass;

    public String getType()
    {
        return type;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String getSpecification()
    {
        return specification;
    }

    public String getTypeValidator()
    {
        return typeValidator;
    }

    public boolean isRequired()
    {
        return required;
    }

    public boolean hasMetaData( String key )
    {
        return metaDataObjects.containsKey( key );
    }

    public Object getMetaData( String key )
        throws ModelloRuntimeException
    {
        return metaDataObjects.get( key );
    }

    public void initialize( ModelClass modelClass )
    {
        this.modelClass = modelClass;

        if ( metaData != null )
        {
            Map metaDataClasses = modelClass.getModel().getMetaDataClasses();

            for ( Iterator it = metaData.iterator(); it.hasNext(); )
            {
                Object object = it.next();
    
                Class clazz = object.getClass();
    
                String key = (String) metaDataClasses.get( clazz.getName() );
    
                metaDataObjects.put( key, object );
            }
        }
    }

    public ModelClass getModelClass()
    {
        return modelClass;
    }
}
