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

package uk.ac.ebi.fg.gwt.resumable.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;

public class ResumableUploader extends JavaScriptObject {

    protected ResumableUploader() {}


    public static ResumableUploader newInstance(String url) {
        return createResumableJso(url);
    }

    private static native ResumableUploader createResumableJso(String url) /*-{
        if (undefined !== $wnd.Resumable) {
            return new $wnd.Resumable({
                target: url,
                query: {xxx: 'xxx'},
                method: "multupart"
            });
        } else {
            console.error('resumable.init: please ensure resumable.js is included');
        }
    }-*/;

    public final native void assignBrowse(Element element)/*-{
        if (undefined !== this.assignBrowse) {
            this.assignBrowse(element);
        } else {
            console.error('resumable.assignBrowse: please obtain an instance through ResumableUpload.newInstance');
        }
    }-*/;

    public final native void assignDrop(Element element)/*-{
        if (undefined !== this.assignDrop) {
            this.assignDrop(element);
        } else {
            console.error('resumable.assignDrop: please obtain an instance through ResumableUpload.newInstance');
        }
    }-*/;

    public final native void upload() /*-{
        if (undefined !== this.upload) {
            this.upload();
        } else {
            console.error('resumable.upload: please obtain an instance through ResumableUpload.newInstance');
        }
    }-*/;

    public final native void addCallback(ResumableFileCallback callback) /*-{
        if (undefined !== this.on) {
            this.on('fileAdded', function(file) {
                callback.@uk.ac.ebi.fg.gwt.resumable.client.ResumableFileCallback::onFileAdded(*)(this, file)
            });
            this.on('filesAdded', function(files) {
                callback.@uk.ac.ebi.fg.gwt.resumable.client.ResumableFileCallback::onFilesAdded(*)(this, files);
            });
            this.on('fileProgress', function(file) {
                callback.@uk.ac.ebi.fg.gwt.resumable.client.ResumableFileCallback::onFileProgress(*)(this, file);
            });
            this.on('fileSuccess', function(file) {
                callback.@uk.ac.ebi.fg.gwt.resumable.client.ResumableFileCallback::onFileSuccess(*)(this, file);
            });
        } else {
            console.error('resumable.on: please obtain an instance through ResumableUpload.newInstance');
        }
    }-*/;

    public final native JsArray<ResumableFile> files() /*-{
        if (undefined !== this.files) {
            return this.files;
        } else {
            console.error('resumable.files: please obtain an instance through ResumableUpload.newInstance')
        }
    }-*/;
}