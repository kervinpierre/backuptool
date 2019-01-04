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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Part or whole BackupFile on disk.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupFilePart implements IBackupFilePart
{
    private static final Logger log 
                = LogManager.getLogger(BackupFilePart.class);

    private final String scheme;
    private final IBackendFileSystem FS;
    private final URI uri;
    private final Path path;

    @Override
    public String getScheme()
    {
        return scheme;
    }

    @Override
    public IBackendFileSystem getFS()
    {
        return FS;
    }

    @Override
    public URI getUri()
    {
        return uri;
    }

    @Override
    public Path getPath()
    {
        return path;
    }

    public BackupFilePart(String scheme, IBackendFileSystem FS, URI uri, Path path)
    {
        this.scheme = scheme;
        this.FS = FS;
        this.uri = uri;
        this.path = path;
    }

    @Override
    public int compareTo(Object o)
    {
        if( o == null )
        {
            throw new NullPointerException("Compared object cannot be null.");
        }

        if( !(o instanceof BackupFilePart) )
        {
            throw new RuntimeException(
                    String.format("Object is not a BackFilePart but '%s'", o.toString()) );
        }

        BackupFilePart bfp = (BackupFilePart)o;

        int res = new CompareToBuilder()
                .append(path, bfp.getPath())
                .toComparison();

        return res;
    }

    @Override
    public String toString()
    {
        String res = super.toString();

        if( getUri() != null && getUri().toString() != null )
        {
            res = getUri().toString();
        }

        return res;
    }
}
