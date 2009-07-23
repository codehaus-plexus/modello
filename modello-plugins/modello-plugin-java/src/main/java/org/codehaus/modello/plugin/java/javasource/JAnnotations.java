package org.codehaus.modello.plugin.java.javasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JAnnotations
{
    private List/*String*/ annotations;

    public JAnnotations()
    {
        this.annotations = new ArrayList();
    }

    public void appendAnnotation( String annotation )
    {
        annotations.add( annotation );
    }

    /**
     * Returns the String representation of this JAnnotations
     * @return the String representation of this JAnnotations
     **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String lineSeparator = System.getProperty( "line.separator" );
        Iterator iterator = annotations.iterator();
        while ( iterator.hasNext() )
        {
            sb.append( iterator.next().toString() );
            if ( iterator.hasNext() )
            {
                sb.append( lineSeparator );
            }
        }
        return sb.toString();
    } //-- toString

    /**
     * prints this Annotations using the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to
     */
    public void print( JSourceWriter jsw )
    {
        Iterator iterator = annotations.iterator();
        while ( iterator.hasNext() )
        {
            jsw.writeln( iterator.next().toString() );
        }
    } // -- print
}
