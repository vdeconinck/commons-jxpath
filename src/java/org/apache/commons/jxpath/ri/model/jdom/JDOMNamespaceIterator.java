/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * An iterator of namespaces of a DOM Node.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.8 $ $Date: 2004/03/04 01:01:27 $
 */
public class JDOMNamespaceIterator implements NodeIterator {
    private NodePointer parent;
    private List namespaces;
    private Set prefixes;
    private int position = 0;

    public JDOMNamespaceIterator(NodePointer parent) {
        this.parent = parent;
        Object node = parent.getNode();
        if (node instanceof Element) {
            namespaces = new ArrayList();
            prefixes = new HashSet();
            collectNamespaces((Element) parent.getNode());
        }
    }

    private void collectNamespaces(Element element) {
        Namespace ns = element.getNamespace();
        if (ns != null && !prefixes.contains(ns.getPrefix())) {
            namespaces.add(ns);
            prefixes.add(ns.getPrefix());
        }
        List others = element.getAdditionalNamespaces();
        for (int i = 0; i < others.size(); i++) {
            ns = (Namespace) others.get(i);
            if (ns != null && !prefixes.contains(ns.getPrefix())) {
                namespaces.add(ns);
                prefixes.add(ns.getPrefix());
            }
        }
        Object parent = element.getParent();
        if (parent instanceof Element) {
            collectNamespaces((Element)parent);
        }
    }

    public NodePointer getNodePointer() {
        if (position == 0) {
            if (!setPosition(1)) {
                return null;
            }
            position = 0;
        }
        int index = position - 1;
        if (index < 0) {
            index = 0;
        }
        Namespace ns = (Namespace) namespaces.get(index);
        return new JDOMNamespacePointer(parent, ns.getPrefix(), ns.getURI());
    }

    public int getPosition() {
        return position;
    }

    public boolean setPosition(int position) {
        this.position = position;
        return position >= 1 && position <= namespaces.size();
    }
}