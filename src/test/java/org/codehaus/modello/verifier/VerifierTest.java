package org.codehaus.modello.verifier;

/*
 * LICENSE
 */

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VerifierTest
    extends TestCase
{
    private class Foo
    {
        private String stringField;

        public Foo()
        {
        }

        public Foo( String stringField )
        {
            this.stringField = stringField;
        }

        public String getStringField()
        {
            return stringField;
        }

        public void setStringField( String stringField )
        {
            this.stringField = stringField;
        }
    }

    private class SubFoo extends Foo
    {
        
    }

    // This class has the same fields and field types as Foo and SubFoo, 
    // but it's not the same type
    private class Bar
    {
        private String stringField;

        public Bar()
        {
        }

        public Bar( String stringField )
        {
            this.stringField = stringField;
        }

        public String getStringField()
        {
            return stringField;
        }

        public void setStringField( String stringField )
        {
            this.stringField = stringField;
        }
    }

    public void testExpectedEqualsFooActualEqualsFoo()
    {
        Foo expected = new Foo( "foo" );

        Foo actual = new Foo( "foo" );

        Verifier.assertField( "Foo", "stringField", expected, actual );
    }

    public void testExpectedEqualsNullActualEqualsNull()
    {
        Foo expected = new Foo();

        Foo actual = new Foo();

        Verifier.assertField( "Foo", "stringField", expected, actual );
    }

    public void testExpectedEqualsFooActualEqualsNull()
    {
        Foo expected = new Foo( "foo" );

        Foo actual = new Foo();

        try
        {
            Verifier.assertField( "Foo", "stringField", expected, actual );

            fail( "Expected NotEqualsVerifierException." );
        }
        catch( NotEqualsVerifierException ex )
        {
            // expected
        }
    }

    public void testExpectedEqualsNullActualEqualsFoo()
    {
        Foo expected = new Foo();

        Foo actual = new Foo( "foo" );

        try
        {
            Verifier.assertField( "Foo", "stringField", expected, actual );

            fail( "Expected NotEqualsVerifierException." );
        }
        catch( NotEqualsVerifierException ex )
        {
            // expected
        }
    }

    public void testExpectedTypeEqualsFooStringEqualsFooActualTypeEqualsSubFoo()
    {
        Foo expected = new Foo( "foo" );

        SubFoo actual = new SubFoo();

        try
        {
            Verifier.assertField( "Foo", "stringField", expected, actual );

            fail( "Expected NotEqualsVerifierException." );
        }
        catch( NotEqualsVerifierException ex )
        {
            // expected
        }
    }

    public void testExpectedTypeEqualsFooActualTypeEqualsSubFoo()
    {
        Foo expected = new Foo( "foo" );

        SubFoo actual = new SubFoo();

        actual.setStringField( "foo" );

        Verifier.assertField( "Foo", "stringField", expected, actual );
    }

    public void testExpectedTypeEqualsSubFooActualTypeEqualsFoo()
    {
        SubFoo expected = new SubFoo();

        Foo actual = new Foo();

        try
        {
            Verifier.assertField( "Foo", "stringField", expected, actual );

            fail( "Expected WrongObjectTypeVerifierException." );
        }
        catch( WrongObjectTypeVerifierException ex )
        {
            // expected
        }
    }

    public void testExpectedTypeEqualsFooActualTypeEqualsBar()
    {
        Foo expected = new Foo();

        Bar actual = new Bar();

        try
        {
            Verifier.assertField( "Foo", "stringField", expected, actual );

            fail( "Expected WrongObjectTypeVerifierException." );
        }
        catch( WrongObjectTypeVerifierException ex )
        {
            // expected
        }
    }
}
