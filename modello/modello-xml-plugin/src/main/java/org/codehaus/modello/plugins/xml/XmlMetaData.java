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

    private String tagName;

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

    /**
     * @return Returns the tag name or the attribute name if it's an attribute.
     */
    public String getTagName()
    {
        return tagName;
    }

    /**
     * @param tagName The tag or attribute name to set.
     */
    public void setTagName( String tagName )
    {
        this.tagName = tagName;
    }
}
