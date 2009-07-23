package org.codehaus.modello.plugin.java.javasource;

import java.io.File;

import org.codehaus.modello.AbstractModelloGeneratorTest;

public class JavaSourceTest
    extends AbstractModelloGeneratorTest
{
    public JavaSourceTest()
    {
        super( "javasource" );
    }

    public void testJavaSource()
    {
        checkJClass();
        checkJCompUnit();
        checkJInterface();
    }

    private void checkJClass()
    {
        JClass testClass = new JClass( "org.acme.JClassTest" );

        testClass.addSourceCode( "// source code" );
        testClass.addSourceCode( "// source code 2" );

        testClass.addImport( "java.util.Vector" );
        testClass.appendAnnotation( "@SuppressWarnings( \"all\" )" );
        testClass.appendAnnotation( "@Deprecated" );
        testClass.addMember( new JField( JType.INT, "x" ) );
        JClass jcString = new JClass( "String" );

        JField field = null;
        field = new JField( JType.INT, "_z" );
        field.appendAnnotation( "@Deprecated" );
        field.getModifiers().setStatic( true );
        testClass.addField( field );

        testClass.getStaticInitializationCode().add( "_z = 75;" );

        field = new JField( jcString, "myString" );
        field.getModifiers().makePrivate();
        testClass.addMember( field );

        // generics test
        JType type = new JCollectionType( "java.util.List", jcString, true );
        field = new JField( type, "generics" );
        testClass.addMember( field );

        type = new JCollectionType( "java.util.List", jcString, false );
        field = new JField( type, "noGenerics" );
        testClass.addMember( field );

        //-- create constructor
        JConstructor cons = testClass.createConstructor();
        cons.appendAnnotation( "@Deprecated" );
        cons.getSourceCode().add( "this.x = 6;" );

        JMethod jMethod = new JMethod( "getX", JType.INT, null );
        jMethod.appendAnnotation( "@Deprecated" );
        jMethod.setSourceCode( "return this.x;" );
        testClass.addMethod( jMethod );

        jMethod = new JMethod( "setX", null, null );
        JParameter param = new JParameter( JType.INT, "x" );
        jMethod.addParameter( param );
        jMethod.setSourceCode( "this.x = x;" );
        testClass.addMethod( jMethod );

        jMethod = new JMethod( "checkParameterAnotation", JType.INT, null );
        param = new JParameter( JType.LONG, "param" );
        param.appendAnnotation( "@Test" );
        param.appendAnnotation( "@Test2" );
        jMethod.addParameter( param );
        jMethod.setSourceCode( "return param;" );
        testClass.addMethod( jMethod );

        //-- create inner-class
        JClass innerClass = testClass.createInnerClass( "Foo" );
        innerClass.appendAnnotation( "@Deprecated" );
        innerClass.addImport( "java.util.Hashtable" );
        innerClass.addMember( new JField( JType.INT, "_type" ) );

        field = new JField( jcString, "_name" );
        field.getModifiers().makePrivate();
        innerClass.addMember( field );

        testClass.createInnerClass( "Bar" );

        //-- create constructor
        cons = innerClass.createConstructor();
        cons.getSourceCode().add( "_name = \"foo\";" );

        jMethod = new JMethod( "getName", jcString, null );
        jMethod.setSourceCode( "return _name;" );
        innerClass.addMethod( jMethod );

        testClass.print( getOutputDirectory().toString(), null );

        assertTrue( new File( getOutputDirectory(), "org/acme/JClassTest.java" ).exists() );
    }

    private void checkJCompUnit()
    {
        JCompUnit unit = new JCompUnit( "com.acme", "JCompUnitTest.java" );

        JClass testClass = new JClass( "Test" );

        testClass.addImport( "java.util.Vector" );
        testClass.addMember( new JField( JType.INT, "x" ) );

        JField field = null;
        field = new JField( JType.INT, "_z" );
        field.getModifiers().setStatic( true );
        testClass.addField( field );

        testClass.getStaticInitializationCode().add( "_z = 75;" );

        JClass jcString = new JClass( "String" );
        field = new JField( jcString, "myString" );
        field.getModifiers().makePrivate();
        testClass.addMember( field );

        // -- create constructor
        JConstructor cons = testClass.createConstructor();
        testClass.addConstructor( cons );
        cons.getSourceCode().add( "this.x = 6;" );

        JMethod jMethod = new JMethod( "getX", JType.INT, null );
        jMethod.setSourceCode( "return this.x;" );
        testClass.addMethod( jMethod );

        unit.addClass( testClass );

        JClass fooClass = new JClass( "Foo" );
        unit.addClass( fooClass );

        unit.print( getOutputDirectory().toString(), null );

        assertTrue( new File( getOutputDirectory(), "com/acme/JCompUnitTest.java" ).exists() );
    }

    private void checkJInterface()
    {
        JInterface jInterface = new JInterface( "InterfaceTest" );

        // -- add an import
        jInterface.addImport( "java.util.Vector" );
        JClass jString = new JClass( "String" );

        // -- add an interface
        jInterface.addInterface( "java.io.Serializable" );

        // -- add a static field
        JField jField = new JField( new JClass( "java.lang.String" ), "TEST" );
        jField.setInitString( "\"Test\"" );
        jField.getModifiers().setStatic( true );
        jField.getModifiers().makePublic();
        jInterface.addField( jField );

        // -- add a method signature
        JMethodSignature jMethodSig = new JMethodSignature( "getName", jString );
        jInterface.addMethod( jMethodSig );
        jInterface.print( getOutputDirectory().toString(), null );

        assertTrue( new File( getOutputDirectory(), "InterfaceTest.java" ).exists() );
    }
}
