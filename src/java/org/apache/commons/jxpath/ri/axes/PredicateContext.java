/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/axes/PredicateContext.java,v 1.21 2003/10/09 21:31:39 rdonkin Exp $
 * $Revision: 1.21 $
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

import java.util.Iterator;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.NameAttributeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;

/**
 * EvalContext that checks predicates.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.21 $ $Date: 2003/10/09 21:31:39 $
 */
public class PredicateContext extends EvalContext {
    private Expression expression;
    private boolean done = false;
    private Expression nameTestExpression;
    private PropertyPointer dynamicPropertyPointer;

    public PredicateContext(EvalContext parentContext, Expression expression) {
        super(parentContext);
        this.expression = expression;
        if (expression instanceof NameAttributeTest) {
            nameTestExpression =
                ((NameAttributeTest) expression).getNameTestExpression();
        }
    }

    public boolean nextNode() {
        if (done) {
            return false;
        }
        while (parentContext.nextNode()) {
            if (setupDynamicPropertyPointer()) {
                Object pred = nameTestExpression.computeValue(parentContext);
                String propertyName = InfoSetUtil.stringValue(pred);

                // At this point it would be nice to say: 
                // dynamicPropertyPointer.setPropertyName(propertyName)
                // and then: dynamicPropertyPointer.isActual().
                // However some PropertyPointers, e.g. DynamicPropertyPointer
                // will declare that any property you ask for is actual.
                // That's not acceptable for us: we really need to know
                // if the property is currently declared. Thus,
                // we'll need to perform a search.
                boolean ok = false;
                String names[] = dynamicPropertyPointer.getPropertyNames();
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals(propertyName)) {
                        ok = true;
                        break;
                    }
                }
                if (ok) {
                    dynamicPropertyPointer.setPropertyName(propertyName);
                    position++;
                    return true;
                }
            }
            else {
                Object pred = expression.computeValue(parentContext);
                if (pred instanceof Iterator) {
                    if (!((Iterator) pred).hasNext()) {
                        return false;
                    }
                    pred = ((Iterator) pred).next();
                }

                if (pred instanceof NodePointer) {
                    pred = ((NodePointer) pred).getNode();
                }

                if (pred instanceof Number) {
                    int pos = (int) InfoSetUtil.doubleValue(pred);
                    position++;
                    done = true;
                    return parentContext.setPosition(pos);
                }
                else if (InfoSetUtil.booleanValue(pred)) {
                    position++;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used for an optimized access to dynamic properties using the
     * "map[@name = 'name']" syntax
     */
    private boolean setupDynamicPropertyPointer() {
        if (nameTestExpression == null) {
            return false;
        }

        NodePointer parent = parentContext.getCurrentNodePointer();
        if (parent == null) {
            return false;
        }
        parent = parent.getValuePointer();
        if (!(parent instanceof PropertyOwnerPointer)) {
            return false;
        }
        dynamicPropertyPointer =
            (PropertyPointer) ((PropertyOwnerPointer) parent)
                .getPropertyPointer()
                .clone();
        return true;
    }

    public boolean setPosition(int position) {
        if (nameTestExpression == null) {
            return setPositionStandard(position);
        }
        else {
            if (dynamicPropertyPointer == null) {
                if (!setupDynamicPropertyPointer()) {
                    return setPositionStandard(position);
                }
            }
            if (position < 1
                || position > dynamicPropertyPointer.getLength()) {
                return false;
            }
            dynamicPropertyPointer.setIndex(position - 1);
            return true;
        }
    }

    public NodePointer getCurrentNodePointer() {
        if (position == 0) {
            if (!setPosition(1)) {
                return null;
            }
        }
        if (dynamicPropertyPointer != null) {
            return dynamicPropertyPointer.getValuePointer();
        }
        else {
            return parentContext.getCurrentNodePointer();
        }
    }

    public void reset() {
        super.reset();
        done = false;
    }

    public boolean nextSet() {
        reset();
        return parentContext.nextSet();
    }

    private boolean setPositionStandard(int position) {
        if (this.position > position) {
            reset();
        }

        while (this.position < position) {
            if (!nextNode()) {
                return false;
            }
        }
        return true;
    }
}