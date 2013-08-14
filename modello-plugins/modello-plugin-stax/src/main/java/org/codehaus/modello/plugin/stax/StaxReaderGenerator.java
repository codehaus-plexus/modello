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
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class StaxReaderGenerator
    extends AbstractStaxGenerator
{

    private boolean requiresDomSupport;

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = false;

        try
        {
            generateStaxReader();

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

    /**
     * Generate a StAX reader, a <code><i>ModelName</i>StaxReader</code> class in <code>io.stax</code> sub-package
     * with <code>public <i>RootClass</i> read( ... )</code> methods.
     *
     * @throws ModelloException
     * @throws IOException
     */
    private void generateStaxReader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() )
            + ".io.stax";

        String unmarshallerName = getFileName( "StaxReader" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.io.FileInputStream" );
        jClass.addImport( "java.io.InputStream" );
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

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write reference resolvers.
        // ----------------------------------------------------------------------

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );
        JClass rootType = new JClass( root.getName() );

        GeneratorNode rootNode = findRequiredReferenceResolvers( root, null );

        writeReferenceResolvers( rootNode, jClass );
        for ( GeneratorNode node : rootNode.getNodesWithReferencableChildren().values() )
        {
            writeReferenceResolvers( node, jClass );
        }

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        if ( requiresDomSupport )
        {
            writeBuildDomMethod( jClass );
        }

        // ----------------------------------------------------------------------
        // Write the read(XMLStreamReader,boolean) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod unmarshall = new JMethod( "read", rootType, null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        String tagName = resolveTagName( root );
        String className = root.getName();
        String variableName = uncapitalise( className );

        if ( requiresDomSupport && !domAsXpp3 )
        {
            sc.add( "if ( _doc_ == null )" );
            sc.add( "{" );
            sc.indent();
            sc.add( "try" );
            sc.add( "{" );
            sc.addIndented( "initDoc();" );
            sc.add( "}" );
            sc.add( "catch ( javax.xml.parsers.ParserConfigurationException pce )" );
            sc.add( "{" );
            sc.addIndented( "throw new XMLStreamException( \"Unable to create DOM document: \" + pce.getMessage(), pce );" );
            sc.add( "}" );
            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "int eventType = xmlStreamReader.getEventType();" );

        sc.add( "String encoding = null;" );

        sc.add( "while ( eventType != XMLStreamConstants.END_DOCUMENT )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( eventType == XMLStreamConstants.START_DOCUMENT )" );
        sc.add( "{" );
        sc.addIndented( "encoding = xmlStreamReader.getCharacterEncodingScheme();" );
        sc.add( "}" );

        sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict && ! \"" + tagName + "\".equals( xmlStreamReader.getLocalName() ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Expected root element '" + tagName + "' but "
                        + "found '\" + xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation(), null );" );
        sc.add( "}" );

        VersionDefinition versionDefinition = objectModel.getVersionDefinition();
        if ( versionDefinition != null && versionDefinition.isNamespaceType() )
        {
            sc.add( "String modelVersion = getVersionFromRootNamespace( xmlStreamReader );" );

            writeModelVersionCheck( sc );
        }

        sc.add( className + ' ' + variableName + " = parse" + root.getName() + "( xmlStreamReader, strict );" );

        sc.add( variableName + ".setModelEncoding( encoding );" );

        sc.add( "resolveReferences( " + variableName + " );" );

        sc.add( "return " + variableName + ';' );

        sc.unindent();
        sc.add( "}" );

        sc.add( "eventType = xmlStreamReader.next();" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "throw new XMLStreamException( \"Expected root element '" + tagName + "' but "
                        + "found no element at all: invalid XML document\", xmlStreamReader.getLocation(), null );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(Reader[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( reader );" );

        sc.add( "" );

        sc.add( "return read( xmlStreamReader, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return read( reader, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(InputStream[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "stream" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( stream );" );

        sc.add( "" );

        sc.add( "return read( xmlStreamReader, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "stream" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return read( stream, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(String[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "filePath" ) );

        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "java.io.File file = new java.io.File( filePath );" );

        sc.add( "XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( "
                + "file.toURI().toURL().toExternalForm(), new FileInputStream( file ) );" );

        sc.add( "" );

        sc.add( "return read( xmlStreamReader, strict );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        unmarshall = new JMethod( "read", rootType, null );

        unmarshall.addParameter( new JParameter( new JClass( "String" ), "filePath" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return read( filePath, true );" );

        jClass.addMethod( unmarshall );

        // Determine the version. Currently, it causes the document to be reparsed, but could be made more efficient in
        // future by buffering the read XML and piping that into any consequent read method.

        if ( versionDefinition != null )
        {
            writeDetermineVersionMethod( jClass, objectModel );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void generateStaxReaderDelegate( List<String> versions )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( false, null ) + ".io.stax";

        String unmarshallerName = getFileName( "StaxReaderDelegate" );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );

        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );

        jClass.addImport( "javax.xml.stream.*" );

        jClass.addImport( "org.codehaus.plexus.util.IOUtil" );
        jClass.addImport( "org.codehaus.plexus.util.ReaderFactory" );

        JMethod method = new JMethod( "read", new JClass( "Object" ), null );

        method.addParameter( new JParameter( new JClass( "java.io.File" ), "f" ) );

        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        sc.add( "String modelVersion;" );
        sc.add( "Reader reader = ReaderFactory.newXmlReader( f );" );

        sc.add( "try" );
        sc.add( "{" );
        sc.addIndented( "modelVersion = determineVersion( reader );" );
        sc.add( "}" );
        sc.add( "finally" );
        sc.add( "{" );
        sc.addIndented( "IOUtil.close( reader );" );
        sc.add( "}" );

        sc.add( "reader = ReaderFactory.newXmlReader( f );" );
        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        writeModelVersionHack( sc );

        String prefix = "";
        for ( String version : versions )
        {
            sc.add( prefix + "if ( \"" + version + "\".equals( modelVersion ) )" );
            sc.add( "{" );
            sc.addIndented( "return new " + getModel().getDefaultPackageName( true, new Version( version ) )
                            + ".io.stax." + getFileName( "StaxReader" ) + "().read( reader, strict );" );
            sc.add( "}" );

            prefix = "else ";
        }

        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Document version '\" + modelVersion + \"' has no "
                        + "corresponding reader.\" );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );
        sc.add( "finally" );
        sc.add( "{" );
        sc.addIndented( "IOUtil.close( reader );" );
        sc.add( "}" );

        // ----------------------------------------------------------------------

        method = new JMethod( "read", new JClass( "Object" ), null );

        method.addParameter( new JParameter( new JClass( "java.io.File" ), "f" ) );

        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();
        sc.add( "return read( f, true );" );

        jClass.addMethod( method );

        writeDetermineVersionMethod( jClass, objectModel );

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private static void writeModelVersionHack( JSourceCode sc )
    {
        sc.add( "// legacy hack for pomVersion == 3" );
        sc.add( "if ( \"3\".equals( modelVersion ) )" );
        sc.add( "{" );
        sc.addIndented( "modelVersion = \"3.0.0\";" );
        sc.add( "}" );
    }

    private void writeDetermineVersionMethod( JClass jClass, Model objectModel )
        throws ModelloException
    {
        VersionDefinition versionDefinition = objectModel.getVersionDefinition();

        JMethod method = new JMethod( "determineVersion", new JClass( "String" ), null );

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

        if ( versionDefinition.isNamespaceType() )
        {
            XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) objectModel.getMetadata( XmlModelMetadata.ID );

            String namespace = xmlModelMetadata.getNamespace();
            if ( namespace == null || namespace.indexOf( "${version}" ) < 0 )
            {
                throw new ModelloException( "versionDefinition is namespace, but the model does not declare "
                                            + "xml.namespace on the model element" );
            }

            sc.add( "return getVersionFromRootNamespace( xmlStreamReader );" );

            writeNamespaceVersionGetMethod( namespace, jClass );
        }
        else
        {
            String value = versionDefinition.getValue();

            ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ),
                                                    getGeneratedVersion() );
            ModelField field = root.getField( value, getGeneratedVersion() );

            if ( field == null )
            {
                throw new ModelloException( "versionDefinition is field, but the model root element does not declare a "
                                            + "field '" + value + "'." );
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
        JMethod method = new JMethod( "getVersionFromField", new JType( "String" ), null );
        method.getModifiers().makePrivate();
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );
        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );
        String value = xmlFieldMetadata.getTagName();
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
        sc.addIndented( "return xmlStreamReader.getElementText();" );
        sc.add( "}" );

        if ( field.getAlias() != null )
        {
            sc.add( "if ( depth == 0 && \"" + field.getAlias() + "\".equals( xmlStreamReader.getLocalName() ) )" );
            sc.add( "{" );
            sc.addIndented( "return xmlStreamReader.getElementText();" );
            sc.add( "}" );
        }

        sc.add( "depth++;" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "if ( eventType == XMLStreamConstants.END_ELEMENT )" );
        sc.add( "{" );
        sc.addIndented( "depth--;" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "throw new XMLStreamException( \"Field: '" + value
                + "' does not exist in the document.\", xmlStreamReader.getLocation() );" );
    }

    private static void writeNamespaceVersionGetMethod( String namespace, JClass jClass )
    {
        JMethod method = new JMethod( "getVersionFromRootNamespace", new JType( "String" ), null );
        method.getModifiers().makePrivate();
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );
        jClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        sc.add( "String uri = xmlStreamReader.getNamespaceURI( \"\" );" );

        sc.add( "if ( uri == null )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"No namespace specified, but versionDefinition requires it\", "
                        + "xmlStreamReader.getLocation() );" );
        sc.add( "}" );

        int index = namespace.indexOf( "${version}" );

        sc.add( "String uriPrefix = \"" + namespace.substring( 0, index ) + "\";" );
        sc.add( "String uriSuffix = \"" + namespace.substring( index + 10 ) + "\";" );

        sc.add( "if ( !uri.startsWith( uriPrefix ) || !uri.endsWith( uriSuffix ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Namespace URI: '\" + uri + \"' does not match pattern '"
                        + namespace + "'\", xmlStreamReader.getLocation() );" );
        sc.add( "}" );

        sc.add( "return uri.substring( uriPrefix.length(), uri.length() - uriSuffix.length() );" );
    }

    /**
     * Write code to parse every classes from a model.
     *
     * @param objectModel the model
     * @param jClass the generated class source file
     * @throws ModelloException
     * @see {@link #writeClassParser(ModelClass, JClass, boolean)}
     */
    private void writeAllClassesParser( Model objectModel, JClass jClass )
        throws ModelloException
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            writeClassParser( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    /**
     * Write a <code>private <i>ClassName</i> parse<i>ClassName</i>( ... )</code> method to parse a class from a model.
     *
     * @param modelClass the model class
     * @param jClass the generated class source file
     * @param rootElement is this class the root from the model?
     * @throws ModelloException
     */
    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
        throws ModelloException
    {
        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( "parse" + capClassName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        unmarshall.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( className + ' ' + uncapClassName + " = new " + className + "();" );

        ModelField contentField = getContentField( modelClass.getAllFields( getGeneratedVersion(), true ) );

        if ( contentField != null )
        {
            writeAttributes( modelClass, uncapClassName, sc );

            writePrimitiveField( contentField, contentField.getType(), uncapClassName,
                                 "set" + capitalise( contentField.getName() ), sc );
        }
        else
        {
            sc.add( "java.util.Set parsed = new java.util.HashSet();" );

            String instanceFieldName = getInstanceFieldName( className );

            writeAttributes( modelClass, uncapClassName, sc );

            if ( isAssociationPartToClass( modelClass ) )
            {
                jClass.addField( new JField( new JType( "java.util.Map" ), instanceFieldName ) );

                sc.add( "if ( " + instanceFieldName + " == null )" );
                sc.add( "{" );
                sc.addIndented( instanceFieldName + " = new java.util.HashMap();" );
                sc.add( "}" );

                sc.add( "String v = xmlStreamReader.getAttributeValue( null, \"modello.id\" );" );
                sc.add( "if ( v != null )" );
                sc.add( "{" );
                sc.addIndented( instanceFieldName + ".put( v, " + uncapClassName + " );" );
                sc.add( "}" );
            }

            sc.add( "while ( ( strict ? xmlStreamReader.nextTag() : nextTag( xmlStreamReader ) ) == XMLStreamConstants.START_ELEMENT )" );

            sc.add( "{" );
            sc.indent();

            boolean addElse = false;

            // Write other fields

            for ( ModelField field : modelClass.getAllFields( getGeneratedVersion(), true ) )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                if ( !xmlFieldMetadata.isAttribute() && !xmlFieldMetadata.isTransient() )
                {
                    processField( field, xmlFieldMetadata, addElse, sc, uncapClassName, rootElement, jClass );

                    addElse = true;
                }
            }

            /*
            if ( modelClass.getFields( getGeneratedVersion() ).size() > 0 )
            {
                sc.add( "else" );

                sc.add( "{" );
                sc.addIndented( "parser.nextText();" );
                sc.add( "}" );
            }
*/

            if ( addElse )
            {
                sc.add( "else" );

                sc.add( "{" );
                sc.indent();
            }

            sc.add( "checkUnknownElement( xmlStreamReader, strict );" );

            if ( addElse )
            {
                sc.unindent();
                sc.add( "}" );
            }

            sc.unindent();
            sc.add( "}" );

            // This must be last so that we guarantee the ID has been filled already
            if ( isAssociationPartToClass( modelClass ) )
            {
                List<ModelField> identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

                if ( identifierFields.size() == 1 )
                {
                    ModelField field = (ModelField) identifierFields.get( 0 );

                    String v = uncapClassName + ".get" + capitalise( field.getName() ) + "()";
                    v = getValue( field.getType(), v, (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID ) );
                    sc.add( instanceFieldName + ".put( " + v + ", " + uncapClassName + " );" );
                }
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

        for ( ModelField field : modelClass.getAllFields( getGeneratedVersion(), true ) )
        {
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

        // propagate the flag up
        for ( GeneratorNode child : value.getChildren() )
        {
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

        for ( GeneratorNode child : node.getChildren() )
        {
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
                if ( association.isOneMultiplicity() )
                {
                    sc.add( "String id = (String) refs.get( \"" + association.getName() + "\" );" );
                    sc.add( to + " ref = (" + to + ") " + instanceFieldName + ".get( id );" );

                    // Don't set if it already is, since the Java plugin generates create/break that will throw an
                    // exception

                    sc.add( "if ( ref != null && !ref.equals( value.get" + capAssocName + "() ) )" );
                    sc.add( "{" );
                    sc.addIndented( "value.set" + capAssocName + "( ref );" );
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
                    sc.addIndented( "value.get" + capAssocName + "().set( i, ref );" );
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
                if ( association.isOneMultiplicity() )
                {
                    sc.add( "resolveReferences( value.get" + capitalise( association.getName() ) + "() );" );
                }
                else
                {
                    sc.add( "for ( java.util.Iterator i = value.get" + capitalise( association.getName() )
                            + "().iterator(); i.hasNext(); )" );
                    sc.add( "{" );
                    sc.addIndented( "resolveReferences( (" + association.getTo() + ") i.next() );" );
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

    /**
     * Add code to parse fields of a model class that are XML attributes.
     *
     * @param modelClass the model class
     * @param uncapClassName
     * @param sc the source code to add to
     * @throws ModelloException
     */
    private void writeAttributes( ModelClass modelClass, String uncapClassName, JSourceCode sc )
        throws ModelloException
    {
        for ( ModelField field : modelClass.getAllFields( getGeneratedVersion(), true ) )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isAttribute() && !xmlFieldMetadata.isTransient() )
            {
                writePrimitiveField( field, field.getType(), uncapClassName, "set" + capitalise( field.getName() ),
                                     sc );
            }
        }
    }

    /**
     * Generate code to process a field represented as an XML element.
     *
     * @param field the field to process
     * @param xmlFieldMetadata its XML metadata
     * @param addElse add an <code>else</code> statement before generating a new <code>if</code>
     * @param sc the method source code to add to
     * @param objectName the object name in the source
     * @param rootElement is the enclosing model class the root class (for model version field handling)
     * @param jClass the generated class source file
     * @throws ModelloException
     */
    private void processField( ModelField field, XmlFieldMetadata xmlFieldMetadata, boolean addElse, JSourceCode sc,
                               String objectName, boolean rootElement, JClass jClass )
        throws ModelloException
    {
        String fieldTagName = resolveTagName( field, xmlFieldMetadata );

        String capFieldName = capitalise( field.getName() );

        String singularName = singular( field.getName() );

        String alias;
        if ( StringUtils.isEmpty( field.getAlias() ) )
        {
            alias = "null";
        }
        else
        {
            alias = "\"" + field.getAlias() + "\"";
        }

        String tagComparison = ( addElse ? "else " : "" )
            + "if ( checkFieldWithDuplicate( xmlStreamReader, \"" + fieldTagName + "\", " + alias + ", parsed ) )";

        if ( !( field instanceof ModelAssociation ) )
        {
            sc.add( tagComparison );

            sc.add( "{" );
            sc.indent();

            //ModelField
            writePrimitiveField( field, field.getType(), objectName, "set" + capFieldName, sc );

            if ( rootElement && field.isModelVersionField() )
            {
                sc.add( "String modelVersion = " + objectName + ".get" + capFieldName + "();" );

                writeModelVersionCheck( sc );
            }

            sc.unindent();
            sc.add( "}" );
        }
        else
        { // model association
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( association.isOneMultiplicity() )
            {
                sc.add( tagComparison );

                sc.add( "{" );
                sc.indent();

                ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                if ( referenceIdentifierField != null )
                {
                    addCodeToAddReferences( association, jClass, sc, referenceIdentifierField, objectName );

                    // gobble the rest of the tag
                    sc.add( "while ( xmlStreamReader.getEventType() != XMLStreamConstants.END_ELEMENT )" );
                    sc.add( "{" );
                    sc.addIndented( "xmlStreamReader.next();" );
                    sc.add( "}" );
                }
                else
                {
                    sc.add( objectName + ".set" + capFieldName + "( parse" + association.getTo()
                        + "( xmlStreamReader, strict ) );" );
                }

                sc.unindent();
                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                String valuesTagName = resolveTagName( fieldTagName, xmlAssociationMetadata );

                String type = association.getType();

                boolean wrappedItems = xmlAssociationMetadata.isWrappedItems();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) association.getMetadata( JavaFieldMetadata.ID );

                    String adder;

                    if ( wrappedItems )
                    {
                        sc.add( tagComparison );

                        sc.add( "{" );
                        sc.indent();

                        if ( javaFieldMetadata.isSetter() )
                        {
                            sc.add( type + " " + associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                            adder = associationName + ".add";
                        }
                        else
                        {
                            adder = objectName + ".add" + association.getTo();
                        }

                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"" + valuesTagName + "\".equals( xmlStreamReader.getLocalName() ) )" );

                        sc.add( "{" );
                        sc.indent();
                    }
                    else
                    {
                        sc.add( ( addElse ? "else " : "" )
                            + "if ( \"" + valuesTagName + "\".equals( xmlStreamReader.getLocalName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        if ( javaFieldMetadata.isGetter() && javaFieldMetadata.isSetter() )
                        {
                            sc.add( type + " " + associationName + " = " + objectName + ".get" + capFieldName + "();" );

                            sc.add( "if ( " + associationName + " == null )" );

                            sc.add( "{" );
                            sc.indent();

                            sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                            sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.unindent();
                            sc.add( "}" );

                            adder = associationName + ".add";
                        }
                        else
                        {
                            adder = objectName + ".add" + association.getTo();
                        }
                    }

                    if ( isClassInModel( association.getTo(), field.getModelClass().getModel() ) )
                    {
                        ModelField referenceIdentifierField = getReferenceIdentifierField( association );

                        if ( referenceIdentifierField != null )
                        {
                            addCodeToAddReferences( association, jClass, sc, referenceIdentifierField, objectName );
                        }

                        if ( association.getTo().equals( field.getModelClass().getName() ) )
                        {
                            // HACK: the addXXX method will cause an OOME when compiling a self-referencing class, so we
                            //  just add it to the array. This could disrupt the links if you are using break/create
                            //  constraints in modello.
                            // MODELLO-273 update: Use addXXX only if no other methods are available!
                            sc.add( adder + "( parse" + association.getTo() + "( xmlStreamReader, strict ) );" );
                        }
                        else
                        {
                            sc.add( objectName + ".add" + capitalise( singular( associationName ) ) + "( parse"
                                    + association.getTo() + "( xmlStreamReader, strict ) );" );
                        }
                    }
                    else
                    {
                        writePrimitiveField( association, association.getTo(), associationName, "add", sc );
                    }

                    if ( wrappedItems )
                    {
                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.addIndented( "throw new XMLStreamException( \"Unrecognised tag: '\" + "
                                        + "xmlStreamReader.getLocalName() + \"'\", xmlStreamReader.getLocation() );" );
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

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"" + valuesTagName + "\".equals( xmlStreamReader.getLocalName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "// " + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "while ( xmlStreamReader.nextTag() == XMLStreamConstants.START_ELEMENT )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"key\".equals( xmlStreamReader.getLocalName() ) )" );

                        sc.add( "{" );
                        sc.addIndented( "key = xmlStreamReader.getElementText();" );
                        sc.add( "}" );

                        sc.add( "else if ( \"value\".equals( xmlStreamReader.getLocalName() ) )" );

                        sc.add( "{" );
                        sc.addIndented( "value = xmlStreamReader.getElementText()"
                                        + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.addIndented( "xmlStreamReader.getText();" );
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

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

                        sc.add( "String value = xmlStreamReader.getElementText()"
                                + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );
                    }

                    sc.unindent();
                    sc.add( "}" );
                }
            }
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

        sc.add( "String value = xmlStreamReader.getAttributeValue( null, \"" + referenceIdentifierField.getName()
                + "\" );" );
        sc.add( "if ( value != null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "// This is a reference to an element elsewhere in the model" );
        sc.add( "if ( " + refFieldName + " == null )" );
        sc.add( "{" );
        sc.addIndented( refFieldName + " = new java.util.HashMap();" );
        sc.add( "}" );

        sc.add( "java.util.Map refs = (java.util.Map) " + refFieldName + ".get( " + referredFromClass + " );" );
        sc.add( "if ( refs == null )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "refs = new java.util.HashMap();" );
        sc.add( refFieldName + ".put( " + referredFromClass + ", refs );" );

        sc.unindent();
        sc.add( "}" );

        if ( association.isOneMultiplicity() )
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

        sc.add( "if ( !\"" + getGeneratedVersion() + "\".equals( modelVersion ) )" );
        sc.add( "{" );
        sc.addIndented(
            "throw new XMLStreamException( \"Document model version of '\" + modelVersion + \"' doesn't match reader "
            + "version of '" + getGeneratedVersion() + "'\", xmlStreamReader.getLocation() );" );
        sc.add( "}" );
    }

    /**
     * Write code to set a primitive field with a value got from the parser, with appropriate default value, trimming
     * and required check logic.
     *
     * @param field the model field to set (either XML attribute or element)
     * @param type the type of the value read from XML
     * @param objectName the object name in source
     * @param setterName the setter method name
     * @param sc the source code to add to
     */
    private void writePrimitiveField( ModelField field, String type, String objectName, String setterName,
                                      JSourceCode sc )
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = resolveTagName( field, xmlFieldMetadata );

        String parserGetter;
        if ( xmlFieldMetadata.isAttribute() )
        {
            parserGetter = "xmlStreamReader.getAttributeValue( null, \"" + tagName + "\" )";
        }
        else
        {
            parserGetter = "xmlStreamReader.getElementText()";
        }

/* TODO:
        if ( xmlFieldMetadata.isRequired() )
        {
            parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser, strict )";
        }
*/
        if ( field.getDefaultValue() != null )
        {
            parserGetter = "getDefaultValue( " + parserGetter + ", \"" + field.getDefaultValue() + "\" )";
        }

        if ( xmlFieldMetadata.isTrim() )
        {
            parserGetter = "getTrimmedValue( " + parserGetter + " )";
        }

        if ( "boolean".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getBooleanValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader ) );" );
        }
        else if ( "char".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getCharacterValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader ) );" );
        }
        else if ( "double".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getDoubleValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getFloatValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getIntegerValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getLongValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getShortValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "byte".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( getByteValue( " + parserGetter + ", \"" + tagName
                + "\", xmlStreamReader, strict ) );" );
        }
        else if ( "String".equals( type ) || "Boolean".equals( type ) )
        {
            // TODO: other Primitive types
            sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
        }
        else if ( "Date".equals( type ) )
        {
            sc.add( "String dateFormat = "
                + ( xmlFieldMetadata.getFormat() != null ? "\"" + xmlFieldMetadata.getFormat() + "\"" : "null" ) + ";" );
            sc.add( objectName + "." + setterName + "( getDateValue( " + parserGetter + ", \"" + tagName
                + "\", dateFormat, xmlStreamReader ) );" );
        }
        else if ( "DOM".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( buildDom( xmlStreamReader, " + xmlFieldMetadata.isTrim() + " ) );" );

            requiresDomSupport = true;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }
    }

    private void writeBuildDomMethod( JClass jClass )
    {
        if ( domAsXpp3 )
        {
            jClass.addImport( "org.codehaus.plexus.util.xml.Xpp3Dom" );
        }
        else
        {
            jClass.addField( new JField( new JClass( "org.w3c.dom.Document" ), "_doc_" ) );
            JMethod method = new JMethod( "initDoc", null, null );
            method.getModifiers().makePrivate();
            method.addException( new JClass( "javax.xml.parsers.ParserConfigurationException" ) );

            JSourceCode sc = method.getSourceCode();
            sc.add( "javax.xml.parsers.DocumentBuilderFactory dbfac = javax.xml.parsers.DocumentBuilderFactory.newInstance();" );
            sc.add( "javax.xml.parsers.DocumentBuilder docBuilder = dbfac.newDocumentBuilder();" );
            sc.add( "_doc_ = docBuilder.newDocument();" );
            jClass.addMethod( method );
        }
        String type = domAsXpp3 ? "Xpp3Dom" : "org.w3c.dom.Element";
        JMethod method = new JMethod( "buildDom", new JType( type ), null );
        method.getModifiers().makePrivate();
        method.addParameter( new JParameter( new JType( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "trim" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "java.util.Stack elements = new java.util.Stack();" );

        sc.add( "java.util.Stack values = new java.util.Stack();" );

        sc.add( "int eventType = xmlStreamReader.getEventType();" );

        sc.add( "boolean spacePreserve = false;" );

        sc.add( "while ( xmlStreamReader.hasNext() )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "if ( eventType == XMLStreamConstants.START_ELEMENT )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "spacePreserve = false;" );
        sc.add( "String rawName = xmlStreamReader.getLocalName();" );

        if ( domAsXpp3 )
        {
            sc.add( "Xpp3Dom element = new Xpp3Dom( rawName );" );
        }
        else
        {
            sc.add( "org.w3c.dom.Element element = _doc_.createElement( rawName );" );
        }

        sc.add( "if ( !elements.empty() )" );
        sc.add( "{" );
        sc.indent();
        sc.add( type + " parent = (" + type + ") elements.peek();" );

        sc.add( "parent." + ( domAsXpp3 ? "addChild" : "appendChild" ) + "( element );" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "elements.push( element );" );

        sc.add( "if ( xmlStreamReader.isEndElement() )" );
        sc.add( "{" );
        sc.addIndented( "values.push( null );" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "values.push( new StringBuffer() );" );
        sc.add( "}" );

        sc.add( "int attributesSize = xmlStreamReader.getAttributeCount();" );

        sc.add( "for ( int i = 0; i < attributesSize; i++ )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String name = xmlStreamReader.getAttributeLocalName( i );" );

        sc.add( "String value = xmlStreamReader.getAttributeValue( i );" );

        sc.add( "element.setAttribute( name, value );" );

        sc.add( "spacePreserve = spacePreserve || ( \"xml\".equals( xmlStreamReader.getAttributePrefix( i ) ) && \"space\".equals( name ) && \"preserve\".equals( value ) );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XMLStreamConstants.CHARACTERS )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "StringBuffer valueBuffer = (StringBuffer) values.peek();" );

        sc.add( "String text = xmlStreamReader.getText();" );

        sc.add( "if ( trim && !spacePreserve )" );
        sc.add( "{" );
        sc.addIndented( "text = text.trim();" );
        sc.add( "}" );

        sc.add( "valueBuffer.append( text );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XMLStreamConstants.END_ELEMENT )" );
        sc.add( "{" );
        sc.indent();

        sc.add( type + " element = (" + type + ") elements.pop();" );

        sc.add( "// this Object could be null if it is a singleton tag" );
        sc.add( "Object accumulatedValue = values.pop();" );

        sc.add( "if ( " + ( domAsXpp3 ? "element.getChildCount() == 0" : "!element.hasChildNodes()" ) + " )" );
        sc.add( "{" );
        sc.addIndented( "element." + ( domAsXpp3 ? "setValue" : "setTextContent" ) + "( ( accumulatedValue == null ) ? null : accumulatedValue.toString() );" );
        sc.add( "}" );

        sc.add( "if ( values.empty() )" );
        sc.add( "{" );
        sc.addIndented( "return element;" );
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
        JMethod method = new JMethod( "getTrimmedValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "s = s.trim();" );
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getDefaultValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "v" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.addIndented( "s = v;" );
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getRequiredAttributeValue", new JClass( "String" ), null );
        method.addException( new JClass( "XMLStreamException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Missing required value for attribute '\" + attribute + \"'\", "
                        + "xmlStreamReader.getLocation() );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getBooleanValue", JType.BOOLEAN, null );
        method.addException( new JClass( "XMLStreamException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return Boolean.valueOf( s ).booleanValue();" );
        sc.add( "}" );

        sc.add( "return false;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getCharacterValue", JType.CHAR, null );
        method.addException( new JClass( "XMLStreamException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return s.charAt( 0 );" );
        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getIntegerValue", JType.INT, "Integer.valueOf( s ).intValue()", "an integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getShortValue", JType.SHORT, "Short.valueOf( s ).shortValue()",
                                       "a short integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getByteValue", JType.BYTE, "Byte.valueOf( s ).byteValue()", "a byte" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getLongValue", JType.LONG, "Long.valueOf( s ).longValue()", "a long integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getFloatValue", JType.FLOAT, "Float.valueOf( s ).floatValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getDoubleValue", JType.DOUBLE, "Double.valueOf( s ).doubleValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getDateValue", new JClass( "java.util.Date" ), null );
        method.addException( new JClass( "XMLStreamException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "dateFormat" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        writeDateParsingHelper( method.getSourceCode(), "new XMLStreamException( e.getMessage(), xmlStreamReader.getLocation(), e )" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkFieldWithDuplicate", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "alias" ) );
        method.addParameter( new JParameter( new JClass( "java.util.Set" ), "parsed" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( !( xmlStreamReader.getLocalName().equals( tagName ) ||"
                + " xmlStreamReader.getLocalName().equals( alias ) ) )" );

        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );

        sc.add( "if ( !parsed.add( tagName ) )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new XMLStreamException( \"Duplicated tag: '\" + tagName + \"'\", xmlStreamReader.getLocation() );" );
        sc.add( "}" );

        sc.add( "return true;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownElement", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Unrecognised tag: '\" + xmlStreamReader.getLocalName() + "
                        + "\"'\", xmlStreamReader.getLocation() );" );
        sc.add( "}" );

        sc.add( "int unrecognizedTagCount = 1;" );
        sc.add( "while( unrecognizedTagCount != 0 )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "xmlStreamReader.next();" );
        sc.add( "if ( xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT )" );

        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount++;" );
        sc.add( "}" );

        sc.add( "else if ( xmlStreamReader.getEventType() == XMLStreamConstants.END_ELEMENT )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount--;" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "nextTag", JType.INT, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addException( new JClass( "XMLStreamException" ) );

        sc = method.getSourceCode();

        sc.add( "while ( true )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "int eventType = xmlStreamReader.next();" );
        sc.add( "switch ( eventType )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "case XMLStreamConstants.CHARACTERS:" );
        sc.add( "case XMLStreamConstants.CDATA:" );
        sc.add( "case XMLStreamConstants.SPACE:" );
        sc.add( "case XMLStreamConstants.PROCESSING_INSTRUCTION:" );
        sc.add( "case XMLStreamConstants.COMMENT:" );
        sc.addIndented( "break;" );
        sc.add( "case XMLStreamConstants.START_ELEMENT:" );
        sc.add( "case XMLStreamConstants.END_ELEMENT:" );
        sc.addIndented( "return eventType;" );
        sc.add( "default:" );
        sc.addIndented( "throw new XMLStreamException( \"expected start or end tag\", xmlStreamReader.getLocation() );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( method );
    }

    private JMethod convertNumericalType( String methodName, JType returnType, String expression, String typeDesc )
    {
        JMethod method = new JMethod( methodName, returnType, null );
        method.addException( new JClass( "XMLStreamException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XMLStreamReader" ), "xmlStreamReader" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "try" );

        sc.add( "{" );
        sc.addIndented( "return " + expression + ";" );
        sc.add( "}" );

        sc.add( "catch ( NumberFormatException nfe )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new XMLStreamException( \"Unable to parse element '\" + attribute + \"', must be "
                        + typeDesc + " but was '\" + s + \"'\", xmlStreamReader.getLocation(), nfe );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return 0;" );

        return method;
    }
}
