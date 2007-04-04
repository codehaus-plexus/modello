package org.codehaus.modello.plugin.java;

/*
 * Copyright 2001-2007 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.ModelloGenerator;
import org.codehaus.modello.plugin.java.javasource.JClass;

import java.util.Iterator;

/**
 * AbstractJavaModelloGenerator - similar in scope to {@link AbstractModelloGenerator} but with features that
 * java generators can use.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractJavaModelloGenerator
    extends AbstractModelloGenerator
    implements ModelloGenerator
{
    protected void addModelImports( JClass jClass, BaseElement baseElem )
        throws ModelloException
    {
        for ( Iterator i = getModel().getInterfaces( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelInterface modelInterface = (ModelInterface) i.next();

            if ( baseElem != null && baseElem instanceof ModelInterface )
            {
                if ( modelInterface.equals( (ModelInterface) baseElem )
                    || modelInterface.getPackageName( isPackageWithVersion(), getGeneratedVersion() )
                        .equals(
                                 ( (ModelInterface) baseElem ).getPackageName( isPackageWithVersion(),
                                                                               getGeneratedVersion() ) ) )
                {
                    continue;
                }
            }

            if ( isPackageWithVersion() )
            {
                jClass.addImport( modelInterface.getPackageName( true, getGeneratedVersion() ) + "."
                    + modelInterface.getName() );
            }
            else
            {
                jClass.addImport( modelInterface.getPackageName( false, null ) + "." + modelInterface.getName() );
            }
        }

        for ( Iterator i = getModel().getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( baseElem != null && baseElem instanceof ModelClass )
            {
                if ( modelClass.equals( (ModelClass) baseElem )
                    || modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() )
                        .equals(
                                 ( (ModelClass) baseElem ).getPackageName( isPackageWithVersion(),
                                                                           getGeneratedVersion() ) ) )
                {
                    continue;
                }
            }

            if ( isPackageWithVersion() )
            {
                jClass
                    .addImport( modelClass.getPackageName( true, getGeneratedVersion() ) + "." + modelClass.getName() );
            }
            else
            {
                jClass.addImport( modelClass.getPackageName( false, null ) + "." + modelClass.getName() );
            }
        }
    }

    protected String getPrefix( JavaFieldMetadata javaFieldMetadata )
    {
        return javaFieldMetadata.isBooleanGetter() ? "is" : "get";
    }
}
