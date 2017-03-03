package org.codehaus.modello.plugin.xpp3;

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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JConstructor;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JModifiers;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaClassMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class Xpp3ReaderGenerator
    extends AbstractXpp3Generator
{

    private static final String SOURCE_PARAM = "source";

    private static final String LOCATION_VAR = "_location";

    private ModelClass locationTracker;

    private String locationField;

    private ModelClass sourceTracker;

    private String trackingArgs;

    private boolean requiresDomSupport;

    protected boolean isLocationTracking()
    {
        return false;
    }

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        locationTracker = sourceTracker = null;
        trackingArgs = locationField = "";
        requiresDomSupport = false;

        if ( isLocationTracking() )
        {
            locationTracker = model.getLocationTracker( getGeneratedVersion() );
            if ( locationTracker == null )
            {
                throw new ModelloException( "No model class has been marked as location tracker"
                                                + " via the attribute locationTracker=\"locations\""
                                                + ", cannot generate extended reader." );
            }

            locationField =
                ( (ModelClassMetadata) locationTracker.getMetadata( ModelClassMetadata.ID ) ).getLocationTracker();

            sourceTracker = model.getSourceTracker( getGeneratedVersion() );

            if ( sourceTracker != null )
            {
                trackingArgs += ", " + SOURCE_PARAM;
            }
        }

        try
        {
            generateXpp3Reader();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating XPP3 Reader.", ex );
        }
    }

    private void writeAllClassesReaders( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            if ( isTrackingSupport( clazz ) )
            {
                continue;
            }

            writeClassReaders( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    private void writeClassReaders( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        JavaClassMetadata javaClassMetadata =
            (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.class.getName() );

        // Skip abstract classes, no way to parse them out into objects
        if ( javaClassMetadata.isAbstract() )
        {
            return;
        }

        XmlClassMetadata xmlClassMetadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );
        if ( !rootElement && !xmlClassMetadata.isStandaloneRead() )
        {
            return;
        }

        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String readerMethodName = "read";
        if ( !rootElement )
        {
            readerMethodName += capClassName;
        }

        // ----------------------------------------------------------------------
        // Write the read(XmlPullParser,boolean) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod unmarshall = new JMethod( readerMethodName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XmlPullParserException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        String tagName = resolveTagName( modelClass );
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
            sc.addIndented(
                "throw new XmlPullParserException( \"Unable to create DOM document: \" + pce.getMessage(), parser, pce );" );
            sc.add( "}" );
            sc.unindent();
            sc.add( "}" );
        }
        
        sc.add( className + ' ' + variableName + " = null;" );

        sc.add( "int eventType = parser.getEventType();" );
        
        sc.add( "boolean parsed = false;" );

        sc.add( "while ( eventType != XmlPullParser.END_DOCUMENT )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( eventType == XmlPullParser.START_TAG )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict && ! \"" + tagName + "\".equals( parser.getName() ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new XmlPullParserException( \"Expected root element '" + tagName + "' but "
                            + "found '\" + parser.getName() + \"'\", parser, null );" );
        sc.add( "}" );
        
        sc.add( "else if ( parsed )" );

        sc.add( "{" );
        sc.addIndented( "// fallback, already expected a XmlPullParserException due to invalid XML" );
        sc.addIndented( "throw new XmlPullParserException( \"Duplicated tag: '" + tagName + "'\", parser, null );" );
        sc.add( "}" );

        sc.add( variableName + " = parse" + capClassName + "( parser, strict" + trackingArgs + " );" );

        if ( rootElement )
        {
            sc.add( variableName + ".setModelEncoding( parser.getInputEncoding() );" );
        }
        sc.add( "parsed = true;" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "eventType = parser.next();" );

        sc.unindent();
        sc.add( "}" );
        
        sc.add( "if ( parsed )" );
        sc.add( "{" );
        sc.addIndented( "return " + variableName + ';' );
        sc.add( "}" );

        sc.add( "throw new XmlPullParserException( \"Expected root element '" + tagName + "' but "
                    + "found no element at all: invalid XML document\", parser, null );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(Reader[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );
        unmarshall.setComment( "@see ReaderFactory#newXmlReader" );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XmlPullParserException" ) );

        sc = unmarshall.getSourceCode();

        sc.add("XmlPullParser parser = addDefaultEntities ? new MXParser(EntityReplacementMap.defaultEntityReplacementMap) : new MXParser( );");

        sc.add( "" );

        sc.add( "parser.setInput( reader );" );

        sc.add( "" );

        sc.add( "" );

        sc.add( "return " + readerMethodName + "( parser, strict" + trackingArgs + " );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------

        if ( locationTracker == null )
        {
            unmarshall = new JMethod( readerMethodName, new JClass( className ), null );
            unmarshall.setComment( "@see ReaderFactory#newXmlReader" );

            unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

            unmarshall.addException( new JClass( "IOException" ) );
            unmarshall.addException( new JClass( "XmlPullParserException" ) );

            sc = unmarshall.getSourceCode();
            sc.add( "return " + readerMethodName + "( reader, true );" );

            jClass.addMethod( unmarshall );
        }

        // ----------------------------------------------------------------------
        // Write the read(InputStream[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XmlPullParserException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return " + readerMethodName + "( ReaderFactory.newXmlReader( in ), strict" + trackingArgs + " );" );

        jClass.addMethod( unmarshall );

        // --------------------------------------------------------------------

        if ( locationTracker == null )
        {
            unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

            unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );

            unmarshall.addException( new JClass( "IOException" ) );
            unmarshall.addException( new JClass( "XmlPullParserException" ) );

            sc = unmarshall.getSourceCode();

            sc.add( "return " + readerMethodName + "( ReaderFactory.newXmlReader( in ) );" );

            jClass.addMethod( unmarshall );
        }
    }

    private void generateXpp3Reader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName =
            objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) + ".io.xpp3";

        String unmarshallerName = getFileName( "Xpp3Reader" + ( isLocationTracking() ? "Ex" : "" ) );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "org.codehaus.plexus.util.ReaderFactory" );
        jClass.addImport( "org.codehaus.plexus.util.xml.pull.MXParser" );
        jClass.addImport( "org.codehaus.plexus.util.xml.pull.EntityReplacementMap" );
        jClass.addImport( "org.codehaus.plexus.util.xml.pull.XmlPullParser" );
        jClass.addImport( "org.codehaus.plexus.util.xml.pull.XmlPullParserException" );
        jClass.addImport( "java.io.InputStream" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.text.DateFormat" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write option setters
        // ----------------------------------------------------------------------

        JConstructor constructor2 = new JConstructor(jClass);
        constructor2.getSourceCode().add( "this( new ContentTransformer()\n" + "        {\n"
                                              + "            public String transform( String source, String fieldName )\n"
                                              + "            {\n" + "                return source;\n"
                                              + "            }\n" + "        } );" );
        jClass.addConstructor( constructor2 );


        JConstructor constructor = new JConstructor(jClass);
        constructor.addParameter( new JParameter( new JType("ContentTransformer"), "contentTransformer"  ) );
        constructor.getSourceCode().add( "this.contentTransformer = contentTransformer;" );
        jClass.addConstructor( constructor );

        jClass.addSourceCode(  "public static interface ContentTransformer\n" + "{\n" + "    /**\n"
                                   + "     * Interpolate the value read from the xpp3 document\n"
                                   + "     * @param source The source value\n"
                                   + "     * @param fieldName A description of the field being interpolated. The implementation may use this to\n"
                                   + "     *                           log stuff.\n"
                                   + "     * @return The interpolated value.\n" + "     */\n"
                                   + "    String transform( String source, String fieldName );\n" + "}\n");

        // The Field
        JField addDefaultEntities = new JField( JType.BOOLEAN, "addDefaultEntities" );

        addDefaultEntities.setComment(
            "If set the parser will be loaded with all single characters from the XHTML specification.\n"
                + "The entities used:\n" + "<ul>\n" + "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent</li>\n"
                + "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent</li>\n"
                + "<li>http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent</li>\n" + "</ul>\n" );

        addDefaultEntities.setInitString( "true" );


        jClass.addField( addDefaultEntities );

        JField contentTransformer = new JField( new JType("ContentTransformer"), "contentTransformer" );
        JModifiers jModifiers = new JModifiers();
        jModifiers.setFinal(  true );
        contentTransformer.setModifiers( jModifiers );

        jClass.addField( contentTransformer );


        // The setter
        JMethod addDefaultEntitiesSetter = new JMethod( "setAddDefaultEntities" );

        addDefaultEntitiesSetter.addParameter( new JParameter( JType.BOOLEAN, "addDefaultEntities" ) );

        addDefaultEntitiesSetter.setSourceCode( "this.addDefaultEntities = addDefaultEntities;" );

        addDefaultEntitiesSetter.setComment( "Sets the state of the \"add default entities\" flag." );

        jClass.addMethod( addDefaultEntitiesSetter );

        // The getter
        JMethod addDefaultEntitiesGetter = new JMethod( "getAddDefaultEntities", JType.BOOLEAN, null );

        addDefaultEntitiesGetter.setComment( "Returns the state of the \"add default entities\" flag." );

        addDefaultEntitiesGetter.setSourceCode( "return addDefaultEntities;" );

        jClass.addMethod( addDefaultEntitiesGetter );

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write the class readers
        // ----------------------------------------------------------------------

        writeAllClassesReaders( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        if ( requiresDomSupport )
        {
            writeBuildDomMethod( jClass );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            if ( isTrackingSupport( clazz ) )
            {
                continue;
            }

            writeClassParser( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        JavaClassMetadata javaClassMetadata =
            (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.class.getName() );

        // Skip abstract classes, no way to parse them out into objects
        if ( javaClassMetadata.isAbstract() )
        {
            return;
        }

        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( "parse" + capClassName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );
        unmarshall.addException( new JClass( "XmlPullParserException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "String tagName = parser.getName();" );
        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        if ( locationTracker != null )
        {
            sc.add( locationTracker.getName() + " " + LOCATION_VAR + ";" );
            writeNewSetLocation( "\"\"", uncapClassName, null, sc );
        }

        ModelField contentField = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // read all XML attributes first
        contentField = writeClassAttributesParser( modelFields, uncapClassName, rootElement, sc, jClass );

        // then read content, either content field or elements
        if ( contentField != null )
        {
            writePrimitiveField( contentField, contentField.getType(), uncapClassName, uncapClassName, "\"\"",
                                 "set" + capitalise( contentField.getName() ), sc );
        }
        else
        {
            //Write other fields

            sc.add( "java.util.Set parsed = new java.util.HashSet();" );

            sc.add( "while ( ( strict ? parser.nextTag() : nextTag( parser ) ) == XmlPullParser.START_TAG )" );

            sc.add( "{" );
            sc.indent();

            boolean addElse = false;

            for ( ModelField field : modelFields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                if ( !xmlFieldMetadata.isAttribute() )
                {
                    processField( field, xmlFieldMetadata, addElse, sc, uncapClassName, jClass );

                    addElse = true;
                }
            }

            if ( addElse )
            {
                sc.add( "else" );

                sc.add( "{" );
                sc.indent();
            }

            sc.add( "checkUnknownElement( parser, strict );" );

            if ( addElse )
            {
                sc.unindent();
                sc.add( "}" );
            }

            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    private ModelField writeClassAttributesParser( List<ModelField> modelFields, String objectName, boolean rootElement,
                                                   JSourceCode sc, JClass jClass )
    {
        ModelField contentField = null;

        sc.add( "for ( int i = parser.getAttributeCount() - 1; i >= 0; i-- )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String name = parser.getAttributeName( i );" );
        sc.add( "String value = parser.getAttributeValue( i );" );
        sc.add( "" );

        sc.add("if ( name.indexOf( ':' ) >= 0 )");
        sc.add("{");
        sc.addIndented("// just ignore attributes with non-default namespace (for example: xmlns:xsi)");
        sc.add("}");
        if ( rootElement )
        {
            sc.add( "else if ( \"xmlns\".equals( name ) )" );
            sc.add( "{" );
            sc.addIndented( "// ignore xmlns attribute in root class, which is a reserved attribute name" );
            sc.add( "}" );
        }

        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isAttribute() )
            {
                String tagName = resolveTagName( field, xmlFieldMetadata );

                sc.add( "else if ( \"" + tagName + "\".equals( name ) )" );
                sc.add( "{" );
                sc.indent();

                writePrimitiveField( field, field.getType(), objectName, objectName, "\"" + field.getName() + "\"",
                                     "set" + capitalise( field.getName() ), sc );

                sc.unindent();
                sc.add( "}" );
            }
            // TODO check if we have already one with this type and throws Exception
            if ( xmlFieldMetadata.isContent() )
            {
                contentField = field;
            }
        }
        sc.add( "else" );

        sc.add( "{" );
        sc.addIndented("checkUnknownAttribute( parser, name, tagName, strict );");
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        return contentField;
    }

    /**
     * Generate code to process a field represented as an XML element.
     *
     * @param field            the field to process
     * @param xmlFieldMetadata its XML metadata
     * @param addElse          add an <code>else</code> statement before generating a new <code>if</code>
     * @param sc               the method source code to add to
     * @param objectName       the object name in the source
     * @param jClass           the generated class source file
     */
    private void processField( ModelField field, XmlFieldMetadata xmlFieldMetadata, boolean addElse, JSourceCode sc,
                               String objectName, JClass jClass )
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

        String tagComparison =
            ( addElse ? "else " : "" ) + "if ( checkFieldWithDuplicate( parser, \"" + fieldTagName + "\", " + alias
                + ", parsed ) )";

        if ( !( field instanceof ModelAssociation ) )
        { // model field
            sc.add( tagComparison );

            sc.add( "{" );

            sc.indent();

            writePrimitiveField( field, field.getType(), objectName, objectName, "\"" + field.getName() + "\"",
                                 "set" + capFieldName, sc );

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
                sc.addIndented(
                    objectName + ".set" + capFieldName + "( parse" + association.getTo() + "( parser, strict"
                        + trackingArgs + " ) );" );
                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                String valuesTagName = resolveTagName( fieldTagName, xmlAssociationMetadata );

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    boolean wrappedItems = xmlAssociationMetadata.isWrappedItems();

                    boolean inModel = isClassInModel( association.getTo(), field.getModelClass().getModel() );

                    JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) association.getMetadata( JavaFieldMetadata.ID );

                    String adder;

                    if ( wrappedItems )
                    {
                        sc.add( tagComparison );

                        sc.add( "{" );
                        sc.indent();

                        if ( javaFieldMetadata.isSetter() )
                        {
                        	String associationType = type;
                        	String defaultValue = association.getDefaultValue();
                            if( useJava5 )
                            {
                            	// defaultValue looks like java.util.ArrayList/*<Type>*/()
                            	defaultValue = defaultValue.replace("/*", "").replace("*/", "");
                            	
                            	associationType = type + '<' + association.getTo() + '>';
                            }
                            sc.add( associationType + " " + associationName + " = " + defaultValue + ";" );

                            sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                            adder = associationName + ".add";
                        }
                        else
                        {
                            adder = objectName + ".add" + association.getTo();
                        }

                        if ( !inModel && locationTracker != null )
                        {
                            sc.add( locationTracker.getName() + " " + LOCATION_VAR + "s;" );
                            writeNewSetLocation( field, objectName, LOCATION_VAR + "s", sc );
                        }

                        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"" + valuesTagName + "\".equals( parser.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();
                    }
                    else
                    {
                        sc.add( ( addElse ? "else " : "" ) + "if ( \"" + valuesTagName
                                    + "\".equals( parser.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        if ( javaFieldMetadata.isGetter() && javaFieldMetadata.isSetter() )
                        {
                        	String associationType = type;
                            String defaultValue = association.getDefaultValue();
                            if( useJava5 )
                            {
                            	// defaultValue looks like java.util.ArrayList/*<Type>*/()
                            	defaultValue = defaultValue.replace("/*", "").replace("*/", "");
                            	
                            	associationType = type + '<' + association.getTo() + '>';
                            }
                        	
                            sc.add( associationType + " " + associationName + " = " + objectName + ".get" + capFieldName + "();" );

                            sc.add( "if ( " + associationName + " == null )" );

                            sc.add( "{" );
                            sc.indent();
                            
                            sc.add( associationName + " = " + defaultValue + ";" );

                            sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                            sc.unindent();
                            sc.add( "}" );

                            adder = associationName + ".add";
                        }
                        else
                        {
                            adder = objectName + ".add" + association.getTo();
                        }

                        if ( !inModel && locationTracker != null )
                        {
                            sc.add( locationTracker.getName() + " " + LOCATION_VAR + "s = " + objectName + ".get"
                                        + capitalise( singular( locationField ) ) + "( \"" + field.getName()
                                        + "\" );" );
                            sc.add( "if ( " + LOCATION_VAR + "s == null )" );
                            sc.add( "{" );
                            sc.indent();
                            writeNewSetLocation( field, objectName, LOCATION_VAR + "s", sc );
                            sc.unindent();
                            sc.add( "}" );
                        }
                    }

                    if ( inModel )
                    {
                        sc.add( adder + "( parse" + association.getTo() + "( parser, strict" + trackingArgs + " ) );" );
                    }
                    else
                    {
                        String key;
                        if ( ModelDefault.SET.equals( type ) )
                        {
                            key = "?";
                        }
                        else
                        {
                            key = ( useJava5 ? "Integer.valueOf" : "new java.lang.Integer" ) + "( " + associationName
                                + ".size() )";
                        }
                        writePrimitiveField( association, association.getTo(), associationName, LOCATION_VAR + "s", key,
                                             "add", sc );
                    }

                    if ( wrappedItems )
                    {
                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.addIndented( "checkUnknownElement( parser, strict );" );
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

                    if ( locationTracker != null )
                    {
                        sc.add( locationTracker.getName() + " " + LOCATION_VAR + "s;" );
                        writeNewSetLocation( field, objectName, LOCATION_VAR + "s", sc );
                    }

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"" + valuesTagName + "\".equals( parser.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        writeNewLocation( LOCATION_VAR, sc );

                        sc.add( "// " + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( \"key\".equals( parser.getName() ) )" );

                        sc.add( "{" );
                        sc.addIndented( "key = parser.nextText();" );
                        sc.add( "}" );

                        sc.add( "else if ( \"value\".equals( parser.getName() ) )" );

                        sc.add( "{" );
                        sc.indent();
                        writeNewLocation( LOCATION_VAR, sc );
                        sc.add( "value = parser.nextText()" + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );
                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.addIndented( "parser.nextText();" );
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );
                        writeSetLocation( "key", LOCATION_VAR + "s", LOCATION_VAR, sc );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( "parser.next();" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //INLINE Mode

                        sc.add( "while ( parser.nextTag() == XmlPullParser.START_TAG )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = parser.getName();" );

                        writeNewSetLocation( "key", LOCATION_VAR + "s", null, sc );

                        sc.add(
                            "String value = parser.nextText()" + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );

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

    private void writePrimitiveField( ModelField field, String type, String objectName, String locatorName,
                                      String locationKey, String setterName, JSourceCode sc )
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String tagName = resolveTagName(field, xmlFieldMetadata);

        String parserGetter;
        if ( xmlFieldMetadata.isAttribute() )
        {
            parserGetter = "value"; // local variable created in the parsing block
        }
        else
        {
            parserGetter = "parser.nextText()";
        }

/* TODO: this and a default
        if ( xmlFieldMetadata.isRequired() )
        {
            parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser, strict )";
        }
*/

        if ( xmlFieldMetadata.isTrim() )
        {
            parserGetter = "interpolatedTrimmed( " + parserGetter + ", \"" + tagName + "\" )";
        }

        String keyCapture = "";
        writeNewLocation( null, sc );
        if ( locationTracker != null && "?".equals( locationKey ) )
        {
        	if ( useJava5 )
        	{
        		sc.add( type + " _key;" );
        	}
        	else
        	{
                sc.add( "Object _key;" );
        	}
            locationKey = "_key";
            keyCapture = "_key = ";
        }
        else
        {
            writeSetLocation( locationKey, locatorName, null, sc );
        }

        if ( "boolean".equals( type ) || "Boolean".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( " + keyCapture + "getBooleanValue( " + parserGetter + ", \""
                        + tagName + "\", parser, \"" + field.getDefaultValue() + "\" ) );" );
        }
        else if ( "char".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( " + keyCapture + "getCharacterValue( " + parserGetter + ", \""
                        + tagName + "\", parser ) );" );
        }
        else if ( "double".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getDoubleValue( " + parserGetter + ", \"" + tagName
                    + "\", parser, strict ) );" );
        }
        else if ( "float".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getFloatValue( " + parserGetter + ", \"" + tagName
                    + "\", parser, strict ) );" );
        }
        else if ( "int".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( " + keyCapture + "getIntegerValue( " + parserGetter + ", \""
                        + tagName + "\", parser, strict ) );" );
        }
        else if ( "long".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getLongValue( " + parserGetter + ", \"" + tagName
                    + "\", parser, strict ) );" );
        }
        else if ( "short".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getShortValue( " + parserGetter + ", \"" + tagName
                    + "\", parser, strict ) );" );
        }
        else if ( "byte".equals( type ) )
        {
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getByteValue( " + parserGetter + ", \"" + tagName
                    + "\", parser, strict ) );" );
        }
        else if ( "String".equals( type ) )
        {
            // TODO: other Primitive types
            sc.add( objectName + "." + setterName + "( " + keyCapture + parserGetter + " );" );
        }
        else if ( "Date".equals( type ) )
        {
            String format = xmlFieldMetadata.getFormat();
            sc.add( "String dateFormat = " + ( format != null ? "\"" + format + "\"" : "null" ) + ";" );
            sc.add(
                objectName + "." + setterName + "( " + keyCapture + "getDateValue( " + parserGetter + ", \"" + tagName
                    + "\", dateFormat, parser ) );" );
        }
        else if ( "DOM".equals( type ) )
        {
            sc.add( objectName + "." + setterName + "( " + keyCapture + ( domAsXpp3
                ? "org.codehaus.plexus.util.xml.Xpp3DomBuilder.build"
                : "buildDom" ) + "( parser, " + xmlFieldMetadata.isTrim() + " ) );" );

            requiresDomSupport = true;
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type: " + type );
        }

        if ( keyCapture.length() > 0 )
        {
            writeSetLocation( locationKey, locatorName, null, sc );
        }
    }

    private void writeBuildDomMethod( JClass jClass )
    {
        if ( domAsXpp3 )
        {
            // no need, Xpp3DomBuilder provided by plexus-utils
            return;
        }
        jClass.addField( new JField( new JClass( "org.w3c.dom.Document" ), "_doc_" ) );
        JMethod method = new JMethod( "initDoc", null, null );
        method.getModifiers().makePrivate();
        method.addException( new JClass( "javax.xml.parsers.ParserConfigurationException" ) );

        JSourceCode sc = method.getSourceCode();
        sc.add(
            "javax.xml.parsers.DocumentBuilderFactory dbfac = javax.xml.parsers.DocumentBuilderFactory.newInstance();" );
        sc.add( "javax.xml.parsers.DocumentBuilder docBuilder = dbfac.newDocumentBuilder();" );
        sc.add( "_doc_ = docBuilder.newDocument();" );
        jClass.addMethod( method );

        String type = "org.w3c.dom.Element";
        method = new JMethod( "buildDom", new JType( type ), null );
        method.getModifiers().makePrivate();
        method.addParameter( new JParameter( new JType( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "trim" ) );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "java.util.Stack elements = new java.util.Stack();" );

        sc.add( "java.util.Stack values = new java.util.Stack();" );

        sc.add( "int eventType = parser.getEventType();" );

        sc.add( "boolean spacePreserve = false;" );

        sc.add( "while ( eventType != XmlPullParser.END_DOCUMENT )" );
        sc.add( "{" );
        sc.indent();

        sc.add( "if ( eventType == XmlPullParser.START_TAG )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "spacePreserve = false;" );
        sc.add( "String rawName = parser.getName();" );

        sc.add( "org.w3c.dom.Element element = _doc_.createElement( rawName );" );

        sc.add( "if ( !elements.empty() )" );
        sc.add( "{" );
        sc.indent();
        sc.add( type + " parent = (" + type + ") elements.peek();" );

        sc.add( "parent.appendChild( element );" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "elements.push( element );" );

        sc.add( "if ( parser.isEmptyElementTag() )" );
        sc.add( "{" );
        sc.addIndented( "values.push( null );" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "values.push( new StringBuffer() );" );
        sc.add( "}" );

        sc.add( "int attributesSize = parser.getAttributeCount();" );

        sc.add( "for ( int i = 0; i < attributesSize; i++ )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "String name = parser.getAttributeName( i );" );

        sc.add( "String value = parser.getAttributeValue( i );" );

        sc.add( "element.setAttribute( name, value );" );
        sc.add( "spacePreserve = spacePreserve || ( \"xml:space\".equals( name ) && \"preserve\".equals( value ) );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XmlPullParser.TEXT )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "StringBuffer valueBuffer = (StringBuffer) values.peek();" );

        sc.add( "String text = parser.getText();" );

        sc.add( "if ( trim && !spacePreserve )" );
        sc.add( "{" );
        sc.addIndented( "text = text.trim();" );
        sc.add( "}" );

        sc.add( "valueBuffer.append( text );" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "else if ( eventType == XmlPullParser.END_TAG )" );
        sc.add( "{" );
        sc.indent();

        sc.add( type + " element = (" + type + ") elements.pop();" );

        sc.add( "// this Object could be null if it is a singleton tag" );
        sc.add( "Object accumulatedValue = values.pop();" );

        sc.add( "if ( !element.hasChildNodes() )" );
        sc.add( "{" );
        sc.addIndented(
            "element.setTextContent( ( accumulatedValue == null ) ? null : accumulatedValue.toString() );" );
        sc.add( "}" );

        sc.add( "if ( values.empty() )" );
        sc.add( "{" );
        sc.addIndented( "return element;" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        sc.add( "eventType = parser.next();" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "throw new IllegalStateException( \"End of document found before returning to 0 depth\" );" );

        jClass.addMethod( method );
    }

    private void writeHelpers( JClass jClass )
    {
        jClass.addMethod(getTrimmedValueMethod());
        jClass.addMethod(getInterpolatedTrimmed());
        jClass.addMethod(getRequiredAttributeValueMethod());
        jClass.addMethod(getBooleanValueMethod());
        jClass.addMethod(getBooleanValue2Method());
        jClass.addMethod( getCharacterValueMethod() );
        jClass.addMethod(convertNumericalType( "getIntegerValue", JType.INT, "Integer.valueOf( s ).intValue()", "an integer" ));
        jClass.addMethod(convertNumericalType( "getShortValue", JType.SHORT, "Short.valueOf( s ).shortValue()", "a short integer" ));
        jClass.addMethod(convertNumericalType( "getByteValue", JType.BYTE, "Byte.valueOf( s ).byteValue()", "a byte" ));
        jClass.addMethod(convertNumericalType( "getLongValue", JType.LONG, "Long.valueOf( s ).longValue()", "a long integer" ));
        jClass.addMethod(convertNumericalType( "getFloatValue", JType.FLOAT, "Float.valueOf( s ).floatValue()",
                                       "a floating point number" ));
        jClass.addMethod(convertNumericalType( "getDoubleValue", JType.DOUBLE, "Double.valueOf( s ).doubleValue()",
                                       "a floating point number" ));
        jClass.addMethod( getDateValueMethod() );
        jClass.addMethod( getDateValue2Method() );
        jClass.addMethod( getCheckFieldWithDuplicateMethod() );
        jClass.addMethod( getCheckUnknonwElement2Method() );
        jClass.addMethod( getCheckUnknownAttributeMethod() );
        jClass.addMethod( getNextTagMethod() );
    }

    private JMethod getCheckUnknonwElement2Method() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "checkUnknownElement", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new XmlPullParserException( \"Unrecognised tag: '\" + parser.getName() + \"'\", parser, null );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add( "for ( int unrecognizedTagCount = 1; unrecognizedTagCount > 0; )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "int eventType = parser.next();" );
        sc.add( "if ( eventType == XmlPullParser.START_TAG )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount++;" );
        sc.add( "}" );
        sc.add( "else if ( eventType == XmlPullParser.END_TAG )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount--;" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        return method;
    }

    private JMethod getNextTagMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "nextTag", JType.INT, null );
        method.addException(new JClass("IOException"));
        method.addException(new JClass("XmlPullParserException"));
        method.getModifiers().makePrivate();

        method.addParameter(new JParameter(new JClass("XmlPullParser"), "parser"));

        sc = method.getSourceCode();

        sc.add( "int eventType = parser.next();" );
        sc.add( "if ( eventType == XmlPullParser.TEXT )" );
        sc.add("{");
        sc.addIndented("eventType = parser.next();");
        sc.add( "}" );
        sc.add( "if ( eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_TAG )" );
        sc.add("{");
        sc.addIndented(
                "throw new XmlPullParserException( \"expected START_TAG or END_TAG not \" + XmlPullParser.TYPES[eventType], parser, null );");
        sc.add("}");
        sc.add( "return eventType;" );
        return method;
    }

    private JMethod getCheckUnknownAttributeMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "checkUnknownAttribute", null, null );
        method.getModifiers().makePrivate();

        method.addParameter(new JParameter(new JClass("XmlPullParser"), "parser"));
        method.addParameter(new JParameter(new JClass("String"), "attribute"));
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException(new JClass("XmlPullParserException"));
        method.addException(new JClass("IOException"));

        sc = method.getSourceCode();

        if ( strictXmlAttributes )
        {
            sc.add(
                "// strictXmlAttributes = true for model: if strict == true, not only elements are checked but attributes too" );
            sc.add( "if ( strict )" );

            sc.add( "{" );
            sc.addIndented(
                "throw new XmlPullParserException( \"Unknown attribute '\" + attribute + \"' for tag '\" + tagName + \"'\", parser, null );" );
            sc.add( "}" );
        }
        else
        {
            sc.add(
                "// strictXmlAttributes = false for model: always ignore unknown XML attribute, even if strict == true" );
        }
        return method;
    }

    private JMethod getCheckFieldWithDuplicateMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "checkFieldWithDuplicate", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "alias" ) );
        method.addParameter( new JParameter( new JClass( "java.util.Set" ), "parsed" ) );
        method.addException( new JClass( "XmlPullParserException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( !( parser.getName().equals( tagName ) || parser.getName().equals( alias ) ) )" );

        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );

        sc.add( "if ( !parsed.add( tagName ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new XmlPullParserException( \"Duplicated tag: '\" + tagName + \"'\", parser, null );" );
        sc.add( "}" );

        sc.add( "return true;" );
        return method;
    }

    private JMethod getDateValue2Method() {
        JMethod method;
        method = new JMethod( "getDateValue", new JClass( "java.util.Date" ), null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter(new JParameter(new JClass("String"), "dateFormat"));
        method.addParameter(new JParameter(new JClass("XmlPullParser"), "parser"));
        method.addException(new JClass("XmlPullParserException"));

        writeDateParsingHelper(method.getSourceCode(), "new XmlPullParserException( e.getMessage(), parser, e )");
        return method;
    }

    private JMethod getDateValueMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "getDateValue", new JClass( "java.util.Date" ), null );
        method.addException(new JClass("XmlPullParserException"));
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addException( new JClass( "XmlPullParserException" ) );

        sc = method.getSourceCode();

        sc.add("return getDateValue( s, attribute, null, parser );");
        return method;
    }

    private JMethod getCharacterValueMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "getCharacterValue", JType.CHAR, null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return s.charAt( 0 );" );
        sc.add( "}" );

        sc.add( "return 0;" );
        return method;
    }

    private JMethod getBooleanValue2Method() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "getBooleanValue", JType.BOOLEAN, null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "defaultValue" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null && s.length() != 0 )" );

        sc.add( "{" );
        sc.addIndented( "return Boolean.valueOf( s ).booleanValue();" );
        sc.add( "}" );

        sc.add( "if ( defaultValue != null )" );

        sc.add( "{" );
        sc.addIndented( "return Boolean.valueOf( defaultValue ).booleanValue();" );
        sc.add( "}" );

        sc.add( "return false;" );
        return method;
    }

    private JMethod getBooleanValueMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "getBooleanValue", JType.BOOLEAN, null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );

        sc = method.getSourceCode();

        sc.add( "return getBooleanValue( s, attribute, parser, null );" );
        return method;
    }

    private JMethod getRequiredAttributeValueMethod() {
        JMethod method;
        JSourceCode sc;
        method = new JMethod( "getRequiredAttributeValue", new JClass( "String" ), null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new XmlPullParserException( \"Missing required value for attribute '\" + attribute + \"'\", parser, null );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return s;" );
        return method;
    }

    private JMethod getTrimmedValueMethod() {
        JMethod method = new JMethod( "getTrimmedValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "s = s.trim();" );
        sc.add( "}" );

        sc.add( "return s;" );
        return method;
    }

    private JMethod getInterpolatedTrimmed() {
        JMethod method = new JMethod( "interpolatedTrimmed", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "value" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "context" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "return getTrimmedValue( contentTransformer.transform( value, context ) );" );
        return method;
    }


    private JMethod convertNumericalType( String methodName, JType returnType, String expression, String typeDesc )
    {
        JMethod method = new JMethod( methodName, returnType, null );
        method.addException( new JClass( "XmlPullParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "XmlPullParser" ), "parser" ) );
        method.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );

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
        sc.addIndented(
            "throw new XmlPullParserException( \"Unable to parse element '\" + attribute + \"', must be " + typeDesc
                + "\", parser, nfe );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return 0;" );

        return method;
    }

    private void addTrackingParameters( JMethod method )
    {
        if ( sourceTracker != null )
        {
            method.addParameter( new JParameter( new JClass( sourceTracker.getName() ), SOURCE_PARAM ) );
        }
    }

    private void writeNewSetLocation( ModelField field, String objectName, String trackerVariable, JSourceCode sc )
    {
        writeNewSetLocation( "\"" + field.getName() + "\"", objectName, trackerVariable, sc );
    }

    private void writeNewSetLocation( String key, String objectName, String trackerVariable, JSourceCode sc )
    {
        writeNewLocation( trackerVariable, sc );
        writeSetLocation( key, objectName, trackerVariable, sc );
    }

    private void writeNewLocation( String trackerVariable, JSourceCode sc )
    {
        if ( locationTracker == null )
        {
            return;
        }

        String constr = "new " + locationTracker.getName() + "( parser.getLineNumber(), parser.getColumnNumber()";
        constr += ( sourceTracker != null ) ? ", " + SOURCE_PARAM : "";
        constr += " )";

        sc.add( ( ( trackerVariable != null ) ? trackerVariable : LOCATION_VAR ) + " = " + constr + ";" );
    }

    private void writeSetLocation( String key, String objectName, String trackerVariable, JSourceCode sc )
    {
        if ( locationTracker == null )
        {
            return;
        }

        String variable = ( trackerVariable != null ) ? trackerVariable : LOCATION_VAR;

        sc.add( objectName + ".set" + capitalise( singular( locationField ) ) + "( " + key + ", " + variable + " );" );
    }

}
