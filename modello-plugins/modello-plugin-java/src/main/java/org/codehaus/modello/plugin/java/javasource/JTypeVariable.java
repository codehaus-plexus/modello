package org.codehaus.modello.plugin.java.javasource;

public class JTypeVariable
{
    private final String value;
    
    private final String name;
    
    public JTypeVariable( String value )
    {
        this.value = value;
        this.name = value.split( " " )[0];
    }
    
    public String getName()
    {
        return name;
    }
    
    public void print( JSourceWriter jsw )
    {
        jsw.write( value );
    }
}
