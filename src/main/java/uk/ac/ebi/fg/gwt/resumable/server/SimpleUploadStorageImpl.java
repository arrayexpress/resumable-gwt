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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleUploadStorageImpl implements UploadStorage {

    private final String uploadDirectory;

    public SimpleUploadStorageImpl() {
        this.uploadDirectory = System.getProperty("java.io.tmpdir");
    }

    public SimpleUploadStorageImpl(String uploadDirectory) {
        if (null != uploadDirectory && new File(uploadDirectory).isDirectory() && new File(uploadDirectory).canWrite()) {
            this.uploadDirectory = uploadDirectory;
        } else {
            this.uploadDirectory = System.getProperty("java.io.tmpdir");
        }
    }

    @Override
    public boolean hasChunk(FileChunkInfo info) {
        return getStorageInfo(info).hasChunk(info.chunkNumber);
    }

    @Override
    public boolean hasAllChunks(FileChunkInfo info) {
        return getStorageInfo(info).hasAllChunks(info);
    }

    @Override
    public void storeChunk(FileChunkInfo info, InputStream stream, long length) throws IOException {
        FileStorageInfo storageInfo = getStorageInfo(info);

        if (!storageInfo.hasChunk(info.chunkNumber)) {
            try (RandomAccessFile raf = new RandomAccessFile(storageInfo.storageFileLocation, "rw")) {

                //Seek to offset
                raf.seek((info.chunkNumber - 1) * (long) info.chunkSize);

                long read = 0;
                byte[] buffer = new byte[Math.min(info.chunkSize, 16384)];
                while (read < length) {
                    int r = stream.read(buffer);
                    if (r < 0) {
                        break;
                    }
                    raf.write(buffer, 0, r);
                    read += r;
                }
            }
            storageInfo.addChunk(info.chunkNumber);
            if (storageInfo.hasAllChunks(info)) {
                if (!new File(storageInfo.storageFileLocation).renameTo(
                        new File(storageInfo.storageFileLocation.replaceFirst("[.]upload$", "")))
                        ) {
                    throw new IOException("Unable to rename file " + storageInfo.storageFileLocation + " to " + storageInfo.fileName);
                }
            }
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

        public boolean hasChunk(int chunkNumber) {
            return chunks.contains(chunkNumber);
        }

        public void addChunk(int chunkNumber) {
            chunks.add(chunkNumber);
        }

        public boolean hasAllChunks(FileChunkInfo info) {
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


}
