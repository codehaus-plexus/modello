package org.codehaus.modello;

import java.util.List;

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

    String code;

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
}