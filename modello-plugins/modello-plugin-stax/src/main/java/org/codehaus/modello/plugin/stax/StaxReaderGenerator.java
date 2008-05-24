package org.codehaus.modello.plugin.stax;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionDefinition;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugins.xml.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id: StaxReaderGenerator.java 674 2006-11-15 08:19:45Z brett $
 */
public class StaxReaderGenerator
    extends AbstractStaxGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateStaxReaders();

            VersionDefinition versionDefinition = model.getVersionDefinition();
            if ( versionDefinition != null )
            {
                String versions = parameters.getProperty( ModelloParameterConstants.ALL_VERSIONS );

                if ( versions != null )
                {
                    generateStaxReaderDelegate( Arrays.asList( versions.split( "," ) ) );
                }
            }
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating StAX Reader.", ex );
        }
    }

    private void generateStaxReaders()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName;

        if ( isPackageWithVersion() )
        {
            packageName = objectModel.getDefaultPackageName( true, getGeneratedVersion() );
        }
        else
        {
            packageName = objectModel.getDefaultPackageName( false, null );
        }

        packageName += ".io.stax";

        String unmarshallerName = getFileName( "StaxReader" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( unmarshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "java.io.IOException" );

        jClass.addImport( "java.io.Reader" );
        
        jClass.addImport( "java.io.File" );

        jClass.addImport( "java.io.FileInputStream" );
        
        jClass.addImport( "java.io.StringWriter" );

        jClass.addImport( "java.io.StringReader" );

        jClass.addImport( "java.io.ByteArrayInputStream" );

        jClass.addImport( "java.io.InputStreamReader" );

        jClass.addImport( "java.text.DateFormat" );

        jClass.addImport( "java.text.ParsePosition" );

        jClass.addImport( "java.util.regex.Matcher" );

        jClass.addImport( "java.util.regex.Pattern" );

        jClass.addImport( "java.util.Locale" );

        jClass.addImport( "javax.xml.stream.*" );

        jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write the parse method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        GeneratorNode rootNode = findRequiredReferenceResolvers( root, null );

        writeReferenceResolvers( rootNode, jClass );
        for ( Iterator i = rootNode.getNodesWithReferencableChildren().values().iterator(); i.hasNext(); )
        {
            GeneratorNode node = (GeneratorNode) i.next();
            writeReferenceResolvers( node, jClass );
        }

        JClass returnType = new JClass( root.getName() );
        JMethod method = new JMethod( returnType, "read" );

        method.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( reader );" );

        sc.add( "" );

        sc.add( "String encoding = xmlStreamReader.getCharacterEncodingScheme();" );

        sc.add( returnType + " value = parse" + root.getName() + "( \"" + getTagName( root ) +
            "\", xmlStreamReader, strict, encoding );" );

        sc.add( "resolveReferences( value );" );

        sc.add( "return value;" );

        jClass.addMethod( method );

        method = new JMethod( returnType, "read" );

        method.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();
        sc.add( "return read( reader, true );" );

        jClass.addMethod( method );

        method = new JMethod( returnType, "read" );

        method.addParameter( new JParameter( new JClass( "String" ), "filePath" ) );

        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();

        sc.add( "File file = new File(filePath);" );
        
        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( file.toURL().toExternalForm(), new FileInputStream(file) );" );

        sc.add( "" );

        sc.add( "String encoding = xmlStreamReader.getCharacterEncodingScheme();" );

        sc.add( returnType + " value = parse" + root.getName() + "( \"" + getTagName( root ) +
            "\", xmlStreamReader, strict, encoding );" );

        sc.add( "resolveReferences( value );" );

        sc.add( "return value;" );

        jClass.addMethod( method );

        method = new JMethod( returnType, "read" );

        method.addParameter( new JParameter( new JClass( "String" ), "filePath" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();
        sc.add( "return read( filePath, true );" );

        jClass.addMethod( method );

        // Determine the version. Currently, it causes the document to be reparsed, but could be made more effecient in
        // future by buffering the read XML and piping that into any consequent read method.

        VersionDefinition versionDefinition = objectModel.getVersionDefinition();
        if ( versionDefinition != null )
        {
            writeDetermineVersionMethod( jClass, objectModel );
        }

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        writeBuildDomMethod( jClass );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void generateStaxReaderDelegate( List/*<String>*/ versions )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( false, null );

        packageName += ".io.stax";

        String directory = packageName.replace( '.', '/' );

        String unmarshallerName = getFileName( "StaxReaderDelegate" );

        File f = new File( new File( getOutputDirectory(), directory ), unmarshallerName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        Writer writer = WriterFactory.newPlatformWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( unmarshallerName );

        jClass.setPackageName( packageName );

        jClass.addImport( "java.io.File" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );

        jClass.addImport( "javax.xml.stream.*" );

        jClass.addImport( "org.codehaus.plexus.util.IOUtil" );
        jClass.addImport( "org.codehaus.plexus.util.ReaderFactory" );

        JMethod method = new JMethod( new JClass( "Object" ), "read" );

        method.addParameter( new JParameter( new JClass( "File" ), "f" ) );

        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        sc.add( "String modelVersion;" );
        sc.add( "Reader reader = ReaderFactory.newXmlReader( f );" );

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        sc.add( "modelVersion = determineVersion( reader );" );

        sc.unindent();
        sc.add( "}" );
        sc.add( "finally" );
        sc.add( "{" );
        sc.indent();

        sc.add( "IOUtil.close( reader );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "reader = ReaderFactory.newXmlReader( f );" );
        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        writeModelVersionHack( sc );

        String prefix = "";
        for ( Iterator i = versions.iterator(); i.hasNext(); )
        {
            String version = (String) i.next();

            sc.add( prefix + "if ( \"" + version + "\".equals( modelVersion ) )" );
            sc.add( "{" );
            sc.indent();

            sc.add( "return new " + getModel().getDefaultPackageName( true, new Version( version ) ) + ".io.stax." +
                getFileName( "StaxReader" ) + "().read( reader, strict );" );

            sc.unindent();
            sc.add( "}" );

            prefix = "else ";
        }

        sc.add( "else" );
        sc.add( "{" );
        sc.indent();

        sc.add(
            "throw new XMLStreamException( \"Document version '\" + modelVersion + \"' has no corresponding reader.\" );" );

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );
        sc.add( "finally" );
        sc.add( "{" );
        sc.indent();

        sc.add( "IOUtil.close( reader );" );

        sc.unindent();
        sc.add( "}" );

        method = new JMethod( new JClass( "Object" ), "read" );

        method.addParameter( new JParameter( new JClass( "File" ), "f" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();
        sc.add( "return read( f, true );" );

        jClass.addMethod( method );

        writeDetermineVersionMethod( jClass, objectModel );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }

    private static void writeModelVersionHack( JSourceCode sc )
    {
        sc.add( "// legacy hack for pomVersion == 3" );
        sc.add( "if ( modelVersion.equals( \"3\" ) ) modelVersion = \"3.0.0\";" );
    }

    private void writeDetermineVersionMethod( JClass jClass, Model objectModel )
        throws ModelloException
    {
        VersionDefinition versionDefinition = objectModel.getVersionDefinition();

        JMethod method = new JMethod( new JClass( "String" ), "determineVersion" );

        method.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( reader );" );

        sc.add( "while ( xmlStreamReader.hasNext() )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "int eventType = xmlStreamReader.next();" );

        sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );

        sc.add( "{" );

        sc.indent();

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );
        if ( "namespace".equals( versionDefinition.getType() ) )
        {
            XmlClassMetadata metadata = (XmlClassMetadata) root.getMetadata( XmlClassMetadata.ID );

            String namespace = metadata.getNamespace();
            if ( namespace == null || namespace.indexOf( "${version}" ) < 0 )
            {
                throw new ModelloException(
                    "versionDefinition is namespace, but the model does not declare xml.namespace on the root element" );
            }

            sc.add( "return getVersionFromRootNamespace( xmlStreamReader );" );

            writeNamespaceVersionGetMethod( namespace, jClass );
        }
        else
        {
            String value = versionDefinition.getValue();
            ModelField field = root.getField( value, getGeneratedVersion() );

            if ( field == null )
            {
                throw new ModelloException(
                    "versionDefinition is field, but the model root element does not declare a field '" + value +
                        "'." );
            }

            if ( !"String".equals( field.getType() ) )
            {
                throw new ModelloException( "versionDefinition is field, but the field is not of type String" );
            }

            sc.add( "return getVersionFromField( xmlStreamReader );" );

            writeFieldVersionGetMethod( field, jClass );
        }

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "throw new XMLStreamException( \"Version not found in document\", xmlStreamReader.getLocation() );" );

        jClass.addMethod( method );
    }

    private static void writeFieldVersionGetMethod( ModelField field, JClass jClass )
    {
        JMethod method = new JMethod( new JType( "String" ), "getVersionFromField" );
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );
        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        XmlFieldMetadata metadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );
        String value = metadata.getTagName();
        if ( value == null )
        {
            value = field.getName();
        }

        // we are now at the root element. Search child elements for the correct tag name

        sc.add( "int depth = 0;" );

        sc.add( "while ( depth >= 0 )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "int eventType = xmlStreamReader.next();" );

        sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );
        sc.add( "{" );

        sc.indent();

        sc.add( "if ( depth == 0 && \"" + value + "\".equals( xmlStreamReader.getLocalName() ) )" );
        sc.add( "{" );

        sc.indent();

        sc.add( "return xmlStreamReader.getElementText();" );

        sc.unindent();

        sc.add( "}" );

        if ( field.getAlias() != null )
        {
            sc.add( "if ( depth == 0 && \"" + field.getAlias() + "\".equals( xmlStreamReader.getLocalName() ) )" );
            sc.add( "{" );

            sc.indent();

            sc.add( "return xmlStreamReader.getElementText();" );

            sc.unindent();

            sc.add( "}" );
        }

        sc.add( "depth++;" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "if ( eventType == XMLStreamConstants.END_ELEMENT )" );
        sc.add( "{" );

        sc.indent();

        sc.add( "depth--;" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "throw new XMLStreamException( \"Field: '" + value +
            "' does not exist in the document.\", xmlStreamReader.getLocation() );" );
    }

    private static void writeNamespaceVersionGetMethod( String namespace, JClass jClass )
    {
        JMethod method = new JMethod( new JType( "String" ), "getVersionFromRootNamespace" );
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );
        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        sc.add( "String uri = xmlStreamReader.getNamespaceURI( \"\" );" );

        sc.add( "if ( uri == null )" );

        sc.add( "{" );

        sc.indent();

        sc.add(
            "throw new XMLStreamException( \"No namespace specified, but versionDefinition requires it\", xmlStreamReader.getLocation() );" );

        sc.unindent();

        sc.add( "}" );

        int index = namespace.indexOf( "${version}" );

        sc.add( "String uriPrefix = \"" + namespace.substring( 0, index ) + "\";" );
        sc.add( "String uriSuffix = \"" + namespace.substring( index + 10 ) + "\";" );

        sc.add( "if ( !uri.startsWith( uriPrefix ) || !uri.endsWith( uriSuffix ) )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new XMLStreamException( \"Namespace URI: '\" + uri + \"' does not match pattern '" + namespace +
            "'\", xmlStreamReader.getLocation() );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return uri.substring( uriPrefix.length(), uri.length() - uriSuffix.length() );" );
    }

    private String getTagName( ModelClass root )
    {
        XmlClassMetadata metadata = (XmlClassMetadata) root.getMetadata( XmlClassMetadata.ID );

        String tagName = metadata.getTagName();

        if ( tagName != null )
        {
            return tagName;
        }

        return uncapitalise( root.getName() );
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
        throws ModelloException
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass clazz = (ModelClass) i.next();

            if ( root.getName().equals( clazz.getName() ) )
            {
                writeClassParser( clazz, jClass, true );
            }
            else
            {
                writeClassParser( clazz, jClass, false );
            }
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
        throws ModelloException
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JClass returnType = new JClass( className );
        JMethod unmarshall = new JMethod( returnType, "parse" + capClassName );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );

        unmarshall.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );

        unmarshall.addParameter( new JParameter( JType.Boolean, "strict" ) );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "encoding" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        unmarshall.addException( new JClass( "XMLStreamException" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        sc.add( uncapClassName + ".setModelEncoding( encoding );" );

        sc.add( "java.util.Set parsed = new java.util.HashSet();" );

        String instanceFieldName = getInstanceFieldName( className );

        if ( rootElement )
        {
            sc.add( "boolean foundRoot = false;" );

            sc.add( "while ( xmlStreamReader.hasNext() )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "int eventType = xmlStreamReader.next();" );

            sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );
        }
        else
        {
            writeAttributes( modelClass, uncapClassName, sc );

            if ( isAssociationPartToClass( modelClass ) )
            {
                jClass.addField( new JField( new JType( "java.util.Map" ), instanceFieldName ) );

                sc.add( "if ( " + instanceFieldName + " == null )" );
                sc.add( "{" );
                sc.indent();

                sc.add( instanceFieldName + " = new java.util.HashMap();" );

                sc.unindent();
                sc.add( "}" );

                sc.add( "String v = xmlStreamReader.getAttributeValue( null, \"modello.id\" );" );
                sc.add( "if ( v != null )" );
                sc.add( "{" );
                sc.indent();

                sc.add( instanceFieldName + ".put( v, " + uncapClassName + ");" );

                sc.unindent();
                sc.add( "}" );
            }

            sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );
        }

        sc.add( "{" );

        sc.indent();

        String statement = "if";

        if ( rootElement )
        {
            sc.add( "if ( xmlStreamReader.getLocalName().equals( tagName ) )" );

            sc.add( "{" );

            sc.indent();

            VersionDefinition versionDefinition = modelClass.getModel().getVersionDefinition();
            if ( versionDefinition != null && "namespace".equals( versionDefinition.getType() ) )
            {
                sc.add( "String modelVersion = getVersionFromRootNamespace( xmlStreamReader );" );

                writeModelVersionCheck( sc );
            }

            writeAttributes( modelClass, uncapClassName, sc );

            sc.add( "foundRoot = true;" );

            sc.unindent();

            sc.add( "}" );

            statement = "else if";
        }

        //Write other fields

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( !fieldMetadata.isAttribute() )
            {
                processField( fieldMetadata, field, statement, sc, uncapClassName, modelClass, rootElement, jClass );

                statement = "else if";
            }
        }
        if ( !rootElement )
        {
/*
            if ( modelClass.getFields( getGeneratedVersion() ).size() > 0 )
            {
                sc.add( "else" );

                sc.add( "{" );

                sc.indent();

                sc.add( "parser.nextText();" );

                sc.unindent();

                sc.add( "}" );
            }
*/

            if ( statement.startsWith( "else" ) )
            {
                sc.add( "else" );

                sc.add( "{" );

                sc.indent();
            }

            sc.add( "if ( strict )" );

            sc.add( "{" );

            sc.indent();

            sc.add(
                "throw new XMLStreamException( \"Unrecognised tag: '\" + xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation() );" );

            sc.unindent();

            sc.add( "}" );

            if ( statement.startsWith( "else" ) )
            {
                sc.unindent();

                sc.add( "}" );
            }
        }
        else
        {
            sc.add( "else" );

            sc.add( "{" );

            sc.indent();

            sc.add( "if ( foundRoot )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "if ( strict )" );

            sc.add( "{" );

            sc.indent();

            sc.add(
                "throw new XMLStreamException( \"Unrecognised tag: '\" + xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation() );" );

            sc.unindent();

            sc.add( "}" );

            sc.unindent();

            sc.add( "}" );

            sc.unindent();

            sc.add( "}" );

            sc.unindent();

            sc.add( "}" );
        }

        sc.unindent();

        sc.add( "}" );

        // This must be last so that we guarantee the ID has been filled already
        if ( isAssociationPartToClass( modelClass ) )
        {
            List identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

            if ( identifierFields.size() == 1 )
            {
                ModelField field = (ModelField) identifierFields.get( 0 );

                String v = uncapClassName + ".get" + capitalise( field.getName() ) + "()";
                v = getValue( field.getType(), v, (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID ) );
                sc.add( instanceFieldName + ".put( " + v + ", " + uncapClassName + ");" );
            }
        }

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    private GeneratorNode findRequiredReferenceResolvers( ModelClass modelClass, GeneratorNode parent )
        throws ModelloException
    {
        String className = modelClass.getName();

        GeneratorNode value = new GeneratorNode( className, parent );

        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            if ( field instanceof ModelAssociation )
            {
                ModelAssociation association = (ModelAssociation) field;

                if ( isClassInModel( association.getTo(), getModel() ) )
                {
                    ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                    GeneratorNode child = null;
                    if ( referenceIdentifierField != null )
                    {
                        child = new GeneratorNode( association, parent );
                        child.setReferencable( true );
                    }
                    else
                    {
                        if ( !value.getChain().contains( association.getTo() ) )
                        {
                            // descend into child
                            child = findRequiredReferenceResolvers( association.getToClass(), value );
                            child.setAssociation( association );
                        }
                    }
                    if ( child != null )
                    {
                        value.addChild( child );
                    }
                }
            }
        }

        // propogate the flag up
        for ( Iterator i = value.getChildren().iterator(); i.hasNext(); )
        {
            GeneratorNode child = (GeneratorNode) i.next();

            if ( child.isReferencable() || child.isReferencableChildren() )
            {
                value.setReferencableChildren( true );
            }

            value.addNodesWithReferencableChildren( child.getNodesWithReferencableChildren() );
        }

        return value;
    }

    private void writeReferenceResolvers( GeneratorNode node, JClass jClass )
    {
        JMethod unmarshall = new JMethod( "resolveReferences" );

        unmarshall.addParameter( new JParameter( new JClass( node.getTo() ), "value" ) );

        unmarshall.getModifiers().makePrivate();

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "java.util.Map refs;" );

        for ( Iterator i = node.getChildren().iterator(); i.hasNext(); )
        {
            GeneratorNode child = (GeneratorNode) i.next();

            if ( child.isReferencable() )
            {
                ModelAssociation association = child.getAssociation();
                String refFieldName = getRefFieldName( association );
                String to = association.getTo();
                String instanceFieldName = getInstanceFieldName( to );

                sc.add( "if ( " + refFieldName + " != null )" );
                sc.add( "{" );
                sc.indent();

                sc.add( "refs = (java.util.Map) " + refFieldName + ".get( value );" );

                sc.add( "if ( refs != null )" );
                sc.add( "{" );
                sc.indent();

                String capAssocName = capitalise( association.getName() );
                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    sc.add( "String id = (String) refs.get( \"" + association.getName() + "\" );" );
                    sc.add( to + " ref = (" + to + ") " + instanceFieldName + ".get( id );" );

                    // Don't set if it already is, since the Java plugin generates create/break that will throw an
                    // exception

                    sc.add( "if ( ref != null && !ref.equals( value.get" + capAssocName + "() ) )" );
                    sc.add( "{" );
                    sc.indent();

                    sc.add( "value.set" + capAssocName + "( ref );" );

                    sc.unindent();
                    sc.add( "}" );
                }
                else
                {
                    sc.add( "for ( int i = 0; i < value.get" + capAssocName + "().size(); i++ )" );
                    sc.add( "{" );
                    sc.indent();

                    sc.add( "String id = (String) refs.get( \"" + association.getName() + ".\" + i );" );
                    sc.add( to + " ref = (" + to + ") " + instanceFieldName + ".get( id );" );
                    sc.add( "if ( ref != null )" );
                    sc.add( "{" );
                    sc.indent();

                    sc.add( "value.get" + capAssocName + "().set( i, ref );" );

                    sc.unindent();
                    sc.add( "}" );

                    sc.unindent();
                    sc.add( "}" );
                }

                sc.unindent();
                sc.add( "}" );

                sc.unindent();
                sc.add( "}" );
            }

            if ( child.isReferencableChildren() )
            {
                ModelAssociation association = child.getAssociation();
                if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    sc.add( "resolveReferences( value.get" + capitalise( association.getName() ) + "() );" );
                }
                else
                {
                    sc.add( "for ( java.util.Iterator i = value.get" + capitalise( association.getName() ) +
                        "().iterator(); i.hasNext(); )" );
                    sc.add( "{" );
                    sc.indent();

                    sc.add( "resolveReferences( (" + association.getTo() + ") i.next() );" );

                    sc.unindent();
                    sc.add( "}" );
                }
            }
        }

        jClass.addMethod( unmarshall );
    }

    private static String getRefFieldName( ModelAssociation association )
    {
        return uncapitalise( association.getTo() ) + "References";
    }

    private static String getInstanceFieldName( String to )
    {
        return uncapitalise( to ) + "Instances";
    }

    private void writeAttributes( ModelClass modelClass, String uncapClassName, JSourceCode sc )
        throws ModelloException
    {
        for ( Iterator i = modelClass.getAllFields( getGeneratedVersion(), true ).iterator(); i.hasNext(); )
        {
            ModelField field = (ModelField) i.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( fieldMetadata.isAttribute() )
            {
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ),
                                     sc );
            }
        }
    }

    private void processField( XmlFieldMetadata fieldMetadata, ModelField field, String statement, JSourceCode sc,
                               String uncapClassName, ModelClass modelClass, boolean rootElement, JClass jClass )
        throws ModelloException
    {
        String tagName = fieldMetadata.getTagName();

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        String singularTagName = fieldMetadata.getAssociationTagName();

        if ( singularTagName == null )
        {
            singularTagName = singular( tagName );
        }

        boolean wrappedList = XmlFieldMetadata.LIST_STYLE_WRAPPED.equals( fieldMetadata.getListStyle() );

        String capFieldName = capitalise( field.getName() );

        String singularName = singular( field.getName() );

        String optionalCheck = "";
        if ( field.getAlias() != null && field.getAlias().length() > 0 )
        {
            optionalCheck = "|| xmlStreamReader.getLocalName().equals( \"" + field.getAlias() + "\" ) ";
        }

        String tagComparison =
            statement + " ( xmlStreamReader.getLocalName().equals( \"" + tagName + "\" ) " + optionalCheck + " )";

        if ( field instanceof ModelAssociation )
        {
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
            {
                sc.add( tagComparison );

                sc.add( "{" );

                sc.indent();

                addCodeToCheckIfParsed( sc, tagName );

                ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                if ( referenceIdentifierField != null )
                {
                    addCodeToAddReferences( association, jClass, sc, referenceIdentifierField, uncapClassName );

                    // gobble the rest of the tag
                    sc.add( "while ( xmlStreamReader.getEventType() != XMLStreamConstants.END_ELEMENT )" );
                    sc.add( "{" );
                    sc.indent();
                    sc.add( "xmlStreamReader.next();" );
                    sc.unindent();
                    sc.add( "}" );
                }
                else
                {
                    sc.add( uncapClassName + ".set" + capFieldName + "( parse" + association.getTo() + "( \"" +
                        tagName + "\", xmlStreamReader, strict, encoding ) );" );
                }

                sc.unindent();

                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    if ( wrappedList )
                    {
                        sc.add( tagComparison );

                        sc.add( "{" );

                        sc.indent();

                        addCodeToCheckIfParsed( sc, tagName );

                        sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "if ( xmlStreamReader.getLocalName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();
                    }
                    else
                    {
                        sc.add(
                            statement + " ( xmlStreamReader.getLocalName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( type + " " + associationName + " = " + uncapClassName + ".get" + capFieldName + "();" );

                        sc.add( "if ( " + associationName + " == null )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( uncapClassName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.unindent();

                        sc.add( "}" );
                    }

                    if ( isClassInModel( association.getTo(), modelClass.getModel() ) )
                    {
                        ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                        if ( referenceIdentifierField != null )
                        {
                            addCodeToAddReferences( association, jClass, sc, referenceIdentifierField, uncapClassName );
                        }

                        if ( association.getTo().equals( modelClass.getName() ) )
                        {
                            // HACK: the addXXX method will cause an OOME when compiling a self-referencing class, so we
                            //  just add it to the array. This could disrupt the links if you are using break/create
                            //  constraints in modello.
                            sc.add( associationName + ".add( parse" + association.getTo() + "( \"" + singularTagName +
                                "\", xmlStreamReader, strict, encoding ) );" );
                        }
                        else
                        {
                            sc.add( uncapClassName + ".add" + capitalise( singular( associationName ) ) + "( parse" +
                                association.getTo() + "( \"" + singularTagName +
                                "\", xmlStreamReader, strict, encoding ) );" );
                        }
                    }
                    else
                    {
                        writePrimitiveField( association, association.getTo(), associationName, "add", sc );
                    }

                    if ( wrappedList )
                    {
                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add(
                            "throw new XMLStreamException( \"Unrecognised tag: '\" + xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation() );" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );
                    }
                    else
                    {

                        sc.unindent();

                        sc.add( "}" );
                    }
                }
                else
                {
                    //Map or Properties

                    sc.add( tagComparison );

                    sc.add( "{" );

                    sc.indent();

                    addCodeToCheckIfParsed( sc, tagName );

                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                    if ( XmlAssociationMetadata.EXPLODE_MODE.equals( xmlAssociationMetadata.getMapStyle() ) )
                    {
                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "if ( xmlStreamReader.getLocalName().equals( \"" + singularTagName + "\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "//" + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "if ( xmlStreamReader.getLocalName().equals( \"key\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "key = xmlStreamReader.getElementText();" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else if ( xmlStreamReader.getLocalName().equals( \"value\" ) )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "value = xmlStreamReader.getElementText()" );

                        if ( fieldMetadata.isTrim() )
                        {
                            sc.add( ".trim()" );
                        }

                        sc.add( ";" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "xmlStreamReader.getText();" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();

                        sc.add( "}" );

                        sc.add( "xmlStreamReader.next();" );

                        sc.unindent();

                        sc.add( "}" );
                    }
                    else
                    {
                        //INLINE Mode

                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );

                        sc.indent();

                        sc.add( "String key = xmlStreamReader.getLocalName();" );

                        sc.add( "String value = xmlStreamReader.getElementText()" );

                        if ( fieldMetadata.isTrim() )
                        {
                            sc.add( ".trim()" );
                        }

                        sc.add( ";" );

                        sc.add( uncapClassName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();

                        sc.add( "}" );
                    }

                    sc.unindent();

                    sc.add( "}" );
                }
            }
        }
        else
        {
            sc.add( tagComparison );

            sc.add( "{" );

            sc.indent();

            addCodeToCheckIfParsed( sc, tagName );

            //ModelField
            writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ), sc );

            if ( rootElement && field.isModelVersionField() )
            {
                sc.add( "String modelVersion = " + uncapClassName + ".get" + capitalise( field.getName() ) + "();" );

                writeModelVersionCheck( sc );
            }

            sc.unindent();

            sc.add( "}" );
        }
    }

    private static void addCodeToAddReferences( ModelAssociation association, JClass jClass, JSourceCode sc,
                                                ModelField referenceIdentifierField, String referredFromClass )
    {
        String refFieldName = getRefFieldName( association );
        if ( jClass.getField( refFieldName ) == null )
        {
            jClass.addField( new JField( new JType( "java.util.Map" ), refFieldName ) );
        }

        sc.add( "String value = xmlStreamReader.getAttributeValue( null, \"" + referenceIdentifierField.getName() +
            "\" );" );
        sc.add( "if ( value != null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "// This is a reference to an element elsewhere in the model" );
        sc.add( "if ( " + refFieldName + " == null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( refFieldName + " = new java.util.HashMap();" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "java.util.Map refs = (java.util.Map) " + refFieldName + ".get( " + referredFromClass + " );" );
        sc.add( "if ( refs == null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "refs = new java.util.HashMap();" );
        sc.add( refFieldName + ".put( " + referredFromClass + ", refs );" );

        sc.unindent();
        sc.add( "}" );

        if ( ModelAssociation.ONE_MULTIPLICITY.equals( association.getMultiplicity() ) )
        {
            sc.add( "refs.put( \"" + association.getName() + "\", value );" );
        }
        else
        {
            sc.add( "refs.put( \"" + association.getName() + ".\" + " + association.getName() + ".size(), value );" );
        }
        sc.unindent();
        sc.add( "}" );
    }

    private void writeModelVersionCheck( JSourceCode sc )
    {
        writeModelVersionHack( sc );

        sc.add( "if ( !modelVersion.equals( \"" + getGeneratedVersion() + "\" ) )" );
        sc.add( "{" );
        sc.indent();

        sc.add(
            "throw new XMLStreamException( \"Document model version of '\" + modelVersion + \"' doesn't match reader version of '" +
                getGeneratedVersion() + "'\", xmlStreamReader.getLocation() );" );

        sc.unindent();
        sc.add( "}" );
    }

    private void addCodeToCheckIfParsed( JSourceCode sc, String tagName )
    {
        sc.add( "if ( parsed.contains( \"" + tagName + "\" ) )" );

        sc.add( "{" );

        sc.indent();

        sc.add(
            "throw new XMLStreamException( \"Duplicated tag: '\" + xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation() );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "parsed.add( \"" + tagName + "\" );" );
    }

    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc )
    {
        XmlFieldMetadata fieldMetaData = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = fieldMetaData.getTagName();

        if ( tagName == null )
        {
            tagName = field.getName();
        }

        String parserGetter;
        if ( fieldMetaData.isAttribute() )
        {
            parserGetter = "xmlStreamReader.getAttributeValue( null, \"" + tagName + "\" )";
        }
        else
        {
            parserGetter = "xmlStreamReader.getElementText()";
        }

/* TODO:
        if ( fieldMetaData.isRequired() )
        {
            parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser, strict, encoding )";
        }
*/
        if ( field.getDefaultValue() != null )
        {
            parserGetter = "getDefaultValue( " + parserGetter + ", \"" + field.getDefaultValue() + "\" )";
        }

        if ( fieldMetaData.isTrim() )
        {
            parserGetter = "getTrimmedValue( " + parserGetter + ")";
        }

        if ( "boolean".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getBooleanValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader ) );" );
        }
        else if ( "char".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getCharacterValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader ) );" );
        }
        else if ( "double".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getDoubleValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader, strict ) );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getFloatValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader, strict ) );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getIntegerValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader, strict ) );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getLongValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader, strict ) );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getShortValue( " + parserGetter + ", \"" + tagName +
                "\", xmlStreamReader, strict ) );" );
        }
        else if ( "String".equals( type ) || "Boolean".equals( type ) )
        {
            // TODO: other Primitive types
            sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
        }
        else if ( "Date".equals( type ) )
        {
            sc.add( "String dateFormat = " +
                ( fieldMetaData.getFormat() != null ? "\"" + fieldMetaData.getFormat() + "\"" : "null" ) + ";" );
            sc.add( objectName + "." + setterName + "( getDateValue( " + parserGetter + ", \"" + tagName +
                "\", dateFormat, xmlStreamReader ) );" );
        }
        else if ( "DOM".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( buildDom( xmlStreamReader ) );" );
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }
    }

    private void writeBuildDomMethod( JClass jClass )
    {
        JMethod method = new JMethod( new JType( "Xpp3Dom" ), "buildDom" );
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "java.util.List elements = new java.util.ArrayList();" );

        sc.add( "java.util.List values = new java.util.ArrayList();" );

        sc.add( "int eventType = xmlStreamReader.getEventType();" );

        sc.add( "while ( xmlStreamReader.hasNext() )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String rawName = xmlStreamReader.getLocalName();" );

        sc.add( "Xpp3Dom childConfiguration = new Xpp3Dom( rawName );" );

        sc.add( "int depth = elements.size();" );

        sc.add( "if ( depth > 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "Xpp3Dom parent = (Xpp3Dom) elements.get( depth - 1 );" );

        sc.add( "parent.addChild( childConfiguration );" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "elements.add( childConfiguration );" );

        sc.add( "if ( xmlStreamReader.isEndElement() )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "values.add( null );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.indent();
        sc.add( "values.add( new StringBuffer() );" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "int attributesSize = xmlStreamReader.getAttributeCount();" );

        sc.add( "for ( int i = 0; i < attributesSize; i++ )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String name = xmlStreamReader.getAttributeLocalName( i );" );

        sc.add( "String value = xmlStreamReader.getAttributeValue( i );" );

        sc.add( "childConfiguration.setAttribute( name, value );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XMLStreamConstants.CHARACTERS )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "int depth = values.size() - 1;" );
        sc.add( "StringBuffer valueBuffer = (StringBuffer) values.get( depth );" );

        sc.add( "String text = xmlStreamReader.getText();" );

        sc.add( "text = text.trim();" );

        sc.add( "valueBuffer.append( text );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XMLStreamConstants.END_ELEMENT )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "int depth = elements.size() - 1;" );

        sc.add( "Xpp3Dom finishedConfiguration = (Xpp3Dom) elements.remove( depth );" );

        sc.add( "// this Object could be null if it is a singleton tag" );
        sc.add( "Object accumulatedValue = values.remove( depth );" );

        sc.add( "if ( finishedConfiguration.getChildCount() == 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "if ( accumulatedValue == null )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "finishedConfiguration.setValue( null );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.indent();
        sc.add( "finishedConfiguration.setValue( accumulatedValue.toString() );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "if ( depth == 0 )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "return finishedConfiguration;" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "eventType = xmlStreamReader.next();" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "throw new IllegalStateException( \"End of document found before returning to 0 depth\" );" );

        jClass.addMethod( method );
    }

    private void writeHelpers( JClass jClass )
    {
        JMethod method = new JMethod( new JClass( "String" ), "getTrimmedValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "s = s.trim();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "String" ), "getDefaultValue" );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "v" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "s = v;" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "String" ), "getRequiredAttributeValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );

        sc.indent();

        sc.add(
            "throw new XMLStreamException( \"Missing required value for attribute '\" + attribute + \"'\", xmlStreamReader.getLocation() );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Boolean, "getBooleanValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return Boolean.valueOf( s ).booleanValue();" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return false;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Char, "getCharacterValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return s.charAt( 0 );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        method = new JMethod( JType.Int, "getIntegerValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Integer.valueOf( s ).intValue()", "an integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Short, "getShortValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Short.valueOf( s ).shortValue()", "a short integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Long, "getLongValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Long.valueOf( s ).longValue()", "a long integer" );

        jClass.addMethod( method );

        method = new JMethod( JType.Float, "getFloatValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Float.valueOf( s ).floatValue()", "a floating point number" );

        jClass.addMethod( method );

        method = new JMethod( JType.Double, "getDoubleValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.Boolean, "strict" ) );

        sc = method.getSourceCode();

        convertNumericalType( sc, "Double.valueOf( s ).doubleValue()", "a floating point number" );

        jClass.addMethod( method );

        method = new JMethod( new JClass( "java.util.Date" ), "getDateValue" );
        method.addException( new JClass( "XMLStreamException" ) );

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "dateFormat" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "if ( dateFormat == null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return new java.util.Date( Long.valueOf( s ).longValue() );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "else" );

        sc.add( "{" );

        sc.indent();

        sc.add( "DateFormat dateParser = new java.text.SimpleDateFormat( dateFormat, Locale.US );" );

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        sc.add( "return dateParser.parse( s );" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "catch ( java.text.ParseException e )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "throw new XMLStreamException( e.getMessage() );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return null;" );

        jClass.addMethod( method );
    }

    private void convertNumericalType( JSourceCode sc, String expression, String typeDesc )
    {
        sc.add( "if ( s != null )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "try" );

        sc.add( "{" );

        sc.indent();

        sc.add( "return " + expression + ";" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "catch ( NumberFormatException e )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new XMLStreamException( \"Unable to parse element '\" + attribute + \"', must be " + typeDesc +
            " but was '\" + s + \"'\", xmlStreamReader.getLocation() );" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.unindent();

        sc.add( "}" );

        sc.add( "return 0;" );
    }
}
