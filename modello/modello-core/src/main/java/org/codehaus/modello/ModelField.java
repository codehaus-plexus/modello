package org.codehaus.modello;


/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class ModelField
    extends Base
{
    String type;

    String specification;

    String defaultValue;

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
}