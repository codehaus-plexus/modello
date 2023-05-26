/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2002 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.codehaus.modello.plugin.java.javasource;

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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of the Java Source code for a Java Interface.
 * This is a useful utility when creating in memory source code.
 * The code in this package was modelled after the Java Reflection API
 * as much as possible to reduce the learning curve.
 *
 * @author <a href="mailto:skopp@riege.de">Martin Skopp</a>
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public final class JInterface extends JStructure {

    /**
     * The fields for this JInterface
     */
    private Map<String, JField> fields = null;

    /**
     * The list of methods of this JInterface
     */
    private List<JMethodSignature> methods = null;

    /**
     * Creates a new JInterface with the given name.
     *
     * @param name the name of the JInterface.
     * @throws java.lang.IllegalArgumentException when the given name
     * is not a valid Class name.
     **/
    public JInterface(String name) throws IllegalArgumentException {
        super(name);
        methods = new ArrayList<JMethodSignature>();

        // -- initialize default Java doc
        getJDocComment().appendComment("Interface " + getLocalName() + ".");
    } // -- JInterface

    /**
     * Adds the given JField to this JStructure.
     * <p>
     * This method is implemented by subclasses and
     * should only accept the proper fields for the
     * subclass otherwise an IllegalArgumentException
     * will be thrown. For example a JInterface will
     * only accept static fields.
     * <p>
     * @param jField, the JField to add
     * @exception java.lang.IllegalArgumentException when the given
     * JField has a name of an existing JField
     */
    public void addField(JField jField) throws IllegalArgumentException {
        if (jField == null) {
            throw new IllegalArgumentException("argument 'jField' cannot be null");
        }

        String name = jField.getName();

        // -- check for duplicate field name
        if ((fields != null) && (fields.get(name) != null)) {
            String err = "duplicate name found: " + name;
            throw new IllegalArgumentException(err);
        }

        // -- check for proper modifiers
        JModifiers modifiers = jField.getModifiers();
        if (!modifiers.isStatic()) {
            throw new IllegalArgumentException("Fields added to a JInterface must be static.");
        }
        if (modifiers.isPrivate()) {
            throw new IllegalArgumentException("Fields added to a JInterface must not be private.");
        }

        // -- only initialize fields if we need it, many interfaces
        // -- don't contain any fields, no need to waste space
        if (fields == null) {
            fields = new LinkedHashMap<>(3);
        }

        fields.put(name, jField);
    }

    /**
     * Adds the given JMember to this JStructure.
     * <p>
     * This method is implemented by subclasses and
     * should only accept the proper types for the
     * subclass otherwise an IllegalArgumentException
     * will be thrown.
     * <p>
     * @param jMember the JMember to add to this JStructure.
     * @throws java.lang.IllegalArgumentException when the given
     * JMember has the same name of an existing JField
     * or JMethod respectively.
     */
    public void addMember(JMember jMember) throws IllegalArgumentException {
        if (jMember == null) {
            throw new IllegalArgumentException("argument 'jMember' may not be null.");
        }
        if (jMember instanceof JField) {
            addField((JField) jMember);
        } else {
            throw new IllegalArgumentException("invalid member for JInterface: " + jMember.toString());
        }
    } // -- addMember

    /**
     * Adds the given JMethodSignature to this JClass
     *
     * @param jMethodSig the JMethodSignature to add.
     * @throws java.lang.IllegalArgumentException when the given
     * JMethodSignature conflicts with an existing
     * method signature.
     */
    public void addMethod(JMethodSignature jMethodSig) throws IllegalArgumentException {
        if (jMethodSig == null) {
            String err = "The JMethodSignature cannot be null.";
            throw new IllegalArgumentException(err);
        }

        // -- check method name and signatures *add later*

        // -- keep method list sorted for esthetics when printing
        // -- START SORT :-)
        boolean added = false;
        //        short modifierVal = 0;
        JModifiers modifiers = jMethodSig.getModifiers();
        for (int i = 0; i < methods.size(); i++) {
            JMethodSignature tmp = methods.get(i);
            // -- first compare modifiers
            if (tmp.getModifiers().isProtected()) {
                if (!modifiers.isProtected()) {
                    methods.add(i, jMethodSig);
                    added = true;
                    break;
                }
            }
            // -- compare names
            if (jMethodSig.getName().compareTo(tmp.getName()) < 0) {
                methods.add(i, jMethodSig);
                added = true;
                break;
            }
        }
        // -- END SORT
        if (!added) methods.add(jMethodSig);

        // -- check return type to make sure it's included in the
        // -- import list
        JType jType = jMethodSig.getReturnType();
        if (jType != null) {
            while (jType.isArray()) jType = jType.getComponentType();

            if (!jType.isPrimitive()) addImport(jType.getName());
        }
        // -- check exceptions
        JClass[] exceptions = jMethodSig.getExceptions();
        for (JClass exception : exceptions) {
            addImport(exception.getName());
        }
    } // -- addMethod

    /**
     * Returns the field with the given name, or null if no field
     * was found with the given name.
     *
     * @param name the name of the field to return.
     * @return the field with the given name, or null if no field
     * was found with the given name.
     */
    public JField getField(String name) {
        if (fields == null) return null;
        return (JField) fields.get(name);
    } // -- getField

    /**
     * Returns an array of all the JFields of this JStructure
     *
     * @return an array of all the JFields of this JStructure
     */
    public JField[] getFields() {
        if (fields == null) {
            return new JField[0];
        }
        return fields.values().toArray(new JField[0]);
    } // -- getFields

    /**
     * Returns an array of all the JMethodSignatures of this JInterface.
     *
     * @return an array of all the JMethodSignatures of this JInterface.
     **/
    public JMethodSignature[] getMethods() {
        return methods.toArray(new JMethodSignature[0]);
    } // -- getMethods

    /**
     * Returns the JMethodSignature with the given name,
     * and occuring at or after the given starting index.
     *
     * @param name the name of the JMethodSignature to return.
     * @param startIndex the starting index to begin searching
     * from.
     * @return the JMethodSignature, or null if not found.
     **/
    public JMethodSignature getMethod(String name, int startIndex) {
        for (JMethodSignature jMethod : methods) {
            if (jMethod.getName().equals(name)) return jMethod;
        }
        return null;
    } // -- getMethod

    /**
     * Returns the JMethodSignature at the given index.
     *
     * @param index the index of the JMethodSignature to return.
     * @return the JMethodSignature at the given index.
     **/
    public JMethodSignature getMethod(int index) {
        return methods.get(index);
    } // -- getMethod

    /**
     * Prints the source code for this JInterface to the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to. [May not be null]
     */
    public void print(JSourceWriter jsw) {
        print(jsw, false);
    }

    /**
     * Prints the source code for this JInterface to the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to. [May not be null]
     * @param classOnly whether the header, package and imports should be printed too
     */
    public void print(JSourceWriter jsw, boolean classOnly) {

        if (jsw == null) {
            throw new IllegalArgumentException("argument 'jsw' should not be null.");
        }

        StringBuilder buffer = new StringBuilder();

        if (!classOnly) {
            printHeader(jsw);
            printPackageDeclaration(jsw);
            printImportDeclarations(jsw);
        }

        // ------------/
        // - Java Doc -/
        // ------------/

        getJDocComment().print(jsw);

        JAnnotations annotations = getAnnotations();
        if (annotations != null) annotations.print(jsw);

        // -- print class information
        // -- we need to add some JavaDoc API adding comments

        buffer.setLength(0);

        JModifiers modifiers = getModifiers();
        if (modifiers.isPrivate()) {
            buffer.append("private ");
        } else if (modifiers.isPublic()) {
            buffer.append("public ");
        }

        if (modifiers.isAbstract()) {
            buffer.append("abstract ");
        }

        buffer.append("interface ");
        buffer.append(getLocalName());
        jsw.writeln(buffer.toString());
        buffer.setLength(0);
        jsw.indent();

        if (getInterfaceCount() > 0) {
            Enumeration<String> e = getInterfaces();
            buffer.append("extends ");
            while (e.hasMoreElements()) {
                buffer.append(e.nextElement());
                if (e.hasMoreElements()) buffer.append(", ");
            }

            jsw.writeln(buffer.toString());
            buffer.setLength(0);
        }

        jsw.unindent();

        jsw.writeln('{');

        jsw.indent();

        // -- declare static members

        if (fields != null) {
            if (fields.size() > 0) {
                jsw.writeln();
                jsw.writeln("  //--------------------------/");
                jsw.writeln(" //- Class/Member Variables -/");
                jsw.writeln("//--------------------------/");
                jsw.writeln();
            }

            for (JField jField : fields.values()) {
                // -- print Java comment
                JDocComment comment = jField.getComment();
                if (comment != null) comment.print(jsw);

                // -- print member
                jsw.write(jField.getModifiers().toString());
                jsw.write(' ');

                JType type = jField.getType();
                String typeName = type.toString();
                // -- for esthetics use short name in some cases
                if (typeName.equals(toString())) {
                    typeName = type.getLocalName();
                }
                jsw.write(typeName);
                jsw.write(' ');
                jsw.write(jField.getName());

                String init = jField.getInitString();
                if (init != null) {
                    jsw.write(" = ");
                    jsw.write(init);
                }

                jsw.writeln(';');
                jsw.writeln();
            }
        }

        // -- print method signatures

        if (methods.size() > 0) {
            jsw.writeln();
            jsw.writeln("  //-----------/");
            jsw.writeln(" //- Methods -/");
            jsw.writeln("//-----------/");
            jsw.writeln();
        }

        for (JMethodSignature signature : methods) {
            signature.print(jsw);
            jsw.writeln(';');
        }

        for (String sourceCodeEntry : sourceCodeEntries) {
            jsw.writeln(sourceCodeEntry);
        }

        jsw.unindent();
        jsw.writeln('}');
        jsw.flush();
        jsw.close();
    } // -- printSource

    private List<String> sourceCodeEntries = new ArrayList<String>();

    public void addSourceCode(String sourceCode) {
        sourceCodeEntries.add(sourceCode);
    }
} // -- JInterface
