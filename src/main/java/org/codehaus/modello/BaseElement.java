package org.codehaus.modello;

/*
 * Copyright (c) 2004, Jason van Zyl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.HashMap;
import java.util.Map;

import org.codehaus.modello.metadata.Metadata;

/**
 * This is the base class for all elements of the model.
 * 
 * The name attribute is immutable because it's used as the key.
 * 
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class BaseElement
{
    private String name;

    private String description;

    private String version;

    private String comment;

    private transient Map metadata = new HashMap();

    private transient VersionRange elementVersion;

    private boolean nameRequired;

    public abstract void validateElement()
        throws ModelValidationException;

    public BaseElement( boolean nameRequired )
    {
        this.nameRequired = nameRequired;
    }

    public BaseElement( boolean nameRequired, String name )
    {
        this.nameRequired = nameRequired;

        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public boolean hasMetadata( String key )
    {
        return metadata.containsKey( key );
    }

    public void addMetadata( Metadata metadata )
    {
        this.metadata.put( metadata.getClass().getName(), metadata );
    }

    protected Metadata getMetadata( Class type, String key )
    {
        Metadata metadata = (Metadata) this.metadata.get( key );

        if ( metadata == null )
        {
            throw new ModelloRuntimeException( "No such metadata: " + key );
        }

        if ( !type.isAssignableFrom( metadata.getClass() ) )
        {
            throw new ModelloRuntimeException( "The metadata is not of the expected type. Key: " + key + ", expected type: " + type.getName() );
        }

        return metadata;
    }

    public VersionRange getElementVersion()
    {
        return elementVersion;
    }

    // ----------------------------------------------------------------------
    // Validation utils
    // ----------------------------------------------------------------------

    protected void validateFieldNotEmpty( String objectName, String fieldName, String value )
        throws ModelValidationException
    {
        if ( value == null )
        {
            throw new ModelValidationException( "Missing value '" + fieldName + "' from " + objectName + "." );
        }

        if ( isEmpty( value ) )
        {
            throw new ModelValidationException( "Empty value '" + fieldName + "' from " + objectName + "." );
        }
    }

    public final void validate()
        throws ModelValidationException
    {
        if ( nameRequired )
        {
            validateFieldNotEmpty( "Element.name", "name", name );
        }

        if ( isEmpty( version ) )
        {
            version = "0.0.0+";
        }

        elementVersion = new VersionRange( version );

        validateElement();
    }

    protected boolean isEmpty( String string )
    {
        return string == null || string.trim().length() == 0;
    }
}
