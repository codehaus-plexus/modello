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

import org.codehaus.plexus.PlexusTestCase;

import java.util.List;

/**
 * SQLReservedWordsTest 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class SQLReservedWordsTest extends PlexusTestCase
{
    private SQLReservedWords keywords;

    protected void setUp() throws Exception
    {
        super.setUp();

        keywords = (SQLReservedWords) lookup( SQLReservedWords.class.getName(), "default" );
    }

    protected void tearDown() throws Exception
    {
        // No real point in doing this, as the object is essentially read-only.
        release( keywords );
        super.tearDown();
    }

    public void testTrueKeywords()
    {
        assertNotNull( keywords );

        // Normal Usage
        assertTrue( keywords.isKeyword( "SELECT" ) );
        assertTrue( keywords.isKeyword( "INSERT" ) );
        assertTrue( keywords.isKeyword( "TIMEZONE_HOUR" ) );
        assertTrue( keywords.isKeyword( "IF" ) );
        assertTrue( keywords.isKeyword( "IN" ) );

        // Bad formatted, but otherwise good keywords.
        assertTrue( keywords.isKeyword( "SQLEXCEPTION                 " ) );
        assertTrue( keywords.isKeyword( "LOOP\t" ) );
        assertTrue( keywords.isKeyword( "\n\nEXISTS" ) );
        assertTrue( keywords.isKeyword( " into " ) );
        assertTrue( keywords.isKeyword( "Match " ) );
    }

    public void testNotKeywords()
    {
        assertNotNull( keywords );

        assertFalse( keywords.isKeyword( "MAVEN" ) );
        assertFalse( keywords.isKeyword( "REPOSITORY" ) );
        assertFalse( keywords.isKeyword( "Artifact" ) );
        assertFalse( keywords.isKeyword( null ) );
        assertFalse( keywords.isKeyword( "" ) );
        assertFalse( keywords.isKeyword( "    " ) );
        assertFalse( keywords.isKeyword( "filename" ) );
        assertFalse( keywords.isKeyword( "pathTo" ) );
        assertFalse( keywords.isKeyword( "modello" ) );
        assertFalse( keywords.isKeyword( "versions" ) );
    }

    public void testKeywordSourceList()
    {
        List sources;

        sources = keywords.getKeywordSourceList( "MAVEN" );
        assertNull( "Should be null.", sources );

        sources = keywords.getKeywordSourceList( "IF" );
        assertNotNull( "Should not be null.", sources );
        assertTrue( sources.size() > 5 );
    }

    public void testKeywordSourceString()
    {
        String actual = keywords.getKeywordSourceString( "MAVEN" );
        assertNull( "Should be null.", actual );

        actual = keywords.getKeywordSourceString( "ADD" );
        assertEquals( "SQL 92, SQL 99, SQL 2003, JDBC, Derby Server, HSQLDB, MySQL, PostgreSQL, Oracle, PL/SQL, " +
                "Microsoft SQL Server, Microsoft Access, IBM DB/2, ODBC", actual );

        actual = keywords.getKeywordSourceString( "MULTISET" );
        assertEquals( "SQL 2003, HSQLDB", actual );
    }
}
