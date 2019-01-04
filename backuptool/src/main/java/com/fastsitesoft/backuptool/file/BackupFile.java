/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 * 
 *  NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.file;

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The actual backup file on disk.  Will be TAR, or TAR/GZ or TAR/AES, etc.
 *
 * 1. One tar file per base directory or base file chunk
 * 2. Chunks of this file are valid tar files
 * 3. Chunks of this file are valid GZip blocks
 * 4. Chunks of this file are valid AES blocks
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupFile implements IBackupFile
{
    private static final Logger log 
                                = LogManager.getLogger(BackupFile.class);

    private final List<IBackupFilePart> parts;

    @Override
    public List<IBackupFilePart> getParts()
    {
        return parts;
    }

    public BackupFile(final List<IBackupFilePart> parts)
    {
        this.parts = parts;
    }
}
