/*
 * Copyright (c) 2004, Codehaus.org
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

import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;

import org.codehaus.modello.test.features.BaseClass;
import org.codehaus.modello.test.features.InterfacesFeature;
import org.codehaus.modello.test.features.JavaAbstractFeature;
import org.codehaus.modello.test.features.SimpleTypes;
import org.codehaus.modello.test.features.SubClassLevel1;
import org.codehaus.modello.test.features.SubClassLevel2;
import org.codehaus.modello.test.features.SubClassLevel3;
import org.codehaus.modello.test.features.XmlAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

/**
 * @author Hervé Boutemy
 * @version $Id$
 */
public class JavaVerifier
    extends Verifier
{
    public void verify()
    {
        try
        {
            verifySimpleTypes();
            verifyXmlAttributes();
        }
        catch ( NoSuchFieldException nsfe )
        {
            throw new VerifierException( "field not found", nsfe );
        }
        catch ( NoSuchMethodException nsme )
        {
            throw new VerifierException( "method not found", nsme );
        }
        verifyDefaultValues();
        verifyJavaFeatures();
    }

    private void checkField( Class clazz, Class type, String attributeName, String getterName, String setterName )
        throws NoSuchFieldException, NoSuchMethodException
    {
        Field field = clazz.getDeclaredField( attributeName );
        Assert.assertEquals( attributeName + " attribute type", type, field.getType() );
        Assert.assertTrue( attributeName + " attribute should be private", Modifier.isPrivate( field.getModifiers() ) );

        Method getter = clazz.getMethod( getterName, null );
        Assert.assertNotNull( getterName + "() method", getter );
        Assert.assertEquals( getterName + "() method return type", type, getter.getReturnType() );
        Assert.assertTrue( getterName + "() method should be public", Modifier.isPublic( getter.getModifiers() ) );

        Method setter = clazz.getMethod( setterName, new Class[] { type } );
        Assert.assertNotNull( setterName + "( " + type.getName() + " ) method", setter );
        Assert.assertTrue( setterName + "( " + type.getName() + " ) method should be public",
                           Modifier.isPublic( setter.getModifiers() ) );
    }

    public void verifySimpleTypes()
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkField( SimpleTypes.class, Boolean.TYPE, "primitiveBoolean", "isPrimitiveBoolean", "setPrimitiveBoolean" );
        checkField( SimpleTypes.class, Byte.TYPE, "primitiveByte", "getPrimitiveByte", "setPrimitiveByte" );
        checkField( SimpleTypes.class, Character.TYPE, "primitiveChar", "getPrimitiveChar", "setPrimitiveChar" );
        checkField( SimpleTypes.class, Short.TYPE, "primitiveShort", "getPrimitiveShort", "setPrimitiveShort" );
        checkField( SimpleTypes.class, Integer.TYPE, "primitiveInt", "getPrimitiveInt", "setPrimitiveInt" );
        checkField( SimpleTypes.class, Long.TYPE, "primitiveLong", "getPrimitiveLong", "setPrimitiveLong" );
        checkField( SimpleTypes.class, Float.TYPE, "primitiveFloat", "getPrimitiveFloat", "setPrimitiveFloat" );
        checkField( SimpleTypes.class, Double.TYPE, "primitiveDouble", "getPrimitiveDouble", "setPrimitiveDouble" );
        checkField( SimpleTypes.class, Boolean.class, "objectBoolean", "isObjectBoolean", "setObjectBoolean" );
        checkField( SimpleTypes.class, String.class, "objectString", "getObjectString", "setObjectString" );
    }

    public void verifyXmlAttributes()
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkField( XmlAttributes.class, Boolean.TYPE, "primitiveBoolean", "isPrimitiveBoolean",
                    "setPrimitiveBoolean" );
        checkField( XmlAttributes.class, Byte.TYPE, "primitiveByte", "getPrimitiveByte", "setPrimitiveByte" );
        checkField( XmlAttributes.class, Character.TYPE, "primitiveChar", "getPrimitiveChar", "setPrimitiveChar" );
        checkField( XmlAttributes.class, Short.TYPE, "primitiveShort", "getPrimitiveShort", "setPrimitiveShort" );
        checkField( XmlAttributes.class, Integer.TYPE, "primitiveInt", "getPrimitiveInt", "setPrimitiveInt" );
        checkField( XmlAttributes.class, Long.TYPE, "primitiveLong", "getPrimitiveLong", "setPrimitiveLong" );
        checkField( XmlAttributes.class, Float.TYPE, "primitiveFloat", "getPrimitiveFloat", "setPrimitiveFloat" );
        checkField( XmlAttributes.class, Double.TYPE, "primitiveDouble", "getPrimitiveDouble", "setPrimitiveDouble" );
        checkField( XmlAttributes.class, Boolean.class, "objectBoolean", "isObjectBoolean", "setObjectBoolean" );
        checkField( XmlAttributes.class, String.class, "objectString", "getObjectString", "setObjectString" );
        checkField( XmlAttributes.class, Date.class, "objectDate", "getObjectDate", "setObjectDate" );
    }

    public void verifyDefaultValues()
    {
        SimpleTypes simple = new SimpleTypes();
        Assert.assertEquals( "primitiveBoolean", true, simple.isPrimitiveBoolean() );
        Assert.assertEquals( "primitiveByte", 12, simple.getPrimitiveByte() );
        Assert.assertEquals( "primitiveChar", 'H', simple.getPrimitiveChar() );
        Assert.assertEquals( "primitiveShort", (short) 1212, simple.getPrimitiveShort() );
        Assert.assertEquals( "primitiveInt", 121212, simple.getPrimitiveInt() );
        Assert.assertEquals( "primitiveLong", 12121212, simple.getPrimitiveLong() );
        Assert.assertEquals( "primitiveFloat", 12.12f, simple.getPrimitiveFloat(), 0f );
        Assert.assertEquals( "primitiveDouble", 12.12, simple.getPrimitiveDouble(), 0 );
        Assert.assertEquals( "objectBoolean", Boolean.FALSE, simple.isObjectBoolean() );
        Assert.assertEquals( "objectString", "default value", simple.getObjectString() );
    }

    public void verifyJavaFeatures()
    {
        // java.abstract feature
        if ( !Modifier.isAbstract( JavaAbstractFeature.class.getModifiers() ) )
        {
            throw new VerifierException( "JavaAbstractFeature should be abstract" );
        }

        // interfaces feature
        if ( !java.io.Serializable.class.isAssignableFrom( InterfacesFeature.class ) )
        {
            throw new VerifierException( "InterfacesFeature should implement java.io.Serializable" );
        }
        if ( !java.rmi.Remote.class.isAssignableFrom( InterfacesFeature.class ) )
        {
            throw new VerifierException( "InterfacesFeature should implement java.rmi.Remote" );
        }

        // superClass feature
        if ( !BaseClass.class.isAssignableFrom( SubClassLevel1.class ) )
        {
            throw new VerifierException( "SubClassLevel1 should extend BaseClass" );
        }
        if ( !SubClassLevel1.class.isAssignableFrom( SubClassLevel2.class ) )
        {
            throw new VerifierException( "SubClassLevel2 should extend SubClassLevel1" );
        }
        if ( !SubClassLevel2.class.isAssignableFrom( SubClassLevel3.class ) )
        {
            throw new VerifierException( "SubClassLevel3 should extend SubClassLevel2" );
        }
    }
}
