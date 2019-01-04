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

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single Job Archive Listing File.
 *
 * Created by kervin on 2015-08-15.
 */
public final class JobArchiveListingState
{
    private static final Logger log = LogManager.getLogger(JobArchiveListingState.class);

    private final UUID archiveId;
    private final Path path;
    private final List<JobArchiveListing> listing;

    public UUID getArchiveId()
    {
        return archiveId;
    }

    public Path getPath()
    {
        return path;
    }

    public List<JobArchiveListing> getListing()
    {
        return listing;
    }

    private JobArchiveListingState(final UUID archiveId,
                                   final Path path,
                                   final List<JobArchiveListing> list)
    {
        this.archiveId = archiveId;
        this.path = path;
        this.listing = list;
    }

    public static JobArchiveListingState from(final UUID archiveId, final Path path)
    {
        JobArchiveListingState res;
        List<JobArchiveListing> list = new ArrayList<>();

        res = new JobArchiveListingState(archiveId, path, list);

        return res;
    }

    public static JobArchiveListingState from( final UUID archiveId,
                                               final Path path,
                                               final List<JobArchiveListing> list)
    {
        JobArchiveListingState res;

        res = new JobArchiveListingState(archiveId, path, list);
        return res;
    }


    public static Path getListingName(Path p, boolean checkExists) throws BackupToolException
    {
        Path res;

        String tempListName = String.format("%s.listing.xml",
                p.toString());
        res = Paths.get(tempListName);

        if( checkExists && Files.exists(res) )
        {
            String errMsg =
                    String.format("Listing file already exists '%s'", res);
            log.debug(errMsg);

            throw new BackupToolException(errMsg);
        }

        return res;
    }
}
