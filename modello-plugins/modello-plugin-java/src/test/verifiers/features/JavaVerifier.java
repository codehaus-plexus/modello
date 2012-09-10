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

import org.codehaus.modello.OrderedProperties;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;

import org.codehaus.modello.test.features.AssociationFeatures;
import org.codehaus.modello.test.features.BaseClass;
import org.codehaus.modello.test.features.Bidirectional;
import org.codehaus.modello.test.features.BidiInList;
import org.codehaus.modello.test.features.BidiInSet;
import org.codehaus.modello.test.features.InterfacesFeature;
import org.codehaus.modello.test.features.JavaAbstractFeature;
import org.codehaus.modello.test.features.JavaFeatures;
import org.codehaus.modello.test.features.NodeItem;
import org.codehaus.modello.test.features.Reference;
import org.codehaus.modello.test.features.SimpleInterface;
import org.codehaus.modello.test.features.SimpleTypes;
import org.codehaus.modello.test.features.SubClassLevel1;
import org.codehaus.modello.test.features.SubClassLevel2;
import org.codehaus.modello.test.features.SubClassLevel3;
import org.codehaus.modello.test.features.SubInterface;
import org.codehaus.modello.test.features.Thing;
import org.codehaus.modello.test.features.Thingy;
import org.codehaus.modello.test.features.XmlAttributes;
import org.codehaus.modello.test.features.XmlFeatures;
import org.codehaus.modello.test.features.other.SubInterfaceInPackage;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

/**
 * @author Herve Boutemy
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
            verifyJavaFeatures();
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
        verifyInterfaces();

        verifyMisc();

        verifyClone();
    }

    /**
     * Check that a field has been propertly declared with public accessors.
     *
     * @param clazz the class that should contain the field
     * @param attributeName the field's attribute name
     * @param type the field expected type
     * @param getterName the expected getter method name
     * @param setterName the expected setter method name
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    private void checkField( Class clazz, String attributeName, Class type, String getterName, String setterName )
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkField( clazz, attributeName, type, getterName, setterName,
                    type /* by default, accessors use same type as corresponding field */ );
    }

    /**
     * Check that a field has been propertly declared with public accessors.
     *
     * @param clazz the class that should contain the field
     * @param attributeName the field's attribute name
     * @param type the field expected type
     * @param getterName the expected getter method name
     * @param setterName the expected setter method name
     * @param getterAndSetterType the type expected in getter and setter methods
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    private void checkField( Class clazz, String attributeName, Class type, String getterName, String setterName,
                             Class getterAndSetterType)
        throws NoSuchFieldException, NoSuchMethodException
    {
        Field field = clazz.getDeclaredField( attributeName );
        Assert.assertEquals( attributeName + " attribute type", type, field.getType() );
        Assert.assertTrue( attributeName + " attribute should be private", Modifier.isPrivate( field.getModifiers() ) );

        Method getter = clazz.getMethod( getterName, (Class[]) null );
        Assert.assertNotNull( getterName + "() method", getter );
        Assert.assertEquals( getterName + "() method return type", getterAndSetterType, getter.getReturnType() );
        Assert.assertTrue( getterName + "() method should be public", Modifier.isPublic( getter.getModifiers() ) );

        Method setter = clazz.getMethod( setterName, new Class[] { getterAndSetterType } );
        Assert.assertNotNull( setterName + "( " + type.getName() + " ) method", setter );
        Assert.assertTrue( setterName + "( " + type.getName() + " ) method should be public",
                           Modifier.isPublic( setter.getModifiers() ) );
    }

    /**
     * Check fields declaration common to SimpleTypes and XmlAttributes classes.
     *
     * @param clazz the actuel class to check
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    private void checkCommonFields( Class clazz )
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkField( clazz, "primitiveBoolean", Boolean.TYPE  , "isPrimitiveBoolean", "setPrimitiveBoolean" );
        checkField( clazz, "primitiveByte"   , Byte.TYPE     , "getPrimitiveByte"  , "setPrimitiveByte" );
        checkField( clazz, "primitiveChar"   , Character.TYPE, "getPrimitiveChar"  , "setPrimitiveChar" );
        checkField( clazz, "primitiveShort"  , Short.TYPE    , "getPrimitiveShort" , "setPrimitiveShort" );
        checkField( clazz, "primitiveInt"    , Integer.TYPE  , "getPrimitiveInt"   , "setPrimitiveInt" );
        checkField( clazz, "primitiveLong"   , Long.TYPE     , "getPrimitiveLong"  , "setPrimitiveLong" );
        checkField( clazz, "primitiveFloat"  , Float.TYPE    , "getPrimitiveFloat" , "setPrimitiveFloat" );
        checkField( clazz, "primitiveDouble" , Double.TYPE   , "getPrimitiveDouble", "setPrimitiveDouble" );
        checkField( clazz, "objectBoolean"   , Boolean.class , "isObjectBoolean"   , "setObjectBoolean" );
        checkField( clazz, "objectString"    , String.class  , "getObjectString"   , "setObjectString" );
        checkField( clazz, "objectDate"      , Date.class    , "getObjectDate"     , "setObjectDate" );
    }

    /**
     * Verify SimpleTypes generated class.
     *
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    public void verifySimpleTypes()
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkCommonFields( SimpleTypes.class );
    }

    /**
     * Verify XmlAttributes generated class.
     *
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    public void verifyXmlAttributes()
        throws NoSuchFieldException, NoSuchMethodException
    {
        checkCommonFields( XmlAttributes.class );
    }

    /**
     * Verify default values.
     */
    public void verifyDefaultValues()
    {
        SimpleTypes simple = new SimpleTypes();
        Assert.assertEquals( "primitiveBoolean", true           , simple.isPrimitiveBoolean() );
        Assert.assertEquals( "primitiveByte"   , 12             , simple.getPrimitiveByte() );
        Assert.assertEquals( "primitiveChar"   , 'H'            , simple.getPrimitiveChar() );
        Assert.assertEquals( "primitiveShort"  , (short) 1212   , simple.getPrimitiveShort() );
        Assert.assertEquals( "primitiveInt"    , 121212         , simple.getPrimitiveInt() );
        Assert.assertEquals( "primitiveLong"   , 1234567890123L , simple.getPrimitiveLong() );
        Assert.assertEquals( "primitiveFloat"  , 12.12f         , simple.getPrimitiveFloat(), 0f );
        Assert.assertEquals( "primitiveDouble" , 12.12          , simple.getPrimitiveDouble(), 0 );
        Assert.assertEquals( "objectBoolean"   , Boolean.TRUE   , simple.isObjectBoolean() );
        Assert.assertEquals( "objectByte"      , 12             , simple.getObjectByte().byteValue() );
        Assert.assertEquals( "objectChar"      , 'H'            , simple.getObjectCharacter().charValue() );
        Assert.assertEquals( "objectShort"     , (short) 1212   , simple.getObjectShort().shortValue() );
        Assert.assertEquals( "objectInt"       , 121212         , simple.getObjectInteger().intValue() );
        Assert.assertEquals( "objectLong"      , 1234567890123L , simple.getObjectLong().longValue() );
        Assert.assertEquals( "objectFloat"     , 12.12f         , simple.getObjectFloat().floatValue(), 0f );
        Assert.assertEquals( "objectDouble"    , 12.12          , simple.getObjectDouble().doubleValue(), 0 );
        Assert.assertEquals( "objectString"    , "default value", simple.getObjectString() );

        Assert.assertEquals( "primitiveBoolean", false          , simple.isPrimitiveBooleanNoDefault() );
        Assert.assertEquals( "primitiveByte"   , 0              , simple.getPrimitiveByteNoDefault() );
        Assert.assertEquals( "primitiveChar"   , '\0'           , simple.getPrimitiveCharNoDefault() );
        Assert.assertEquals( "primitiveShort"  , 0              , simple.getPrimitiveShortNoDefault() );
        Assert.assertEquals( "primitiveInt"    , 0              , simple.getPrimitiveIntNoDefault() );
        Assert.assertEquals( "primitiveLong"   , 0              , simple.getPrimitiveLongNoDefault() );
        Assert.assertEquals( "primitiveFloat"  , 0              , simple.getPrimitiveFloatNoDefault(), 0f );
        Assert.assertEquals( "primitiveDouble" , 0              , simple.getPrimitiveDoubleNoDefault(), 0 );
        Assert.assertEquals( "objectBoolean"   , null           , simple.isObjectBooleanNoDefault() );
        Assert.assertEquals( "objectByte"      , null           , simple.getObjectByteNoDefault() );
        Assert.assertEquals( "objectChar"      , null           , simple.getObjectCharacterNoDefault() );
        Assert.assertEquals( "objectShort"     , null           , simple.getObjectShortNoDefault() );
        Assert.assertEquals( "objectInt"       , null           , simple.getObjectIntegerNoDefault() );
        Assert.assertEquals( "objectLong"      , null           , simple.getObjectLongNoDefault() );
        Assert.assertEquals( "objectFloat"     , null           , simple.getObjectFloatNoDefault() );
        Assert.assertEquals( "objectDouble"    , null           , simple.getObjectDoubleNoDefault() );
        Assert.assertEquals( "objectString"    , null           , simple.getObjectStringNoDefault() );

        XmlAttributes xmlAttributes = new XmlAttributes();
        Assert.assertEquals( "primitiveBoolean", true           , xmlAttributes.isPrimitiveBoolean() );
        Assert.assertEquals( "primitiveByte"   , 12             , xmlAttributes.getPrimitiveByte() );
        Assert.assertEquals( "primitiveChar"   , 'H'            , xmlAttributes.getPrimitiveChar() );
        Assert.assertEquals( "primitiveShort"  , (short) 1212   , xmlAttributes.getPrimitiveShort() );
        Assert.assertEquals( "primitiveInt"    , 121212         , xmlAttributes.getPrimitiveInt() );
        Assert.assertEquals( "primitiveLong"   , 1234567890123L , xmlAttributes.getPrimitiveLong() );
        Assert.assertEquals( "primitiveFloat"  , 12.12f         , xmlAttributes.getPrimitiveFloat(), 0f );
        Assert.assertEquals( "primitiveDouble" , 12.12          , xmlAttributes.getPrimitiveDouble(), 0 );
        Assert.assertEquals( "objectBoolean"   , Boolean.TRUE   , xmlAttributes.isObjectBoolean() );
        Assert.assertEquals( "objectByte"      , 12             , xmlAttributes.getObjectByte().byteValue() );
        Assert.assertEquals( "objectChar"      , 'H'            , xmlAttributes.getObjectCharacter().charValue() );
        Assert.assertEquals( "objectShort"     , (short) 1212   , xmlAttributes.getObjectShort().shortValue() );
        Assert.assertEquals( "objectInt"       , 121212         , xmlAttributes.getObjectInteger().intValue() );
        Assert.assertEquals( "objectLong"      , 1234567890123L , xmlAttributes.getObjectLong().longValue() );
        Assert.assertEquals( "objectFloat"     , 12.12f         , xmlAttributes.getObjectFloat().floatValue(), 0f );
        Assert.assertEquals( "objectDouble"    , 12.12          , xmlAttributes.getObjectDouble().doubleValue(), 0 );
        Assert.assertEquals( "objectString"    , "default value", xmlAttributes.getObjectString() );
    }

    public void verifyJavaFeatures()
        throws NoSuchFieldException, NoSuchMethodException
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
        if ( !SubInterface.class.isAssignableFrom( InterfacesFeature.class ) )
        {
            throw new VerifierException( "InterfacesFeature should implement SubInterface" );
        }
        if ( !SubInterfaceInPackage.class.isAssignableFrom( InterfacesFeature.class ) )
        {
            throw new VerifierException( "InterfacesFeature should implement SubInterfaceInPackage" );
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

        // methods for collections
        AssociationFeatures association = new AssociationFeatures();
        // add/remove for List
        association.setListReferences( new ArrayList() );
        List list = association.getListReferences();
        association.addListReference( new Reference() );
        association.removeListReference( new Reference() );
        // add/remove for Set
        association.setSetReferences( new HashSet() );
        Set set = association.getSetReferences();
        association.addSetReference( new Reference() );
        association.removeSetReference( new Reference() );

        // java.adder=false
        JavaFeatures java = new JavaFeatures();
        java.setJavaListNoAdd( new ArrayList() );
        list = java.getJavaListNoAdd();
        checkNoMethod( JavaFeatures.class, "addJavaListNoAdd", Reference.class );
        checkNoMethod( JavaFeatures.class, "removeJavaListNoAdd", Reference.class );
        java.setJavaSetNoAdd( new HashSet() );
        set = java.getJavaSetNoAdd();
        checkNoMethod( JavaFeatures.class, "addJavaSetNoAdd", Reference.class );
        checkNoMethod( JavaFeatures.class, "removeJavaSetNoAdd", Reference.class );

        // bidi
        Bidirectional bidi = new Bidirectional();
        association.setBidi( bidi );
        Assert.assertEquals( "setting bidi in association should set the reverse association",
                             association, bidi.getParent() );
        bidi.setParent( null );
        Assert.assertNull( "setting parent to null in bidi should remove value in association", association.getBidi() );

        BidiInList bidiInList = new BidiInList();
        association.addListOfBidi( bidiInList );
        Assert.assertEquals( "setting bidi in many association should set the reverse association",
                             association, bidiInList.getParent() );
        bidiInList.setParent( null );
        Assert.assertEquals( 0, association.getListOfBidis().size() );
        bidiInList.setParent( association );
        Assert.assertEquals( bidiInList, association.getListOfBidis().get( 0 ) );
        association.removeListOfBidi( bidiInList );
        Assert.assertEquals( 0, association.getListOfBidis().size() );

        BidiInSet bidiInSet = new BidiInSet();
        association.addSetOfBidi( bidiInSet );
        Assert.assertEquals( "setting bidi in many association should set the reverse association",
                             association, bidiInSet.getParent() );
        bidiInSet.setParent( null );
        Assert.assertEquals( 0, association.getSetOfBidis().size() );
        bidiInSet.setParent( association );
        Assert.assertEquals( bidiInSet, association.getSetOfBidis().iterator().next() );
        association.removeSetOfBidi( bidiInSet );
        Assert.assertEquals( 0, association.getSetOfBidis().size() );

        // class with single association to itself, but not bidi!
        NodeItem parentNode = new NodeItem();
        NodeItem childNode = new NodeItem();
        parentNode.setChild( childNode );
        assertSame( childNode, parentNode.getChild() );
        assertNull( childNode.getChild() );

        // java.useInterface
        checkField( JavaFeatures.class, "useInterface", SubClassLevel1.class, "getUseInterface", "setUseInterface",
                    BaseClass.class);
    }

    /**
     * Check that a method doesn't exist.
     *
     * @param clazz the class to check
     * @param method the method name that shouldn't exist
     * @param attribute the method attribute type
     */
    private void checkNoMethod( Class clazz, String method, Class attribute )
    {
        try
        {
            clazz.getMethod( method, new Class[] { attribute } );
            throw new VerifierException( clazz.getName() + " should not contain " + method + "( "
                                         + attribute.getName() + " ) method." );
        }
        catch ( NoSuchMethodException nsme )
        {
            // ok, that's expected
        }
    }

    public void verifyInterfaces()
    {
        Assert.assertTrue( "SimpleInterface should be an interface", SimpleInterface.class.isInterface() );
        Assert.assertTrue( "SubInterface should be an interface", SubInterface.class.isInterface() );
        Assert.assertTrue( "SubInterfaceInPackage should be an interface", SubInterfaceInPackage.class.isInterface() );

        // superInterface feature
        if ( !SimpleInterface.class.isAssignableFrom( SubInterface.class ) )
        {
            throw new VerifierException( "SubInterface should extend SimpleInterface" );
        }
        if ( !SimpleInterface.class.isAssignableFrom( SubInterfaceInPackage.class ) )
        {
            throw new VerifierException( "SubInterfaceInPackage should extend SimpleInterface" );
        }

        // codeSegments
        Assert.assertNotNull( "SimpleInterface.CODE_SEGMENT should be here", SimpleInterface.CODE_SEGMENT );
    }

    /**
     * Verify misc aspects of the generated classes.
     */
    public void verifyMisc()
    {
        // <default><key>java.util.Properties</key><value>new org.codehaus.modello.OrderedProperties()</value></default>
        if (! ( new XmlFeatures().getExplodeProperties() instanceof OrderedProperties ) )
        {
            throw new VerifierException( "java.util.Properties model default value was ignored" );
        }
    }

    /**
     * Verify generated clone() methods.
     */
    public void verifyClone()
    {
        checkCloneNullSafe();

        checkClone();
    }

    private void checkCloneNullSafe()
    {
        Thing orig = new Thing();

        Thing copy = (Thing) orig.clone();

        assertNotNull( copy );
        assertNotSame( orig, copy );
    }

    private void checkClone()
    {
        Thing orig = new Thing();
        orig.setSomeBoolean( true );
        orig.setSomeChar( 'X' );
        orig.setSomeByte( (byte) 7 );
        orig.setSomeShort( (short) 11 );
        orig.setSomeInt( 13 );
        orig.setSomeLong( 17 );
        orig.setSomeFloat( -2.5f );
        orig.setSomeDouble( 3.14 );
        orig.setSomeString( "test" );
        orig.setSomeDate( new Date() );
        orig.setSomeDom( new Xpp3Dom( "test" ) );
        orig.addSomeStringList( "string" );
        orig.addSomeStringSet( "string" );
        orig.setDeepThingy( new Thingy() );
        orig.addDeepThingyList( new Thingy() );
        orig.addDeepThingySet( new Thingy() );
        orig.setShallowThingy( new Thingy() );
        orig.addShallowThingyList( new Thingy() );
        orig.addShallowThingySet( new Thingy() );
        orig.addSomeProperty( "key", "value" );
        orig.customProperties.setProperty( "key", "value" );

        Thing copy = (Thing) orig.clone();

        assertNotNull( copy );
        assertNotSame( orig, copy );

        assertEquals( orig.isSomeBoolean(), copy.isSomeBoolean() );
        assertEquals( orig.getSomeChar(), copy.getSomeChar() );
        assertEquals( orig.getSomeByte(), copy.getSomeByte() );
        assertEquals( orig.getSomeShort(), copy.getSomeShort() );
        assertEquals( orig.getSomeInt(), copy.getSomeInt() );
        assertEquals( orig.getSomeLong(), copy.getSomeLong() );
        assertEquals( orig.getSomeFloat(), copy.getSomeFloat(), 0.1 );
        assertEquals( orig.getSomeDouble(), copy.getSomeDouble(), 0.1 );
        assertEquals( orig.getSomeString(), copy.getSomeString() );

        assertEquals( orig.getSomeDate(), copy.getSomeDate() );
        assertNotSame( orig.getSomeDate(), copy.getSomeDate() );
        assertEquals( orig.getSomeDom(), copy.getSomeDom() );
        assertNotSame( orig.getSomeDom(), copy.getSomeDom() );

        assertEquals( orig.getSomeStringList(), copy.getSomeStringList() );
        assertNotSame( orig.getSomeStringList(), copy.getSomeStringList() );
        assertEquals( orig.getSomeStringSet(), copy.getSomeStringSet() );
        assertNotSame( orig.getSomeStringSet(), copy.getSomeStringSet() );

        assertNotSame( orig.getDeepThingy(), copy.getDeepThingy() );
        assertNotSame( orig.getDeepThingyList(), copy.getDeepThingyList() );
        assertNotSame( orig.getDeepThingyList().iterator().next(), copy.getDeepThingyList().iterator().next() );
        assertNotSame( orig.getDeepThingySet(), copy.getDeepThingySet() );
        assertNotSame( orig.getDeepThingySet().iterator().next(), copy.getDeepThingySet().iterator().next() );

        assertSame( orig.getShallowThingy(), copy.getShallowThingy() );
        assertNotSame( orig.getShallowThingyList(), copy.getShallowThingyList() );
        assertSame( orig.getShallowThingyList().iterator().next(), copy.getShallowThingyList().iterator().next() );
        assertNotSame( orig.getShallowThingySet(), copy.getShallowThingySet() );
        assertSame( orig.getShallowThingySet().iterator().next(), copy.getShallowThingySet().iterator().next() );

        assertEquals( orig.customProperties, copy.customProperties );
        assertNotSame( orig.customProperties, copy.customProperties );

        Thingy orig2 = new Thingy();
        orig2.setSomeContent( "content" );

        Thingy copy2 = (Thingy) orig2.clone();

        assertNotNull( copy2 );
        assertNotSame( orig2, copy2 );

        assertEquals( "content", copy2.getSomeContent() );
    }

}
