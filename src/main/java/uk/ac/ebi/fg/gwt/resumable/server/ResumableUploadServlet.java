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
import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@MultipartConfig
public class ResumableUploadServlet extends HttpServlet {

    private final static String SERVLET_PARAM_UPLOAD_DIRECTORY  = "uploadDirectory";

    private final static String RESUMABLE_CHUNK_NUMBER          = "resumableChunkNumber";
    private final static String RESUMABLE_CHUNK_SIZE            = "resumableChunkSize";
    private final static String RESUMABLE_TOTAL_SIZE            = "resumableTotalSize";
    private final static String RESUMABLE_IDENTIFIER            = "resumableIdentifier";
    private final static String RESUMABLE_FILENAME              = "resumableFilename";
    private final static String RESUMABLE_RELATIVE_PATH         = "resumableRelativePath";

    private final static String RESPONSE_UPLOADED               = "Uploaded.";
    private final static String RESPONSE_ALL_FINISHED           = "All finished.";

    private static class FileChunkInfo {

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
    }

    private static class FileStorageInfo {

        String fileName;
        String storageFileLocation;
        boolean isComplete;
        Set<Integer> chunks;

        public static FileStorageInfo build(String fileName, String uploadDirectory) {
            FileStorageInfo info = new FileStorageInfo();

            info.fileName = fileName;
            info.chunks = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
            info.isComplete = false;
            info.storageFileLocation = uploadDirectory + File.separator + fileName + ".upload";

            return info;
        }

        public boolean containsChunk(int chunkNumber) {
            return chunks.contains(chunkNumber);
        }

        public void addChunk(int chunkNumber) {
            chunks.add(chunkNumber);
        }

        public boolean checkIsComplete(FileChunkInfo info) {
            if (isComplete) {
                return true;
            }
            int count = (int) Math.ceil(((double) info.fileSize) / ((double) info.chunkSize));
            for (int i = 1; i < count; i++) {
                if (!chunks.contains(i)) {
                    return false;
                }
            }
            isComplete = true;
            return true;
        }
    }

    private final String uploadDirectory;

    public ResumableUploadServlet() {
        String directory = nullToEmpty(getServletConfig().getInitParameter(SERVLET_PARAM_UPLOAD_DIRECTORY));
        uploadDirectory = !directory.isEmpty() ? directory : System.getProperty("java.io.tmpdir");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        FileChunkInfo info = FileChunkInfo.build(request);
        if (!info.isValid()) {
            throw new ServletException("Invalid request parameters");
        }

        FileStorageInfo storageInfo = getStorageInfo(info);

        if (!storageInfo.containsChunk(info.chunkNumber)) {
            try (RandomAccessFile raf = new RandomAccessFile(storageInfo.storageFileLocation, "rw")) {

                //Seek to offset
                raf.seek((info.chunkNumber - 1) * (long) info.chunkSize);

                //Save to file
                InputStream is = isMultipart(request) ?
                        request.getPart("file").getInputStream() : request.getInputStream();

                long read = 0;
                long contentLength = request.getContentLength();
                byte[] buffer = new byte[Math.min(info.chunkSize, 16384)];
                while (read < contentLength) {
                    int r = is.read(buffer);
                    if (r < 0) {
                        break;
                    }
                    raf.write(buffer, 0, r);
                    read += r;
                }
            }
            storageInfo.addChunk(info.chunkNumber);
            if (storageInfo.checkIsComplete(info)) {
                response.getWriter().print(RESPONSE_ALL_FINISHED);
            } else {
                response.getWriter().print(RESPONSE_UPLOADED);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        FileChunkInfo info = FileChunkInfo.build(request);

        if (getStorageInfo(info).containsChunk(info.chunkNumber)) {
            response.getWriter().print(RESPONSE_UPLOADED);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private Map<String, FileStorageInfo> storageMap = new ConcurrentHashMap<>();

    private FileStorageInfo getStorageInfo(FileChunkInfo info) throws IllegalArgumentException {
        if (!info.isValid()) {
            throw new IllegalArgumentException("File chunk info is invalid");
        }
        String fileId = info.id;

        if (!storageMap.containsKey(fileId)) {
            FileStorageInfo storageInfo = FileStorageInfo.build(info.fileName, uploadDirectory);
            storageMap.put(fileId, storageInfo);
            return storageInfo;
        }

        return storageMap.get(fileId);
    }

    private static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return null != contentType && contentType.startsWith("multipart");
    }

    private static String partToString(Part part) throws IOException {

        BufferedReader br;
        StringBuilder sb = new StringBuilder();

        String line;
        try (InputStream is = part.getInputStream()) {
            if (null != is) {
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        }

        return sb.toString();
    }

    private static String getParam(HttpServletRequest request, String paramName) throws ServletException, IOException {
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