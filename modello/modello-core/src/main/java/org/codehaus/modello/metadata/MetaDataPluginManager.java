package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import org.codehaus.modello.plugin.AbstractPluginManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MetaDataPluginManager
    extends AbstractPluginManager
{
    public MetaDataPluginManager()
    {
        super( MetaDataPlugin.class );
    }

    /*
    public void initialize()
        throws ModelloException
    {
        getLogger().debug( "Loading metadata plugins." );

        try
        {
            Enumeration e = getClass().getClassLoader().getResources( "META-INF/modello/metadata.plugin.properties" );
    
            while ( e.hasMoreElements() )
            {
                URL url = (URL) e.nextElement();
    
                Properties p = new Properties();
    
                p.load( url.openStream() );
    
                loadMetaData( p );
            }
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while loading the metadatas.", ex );
        }
    }

    public MetaDataPlugin getMetaData( String metadataId )
        throws ModelloException
    {
        MetaDataPlugin metadata = (MetaDataPlugin) metaDataPlugins.get( metadataId );

        if ( metadata == null )
        {
            throw new ModelloException( "No such metadata: '" + metadataId + "'." );
        }

        return metadata;
    }

    public boolean hasMetaData( String metadataId )
    {
        return metaDataPlugins.containsKey( metadataId );
    }

    public Iterator getMetaDataPlugins()
    {
        return metaDataPlugins.values().iterator();
    }

    private void loadMetaData( Properties p )
        throws ModelloException
    {
        MetaDataPlugin metadata;

        String id = p.getProperty( PROPERTY_ID );

        if ( id == null || id.trim().length() == 0 )
        {
            throw new ModelloException( "Missing property from metadata plugin configuration: " + PROPERTY_ID );
        }

        String implementation = p.getProperty( PROPERTY_IMPLEMENTATION );

        if ( implementation == null || implementation.trim().length() == 0 )
        {
            throw new ModelloException( "Missing property from metadata plugin configuration: " + PROPERTY_IMPLEMENTATION );
        }

        Object obj;

        try
        {
            obj = getClass().forName( implementation ).newInstance();
        }
        catch( ClassNotFoundException ex )
        {
            throw new ModelloException( "Could not find the metadata implementation class: '" + implementation + "'.", ex );
        }
        catch( InstantiationException ex )
        {
            throw new ModelloException( "Could not instanciate the metadata implementation class: '" + implementation + "'.", ex );
        }
        catch( IllegalAccessException ex )
        {
            throw new ModelloException( "Could not access the metadata implementation class: '" + implementation + "'.", ex );
        }

        if ( !(obj instanceof MetaDataPlugin ) )
        {
            throw new ModelloException( "A Modello metadata plugin must implement the MetaDataPlugin interface." );
        }

        metadata = (MetaDataPlugin) obj;

        if ( metadata instanceof LogEnabled )
        {
            ((LogEnabled) metadata).setLogger( getLogger() );
        }

        plugins.put( id, metadata );
    }
*/
}
