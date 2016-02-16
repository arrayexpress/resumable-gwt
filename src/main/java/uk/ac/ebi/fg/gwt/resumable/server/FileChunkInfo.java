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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.ac.ebi.fg.gwt.resumable.server.ResumableUploadServlet.*;

public class FileChunkInfo {

    int         chunkNumber;
    int         chunkSize;
    long        fileSize;
    String      id;
    String      fileName;
    String      relativePath;

    public static FileChunkInfo build(HttpServletRequest request) throws IOException, ServletException {
        FileChunkInfo info = new FileChunkInfo();

        info.chunkNumber = parseInt(getParam(request, RESUMABLE_CHUNK_NUMBER), -1);
        info.chunkSize = parseInt(getParam(request, RESUMABLE_CHUNK_SIZE), -1);
        info.fileSize = parseLong(getParam(request, RESUMABLE_TOTAL_SIZE), -1);
        info.id = nullToEmpty(getParam(request, RESUMABLE_IDENTIFIER));
        info.fileName = nullToEmpty(getParam(request, RESUMABLE_FILENAME));
        info.relativePath = nullToEmpty(getParam(request, RESUMABLE_RELATIVE_PATH));

        return info;
    }

    public boolean isValid() {
        return chunkNumber >= 0 && chunkSize > 0 && fileSize > 0
                && !id.isEmpty() && !fileName.isEmpty() && !relativePath.isEmpty();
    }

    private static String nullToEmpty(String value) {
        if (null == value) {
            return "";
        }
        return value;
    }

    private static int parseInt(String value, int fallbackValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException x) {
            return fallbackValue;
        }
    }

    private static long parseLong(String value, long fallbackValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException x) {
            return fallbackValue;
        }
    }
}
