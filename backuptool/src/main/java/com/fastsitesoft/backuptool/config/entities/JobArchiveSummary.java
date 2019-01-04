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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Class for tracking archive files on disk.  Job Archive Summary summarizes the
 * basic facts about an archive, while the Job Archive class provides a lot more
 * detail.
 *
 * Created by kervin on 2015-08-11.
 */
public final class JobArchiveSummary
{
    private static final Logger log = LogManager.getLogger(JobArchiveSummary.class);

    private final Path path;
    private final Path listingFile;
    private final Integer orderId;
    private final Instant createStart;
    private final Instant createEnd;
    private final BackupToolCompressionType compressionType;
    private final BackupToolEncryptionType encryptionType;

    public Path getPath()
    {
        return path;
    }

    public Path getListingFile()
    {
        return listingFile;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    public Instant getCreateStart()
    {
        return createStart;
    }

    public Instant getCreateEnd()
    {
        return createEnd;
    }

    public BackupToolCompressionType getCompressionType()
    {
        return compressionType;
    }

    public BackupToolEncryptionType getEncryptionType()
    {
        return encryptionType;
    }

    private JobArchiveSummary( final Path path,
                               final Path listingFile,
                               final Integer orderId,
                               final Instant createStart,
                               final Instant createEnd,
                               final BackupToolCompressionType compressionType,
                               final BackupToolEncryptionType encryptionType )
    {
        this.path = path;
        this.listingFile = listingFile;
        this.orderId = orderId;
        this.createStart = createStart;
        this.createEnd = createEnd;
        this.compressionType = compressionType;
        this.encryptionType = encryptionType;
    }

    public static JobArchiveSummary from(final JobArchive archive,
                                         final Path listingFile,
                                         final Instant createStart,
                                         final Instant createEnd)
    {
        return from(archive.getPath(),
                listingFile,
                archive.getOrderId(),
                createStart,
                createEnd,
                archive.getCompressionType(),
                archive.getEncryptionType());
    }

    public static JobArchiveSummary from( final String path,
                                          final String listingFile,
                                          final String orderId,
                                          final String createStart,
                                          final String createEnd,
                                          final String compressionType,
                                          final String encryptionType ) throws BackupToolException
    {
        JobArchiveSummary res;

        Path currPath = Paths.get(path);
        Path currListing = Paths.get(listingFile);

        Integer currOrderId = null;

        if( StringUtils.isNoneBlank(orderId) )
        {
            currOrderId = Integer.parseInt(orderId);
        }

        Instant currStart = null;
        Instant currEnd   = null;

        if( StringUtils.isNoneBlank(createStart))
        {
            currStart = Instant.parse(createStart);
        }

        if( StringUtils.isNoneBlank(createEnd))
        {
            currEnd = Instant.parse(createEnd);
        }

        BackupToolCompressionType comp = BackupToolCompressionType.from(compressionType);

        BackupToolEncryptionType enc = BackupToolEncryptionType.from(encryptionType);

        res = JobArchiveSummary.from(currPath,
                currListing,
                currOrderId,
                currStart,
                currEnd,
                comp,
                enc);

        return res;
    }

    public static JobArchiveSummary from( final Path path,
                                          final Path listingFile,
                                          final Integer orderId,
                                          final Instant createStart,
                                         final Instant createEnd,
                                          final BackupToolCompressionType compressionType,
                                         final BackupToolEncryptionType encryptionType )
    {
        JobArchiveSummary summary;

        summary = new JobArchiveSummary(path, listingFile, orderId, createStart,
                createEnd, compressionType, encryptionType);

        return summary;
    }
}
