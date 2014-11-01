package org.codehaus.modello.plugin.java;

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
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.model.ModelType;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JComment;
import org.codehaus.modello.plugin.java.javasource.JInterface;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JStructure;
import org.codehaus.modello.plugin.java.metadata.JavaClassMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaModelMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;

/**
 * AbstractJavaModelloGenerator - similar in scope to {@link AbstractModelloGenerator} but with features that
 * java generators can use.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public abstract class AbstractJavaModelloGenerator
    extends AbstractModelloGenerator
{
    protected boolean useJava5 = false;

    protected boolean domAsXpp3 = true;

    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        super.initialize( model, parameters );

        useJava5 = Boolean.valueOf( getParameter( parameters, ModelloParameterConstants.USE_JAVA5, "false" ) );

        domAsXpp3 = !"false".equals( parameters.getProperty( ModelloParameterConstants.DOM_AS_XPP3 ) );
    }

    /**
     * Create a new java source file writer, with configured encoding.
     *
     * @param packageName the package of the source file to create
     * @param className the class of the source file to create
     * @return a JSourceWriter with configured encoding
     * @throws IOException
     */
    protected JSourceWriter newJSourceWriter( String packageName, String className )
        throws IOException
    {
        String directory = packageName.replace( '.', File.separatorChar );

        File f = new File( new File( getOutputDirectory(), directory ), className + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        OutputStream os = getBuildContext().newFileOutputStream( f );

        Writer writer = ( getEncoding() == null ) ? WriterFactory.newPlatformWriter( os )
                        : WriterFactory.newWriter( os, getEncoding() );

        return new JSourceWriter( writer );
    }

    private JComment getHeaderComment()
    {
        JComment comment = new JComment();
        comment.setComment( getHeader() );
        return comment;
    }

    protected void initHeader( JClass clazz )
    {
        clazz.setHeader( getHeaderComment() );
    }

    protected void initHeader( JInterface interfaze )
    {
        interfaze.setHeader( getHeaderComment() );
    }

    protected void suppressAllWarnings( Model objectModel, JStructure structure )
    {
        JavaModelMetadata javaModelMetadata = (JavaModelMetadata) objectModel.getMetadata( JavaModelMetadata.ID );

        if ( useJava5 && javaModelMetadata.isSuppressAllWarnings() )
        {
            structure.appendAnnotation( "@SuppressWarnings( \"all\" )" );
        }
    }

    protected void addModelImports( JClass jClass, BaseElement baseElem )
        throws ModelloException
    {
        String basePackageName = null;
        if ( baseElem instanceof ModelType )
        {
            basePackageName = ( (ModelType) baseElem ).getPackageName( isPackageWithVersion(), getGeneratedVersion() );
        }

        // import interfaces
        for ( ModelInterface modelInterface : getModel().getInterfaces( getGeneratedVersion() ) )
        {
            addModelImport( jClass, modelInterface, basePackageName );
        }

        // import classes
        for ( ModelClass modelClass : getClasses( getModel() ) )
        {
            addModelImport( jClass, modelClass, basePackageName );
        }
    }

    private void addModelImport( JClass jClass, ModelType modelType, String basePackageName )
    {
        String packageName = modelType.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

        if ( !packageName.equals( basePackageName ) )
        {
            jClass.addImport( packageName + '.' + modelType.getName() );
        }
    }

    protected String getPrefix( JavaFieldMetadata javaFieldMetadata )
    {
        return javaFieldMetadata.isBooleanGetter() ? "is" : "get";
    }

    protected String getDefaultValue( ModelAssociation association )
    {
        String value = association.getDefaultValue();

        if ( useJava5 )
        {
            value = StringUtils.replaceOnce( StringUtils.replaceOnce( value, "/*", "" ), "*/", "" );
        }

        return value;
    }

    protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    protected String getJavaDefaultValue( ModelField modelField )
        throws ModelloException
    {
        String type = modelField.getType();
        String value = modelField.getDefaultValue();

        if ( "String".equals( type ) )
        {
            return '"' + escapeStringLiteral( value ) + '"';
        }
        else if ( "char".equals( type ) )
        {
            return '\'' + escapeStringLiteral( value ) + '\'';
        }
        else if ( "long".equals( type ) )
        {
            return value + 'L';
        }
        else if ( "float".equals( type ) )
        {
            return value + 'f';
        }
        else if ( "Date".equals( type ) )
        {
            DateFormat format = new SimpleDateFormat( DEFAULT_DATE_FORMAT, Locale.US );
            try
            {
                Date date = format.parse( value );
                return "new java.util.Date( " + date.getTime() + "L )";
            }
            catch ( ParseException pe )
            {
                throw new ModelloException( "Unparseable default date: " + value, pe );
            }
        }
        else if ( value != null && value.length() > 0 )
        {
            if ( "Character".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, "'" + escapeStringLiteral( value ) + "'", useJava5 );
            }
            else if ( "Boolean".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, value, true );
            }
            else if ( "Byte".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, "(byte) " + value, useJava5 );
            }
            else if ( "Short".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, "(short) " + value, useJava5 );
            }
            else if ( "Integer".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, value, useJava5 );
            }
            else if ( "Long".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, value + 'L', useJava5 );
            }
            else if ( "Float".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, value + 'f', useJava5 );
            }
            else if ( "Double".equals( type ) && !value.contains( type ) )
            {
                return newPrimitiveWrapper( type, value, useJava5 );
            }
        }

        return value;
    }

    private String newPrimitiveWrapper( String type, String value, boolean useJava5 )
    {
        if ( useJava5 )
        {
            return type + ".valueOf( " + value + " )";
        }
        else
        {
            return "new " + type + "( " + value + " )";
        }
    }

    private String escapeStringLiteral( String str )
    {
        StringBuilder buffer = new StringBuilder( str.length() + 32 );

        for ( int i = 0, n = str.length(); i < n; i++ )
        {
            char c = str.charAt( i );
            switch ( c )
            {
                case '\0':
                    buffer.append( "\\0" );
                    break;
                case '\t':
                    buffer.append( "\\t" );
                    break;
                case '\r':
                    buffer.append( "\\r" );
                    break;
                case '\n':
                    buffer.append( "\\n" );
                    break;
                case '\\':
                    buffer.append( "\\\\" );
                    break;
                default:
                    buffer.append( c );
            }
        }

        return buffer.toString();
    }

    protected String getValueChecker( String type, String value, ModelField field )
        throws ModelloException
    {
        String retVal;
        if ( "boolean".equals( type ) || "double".equals( type ) || "float".equals( type ) || "int".equals( type )
            || "long".equals( type ) || "short".equals( type ) || "byte".equals( type ) || "char".equals( type ) )
        {
            retVal = "if ( " + value + " != " + getJavaDefaultValue( field ) + " )";
        }
        else if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type )
            || ModelDefault.MAP.equals( type ) || ModelDefault.PROPERTIES.equals( type ) )
        {
            retVal = "if ( ( " + value + " != null ) && ( " + value + ".size() > 0 ) )";
        }
        else if ( "String".equals( type ) && field.getDefaultValue() != null )
        {
            retVal = "if ( ( " + value + " != null ) && !" + value + ".equals( \"" + field.getDefaultValue() + "\" ) )";
        }
        else if ( "Date".equals( type ) && field.getDefaultValue() != null )
        {
            retVal = "if ( ( " + value + " != null ) && !" + value + ".equals( " + getJavaDefaultValue( field ) + " ) )";
        }
        else
        {
            retVal = "if ( " + value + " != null )";
        }
        return retVal;
    }

    protected List<ModelClass> getClasses( Model model )
    {
        List<ModelClass> modelClasses = new ArrayList<ModelClass>();

        for ( ModelClass modelClass : model.getClasses( getGeneratedVersion() ) )
        {
            if ( isRelevant( modelClass ) )
            {
                modelClasses.add( modelClass );
            }
        }

        return modelClasses;
    }

    protected boolean isRelevant( ModelClass modelClass )
    {
        return isJavaEnabled( modelClass ) && !isTrackingSupport( modelClass );
    }

    protected boolean isJavaEnabled( ModelClass modelClass )
    {
        JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );
        return javaClassMetadata.isEnabled();
    }

    protected boolean isTrackingSupport( ModelClass modelClass )
    {
        ModelClassMetadata modelClassMetadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );
        if ( StringUtils.isNotEmpty( modelClassMetadata.getLocationTracker() ) )
        {
            return true;
        }
        if ( StringUtils.isNotEmpty( modelClassMetadata.getSourceTracker() ) )
        {
            return true;
        }
        return false;
    }

}
