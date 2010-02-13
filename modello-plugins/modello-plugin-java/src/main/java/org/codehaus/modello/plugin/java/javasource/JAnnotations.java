package org.codehaus.modello.plugin.java.javasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JAnnotations
{
    private List<String> annotations;

    public JAnnotations()
    {
        this.annotations = new ArrayList<String>();
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
        for ( Iterator<String> iterator = annotations.iterator(); iterator.hasNext(); )
        {
            sb.append( iterator.next() );
            if ( iterator.hasNext() )
            {
                sb.append( ' ' );
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
        for ( Iterator<String> iterator = annotations.iterator(); iterator.hasNext(); )
        {
            jsw.writeln( iterator.next().toString() );
        }
    } // -- print
}
