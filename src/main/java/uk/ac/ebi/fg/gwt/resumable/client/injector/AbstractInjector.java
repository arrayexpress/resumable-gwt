/*
 *  Copyright 2012 GWT-Bootstrap
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package uk.ac.ebi.fg.gwt.resumable.client.injector;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;

/**
 * Base class for classes that inject someting into the document header.
 *
 * @author Carlos Alexandro Becker
 * @author Dominik Mayer
 */
public abstract class AbstractInjector {

    private static HeadElement head;

    /**
     * Gets the document header.
     *
     * @return the document header
     */
    protected static HeadElement getHead() {
        if (head == null) {
            Element element = Document.get().getElementsByTagName("head")
                    .getItem(0);
            assert element != null : "HTML Head element required";
            HeadElement head = HeadElement.as(element);
            AbstractInjector.head = head;
        }
        return AbstractInjector.head;
    }
}