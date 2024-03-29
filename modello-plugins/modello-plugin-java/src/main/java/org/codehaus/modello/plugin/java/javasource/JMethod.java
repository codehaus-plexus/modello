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
 * Copyright 1999-2002 (C) Intalio, Inc. All Rights Reserved.
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
import java.util.List;

/**
 * A class which holds information about the methods of
 * a JClass.
 * Modelled closely after the Java Reflection API.
 * This class is part of package which is used to
 * create source code.
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JMethod implements JMember {

    /**
     * The set of classes that contain this JMethod.
     **/
    private List<JClass> _classes = null;

    /**
     * The JavaDoc comment for this JMethod. This
     * will overwrite the JavaDoc for the
     * JMethodSignature.
     **/
    private JDocComment jdc = null;

    /**
     * The source code for this method
     **/
    private JSourceCode source = null;

    /**
     * The signature for this method.
     **/
    private JMethodSignature _signature = null;

    /**
     * The annotation(s) for this method.
     */
    private JAnnotations annotations = null;

    /**
     * Creates a new JMethod with the given name and "void" return type.
     *
     * @param name, the method name. Must not be null.
     **/
    public JMethod(String name) {
        this(name, null, null);
    } // -- JMethod

    /**
     * Creates a new JMethod with the given name and returnType.
     * For "void" return types, simply pass in null as the returnType.
     *
     * @param name, the method name. Must not be null.
     * @param returnType the return type of the method. May be null.
     * @deprecated removed in future version of javasource
     **/
    public JMethod(JType returnType, String name) {
        this(name, returnType, null);
    } // -- JMethod

    /**
     * Creates a new JMethod with the given name and returnType.
     * For "void" return types, simply pass in null as the returnType.
     *
     * @param name, the method name. Must not be null.
     * @param returnType the return type of the method. May be null.
     * @param returnDoc Javadoc comment for the &#064;return annotation. If
     *            null, a default (and mostly useless) javadoc comment will be
     *            generated.
     **/
    public JMethod(final String name, final JType returnType, final String returnDoc) {
        if ((name == null) || (name.length() == 0)) {
            String err = "The method name must not be null or zero-length";
            throw new IllegalArgumentException(err);
        }

        _classes = new ArrayList<JClass>(1);
        this.source = new JSourceCode();
        _signature = new JMethodSignature(name, returnType);
        this.jdc = _signature.getJDocComment();
        jdc.appendComment("Method " + name + ".");

        // -- create comment
        if (returnType != null) {
            if (returnDoc != null && returnDoc.length() > 0) {
                jdc.addDescriptor(JDocDescriptor.createReturnDesc(returnDoc));
            } else {
                jdc.addDescriptor(JDocDescriptor.createReturnDesc(returnType.getLocalName()));
            }
        }
    }

    /**
     * Adds the given Exception to this Method's throws clause.
     *
     * @param exp the JClass representing the Exception
     **/
    public void addException(JClass exp) {
        _signature.addException(exp);
    } // -- addException

    /**
     * Adds the given parameter to this JMethod's list of parameters.
     *
     * @param parameter the parameter to add to the this Methods
     * list of parameters.
     * @throws java.lang.IllegalArgumentException when a parameter already
     * exists for this Method with the same name as the new parameter
     **/
    public void addParameter(JParameter parameter) throws IllegalArgumentException {
        _signature.addParameter(parameter);
    } // -- addParameter

    /**
     * Returns the JDocComment describing this member.
     * @return the JDocComment describing this member.
     **/
    public JDocComment getJDocComment() {
        return this.jdc;
    } // -- getJDocComment

    /**
     * Returns the class in which this JMember has been declared
     * @return the class in which this JMember has been declared
     **
     * public JClass getDeclaringClass() {
     * return _declaringClass;
     * } //-- getDeclaringClass
     */

    /**
     * Returns the exceptions that this JMember throws.
     *
     * @return the exceptions that this JMember throws.
     **/
    public JClass[] getExceptions() {
        return _signature.getExceptions();
    } // -- getExceptions

    /**
     * Returns the modifiers for this JMember.
     *
     * @return the modifiers for this JMember.
     **/
    public JModifiers getModifiers() {
        return _signature.getModifiers();
    } // -- getModifiers

    /**
     * Returns the name of this JMember.
     *
     * @return the name of this JMember.
     **/
    public String getName() {
        return _signature.getName();
    } // -- getName

    /**
     * Returns the JParameter at the given index.
     *
     * @param index the index of the JParameter to return.
     * @return the JParameter at the given index.
     **/
    public JParameter getParameter(int index) {
        return _signature.getParameter(index);
    } // -- getParameter

    /**
     * Returns the set of JParameters for this JMethod.
     * <BR>
     * <B>Note:</B> the array is a copy, the params in the array
     * are the actual references.
     *
     * @return the set of JParameters for this JMethod
     **/
    public JParameter[] getParameters() {
        return _signature.getParameters();
    } // -- getParameters

    /**
     * Returns the JType that represents the return type of the method.
     *
     * @return the JType that represents the return type of the method.
     **/
    public JType getReturnType() {
        return _signature.getReturnType();
    } // -- getReturnType

    /**
     * Returns the JMethodSignature for this JMethod.
     *
     * @return the JMethodSignature for this JMethod.
     **/
    public JMethodSignature getSignature() {
        return _signature;
    } // -- getSignature

    /**
     * Returns the JSourceCode for the method body.
     *
     * @return the JSourceCode for the method body.
     **/
    public JSourceCode getSourceCode() {
        return this.source;
    } // -- getSourceCode

    /**
     * Sets the comment describing this member. The comment
     * will be printed when this member is printed with the
     * Class Printer.
     *
     * @param comment the comment for this member
     * @see #getJDocComment
     **/
    public void setComment(String comment) {
        jdc.setComment(comment);
    } // -- setComment

    /**
     * Sets the JModifiers for this JMethod. This
     * JMethod will use only a copy of the JModifiers.
     * <B>Note:</B> The JModifiers will be set in the
     * containing JMethodSignature. If the JMethodSignature
     * is used by other methods, keep in mind that it will be
     * changed.
     *
     * @param modifiers the JModifiers to set.
     **/
    public void setModifiers(JModifiers modifiers) {
        _signature.setModifiers(modifiers);
    } // -- setModifiers

    /**
     * Sets the given string as the source code (method body)
     * for this JMethod.
     *
     * @param source the String that represents the method body.
     **/
    public void setSourceCode(String source) {
        this.source = new JSourceCode(source);
    } // -- setSource

    /**
     * Sets the given JSourceCode as the source code (method body)
     * for this JMethod.
     *
     * @param source the JSourceCode that represents the method body.
     **/
    public void setSourceCode(JSourceCode source) {
        this.source = source;
    } // -- setSource;

    /**
     * Prints this JMethod to the given JSourceWriter.
     *
     * @param jsw the JSourceWriter to print to.
     **/
    public void print(JSourceWriter jsw) {

        // ------------/
        // - Java Doc -/
        // ------------/

        jdc.print(jsw);

        // --------------------/
        // - Annotations     -/
        // --------------------/

        JAnnotations annotations = getAnnotations();
        if (annotations != null) annotations.print(jsw);

        // --------------------/
        // - Method Signature -/
        // --------------------/

        _signature.print(jsw, false);

        if (_signature.getModifiers().isAbstract()) {
            jsw.writeln(";");
        } else {
            jsw.writeln();
            jsw.writeln("{");
            source.print(jsw);
            jsw.write("} //-- ");
            jsw.writeln(toString());
        }
    } // -- print

    /**
     * Returns the String representation of this JMethod,
     * which is the method prototype.
     * @return the String representation of this JMethod, which
     * is simply the method prototype
     **/
    public String toString() {
        return _signature.toString();
    } // -- toString

    // ---------------------/
    // - PROTECTED METHODS -/
    // ---------------------/

    /**
     * Adds the given JClass to the set of classes that
     * contain this method.
     *
     * @param jClass the JClass to add as one of
     * the JClasses that contain this method.
     **/
    protected void addDeclaringClass(JClass jClass) {
        _classes.add(jClass);
    } // -- addDeclaringClass

    /**
     * Removes the given JClass from the set of classes that
     * contain this method.
     *
     * @param jClass the JClass to add as one of
     * the JClasses that contain this method.
     **/
    protected void removeDeclaringClass(JClass jClass) {
        _classes.remove(jClass);
    } // -- removeDeclaringClass

    protected String[] getParameterClassNames() {
        return _signature.getParameterClassNames();
    } // -- getParameterClassNames

    /**
     * @return the annotations
     */
    public JAnnotations getAnnotations() {
        return annotations;
    }

    /**
     * @param annotation the annotation to append
     */
    public void appendAnnotation(String annotation) {
        if (annotations == null) {
            annotations = new JAnnotations();
        }
        annotations.appendAnnotation(annotation);
    }

    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations(JAnnotations annotations) {
        this.annotations = annotations;
    }
} // -- JMember
