/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
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

public interface ResumableCallback {
    void onUploadStart(ResumableUploader uploader);
    void onComplete(ResumableUploader uploader);
    void onProgress(ResumableUploader uploader);
    void onError(ResumableUploader uploader, String message, ResumableFile file);
    void onPause();
    void beforeCancel();
    void onCancel();
    void onChunkingStart(ResumableFile file);
    void onChunkingProgress(ResumableFile file, String ratio);
    void onChunkingComplete(ResumableFile file);
}
