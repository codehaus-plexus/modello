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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConsoleLogger
    implements Logger
{
    public void debug( String message )
    {
        output( "DEBUG", message, null );
    }

    public void debug( String message, Throwable throwable )
    {
        output( "DEBUG", message, throwable );
    }

    public void info( String message )
    {
        output( "INFO", message, null );
    }

    public void info( String message, Throwable throwable )
    {
        output( "INFO", message, throwable );
    }

    public void warn( String message )
    {
        output( "WARN", message, null );
    }

    public void warn( String message, Throwable throwable )
    {
        output( "WARN", message, throwable );
    }

    public void fatal( String message )
    {
        output( "FATAL", message, null );
    }

    public void fatal( String message, Throwable throwable )
    {
        output( "FATAL", message, throwable );
    }

    private void output( String level, String msg, Throwable throwable )
    {
        System.out.println( "[" + level + "] " + msg );

        if ( throwable != null )
        {
            throwable.printStackTrace( System.out );
        }

        System.out.flush();
    }
}
