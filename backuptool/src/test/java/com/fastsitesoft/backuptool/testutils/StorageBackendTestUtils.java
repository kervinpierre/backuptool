/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fastsitesoft.backuptool.testutils;

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;

/**
 *
 * @author kervin
 */
public class StorageBackendTestUtils
{
    public static void uploadFile(IBackendFileSystem fs,
                                       Path localFile, Path remotePath) throws BackupToolException
    {
        IBackupFilePart src = fs.resolveLocalFile(localFile);
        IBackupFilePart dst = fs.resolveRemoteFile(remotePath);
        
        fs.copy(src, dst);
    }
    
    public static void deleteFile(IBackendFileSystem fs,
                                       Path remotePath) throws FileSystemException, BackupToolException
    {
        IBackupFilePart dst = fs.resolveRemoteFile(remotePath);
        
        Boolean delRes = fs.delete(dst);
        Assert.assertTrue(delRes);
    }
    
    public static void deleteFolder(IBackendFileSystem fs,
                                       Path remotePath, boolean recurse) throws FileSystemException, BackupToolException
    {
        IBackupFilePart dst = fs.resolveRemoteFile(remotePath);
        
        int delRes = fs.deleteFolder(dst, recurse);
        Assert.assertTrue(delRes>0);
    }
    
    public static File createTempFile(String prefix, String ext, String content) throws IOException
    {
        File res = File.createTempFile(prefix, ext);
        try(FileWriter fw = new FileWriter(res))
        {
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(content);
            bw.flush();
        }
        
        return res;
    }
}