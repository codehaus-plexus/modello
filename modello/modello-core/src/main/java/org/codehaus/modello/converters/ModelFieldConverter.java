package org.codehaus.modello.converters;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelFieldConverter
//    implements Converter
{/*
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
        if ( !(object instanceof ModelField ) )
        {
            throw new ModelloRuntimeException( "This converter can only convert ModelField's." );
        }

        throw new ModelloRuntimeException( "NOT IMPLEMENTED" );
//        ModelField field = (ModelField) object;

//        XmlMetaData metaData = field.getMetaData( XmlMetaData.ID );

//        writer.addAttribute( );
    }

    public Object unmarshal( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
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
*/
}
