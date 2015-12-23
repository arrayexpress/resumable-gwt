/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.fg.gwt.resumable.client.injector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class Initializer {

    public interface Resources extends ClientBundle {

        @ClientBundle.Source("../resumable-1.0-14122015.js")
        TextResource d3JsScript();
    }

    public static void configure() {
        Resources resources = GWT.create(Resources.class);
        injectJs(resources.d3JsScript());
    }

    private static void injectJs(final TextResource r) {
        JavaScriptInjector.inject(r.getText());
    }
}
