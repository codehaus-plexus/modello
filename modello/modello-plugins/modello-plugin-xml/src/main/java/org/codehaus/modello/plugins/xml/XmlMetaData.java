package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import org.codehaus.modello.metadata.MetaData;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlMetaData
    implements MetaData
{
    public final static String ID = XmlMetaData.class.getName();

    private boolean attribute;

    /**
     * @return Returns the attribute.
     */
    public boolean isAttribute()
    {
        return attribute;
    }

    /**
     * @param attribute The attribute to set.
     */
    public void setAttribute( boolean attribute )
    {
        this.attribute = attribute;
    }
}
