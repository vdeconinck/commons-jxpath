/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMNodePointer.java,v 1.1 2002/08/26 22:29:48 dmitri Exp $
 * $Revision: 1.1 $
 * $Date: 2002/08/26 22:29:48 $
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
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.TypeUtils;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;

/**
 * A Pointer that points to a DOM node.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.1 $ $Date: 2002/08/26 22:29:48 $
 */
public class JDOMNodePointer extends NodePointer {
    private Object node;
    private Map namespaces;
    private String defaultNamespace;
    private String id;

    public static final String XML_NAMESPACE_URI =
            "http://www.w3.org/XML/1998/namespace";
    public static final String XMLNS_NAMESPACE_URI =
            "http://www.w3.org/2000/xmlns/";

    public JDOMNodePointer(Object node, Locale locale){
        super(null, locale);
        this.node = node;
    }

    public JDOMNodePointer(Object node, Locale locale, String id){
        super(null, locale);
        this.node = node;
        this.id = id;
    }

    public JDOMNodePointer(NodePointer parent, Object node){
        super(parent);
        this.node = node;
    }

    public NodeIterator childIterator(NodeTest test, boolean reverse,
                    NodePointer startWith) {
        return new JDOMNodeIterator(this, test, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName name){
        return new JDOMAttributeIterator(this, name);
    }

    public NodeIterator namespaceIterator(){
        return new JDOMNamespaceIterator(this);
    }

    public NodePointer namespacePointer(String prefix){
        return new JDOMNamespacePointer(this, prefix);
    }

    public String getNamespaceURI(){
        if (node instanceof Element){
            Element element = (Element)node;
            return element.getNamespaceURI();
        }
        return null;
    }

    public String getNamespaceURI(String prefix){
        if (node instanceof Element){
            Element element = (Element)node;
            Namespace ns = element.getNamespace(prefix);
//            System.err.println("PREFIX: " + prefix + " NS: " + ns);
            if (ns == null){
                return null;
            }
            return ns.getURI();
        }
        return null;
    }

    public int compareChildNodePointers(
                NodePointer pointer1, NodePointer pointer2)
    {
        Object node1 = pointer1.getBaseValue();
        Object node2 = pointer2.getBaseValue();
        if (node1 == node2){
            return 0;
        }

        if ((node1 instanceof Attribute) && !(node2 instanceof Attribute)){
            return -1;
        }
        else if (!(node1 instanceof Attribute) && (node2 instanceof Attribute)){
            return 1;
        }
        else if ((node1 instanceof Attribute) && (node2 instanceof Attribute)){
            List list = ((Element)getNode()).getAttributes();
            int length = list.size();
            for (int i = 0; i < length; i++){
                Object n = list.get(i);
                if (n == node1){
                    return -1;
                }
                else if (n == node2){
                    return 1;
                }
            }
            return 0;       // Should not happen
        }

        if (!(node instanceof Element)){
            throw new RuntimeException("JXPath internal error: " +
                    "compareChildNodes called for " + node);
        }

        List children = ((Element)node).getContent();
        int length = children.size();
        for (int i = 0; i < length; i++){
            Object n = children.get(i);
            if (n == node1){
                return -1;
            }
            else if (n == node2){
                return 1;
            }
        }

        return 0;
    }


    /**
     * @see org.apache.commons.jxpath.ri.model.NodePointer#getBaseValue()
     */
    public Object getBaseValue() {
        return node;
    }


    /**
     * @see org.apache.commons.jxpath.ri.model.NodePointer#getName()
     */
    public QName getName() {
        String ns = null;
        String ln = null;
        if (node instanceof Element){
            ns = ((Element)node).getNamespacePrefix();
            if (ns != null && ns.equals("")){
                ns = null;
            }
            ln = ((Element)node).getName();
        }
        else if (node instanceof ProcessingInstruction){
            ln = ((ProcessingInstruction)node).getTarget();
        }
        return new QName(ns, ln);
    }

    public QName getExpandedName(){
        return new QName(getNamespaceURI(), getName().getName());
    }

    /**
     * @see org.apache.commons.jxpath.ri.model.NodePointer#getNode()
     */
    public Object getNode() {
        return node;
    }

    public Object getValue(){
        if (node instanceof Element){
            return ((Element)node).getTextTrim();
        }
        else if (node instanceof Comment){
            String text = ((Comment)node).getText();
            if (text != null){
                text = text.trim();
            }
            return text;
        }
        else if (node instanceof Text){
            return ((Text)node).getTextTrim();
        }
        else if (node instanceof CDATA){
            return ((CDATA)node).getTextTrim();
        }
        else if (node instanceof ProcessingInstruction){
            String text = ((ProcessingInstruction)node).getData();
            if (text != null){
                text = text.trim();
            }
            return text;
        }
        return null;
    }


    /**
     * @see org.apache.commons.jxpath.Pointer#setValue(Object)
     */
    public void setValue(Object value) {
        String string = null;
        if (value != null){
            string = (String)TypeUtils.convert(value, String.class);
            if (string.equals("")){
                string = null;
            }
        }

        if (node instanceof Text){
            if (string != null){
                ((Text)node).setText(string);
            }
            else {
                nodeParent(node).removeContent((Text)node);
            }
        }
        else {
            Element element = (Element)node;
            // First remove all text from the element
            List content = new ArrayList(element.getContent());
            for (int i = content.size(); --i >= 0;){
                Object child = content.get(i);
                if (child instanceof Text){
                    element.removeContent((Text)node);
                }
                else if (child instanceof CDATA){
                    element.removeContent((CDATA)node);
                }
            }
            if (string != null){
                element.addContent(new Text(string));
            }
        }
    }

    public boolean testNode(NodeTest test){
        return testNode(this, node, test);
    }

    public static boolean testNode(
            NodePointer pointer, Object node, NodeTest test)
    {
        if (test == null){
            return true;
        }
        else if (test instanceof NodeNameTest){
            if (!(node instanceof Element)){
                return false;
            }

            QName testName = ((NodeNameTest)test).getNodeName();
            String testLocalName = testName.getName();
            if (testLocalName.equals("*") ||
                    testLocalName.equals(
                            JDOMNodePointer.getLocalName((Element)node))){
                String testPrefix = testName.getPrefix();
                String nodePrefix = JDOMNodePointer.getPrefix((Element)node);
                if (equalStrings(testPrefix, nodePrefix)){
                    return true;
                }

                String testNS = pointer.getNamespaceURI(testPrefix);
                if (testNS == null){
                    return false;
                }
                String nodeNS = pointer.getNamespaceURI(nodePrefix);
                return equalStrings(testNS, nodeNS);
            }
        }
        else if (test instanceof NodeTypeTest){
            switch (((NodeTypeTest)test).getNodeType()){
                case Compiler.NODE_TYPE_NODE:
                    return node instanceof Element;
                case Compiler.NODE_TYPE_TEXT:
                    return (node instanceof Text) ||
                        (node instanceof CDATA);
                case Compiler.NODE_TYPE_COMMENT:
                    return node instanceof Comment;
                case Compiler.NODE_TYPE_PI:
                    return node instanceof ProcessingInstruction;
            }
            return false;
        }
        else if (test instanceof ProcessingInstructionTest){
            if (node instanceof ProcessingInstruction){
                String testPI = ((ProcessingInstructionTest)test).getTarget();
                String nodePI = ((ProcessingInstruction)node).getTarget();
                return testPI.equals(nodePI);
            }
        }

        return false;
    }

    private static boolean equalStrings(String s1, String s2){
        if (s1 == null && s2 != null){
            return false;
        }
        if (s1 != null && s2 == null){
            return false;
        }

        if (s1 != null && !s1.trim().equals(s2.trim())){
            return false;
        }

        return true;
    }

    public static String getPrefix(Object node){
        if (node instanceof Element){
            String prefix = ((Element)node).getNamespacePrefix();
            return (prefix == null || prefix.equals("")) ? null : prefix;
        }
        else if (node instanceof Attribute){
            String prefix = ((Attribute)node).getNamespacePrefix();
            return (prefix == null || prefix.equals("")) ? null : prefix;
        }
        return null;
    }

    public static String getLocalName(Object node){
        if (node instanceof Element){
            return ((Element)node).getName();
        }
        else if (node instanceof Attribute){
            return ((Attribute)node).getName();
        }
        return null;
    }

    /**
     * Returns true if the xml:lang attribute for the current node
     * or its parent has the specified prefix <i>lang</i>.
     * If no node has this prefix, calls <code>super.isLanguage(lang)</code>.
     */
    public boolean isLanguage(String lang){
        String current = getLanguage();
        if (current == null){
            return super.isLanguage(lang);
        }
        return current.toUpperCase().startsWith(lang.toUpperCase());
    }

    protected String getLanguage(){
        Object n = node;
        while (n != null){
            if (n instanceof Element){
                Element e = (Element)n;
                String attr = e.getAttributeValue("lang",
                        Namespace.XML_NAMESPACE);
                if (attr != null && !attr.equals("")){
                    return attr;
                }
            }
            n = nodeParent(n);
        }
        return null;
    }

    private Element nodeParent(Object node){
        if (node instanceof Element){
            return ((Element)node).getParent();
        }
        else if (node instanceof Text){
            return ((Text)node).getParent();
        }
        else if (node instanceof CDATA){
            return ((CDATA)node).getParent();
        }
        else if (node instanceof ProcessingInstruction){
            return ((ProcessingInstruction)node).getParent();
        }
        else if (node instanceof Comment){
            return ((Comment)node).getParent();
        }
        return null;
    }

    public NodePointer createChild(
            JXPathContext context, QName name, int index)
    {
        if (index == WHOLE_COLLECTION){
            index = 0;
        }
        if (!getAbstractFactory(context).
                    createObject(context, this, node, name.toString(), index)){
            throw new JXPathException("Factory could not create " +
                    "a child node for path: " +
                    asPath() + "/" + name + "[" + (index+1) + "]");
        }
        NodeIterator it = childIterator(new NodeNameTest(name), false, null);
        if (it == null || !it.setPosition(index + 1)){
            throw new JXPathException("Factory could not create " +
                    "a child node for path: " +
                    asPath() + "/" + name + "[" + (index+1) + "]");
        }
        return it.getNodePointer();
    }

    public NodePointer createChild(
            JXPathContext context, QName name, int index, Object value)
    {
        NodePointer ptr = createChild(context, name, index);
        ptr.setValue(value);
        return ptr;
    }

    public void remove(){
        Element parent = nodeParent(node);
        if (parent == null){
            throw new JXPathException("Cannot remove root JDOM node");
        }
        parent.getContent().remove(node);
    }

    public String asPath(){
        if (id != null){
            return "id('" + escape(id) + "')";
        }

        StringBuffer buffer = new StringBuffer();
        if (parent != null){
            buffer.append(parent.asPath());
        }
        if (node instanceof Element){
            // If the parent pointer is not a JDOMNodePointer, it is
            // the parent's responsibility to produce the node test part
            // of the path
            if (parent instanceof JDOMNodePointer){
                buffer.append('/');
                buffer.append(getName());
                buffer.append('[');
                buffer.append(getRelativePositionByName());
                buffer.append(']');
            }
        }
        else if (node instanceof Text || node instanceof CDATA){
            buffer.append("/text()");
            buffer.append('[').
                    append(getRelativePositionOfTextNode()).
                    append(']');
        }
        else if (node instanceof ProcessingInstruction){
            String target = ((ProcessingInstruction)node).getTarget();
            buffer.append("/processing-instruction(\'").
                    append(target).
                    append("')");
            buffer.append('[').
                    append(getRelativePositionOfPI(target)).
                    append(']');
        }
        return buffer.toString();
    }

    private String escape(String string){
        int index = string.indexOf('\'');
        while (index != -1){
            string = string.substring(0, index) +
                    "&apos;" + string.substring(index + 1);
            index = string.indexOf('\'');
        }
        index = string.indexOf('\"');
        while (index != -1){
            string = string.substring(0, index) +
                    "&quot;" + string.substring(index + 1);
            index = string.indexOf('\"');
        }
        return string;
    }

    private int getRelativePositionByName(){
        if (node instanceof Element){
            Element parent = ((Element)node).getParent();
            if (parent == null){
                return 1;
            }
            List children = parent.getContent();
            int count = 0;
            String name = ((Element)node).getQualifiedName();
            for (int i = 0; i < children.size(); i++){
                Object child = children.get(i);
                if ((child instanceof Element) &&
                    ((Element)child).getQualifiedName().equals(name)){
                    count++;
                }
                if (child == node){
                    break;
                }
            }
            return count;
        }
        return 1;
    }

    private int getRelativePositionOfTextNode(){
        Element parent;
        if (node instanceof Text){
            parent = ((Text)node).getParent();
        }
        else {
            parent = ((CDATA)node).getParent();
        }
        if (parent == null){
            return 1;
        }
        List children = parent.getContent();
        int count = 0;
        for (int i = 0; i < children.size(); i++){
            Object child = children.get(i);
            if (child instanceof Text || child instanceof CDATA){
                count++;
            }
            if (child == node){
                break;
            }
        }
        return count;
    }

    private int getRelativePositionOfPI(String target){
        Element parent = ((ProcessingInstruction)node).getParent();
        if (parent == null){
            return 1;
        }
        List children = parent.getContent();
        int count = 0;
        for (int i = 0; i < children.size(); i++){
            Object child = children.get(i);
            if (child instanceof ProcessingInstruction &&
                  (target == null ||
                   target.equals(((ProcessingInstruction)child).getTarget()))){
                count++;
            }
            if (child == node){
                break;
            }
        }
        return count;
    }

    public int hashCode(){
        return System.identityHashCode(node);
    }

    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        if (!(object instanceof JDOMNodePointer)){
            return false;
        }

        JDOMNodePointer other = (JDOMNodePointer)object;
        return node == other.node;
    }

    private AbstractFactory getAbstractFactory(JXPathContext context){
        AbstractFactory factory = context.getFactory();
        if (factory == null){
            throw new JXPathException(
                    "Factory is not set on the JXPathContext - " +
                    "cannot create path: " + asPath());
        }
        return factory;
    }
}