package org.codehaus.modello.db;

/*
 * Copyright 2001-2007 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * SQLReservedWords - utility object to test against SQL Keywords. 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * 
 * @plexus.component role="org.codehaus.modello.db.SQLReservedWords"
 */
public class SQLReservedWords extends AbstractLogEnabled implements Initializable
{
    private Map keywords;

    /**
     * Tests the provided word to see if it is a keyword.
     * 
     * @param word the word to test.
     * @return true if the provided word is a keyword.
     */
    public boolean isKeyword( String word )
    {
        if ( StringUtils.isEmpty( word ) )
        {
            // empty or null is not a keyword. ;-)
            return false;
        }

        String key = word.trim().toUpperCase();
        return ( keywords.containsKey( key ) );
    }

    /**
     * Obtain the list of {@link KeywordSource} objects that the specified (potential) keyword
     * (might) belong to.
     * 
     * @param word the word to test.
     * @return the {@link List} of {@link KeywordSource} objects, or <code>null</code> if specified word is 
     *      not a reserved word.
     */
    public List /*<KeywordSource>*/getKeywordSourceList( String word )
    {
        if ( StringUtils.isEmpty( word ) )
        {
            // empty or null is not a keyword. ;-)
            return null;
        }

        String key = word.trim().toUpperCase();
        return (List) this.keywords.get( key );
    }

    /**
     * Obtain the comma delimited string of keyword sources that the specified (potential) word
     * (might) belong to.
     * 
     * @param word the wor to test.
     * @return the {@link String} of keyword source names seperated by commas, or <code>null</code> if word is 
     *      not a reserved word.
     */
    public String getKeywordSourceString( String word )
    {
        if ( StringUtils.isEmpty( word ) )
        {
            // empty or null is not a keyword. ;-)
            return null;
        }

        String key = word.trim().toUpperCase();
        List sources = (List) this.keywords.get( key );

        if ( sources == null )
        {
            return null;
        }

        StringBuffer ret = new StringBuffer();

        for ( Iterator it = sources.iterator(); it.hasNext(); )
        {
            KeywordSource source = (KeywordSource) it.next();
            if ( ret.length() > 0 )
            {
                ret.append( ", " );
            }
            ret.append( source.getName() );
        }

        return ret.toString();
    }

    public void initialize() throws InitializationException
    {
        loadKeywords();
    }

    /**
     * Finds the list of keywords and loads them into the {@link #keywords} {@link Map}.
     */
    private void loadKeywords()
    {
        this.keywords = new HashMap();

        Properties props = new Properties();

        URL definitionsURL = this.getClass().getResource( "keywords.properties" );

        if ( definitionsURL == null )
        {
            getLogger().error( "Unable to load definition file: keywords.properties" );
            return;
        }

        InputStream is = null;

        try
        {
            is = definitionsURL.openStream();
            props.load( is );

            String sources[] = StringUtils.split( props.getProperty( "keyword.sources" ), "," );

            for ( int i = 0; i < sources.length; i++ )
            {
                String source = sources[i];
                String sourceName = props.getProperty( "keyword.source." + source + ".name" );
                String sourceSeverity = props.getProperty( "keyword.source." + source + ".severity" );

                KeywordSource keywordSource = new KeywordSource( sourceName, sourceSeverity );

                loadKeywordSource( source + ".txt", keywordSource );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to load definitions file: " + "keywords.properties", e );
            return;
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    private void loadKeywordSource( String resource, KeywordSource source )
    {
        URL keywordsURL = this.getClass().getResource( resource );

        if ( keywordsURL == null )
        {
            getLogger().error( "Unable to find keywords for \"" + resource + "\"" );
            return;
        }

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;

        try
        {
            is = keywordsURL.openStream();
            isr = new InputStreamReader( is );
            reader = new BufferedReader( isr );

            String line = reader.readLine();
            while ( line != null )
            {
                line = line.trim();
                if ( line.length() > 0 )
                {
                    addKeyword( line, source );
                }
                line = reader.readLine();
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Unable to load keywords from " + keywordsURL.toExternalForm() + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( isr );
            IOUtil.close( is );
        }
    }

    private void addKeyword( String keyword, KeywordSource source )
    {
        String key = keyword.trim().toUpperCase();
        List sources = (List) this.keywords.get( key );
        if ( sources == null )
        {
            sources = new ArrayList();
        }
        sources.add( source );
        this.keywords.put( key, sources );
    }

    public class KeywordSource
    {
        private String name;

        private String severity;

        public KeywordSource( String name, String severity )
        {
            this.name = name;
            this.severity = severity;
        }

        public String getName()
        {
            return name;
        }

        public String getSeverity()
        {
            return severity;
        }

        public String toString()
        {
            return name;
        }
    }
}
