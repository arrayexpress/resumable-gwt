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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@MultipartConfig
public class ResumableUploadServlet extends HttpServlet {

    public final static String RESUMABLE_CHUNK_NUMBER          = "resumableChunkNumber";
    public final static String RESUMABLE_CHUNK_SIZE            = "resumableChunkSize";
    public final static String RESUMABLE_TOTAL_SIZE            = "resumableTotalSize";
    public final static String RESUMABLE_IDENTIFIER            = "resumableIdentifier";
    public final static String RESUMABLE_FILENAME              = "resumableFilename";
    public final static String RESUMABLE_RELATIVE_PATH         = "resumableRelativePath";

    private final static String RESPONSE_UPLOADED               = "Uploaded.";
    private final static String RESPONSE_ALL_FINISHED           = "All finished.";

    private UploadStorage storage;

    public ResumableUploadServlet() {
        setStorage(new SimpleUploadStorageImpl());
    }

    public void setStorage(UploadStorage storage) {
        this.storage = storage;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        FileChunkInfo info = buildChunkInfo(request);
        if (!info.isValid()) {
            throw new ServletException("Invalid request parameters");
        }

        if (!storage.hasChunk(info)) {
            try (InputStream is = isMultipart(request) ?
                    request.getPart("file").getInputStream() : request.getInputStream()) {
                long length = isMultipart(request) ?
                        request.getPart("file").getSize() : request.getContentLength();

                storage.storeChunk(info, is, length);
            }
            if (storage.hasAllChunks(info)) {
                response.getWriter().print(RESPONSE_ALL_FINISHED);
            } else {
                response.getWriter().print(RESPONSE_UPLOADED);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        FileChunkInfo info = buildChunkInfo(request);
        if (!info.isValid()) {
            throw new ServletException("Invalid request parameters");
        }


        if (storage.hasChunk(info)) {
            response.getWriter().print(RESPONSE_UPLOADED);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    protected FileChunkInfo buildChunkInfo(HttpServletRequest request) throws IOException, ServletException {
        FileChunkInfo info = new FileChunkInfo();

        info.chunkNumber = parseInt(getParam(request, RESUMABLE_CHUNK_NUMBER), -1);
        info.chunkSize = parseInt(getParam(request, RESUMABLE_CHUNK_SIZE), -1);
        info.fileSize = parseLong(getParam(request, RESUMABLE_TOTAL_SIZE), -1);
        info.id = nullToEmpty(getParam(request, RESUMABLE_IDENTIFIER));
        info.fileName = nullToEmpty(getParam(request, RESUMABLE_FILENAME));
        info.relativePath = nullToEmpty(getParam(request, RESUMABLE_RELATIVE_PATH));

        return info;
    }

    private static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return null != contentType && contentType.startsWith("multipart");
    }

    private static String partToString(Part part) throws IOException {

        BufferedReader br;
        StringBuilder sb = new StringBuilder();

        if (null != part) {
            String line;
            try (InputStream is = part.getInputStream()) {
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            }
        }

        return sb.toString();
    }

    public static String getParam(HttpServletRequest request, String paramName) throws ServletException, IOException {
        return isMultipart(request) ?
                partToString(request.getPart(paramName)) : request.getParameter(paramName);
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