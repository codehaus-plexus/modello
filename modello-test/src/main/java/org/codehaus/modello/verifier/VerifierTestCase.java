package org.codehaus.modello.verifier;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.File;

public abstract class VerifierTestCase
    extends TestCase
{
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

        if ( !expectedValueType.getName().equals( "java.lang.String" ) )
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
        String methodSuffix = Character.toUpperCase( fieldName.charAt( 0 ) ) + fieldName.substring( 1 );

        try
        {
            Method method = clazz.getMethod( "get" + methodSuffix, new Class[ 0 ] );
            if ( method.getReturnType() != Boolean.class )
            {
                return method;
            }
        }
        catch( NoSuchMethodException ex )
        {
            // continue on
        }

        try
        {
            Method method = clazz.getMethod( "is" + methodSuffix, new Class[ 0 ] );
            if ( method.getReturnType() == Boolean.class )
            {
                return method;
            }
        }
        catch( NoSuchMethodException ex )
        {
            // continue on
        }
        throw new VerifierException( "The expected class doesn't have a field named '" + fieldName + "'." );
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
