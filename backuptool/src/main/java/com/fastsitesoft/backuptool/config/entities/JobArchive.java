/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 *
 */

package com.fastsitesoft.backuptool.config.entities;

import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolEncryptionType;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a single archive file on disk.  Serialized in the Job State XML.
 *
 * Created by kervin on 2015-08-16.
 */
public final class JobArchive
{
    private static final Logger log = LogManager.getLogger(JobArchive.class);

    private final JobArchiveListingState listing;
    private final Path path;     // In XML/remote this is relative to the Job Directory
    private final URI uri;       // Don't serialize in XML
    private final Boolean local; // Don't serialize in XML
    private final Long byteCount;// Don't serialize in XML
    private final BackupToolEncryptionType encryptionType;
    private final BackupToolCompressionType compressionType;
    private final Integer orderId;

    public JobArchiveListingState getListing()
    {
        return listing;
    }

    public Path getPath()
    {
        return path;
    }

    public URI getUri()
    {
        return uri;
    }

    public Boolean getLocal()
    {
        return local;
    }

    public Long getByteCount()
    {
        return byteCount;
    }

    public BackupToolEncryptionType getEncryptionType()
    {
        return encryptionType;
    }

    public BackupToolCompressionType getCompressionType()
    {
        return compressionType;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    private JobArchive( final JobArchive obj,
                        final JobArchiveListingState listing,
                        final Path path,
                        final URI uri,
                        final Boolean local,
                        final Long byteCount,
                        final BackupToolEncryptionType encryptionType,
                        final BackupToolCompressionType compressionType,
                        final Integer orderId )
    {
        if( obj != null && obj.getListing() != null )
        {
            this.listing = obj.getListing();
        }
        else
        {
            this.listing = listing;
        }

        if( obj != null && obj.getPath() != null )
        {
            this.path = obj.getPath();
        }
        else
        {
            this.path = path;
        }

        if( obj != null && obj.getUri() != null )
        {
            this.uri = obj.getUri();
        }
        else
        {
            this.uri = uri;
        }

        if( obj != null && obj.getLocal() != null )
        {
            this.local = obj.getLocal();
        }
        else
        {
            this.local = local;
        }

        if( obj != null && obj.getByteCount() != null )
        {
            this.byteCount = obj.getByteCount();
        }
        else if( byteCount != null )
        {
            this.byteCount = byteCount;
        }
        else
        {
            this.byteCount = 0L;
        }

        if( obj != null && obj.getEncryptionType() != null )
        {
            this.encryptionType = obj.getEncryptionType();
        }
        else
        {
            this.encryptionType = encryptionType;
        }

        if( obj != null && obj.getCompressionType() != null )
        {
            this.compressionType = obj.getCompressionType();
        }
        else
        {
            this.compressionType = compressionType;
        }

        if( obj != null && obj.getOrderId() != null )
        {
            this.orderId = obj.getOrderId();
        }
        else
        {
            this.orderId = orderId;
        }
    }

    public static JobArchive from( final JobArchive obj,
                                   final JobArchiveListingState listing,
                                   final Path path,
                                   final URI uri,
                                   final Boolean local,
                                   final Long byteCount,
                                   final BackupToolEncryptionType encryptionType,
                                   final BackupToolCompressionType compressionType,
                                   final Integer orderId )
    {
        JobArchive res;

        res = new JobArchive(obj, listing, path, uri, local, byteCount, encryptionType, compressionType, orderId);

        return res;
    }

    public static JobArchive setListing(JobArchive job, JobArchiveListingState listing)
    {
        JobArchive res;

        res = from( job,
                listing,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        return res;
    }
}
