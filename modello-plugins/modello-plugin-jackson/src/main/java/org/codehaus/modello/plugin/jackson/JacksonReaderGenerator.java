package org.codehaus.modello.plugin.jackson;

/*
 * Copyright (c) 2004-2013, Codehaus.org
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
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class JacksonReaderGenerator
    extends AbstractJacksonGenerator
{

    private static final String SOURCE_PARAM = "source";

    private static final String LOCATION_VAR = "_location";

    private boolean requiresDomSupport;

    private ModelClass locationTracker;

    private String locationField;

    private ModelClass sourceTracker;

    private String trackingArgs;

    protected boolean isLocationTracking()
    {
        return false;
    }

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        requiresDomSupport = false;
        locationTracker = sourceTracker = null;
        trackingArgs = locationField = "";

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
            generateJacksonReader();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating Jackson Reader.", ex );
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
        // Write the read(JsonParser) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod unmarshall = new JMethod( readerMethodName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        String variableName = uncapitalise( className );

        sc.add(
            className + ' ' + variableName + " = parse" + capClassName + "( parser, strict" + trackingArgs + " );" );

        if ( rootElement )
        {
            // TODO
            // sc.add( variableName + ".setModelEncoding( parser.getInputEncoding() );" );
        }

        sc.add( "return " + variableName + ';' );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(Reader[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "JsonParser parser = factory.createParser( reader );" );

        sc.add( "return " + readerMethodName + "( parser, strict );" );

        jClass.addMethod( unmarshall );unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return " + readerMethodName + "( reader, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(InputStream[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return " + readerMethodName + "( new InputStreamReader( in ), strict" + trackingArgs + " );" );

        jClass.addMethod( unmarshall );unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return " + readerMethodName + "( in, true );" );

        jClass.addMethod( unmarshall );

        // --------------------------------------------------------------------
    }

    private void generateJacksonReader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName =
            objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) + ".io.jackson";

        String unmarshallerName = getFileName( "JacksonReader" + ( isLocationTracking() ? "Ex" : "" ) );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "com.fasterxml.jackson.core.JsonFactory" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonParser" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonParser.Feature" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonParseException" );
        jClass.addImport( "com.fasterxml.jackson.core.JsonToken" );
        jClass.addImport( "java.io.InputStream" );
        jClass.addImport( "java.io.InputStreamReader" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "java.util.Set" );
        jClass.addImport( "java.util.HashSet" );

        addModelImports( jClass, null );

        JField factoryField = new JField( new JClass( "JsonFactory" ), "factory" );
        factoryField.getModifiers().setFinal( true );
        factoryField.setInitString( "new JsonFactory()" );
        jClass.addField( factoryField );

        JConstructor jacksonReaderConstructor = new JConstructor( jClass );
        JSourceCode sc = jacksonReaderConstructor.getSourceCode();
        sc.add( "factory.enable( Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER  );" );
        sc.add( "factory.enable( Feature.ALLOW_COMMENTS );" );
        sc.add( "factory.enable( Feature.ALLOW_NON_NUMERIC_NUMBERS );" );
        sc.add( "factory.enable( Feature.ALLOW_NUMERIC_LEADING_ZEROS );" );
        sc.add( "factory.enable( Feature.ALLOW_SINGLE_QUOTES );" );
        sc.add( "factory.enable( Feature.ALLOW_UNQUOTED_CONTROL_CHARS );" );
        sc.add( "factory.enable( Feature.ALLOW_UNQUOTED_FIELD_NAMES );" );

        jClass.addConstructor( jacksonReaderConstructor );

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

        // ----------------------------------------------------------------------
        // DOM support
        // ----------------------------------------------------------------------

        if ( requiresDomSupport )
        {
            getLogger().warn( "Jackson DOM support requires auxiliary com.fasterxml.jackson.core:jackson-databind module!" );

            jClass.addImport( "com.fasterxml.jackson.databind.ObjectMapper" );

            sc.add( "factory.setCodec( new ObjectMapper() );" );
        }

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

        unmarshall.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "if ( JsonToken.START_OBJECT != parser.getCurrentToken() && JsonToken.START_OBJECT != parser.nextToken() )" );
        sc.add( "{" );
        sc.addIndented( "throw new JsonParseException( \"Expected '"
                        + className
                        + "' data to start with an Object\", parser.getCurrentLocation() );" );
        sc.add( "}" );

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        if ( locationTracker != null )
        {
            sc.add( locationTracker.getName() + " " + LOCATION_VAR + ";" );
            writeNewSetLocation( "\"\"", uncapClassName, null, sc );
        }

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        {
            //Write other fields

            sc.add( "Set<String> parsed = new HashSet<String>();" );

            sc.add( "while ( JsonToken.END_OBJECT != parser.nextToken() )" );

            sc.add( "{" );
            sc.indent();

            boolean addElse = false;

            for ( ModelField field : modelFields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                processField( field, xmlFieldMetadata, addElse, sc, uncapClassName, jClass );

                addElse = true;
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
                                 "set" + capFieldName, sc, false );

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

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    boolean inModel = isClassInModel( association.getTo(), field.getModelClass().getModel() );

                    sc.add( ( addElse ? "else " : "" ) + "if ( checkFieldWithDuplicate( parser, \""
                            + fieldTagName
                            + "\", "
                            + alias
                            + ", parsed ) )" );

                    sc.add( "{" );
                    sc.indent();

                    sc.add( "if ( JsonToken.START_ARRAY != parser.nextToken() )" );
                    sc.add( "{" );
                    sc.addIndented( "throw new JsonParseException( \"Expected '"
                                    + fieldTagName
                                    + "' data to start with an Array\", parser.getCurrentLocation() );" );
                    sc.add( "}" );

                    JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) association.getMetadata( JavaFieldMetadata.ID );

                    String adder;

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

                    sc.add( "while ( JsonToken.END_ARRAY != parser.nextToken() )" );

                    sc.add( "{" );
                    sc.indent();

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
                                             "add", sc, true );
                    }

                    sc.unindent();
                    sc.add( "}" );

                    sc.unindent();
                    sc.add( "}" );
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
                        sc.add( "if ( JsonToken.START_ARRAY != parser.nextToken() )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new JsonParseException( \"Expected '"
                                        + fieldTagName
                                        + "' data to start with an Array\", parser.getCurrentLocation() );" );
                        sc.add( "}" );

                        sc.add( "// " + xmlAssociationMetadata.getMapStyle() + " mode." );

                        sc.add( "while ( JsonToken.END_ARRAY != parser.nextToken() )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( JsonToken.START_OBJECT != parser.getCurrentToken() && JsonToken.START_OBJECT != parser.nextToken() )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new JsonParseException( \"Expected '"
                                        + fieldTagName
                                        + "' item data to start with an Object\", parser.getCurrentLocation() );" );
                        sc.add( "}" );

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "Set<String> parsedPropertiesElements = new HashSet<String>();" );

                        sc.add( "while ( JsonToken.END_OBJECT != parser.nextToken() )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( checkFieldWithDuplicate( parser, \"key\", \"\", parsedPropertiesElements ) )" );

                        sc.add( "{" );
                        sc.addIndented( "parser.nextToken();" );

                        String parserGetter = "parser.getText()";

                        if ( xmlFieldMetadata.isTrim() )
                        {
                            parserGetter = "getTrimmedValue( " + parserGetter + " )";
                        }

                        sc.addIndented( "key = " + parserGetter + ";" );
                        sc.add( "}" );

                        sc.add( "else if ( checkFieldWithDuplicate( parser, \"value\", \"\", parsedPropertiesElements ) )" );

                        sc.add( "{" );
                        sc.addIndented( "parser.nextToken();" );

                        parserGetter = "parser.getText()";

                        if ( xmlFieldMetadata.isTrim() )
                        {
                            parserGetter = "getTrimmedValue( " + parserGetter + " )";
                        }

                        sc.addIndented( "value = " + parserGetter + ";" );
                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );
                        sc.addIndented( "checkUnknownElement( parser, strict );" );
                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //INLINE Mode

                        sc.add( "if ( JsonToken.START_OBJECT != parser.nextToken() )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new JsonParseException( \"Expected '"
                                        + fieldTagName
                                        + "' data to start with an Object\", parser.getCurrentLocation() );" );
                        sc.add( "}" );

                        sc.add( "while ( JsonToken.END_OBJECT != parser.nextToken() )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = parser.getCurrentName();" );

                        writeNewSetLocation( "key", LOCATION_VAR + "s", null, sc );

                        sc.add(
                            "String value = parser.nextTextValue()" + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );

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
                                      String locationKey, String setterName, JSourceCode sc, boolean wrappedItem )
    {
        XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

        String parserGetter = null;
        if ( "boolean".equals( type ) || "Boolean".equals( type ) )
        {
            parserGetter = "parser.getBooleanValue()";
        }
        else if ( "int".equals( type ) || "Integer".equals( type ) )
        {
            parserGetter = "parser.getIntValue()";
        }
        else if ( "short".equals( type ) || "Short".equals( type ) )
        {
            parserGetter = "parser.getShortValue()";
        }
        else if ( "long".equals( type ) || "Long".equals( type ) )
        {
            parserGetter = "parser.getLongValue()";
        }
        else if ( "double".equals( type ) || "Double".equals( type ) )
        {
            parserGetter = "parser.getDoubleValue()";
        }
        else if ( "float".equals( type ) || "Float".equals( type ) )
        {
            parserGetter = "parser.getFloatValue()";
        }
        else if ( "byte".equals( type ) )
        {
            parserGetter = "parser.getByteValue()";
        }
        else if ( "String".equals( type ) )
        {
            parserGetter = "parser.getText()";

            if ( xmlFieldMetadata.isTrim() )
            {
                parserGetter = "getTrimmedValue( " + parserGetter + " )";
            }
        }
        else if ( "DOM".equals( type ) )
        {
            requiresDomSupport = true;
            parserGetter = "parser.readValueAsTree()";
        }
        else
        {
            throw new IllegalArgumentException( "Unknown type "
                                                + type
                                                + " for field "
                                                + field.getModelClass().getName()
                                                + "."
                                                + field.getName() );
        }

        String keyCapture = "";
        writeNewLocation( null, sc );
        if ( locationTracker != null && "?".equals( locationKey ) )
        {
            sc.add( "Object _key;" );
            locationKey = "_key";
            keyCapture = "_key = ";
        }
        else
        {
            writeSetLocation( locationKey, locatorName, null, sc );
        }

        // primitives token already consumed when in ARRAY loop
        if ( !wrappedItem )
        {
            sc.add( "parser.nextToken();" );
        }

        sc.add( objectName + "." + setterName + "( " + keyCapture + parserGetter + " );" );

        if ( keyCapture.length() > 0 )
        {
            writeSetLocation( locationKey, locatorName, null, sc );
        }
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

        method = new JMethod( "getRequiredAttributeValue", new JClass( "String" ), null );
        method.addException( new JClass( "JsonParseException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        method.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new JsonParseException( \"Missing required value for attribute '\" + attribute + \"'\", parser.getCurrentLocation() );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkFieldWithDuplicate", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "alias" ) );
        method.addParameter( new JParameter( new JClass( "Set" ), "parsed" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "String currentName = parser.getCurrentName();" );

        sc.add( "" );

        sc.add( "if ( !( currentName.equals( tagName ) || currentName.equals( alias ) ) )" );

        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );

        sc.add( "if ( !parsed.add( tagName ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new JsonParseException( \"Duplicated tag: '\" + tagName + \"'\", parser.getCurrentLocation() );" );
        sc.add( "}" );

        sc.add( "return true;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownElement", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new JsonParseException( \"Unrecognised tag: '\" + parser.getCurrentName() + \"'\", parser.getCurrentLocation() );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add( "for ( int unrecognizedTagCount = 1; unrecognizedTagCount > 0; )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "JsonToken eventType = parser.nextToken();" );
        sc.add( "if ( eventType == JsonToken.START_OBJECT )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount++;" );
        sc.add( "}" );
        sc.add( "else if ( eventType == JsonToken.END_OBJECT )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount--;" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownAttribute", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "JsonParser" ), "parser" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        if ( strictXmlAttributes )
        {
            sc.add(
                "// strictXmlAttributes = true for model: if strict == true, not only elements are checked but attributes too" );
            sc.add( "if ( strict )" );

            sc.add( "{" );
            sc.addIndented(
                "throw new JsonParseException( \"Unknown attribute '\" + attribute + \"' for tag '\" + tagName + \"'\", parser.getCurrentLocation() );" );
            sc.add( "}" );
        }
        else
        {
            sc.add(
                "// strictXmlAttributes = false for model: always ignore unknown XML attribute, even if strict == true" );
        }

        jClass.addMethod( method );
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
