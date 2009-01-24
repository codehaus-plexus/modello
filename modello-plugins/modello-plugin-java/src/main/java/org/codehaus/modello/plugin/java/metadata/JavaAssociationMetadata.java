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
 * @version $Id$
 */
public class JavaAssociationMetadata
    implements AssociationMetadata
{
    public static final String ID = JavaAssociationMetadata.class.getName();

    public static final String LAZY_INIT = "lazy";
    public static final String CONSTRUCTOR_INIT = "constructor";
    public static final String FIELD_INIT = "field";

    public final static List INIT_TYPES;

    static
    {
        INIT_TYPES = new ArrayList();
        INIT_TYPES.add( LAZY_INIT );
        INIT_TYPES.add( CONSTRUCTOR_INIT );
        INIT_TYPES.add( FIELD_INIT );
    }

    private boolean adder = true;

    private boolean generateBreak;

    private boolean generateCreate;

    private String interfaceName;

    private String initializationMode;

    public boolean isAdder()
    {
        return adder;
    }

    public void setAdder( boolean adder )
    {
        this.adder = adder;
    }

    public boolean isGenerateBreak()
    {
        return generateBreak;
    }

    public void setGenerateBreak( boolean generateBreak )
    {
        this.generateBreak = generateBreak;
    }

    public boolean isGenerateCreate()
    {
        return generateCreate;
    }

    public void setGenerateCreate( boolean genreateCreate )
    {
        this.generateCreate = genreateCreate;
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
        this.initializationMode = initializationMode;
    }
}
