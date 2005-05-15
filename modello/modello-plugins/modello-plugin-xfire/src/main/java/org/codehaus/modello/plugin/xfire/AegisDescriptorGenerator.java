package org.codehaus.modello.plugin.xfire;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.xfire.aegis.type.basic.XMLTypeInfo;
import org.codehaus.xfire.util.NamespaceHelper;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @version $Id$
 */
public class AegisDescriptorGenerator
    extends AbstractModelloGenerator
{
    public void generate(Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateDescriptors();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateDescriptors()
        throws ModelloException, IOException
    {
        Model model = getModel();
        Version version = getGeneratedVersion();
        
        String ns = NamespaceHelper.makeNamespaceFromClassName(getPackageName(model), "http");

        // Class descriptors        
        for ( Iterator i = model.getClasses( version ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();
            
            FileWriter writer = new FileWriter( getOutputFile(modelClass) );

            XMLWriter w = new PrettyPrintXMLWriter( writer );

            writer.write( "<?xml version=\"1.0\"?>\n" );

            w.startElement( "mappings" );
            w.addAttribute( "xmlns", XMLTypeInfo.MAPPING_NS );
            w.addAttribute( "xmlns:ns1", ns );

            w.startElement( "mapping" );
            
            // w.addAttribute( "uri", "" );

            writeFieldsForModelClass(model, modelClass, version, w);

            w.endElement();

            w.endElement();

            writer.flush();

            writer.close();
        }
    }

    private void writeFieldsForModelClass(Model model, ModelClass modelClass, Version version, XMLWriter w)
    {
        for ( Iterator j = modelClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
        {
            ModelField field = (ModelField) j.next();

            XFireFieldMetadata fieldMetadata = (XFireFieldMetadata) field.getMetadata( XFireFieldMetadata.ID );

            if (fieldMetadata.isIgnore())
            {
                break;
            }
            
            if ( fieldMetadata.isAttribute() )
            {
                w.startElement( "attribute" );
                
                w.addAttribute("property", field.getName());
                w.addAttribute("name", "ns1:" + field.getName());
                
                w.endElement();
            }
            else if (field instanceof ModelAssociation && 
                     ((ModelAssociation) field).getMultiplicity().equals(ModelAssociation.MANY_MULTIPLICITY))
            {
                ModelAssociation association = (ModelAssociation) field;
                
                w.startElement( "collection" );
                
                w.addAttribute("property", field.getName());
                w.addAttribute("name", "ns1:" + field.getName());
                
                String to = association.getTo();
                if (isClassInModel( association.getTo(), modelClass.getModel() ))
                {
                    w.addAttribute("componentType", 
                                   getPackageName(association.getToClass()) + "." + 
                                       association.getToClass().getName());
                }
                else
                {
                    if (to.equals("String"))
                        to = "java.lang.String";
                    
                    w.addAttribute("componentType", to);
                }
                
                w.endElement();
            }
            else
            {
                w.startElement( "element" );
                
                w.addAttribute("property", field.getName());
                w.addAttribute("name", "ns1:" + field.getName());
                
                w.endElement();
            }
        }
        
        String superclass = modelClass.getSuperClass();
        if (superclass != null)
        {
            ModelClass supertype = model.getClass(superclass, version);
            
            if (supertype != null)
            {
                writeFieldsForModelClass(model, supertype, version, w);
            }
        }
    }
    
    private List getVersions(Model model)
    {
        ArrayList versions = new ArrayList();

        return versions;
    }

    public File getOutputFile(ModelClass modelClass)
    {
        Model objectModel = getModel();

        String packageName = getPackageName(modelClass);

        String directory = packageName.replace( '.', '/' );

        File f = new File( new File( getOutputDirectory(), directory ), modelClass.getName() + ".aegis.xml" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }
        
        return f;
    }

    private String getPackageName(Model objectModel)
    {
        String packageName;
        if ( isPackageWithVersion() )
        {
            packageName = objectModel.getPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = objectModel.getPackageName( false, null );
        }
        return packageName;
    }

    private String getPackageName(ModelClass classModel)
    {
        String packageName;
        if ( isPackageWithVersion() )
        {
            packageName = classModel.getPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = classModel.getPackageName( false, null );
        }
        return packageName;
    }
}
