package org.codehaus.modello.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.modello.ModelloRuntimeException;

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

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelInterface
    extends ModelType
{
    private String superInterface;

    public ModelInterface()
    {
        super();
    }

    public ModelInterface( Model model, String name )
    {
        super( model, name );
    }

    public void setSuperInterface( String superInterface )
    {
        this.superInterface = superInterface;
    }

    public String getSuperInterface()
    {
        return superInterface;
    }

    /**
     * {@inheritDoc}
     */
    public List<ModelField> getAllFields()
    {
        return new ArrayList<ModelField>();
    }

    /**
     * {@inheritDoc}
     */
    public List<ModelField> getAllFields( boolean withInheritedField )
    {
        return new ArrayList<ModelField>();
    }

    public ModelField getField( String type, VersionRange versionRange )
    {
        throw new ModelloRuntimeException( "There are no field '" + type + "' in an interface." );
    }

    public void validateElement()
        throws ModelValidationException
    {
    }
}
