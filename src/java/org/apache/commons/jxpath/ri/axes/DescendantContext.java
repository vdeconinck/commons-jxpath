/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/axes/DescendantContext.java,v 1.15 2003/10/09 21:31:39 rdonkin Exp $
 * $Revision: 1.15 $
 * $Date: 2003/10/09 21:31:39 $
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
package org.apache.commons.jxpath.ri.axes;

import java.util.Stack;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * An EvalContext that walks the "descendant::" and "descendant-or-self::"
 * axes.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.15 $ $Date: 2003/10/09 21:31:39 $
 */
public class DescendantContext extends EvalContext {
    private NodeTest nodeTest;
    private boolean setStarted = false;
    private Stack stack;
    private NodePointer currentNodePointer;
    private boolean includeSelf;
    private static final NodeTest ELEMENT_NODE_TEST =
            new NodeTypeTest(Compiler.NODE_TYPE_NODE);
                        
    public DescendantContext(
            EvalContext parentContext,
            boolean includeSelf,
            NodeTest nodeTest) 
    {
        super(parentContext);
        this.includeSelf = includeSelf;
        this.nodeTest = nodeTest;
    }

    public boolean isChildOrderingRequired() {
        return true;
    }

    public NodePointer getCurrentNodePointer() {
        if (position == 0) {
            if (!setPosition(1)) {
                return null;
            }
        }
        return currentNodePointer;
    }

    public void reset() {
        super.reset();
        setStarted = false;
    }

    public boolean setPosition(int position) {
        if (position < this.position) {
            reset();
        }

        while (this.position < position) {
            if (!nextNode()) {
                return false;
            }
        }
        return true;
    }

    public boolean nextNode() {
        if (!setStarted) {
            setStarted = true;
            stack = new Stack();
            currentNodePointer = parentContext.getCurrentNodePointer();
            if (currentNodePointer != null) {
                if (!currentNodePointer.isLeaf()) {
                    stack.push(
                        currentNodePointer.childIterator(
                            ELEMENT_NODE_TEST,
                            false,
                            null));
                }
                if (includeSelf) {
                    if (currentNodePointer.testNode(nodeTest)) {
                        position++;
                        return true;
                    }
                }
            }
        }

        while (!stack.isEmpty()) {
            NodeIterator it = (NodeIterator) stack.peek();
            if (it.setPosition(it.getPosition() + 1)) {
                currentNodePointer = it.getNodePointer();
                if (!isRecursive()) {
                    if (!currentNodePointer.isLeaf()) {
                        stack.push(
                            currentNodePointer.childIterator(
                                ELEMENT_NODE_TEST,
                                false,
                                null));
                    }
                    if (currentNodePointer.testNode(nodeTest)) {
                        position++;
                        return true;
                    }
                }
            }
            else {
                // We get here only if the name test failed 
                // and the iterator ended
                stack.pop();
            }
        }
        return false;
    }

    /**
     * Checks if we are reentering a bean we have already seen and if so
     * returns true to prevent infinite recursion.
     */
    private boolean isRecursive() {
        Object node = currentNodePointer.getNode();
        for (int i = stack.size() - 1; --i >= 0;) {
            NodeIterator it = (NodeIterator) stack.get(i);
            Pointer pointer = it.getNodePointer();
            if (pointer != null && pointer.getNode() == node) {
                return true;
            }
        }
        return false;
    }
}