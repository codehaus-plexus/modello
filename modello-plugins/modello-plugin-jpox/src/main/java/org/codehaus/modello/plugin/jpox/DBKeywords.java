package org.codehaus.modello.plugin.jpox;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DBKeywords 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DBKeywords
{
    private static final List sql92reserved;

    static
    {
        List reserved = new ArrayList();

        reserved.add( "ADD" );
        reserved.add( "ALL" );
        reserved.add( "ALLOCATE" );
        reserved.add( "ALTER" );
        reserved.add( "AND" );
        reserved.add( "ANY" );
        reserved.add( "ARE" );
        reserved.add( "AS" );
        reserved.add( "ASC" );
        reserved.add( "ASSERTION" );
        reserved.add( "AT" );
        reserved.add( "AUTHORIZATION" );
        reserved.add( "AVG" );
        reserved.add( "BEGIN" );
        reserved.add( "BETWEEN" );
        reserved.add( "BIT" );
        reserved.add( "BOOLEAN" );
        reserved.add( "BOTH" );
        reserved.add( "BY" );
        reserved.add( "CALL" );
        reserved.add( "CASCADE" );
        reserved.add( "CASCADED" );
        reserved.add( "CASE" );
        reserved.add( "CAST" );
        reserved.add( "CHAR" );
        reserved.add( "CHARACTER" );
        reserved.add( "CHECK" );
        reserved.add( "CLOSE" );
        reserved.add( "COLLATE" );
        reserved.add( "COLLATION" );
        reserved.add( "COLUMN" );
        reserved.add( "COMMIT" );
        reserved.add( "CONNECT" );
        reserved.add( "CONNECTION" );
        reserved.add( "CONSTRAINT" );
        reserved.add( "CONSTRAINTS" );
        reserved.add( "CONTINUE" );
        reserved.add( "CONVERT" );
        reserved.add( "CORRESPONDING" );
        reserved.add( "COUNT" );
        reserved.add( "CREATE" );
        reserved.add( "CURRENT" );
        reserved.add( "CURRENT_DATE" );
        reserved.add( "CURRENT_TIME" );
        reserved.add( "CURRENT_TIMESTAMP" );
        reserved.add( "CURRENT_USER" );
        reserved.add( "CURSOR" );
        reserved.add( "DEALLOCATE" );
        reserved.add( "DEC" );
        reserved.add( "DECIMAL" );
        reserved.add( "DECLARE" );
        reserved.add( "DEFERRABLE" );
        reserved.add( "DEFERRED" );
        reserved.add( "DELETE" );
        reserved.add( "DESC" );
        reserved.add( "DESCRIBE" );
        reserved.add( "DIAGNOSTICS" );
        reserved.add( "DISCONNECT" );
        reserved.add( "DISTINCT" );
        reserved.add( "DOUBLE" );
        reserved.add( "DROP" );
        reserved.add( "ELSE" );
        reserved.add( "END" );
        reserved.add( "ENDEXEC" );
        reserved.add( "ESCAPE" );
        reserved.add( "EXCEPT" );
        reserved.add( "EXCEPTION" );
        reserved.add( "EXEC" );
        reserved.add( "EXECUTE" );
        reserved.add( "EXISTS" );
        reserved.add( "EXPLAIN" );
        reserved.add( "EXTERNAL" );
        reserved.add( "FALSE" );
        reserved.add( "FETCH" );
        reserved.add( "FIRST" );
        reserved.add( "FLOAT" );
        reserved.add( "FOR" );
        reserved.add( "FOREIGN" );
        reserved.add( "FOUND" );
        reserved.add( "FROM" );
        reserved.add( "FULL" );
        reserved.add( "FUNCTION" );
        reserved.add( "GET" );
        reserved.add( "GET_CURRENT_CONNECTION" );
        reserved.add( "GLOBAL" );
        reserved.add( "GO" );
        reserved.add( "GOTO" );
        reserved.add( "GRANT" );
        reserved.add( "GROUP" );
        reserved.add( "HAVING" );
        reserved.add( "HOUR" );
        reserved.add( "IDENTITY" );
        reserved.add( "IMMEDIATE" );
        reserved.add( "IN" );
        reserved.add( "INDICATOR" );
        reserved.add( "INITIALLY" );
        reserved.add( "INNER" );
        reserved.add( "INOUT" );
        reserved.add( "INPUT" );
        reserved.add( "INSENSITIVE" );
        reserved.add( "INSERT" );
        reserved.add( "INT" );
        reserved.add( "INTEGER" );
        reserved.add( "INTERSECT" );
        reserved.add( "INTO" );
        reserved.add( "IS" );
        reserved.add( "ISOLATION" );
        reserved.add( "JOIN" );
        reserved.add( "KEY" );
        reserved.add( "LAST" );
        reserved.add( "LEFT" );
        reserved.add( "LIKE" );
        reserved.add( "LONGINT" );
        reserved.add( "LOWER" );
        reserved.add( "LTRIM" );
        reserved.add( "MATCH" );
        reserved.add( "MAX" );
        reserved.add( "MIN" );
        reserved.add( "MINUTE" );
        reserved.add( "NATIONAL" );
        reserved.add( "NATURAL" );
        reserved.add( "NCHAR" );
        reserved.add( "NVARCHAR" );
        reserved.add( "NEXT" );
        reserved.add( "NO" );
        reserved.add( "NOT" );
        reserved.add( "NULL" );
        reserved.add( "NULLIF" );
        reserved.add( "NUMERIC" );
        reserved.add( "OF" );
        reserved.add( "ON" );
        reserved.add( "ONLY" );
        reserved.add( "OPEN" );
        reserved.add( "OPTION" );
        reserved.add( "OR" );
        reserved.add( "ORDER" );
        reserved.add( "OUT" );
        reserved.add( "OUTER" );
        reserved.add( "OUTPUT" );
        reserved.add( "OVERLAPS" );
        reserved.add( "PAD" );
        reserved.add( "PARTIAL" );
        reserved.add( "PREPARE" );
        reserved.add( "PRESERVE" );
        reserved.add( "PRIMARY" );
        reserved.add( "PRIOR" );
        reserved.add( "PRIVILEGES" );
        reserved.add( "PROCEDURE" );
        reserved.add( "PUBLIC" );
        reserved.add( "READ" );
        reserved.add( "REAL" );
        reserved.add( "REFERENCES" );
        reserved.add( "RELATIVE" );
        reserved.add( "RESTRICT" );
        reserved.add( "REVOKE" );
        reserved.add( "RIGHT" );
        reserved.add( "ROLLBACK" );
        reserved.add( "ROWS" );
        reserved.add( "RTRIM" );
        reserved.add( "SCHEMA" );
        reserved.add( "SCROLL" );
        reserved.add( "SECOND" );
        reserved.add( "SELECT" );
        reserved.add( "SESSION_USER" );
        reserved.add( "SET" );
        reserved.add( "SMALLINT" );
        reserved.add( "SOME" );
        reserved.add( "SPACE" );
        reserved.add( "SQL" );
        reserved.add( "SQLCODE" );
        reserved.add( "SQLERROR" );
        reserved.add( "SQLSTATE" );
        reserved.add( "SUBSTR" );
        reserved.add( "SUBSTRING" );
        reserved.add( "SUM" );
        reserved.add( "SYSTEM_USER" );
        reserved.add( "TABLE" );
        reserved.add( "TEMPORARY" );
        reserved.add( "TIMEZONE_HOUR" );
        reserved.add( "TIMEZONE_MINUTE" );
        reserved.add( "TO" );
        reserved.add( "TRAILING" );
        reserved.add( "TRANSACTION" );
        reserved.add( "TRANSLATE" );
        reserved.add( "TRANSLATION" );
        reserved.add( "TRUE" );
        reserved.add( "UNION" );
        reserved.add( "UNIQUE" );
        reserved.add( "UNKNOWN" );
        reserved.add( "UPDATE" );
        reserved.add( "UPPER" );
        reserved.add( "USER" );
        reserved.add( "USING" );
        reserved.add( "VALUES" );
        reserved.add( "VARCHAR" );
        reserved.add( "VARYING" );
        reserved.add( "VIEW" );
        reserved.add( "WHENEVER" );
        reserved.add( "WHERE" );
        reserved.add( "WITH" );
        reserved.add( "WORK" );
        reserved.add( "WRITE" );
        reserved.add( "XML" );
        reserved.add( "XMLEXISTS" );
        reserved.add( "XMLPARSE" );
        reserved.add( "XMLSERIALIZE" );
        reserved.add( "YEAR" );

        sql92reserved = reserved;
    }

    public static boolean isReserved( String tst )
    {
        if ( StringUtils.isEmpty( tst ) )
        {
            return false;
        }
        
        // TODO: Should we include Database Implementation Specific keywords?

        return sql92reserved.contains( tst.trim().toUpperCase() );
    }
}