package org.codehaus.modello.verifier;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public abstract class Verifier
    extends TestCase
{
    public void assertInstanceOf( String message, Class expected, Class actual )
    {
        assertTrue( message, actual.toString().equals( expected.toString() ) );
    }

    public static void assertField( String className, String fieldName, Object expected, Object actual )
    {
        Method expectedMethod;

        Class expectedValueType;

        Class expectedClass = expected.getClass();

        Class actualClass = actual.getClass();

        if ( !expectedClass.isAssignableFrom( actualClass ) )
        {
            throw new WrongObjectTypeVerifierException( className, fieldName, expectedClass.getName(), actualClass.getName() );
        }

        expectedMethod = getMethod( expected.getClass(), fieldName );

        expectedValueType = expectedMethod.getReturnType();

        if ( expectedValueType.isPrimitive() )
        {
            throw new VerifierException( "This tester cannot test primitive types." );
        }

        if ( expectedValueType.isArray() )
        {
            throw new VerifierException( "This tester cannot test primitive types." );
        }

        if ( expectedValueType.getName() != "java.lang.String" )
        {
            throw new VerifierException( "This tester cannot test any object other than java.lang.String." );
        }

        Method actualMethod = getMethod( actualClass, fieldName );

        Class actualValueType = actualMethod.getReturnType();

        // assert that the actual type is the same as the expected type
        // or at least assignable from it.
        if ( !actualValueType.isAssignableFrom( expectedValueType ) )
        {
            throw new WrongReturnTypeVerifierException( className, fieldName, expectedValueType.getName(), actualValueType.getName() );
        }

        Object expectedValue;

        Object actualValue;

        try
        {
            expectedValue = expectedMethod.invoke( expected, new Object[ 0 ] );

            actualValue = actualMethod.invoke( actual, new Object[ 0 ] );
        }
        catch( IllegalAccessException ex )
        {
            throw new VerifierException( "Exception while calling method.", ex );
        }
        catch( InvocationTargetException ex )
        {
            throw new VerifierException( "Exception while calling method.", ex );
        }

        if ( expectedValue == null )
        {
            if ( actualValue == null )
            {
                return;
            }

            throw new NotEqualsVerifierException( className, fieldName, expectedValue, actualValue );
        }

        if ( !expectedValue.equals( actualValue ) )
        {
            throw new NotEqualsVerifierException( className, fieldName, expectedValue, actualValue );
        }
    }

    private static Method getMethod( Class clazz, String fieldName )
    {
        String methodName =  "get" + Character.toUpperCase( fieldName.charAt( 0 ) ) + fieldName.substring( 1 );

        try
        {
            return clazz.getMethod( methodName, new Class[ 0 ] );
        }
        catch( NoSuchMethodException ex )
        {
            throw new VerifierException( "The expected class doesn't have a field named '" + fieldName + "'." );
        }
    }

    private static String getSetterMethodName( String fieldName )
    {
        return "get" + Character.toUpperCase( fieldName.charAt( 0 ) ) + fieldName.substring( 1 );
    }

    protected File getTestFile( String name )
    {
        String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

        return new File( basedir, name );
    }

    protected String getTestPath( String name )
    {
        String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

        return new File( basedir, name ).getAbsolutePath();
    }
}
