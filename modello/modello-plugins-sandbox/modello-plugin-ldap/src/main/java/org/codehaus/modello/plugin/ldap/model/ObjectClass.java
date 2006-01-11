package org.codehaus.modello.plugin.ldap.model;

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

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ObjectClass
{
    private String oid;

    private String name;

    private String description;

    private String superObjectClass;

    private boolean auxiliary;

    private List requiredAttributes = new ArrayList();

    private List notRequiredAttributes = new ArrayList();

    public ObjectClass( String oid, String name, String description, String superObjectClass, boolean auxiliary )
    {
        this.oid = oid;
        this.name = name;
        this.description = description;
        this.superObjectClass = superObjectClass;
        this.auxiliary = auxiliary;
    }

    public String getOid()
    {
        return oid;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSuperObjectClass()
    {
        return superObjectClass;
    }

    public boolean isAuxiliary()
    {
        return auxiliary;
    }

    public List getRequiredAttributes()
    {
        return requiredAttributes;
    }

    public List getNotRequiredAttributes()
    {
        return notRequiredAttributes;
    }
}
