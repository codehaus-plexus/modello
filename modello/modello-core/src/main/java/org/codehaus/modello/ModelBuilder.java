package org.codehaus.modello;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import org.codehaus.modello.converters.ModelFieldConverter;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelBuilder
    extends AbstractLogEnabled
{
//    private XStream xstream;

    private static ModelBuilder instance;
/*
    private GeneratorPluginManager generatorPluginManager;

    private MetaDataPluginManager metaDataPluginManager;
*/
    private ModelFieldConverter fieldConverter;
/*
    public void setGeneratorPluginManager( GeneratorPluginManager generatorPluginManager )
    {
        this.generatorPluginManager = generatorPluginManager;
    }

    public void setMetaDataPluginManager( MetaDataPluginManager metaDataPluginManager )
    {
        this.metaDataPluginManager = metaDataPluginManager;
    }
*/
    public void initialize()
        throws ModelloException
    {/*
        if ( generatorPluginManager == null )
        {
            throw new ModelloException( "Missing requirement: generator plugin manager." );
        }

        if ( metaDataPluginManager == null )
        {
            throw new ModelloException( "Missing requirement: meta data plugin manager." );
        }

        ReflectionProvider reflectionProvider = new PureJavaReflectionProvider();

        ClassMapper classMapper = new DefaultClassMapper();

        HierarchicalStreamDriver xmlReaderDriver = new XppDomDriver();

        xstream = new XStream( reflectionProvider, classMapper, xmlReaderDriver );

        xstream.alias( "model", Model.class );

        xstream.alias( "class", ModelClass.class );

        xstream.alias( "field", ModelField.class );

        xstream.alias( "association", ModelAssociation.class );

        xstream.alias( "codeSegment", CodeSegment.class );

        Converter defaultConverter = xstream.getConverterLookup().defaultConverter();

        fieldConverter = new ModelFieldConverter( defaultConverter );

        xstream.registerConverter( fieldConverter );
        */
    }
/*
    public XStream getXStream()
    {
        return xstream;
    }

    public Model getModel( File modelFile )
        throws ModelloException, ModelValidationException
    {
        String modelContents = fileRead( modelFile );
    
        modelContents = replace( modelContents, "<description>", "<description><![CDATA[" );
    
        modelContents = replace( modelContents, "</description>", "]]></description>" );

        Xpp3Dom dom;

        try
        {
            dom = Xpp3DomBuilder.build( new StringReader( modelContents ) );
        }
        catch( Exception ex )
        {
            throw new ModelloException( "Exception while unmarshalling the model.", ex );
        }

        Model objectModel = (Model) xstream.unmarshal( new XppDomReader( dom ) );

        objectModel.initialize();

        for( Iterator plugins = metaDataPluginManager.getPlugins(); plugins.hasNext(); )
        {
            MetaDataPlugin plugin = (MetaDataPlugin) plugins.next();

            Map classData = Collections.EMPTY_MAP;

            MetaData metaData = plugin.getModelMetaData( objectModel, classData );

            if ( metaData == null )
            {
                throw new ModelloException( "A meta data plugin must not return null." );
            }

            objectModel.addMetaData( metaData );
        }

        for( Iterator classes = objectModel.getClasses().iterator(); classes.hasNext(); )
        {
            ModelClass clazz = (ModelClass) classes.next();

            Map classData = Collections.EMPTY_MAP;

            for( Iterator plugins = metaDataPluginManager.getPlugins(); plugins.hasNext(); )
            {
                MetaDataPlugin plugin = (MetaDataPlugin) plugins.next();

                MetaData metaData = plugin.getClassMetaData( clazz, classData );

                if ( metaData == null )
                {
                    throw new ModelloException( "A meta data plugin must not return null." );
                }

                clazz.addMetaData( metaData );
            }

            for( Iterator fields = clazz.getFields().iterator(); fields.hasNext(); )
            {
                ModelField field = (ModelField) fields.next();

                Map fieldData = fieldConverter.getMetaDataForField( field.getName() );

                for( Iterator plugins = metaDataPluginManager.getPlugins(); plugins.hasNext(); )
                {
                    MetaDataPlugin plugin = (MetaDataPlugin) plugins.next();

                    MetaData metaData = plugin.getFieldMetaData( field, fieldData );

                    if ( metaData == null )
                    {
                        throw new ModelloException( "A meta data plugin must not return null." );
                    }

                    field.addMetaData( metaData );
                }
            }
        }

        objectModel.validate();

        for( Iterator classes = objectModel.getClasses().iterator(); classes.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) classes.next();

            modelClass.validate();

            for( Iterator fields = modelClass.getFields().iterator(); fields.hasNext(); )
            {
                ModelField modelField = (ModelField) fields.next();

                modelField.validate();
            }

            for( Iterator associations = modelClass.getAssociations().iterator(); associations.hasNext(); )
            {
                ModelAssociation modelAssociation = (ModelAssociation) associations.next();

                modelAssociation.validate();
            }
        }

        return objectModel;
    }

    protected String fileRead( File fileName )
        throws ModelloException
    {
        try
        {
            StringBuffer buf = new StringBuffer();

            FileInputStream in = new FileInputStream( fileName );

            int count;

            byte[] b = new byte[512];

            while ( ( count = in.read( b ) ) > 0 )
            {
                buf.append( new String( b, 0, count ) );
            }

            in.close();

            return buf.toString();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error while reading model.", ex );
        }
    }

    public static String replace( String text, String repl, String with )
    {
        return replace( text, repl, with, -1 );
    }

    public static String replace( String text, String repl, String with, int max )
    {
        if ( text == null || repl == null || with == null || repl.length() == 0 )
        {
            return text;
        }

        StringBuffer buf = new StringBuffer( text.length() );

        int start = 0, end = 0;

        while ( ( end = text.indexOf( repl, start ) ) != -1 )
        {
            buf.append( text.substring( start, end ) ).append( with );

            start = end + repl.length();

            if ( --max == 0 )
            {
                break;
            }
        }

        buf.append( text.substring( start ) );

        return buf.toString();
    }
*/
}
