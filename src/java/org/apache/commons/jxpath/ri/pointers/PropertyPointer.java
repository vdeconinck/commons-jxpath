/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/pointers/Attic/PropertyPointer.java,v 1.5 2002/04/12 02:28:06 dmitri Exp $
 * $Revision: 1.5 $
 * $Date: 2002/04/12 02:28:06 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
package org.apache.commons.jxpath.ri.pointers;

import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.compiler.*;

import java.util.*;

/**
 * A pointer allocated by a PropertyOwnerPointer to represent the value of
 * a property of the parent object.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.5 $ $Date: 2002/04/12 02:28:06 $
 */
public abstract class PropertyPointer extends PropertyOwnerPointer {
    protected int propertyIndex = UNSPECIFIED_PROPERTY;
    protected Object bean;

    /**
     * Takes a javabean, a descriptor of a property of that object and
     * an offset within that property (starting with 0).
     */
    public PropertyPointer(NodePointer parent){
        super(parent);
    }

    public int getPropertyIndex(){
        return propertyIndex;
    }

    public void setPropertyIndex(int index){
        propertyIndex = index;
        index = WHOLE_COLLECTION;
    }

    public Object getBean(){
        if (bean == null){
            bean = getParent().getValue();
        }
        return bean;
    }

    public QName getName(){
        return new QName(null, getPropertyName());
    }

    public abstract String getPropertyName();

    public abstract void setPropertyName(String propertyName);

    public abstract int getPropertyCount();

    public abstract String[] getPropertyNames();

    protected abstract boolean isActualProperty();

    public boolean isActual(){
        if (!isActualProperty()){
            return false;
        }

        return super.isActual();
    }

    /**
     * Returns a NodePointer that can be used to access the currently
     * selected property value.
     */
    public NodePointer childNodePointer(){
        return createNodePointer(this, null, getValue());
    }

    public int hashCode(){
        return getParent().hashCode() + propertyIndex + index;
    }

    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        if (!(object instanceof PropertyPointer)){
            return false;
        }

        PropertyPointer other = (PropertyPointer)object;
        return getParent() == other.getParent() &&
                propertyIndex == other.propertyIndex &&
                index == other.index;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        if (getBean() == null){
            buffer.append("null");
        }
        else {
            buffer.append(getBean().getClass().getName());
        }
        buffer.append('@');
        buffer.append(System.identityHashCode(getBean()));
        buffer.append('.');
        buffer.append(getPropertyName());
        if (index != WHOLE_COLLECTION){
            buffer.append('[').append(index).append(']');
        }
        buffer.append(" = ").append(getValue());
        return buffer.toString();
    }
}