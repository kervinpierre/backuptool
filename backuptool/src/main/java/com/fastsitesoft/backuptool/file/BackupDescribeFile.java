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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The a description of a completed or ongoing backup stored on disk.
 *
 * An ongoing job should update the respective files periodically.  Changing the final
 * status code at the end to mark completion.
 *
 * 1. "Job Set State File" in Job Set root keeps track of all jobs.
 *     List of all Job Run IDs
 *     List of all Job Run Paths
 *     List of all Job Run START dates
 *     List of all Job Run END dates
 *     List of all Job Run Types
 *     List of all Job Run System IDs of requester
 *     List of all Job Run Statuses
 *
 * 2. "Job Run State File" in Job folder keeps track of that current job
 *     Job Run START Date
 *     Job Run END Date
 *     Job Run Path
 *     Job Run Status
 *     System ID of requester
 *     List of Archive files in this Job
 *     * Order should be declared in the this file
 *     Job Run ItemList ( from Configuration )
 *     * Item to Archive file mapping for restoring a single file
 *     * tar tvf *.btaf.listing saved as a separate optionally encrypted file
 *     * Should not have to download an archive file to see what's in it
 *     Misc. Job Run Configuration that may be pertinent for Restore Job
 *     Job Type ( FULL or DIFF )
 *     ID of last Job Run ( Needed for a DIFF restore )
 *     DIFF change detector.  CHECKSUM, LASTMODDATE or EITHER
 *     Files with an ERROR or WARN status
 *     * List of files that changed during backup
 *     * List of files that disappeared during backup
 *     * List of files that became inaccessible during backup ( and why )
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupDescribeFile
{
    private static final Logger log 
                                = LogManager.getLogger(BackupDescribeFile.class);
}
