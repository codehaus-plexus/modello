package org.codehaus.modello.plugin.java.metadata;

/*
 * Copyright (c) 2004, Codehaus.org
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

import org.codehaus.modello.metadata.AssociationMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class JavaAssociationMetadata
    implements AssociationMetadata
{
    public static final String ID = JavaAssociationMetadata.class.getName();

    public static final String LAZY_INIT = "lazy";
    public static final String CONSTRUCTOR_INIT = "constructor";
    public static final String FIELD_INIT = "field";

    public static final List<String> INIT_TYPES;

    static
    {
        INIT_TYPES = new ArrayList<String>();
        INIT_TYPES.add( LAZY_INIT );
        INIT_TYPES.add( CONSTRUCTOR_INIT );
        INIT_TYPES.add( FIELD_INIT );
    }

    public static final String CLONE_SHALLOW = "shallow";
    public static final String CLONE_DEEP = "deep";

    public static final List<String> CLONE_MODES;

    static
    {
        CLONE_MODES = new ArrayList<String>();
        CLONE_MODES.add( CLONE_SHALLOW );
        CLONE_MODES.add( CLONE_DEEP );
    }

    private boolean adder = true;

    private boolean bidi;

    private String interfaceName;

    private String initializationMode;

    private String cloneMode;

    public boolean isAdder()
    {
        return adder;
    }

    public void setAdder( boolean adder )
    {
        this.adder = adder;
    }

    public boolean isBidi()
    {
        return bidi;
    }

    public void setBidi( boolean bidi )
    {
        this.bidi = bidi;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName( String interfaceName )
    {
        this.interfaceName = interfaceName;
    }

    public String getInitializationMode()
    {
        return initializationMode;
    }

    public void setInitializationMode( String initializationMode )
    {
        if ( initializationMode == null )
        {
            this.initializationMode = LAZY_INIT;
        }
        else
        {
            this.initializationMode = initializationMode;
        }
    }

    public String getCloneMode()
    {
        return cloneMode;
    }

    public void setCloneMode( String cloneMode )
    {
        this.cloneMode = cloneMode;
    }

}
