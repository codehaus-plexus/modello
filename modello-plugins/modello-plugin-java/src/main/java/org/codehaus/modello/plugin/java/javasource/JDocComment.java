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
 * Copyright 1999-2003 (C) Intalio, Inc. All Rights Reserved.
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A class that "SOMEWHAT" represents a Java Doc Comment.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 */
public class JDocComment {

    /**
     * An ordered list of descriptors
     */
    private List<JDocDescriptor> _descriptors = null;

    /**
     * The internal buffer for this JDocComment
     */
    private StringBuilder _comment = null;

    /**
     * Creates a new JavaDoc Comment
     */
    public JDocComment() {
        super();
        _descriptors = new ArrayList<JDocDescriptor>();
        _comment = new StringBuilder();
    } // --  JDocComment

    /**
     * Adds the given JDocDescriptor to this JDocComment
     *
     * @param jdesc the JDocDescriptor to add
     */
    public void addDescriptor(JDocDescriptor jdesc) {

        if (jdesc == null) return;
        // -- on the fly sorting of descriptors
        if (_descriptors.size() == 0) {
            _descriptors.add(jdesc);
            return;
        }

        for (int i = 0; i < _descriptors.size(); i++) {
            JDocDescriptor jdd = _descriptors.get(i);

            short compare = jdesc.compareTo(jdd);

            switch (compare) {
                case 0: // equal
                    _descriptors.add(i + 1, jdesc);
                    return;
                case -1: // -- less than
                    _descriptors.add(i, jdesc);
                    return;
                case 1:
                    // -- keep looking
                    break;
            }
        }

        // -- if we make it here we need to add
        _descriptors.add(jdesc);
    } // -- addException

    /**
     * Appends the comment String to this JDocComment
     *
     * @param comment the comment to append
     */
    public void appendComment(String comment) {
        _comment.append(comment);
    } // -- appendComment

    /**
     * Returns the String value of this JDocComment.
     *
     * @return the String value of the JDocComment.
     */
    public String getComment() {
        return _comment.toString();
    } // -- getComment

    /**
     * Returns an enumeration of the parameters of this JDocComment
     *
     * @return an enumeration of the parameters of this JDocComment
     */
    public Enumeration<JDocDescriptor> getDescriptors() {
        return Collections.enumeration(_descriptors);
    } // -- getDescriptors

    /**
     * Returns the length of the comment
     *
     * @return the length of the comment
     */
    public int getLength() {
        return _comment.length();
    } // -- getLength

    /**
     * Returns the Parameter Descriptor associated with the
     * given name
     *
     * @param name the name of the parameter
     * @return the Parameter Descriptor associated with the
     * given name
     */
    public JDocDescriptor getParamDescriptor(String name) {
        if (name == null) return null;

        for (JDocDescriptor jdd : _descriptors) {
            if (jdd.getType() == JDocDescriptor.PARAM) {
                if (name.equals(jdd.getName())) return jdd;
            }
        }
        return null;
    } // -- getParamDescriptor

    /**
     * prints this JavaDoc comment using the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to
     */
    public void print(JSourceWriter jsw) {

        // -- I reuse JComment for printing
        JComment jComment = new JComment(JComment.JAVADOC_STYLE);

        jComment.setComment(_comment.toString());

        // -- force a separating "*" for readability
        if (_descriptors.size() > 0) {
            jComment.appendComment("\n");
        }

        for (int i = 0; i < _descriptors.size(); i++) {
            jComment.appendComment("\n");
            jComment.appendComment(_descriptors.get(i).toString());
        }
        jComment.print(jsw);
    } // -- print

    /**
     * Sets the comment String of this JDocComment
     *
     * @param comment the comment String of this JDocComment
     */
    public void setComment(String comment) {
        _comment.setLength(0);
        _comment.append(comment);
    } // -- setComment

    /**
     * Returns the String representation of this Java Doc Comment
     *
     * @return the String representation of this Java Doc Comment
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        sb.append(" * ");

        sb.append(" */\n");

        return sb.toString();
    } // -- toString
} // -- JDocComment
