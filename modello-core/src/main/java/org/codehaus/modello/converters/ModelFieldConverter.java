package org.codehaus.modello.converters;

/*
 * LICENSE
 */

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloRuntimeException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelFieldConverter
    implements Converter
{
    private Converter defaultConverter;

    private Map fields = new HashMap();

    public ModelFieldConverter( Converter defaultConverter )
    {
        this.defaultConverter = defaultConverter;
    }

    public Map getMetaDataForField( String name )
    {
        return (Map) fields.get( name );
    }

    // ----------------------------------------------------------------------
    // Converter Implementation
    // ----------------------------------------------------------------------

    public boolean canConvert( Class clazz )
    {
        return clazz.getName() == ModelField.class.getName();
    }

    public void marshal( Object object, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        throw new ModelloRuntimeException( "Not implemented" );
    }

    public Object unmarshal( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
//        XppDomReader xmlReader = (XppDomReader) reader;

        Object obj = defaultConverter.unmarshal(reader, context);

        if ( !context.getRequiredType().getName().equals( ModelField.class.getName() ) )
        {
            throw new ModelloRuntimeException( "Internal error. This converter can only convert ModelField's." );
        }

        if ( !( obj instanceof ModelField ) )
        {
            throw new ModelloRuntimeException( "Internal error. This converter can only convert ModelField's." );
        }

        ModelField field = (ModelField) obj;

        Map attributes = new HashMap();

        for( Iterator it = reader.getAttributeNames(); it.hasNext(); )
        {
            String name = it.next().toString();

            String value = reader.getAttribute( name );

            attributes.put( name, value );
        }

        fields.put( field.getName(), attributes );

        return obj;
    }
}
