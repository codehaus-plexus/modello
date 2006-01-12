package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import org.codehaus.modello.metadata.FieldMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class HibernateFieldMetadata
    implements FieldMetadata
{
    public final static String ID = HibernateFieldMetadata.class.getName();

    private boolean id;

    private String generator;

    private String length;

    /**
     * @return Returns the id.
     */
    public boolean isId()
    {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId( boolean id )
    {
        this.id = id;
    }

    /**
     * @return Returns the generator.
     */
    public String getGenerator()
    {
        return generator;
    }

    /**
     * @param generator The generator to set.
     */
    public void setGenerator( String generator )
    {
        this.generator = generator;
    }

    /**
     * @return Returns the length.
     */
    public String getLength()
    {
        return length;
    }

    /**
     * @param length The length to set.
     */
    public void setLength( String length )
    {
        this.length = length;
    }
}
