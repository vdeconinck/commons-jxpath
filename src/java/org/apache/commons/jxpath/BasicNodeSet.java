/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/BasicNodeSet.java,v 1.2 2003/10/09 21:31:38 rdonkin Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/09 21:31:38 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Plotnix, Inc,
 * <http://www.plotnix.com/>.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.jxpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of NodeSet that behaves as a collection of pointers. 
 * @author Dmitri Plotnikov
 * @version $Revision: 1.2 $ $Date: 2003/10/09 21:31:38 $
 */
public class BasicNodeSet implements NodeSet {
    private List pointers = new ArrayList();
    private List readOnlyPointers;
    private List nodes;
    private List values;

    public void add(Pointer pointer) {
        pointers.add(pointer);
        readOnlyPointers = null;
    }
    
    public void remove(Pointer pointer) {
        pointers.remove(pointer);
        readOnlyPointers = null;
    }
    
    public List getPointers() {
        if (readOnlyPointers == null) {
            readOnlyPointers = Collections.unmodifiableList(pointers);
        }
        return readOnlyPointers;
    }

    public List getNodes() {
        if (nodes == null) {
            nodes = new ArrayList();
            for (int i = 0; i < pointers.size(); i++) {
                Pointer pointer = (Pointer) pointers.get(i);
                nodes.add(pointer.getValue());
            }
            nodes = Collections.unmodifiableList(nodes);
        }
        return nodes;
    }

    public List getValues() {
        if (values == null) {
            values = new ArrayList();
            for (int i = 0; i < pointers.size(); i++) {
                Pointer pointer = (Pointer) pointers.get(i);
                values.add(pointer.getValue());
            }
            values = Collections.unmodifiableList(values);
        }
        return values;
    }
    
    public String toString() {
        return pointers.toString();
    }
}
