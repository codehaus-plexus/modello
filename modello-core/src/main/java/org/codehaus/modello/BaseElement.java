package org.codehaus.modello;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class BaseElement
{
    String name;

    String description;

    String version;

    String comment;

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getVersion()
    {
        return version;
    }

    public String getComment()
    {
        return comment;
    }
}