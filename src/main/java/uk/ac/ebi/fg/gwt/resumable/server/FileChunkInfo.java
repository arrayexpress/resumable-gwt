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

package uk.ac.ebi.fg.gwt.resumable.server;

public class FileChunkInfo {

    public int          chunkNumber;
    public int          chunkSize;
    public int          currentChunkSize;
    public long         fileSize;
    public String       id;
    public String       fileName;
    public String       relativePath;

    public boolean isValid() {
        return chunkNumber >= 0 && chunkSize > 0 && fileSize > 0
                && !id.isEmpty() && !fileName.isEmpty() && !relativePath.isEmpty();
    }
}
