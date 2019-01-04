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
package com.fastsitesoft.backuptool;

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import com.fastsitesoft.backuptool.config.entities.BackupConfigChunk;
import com.fastsitesoft.backuptool.config.entities.BackupConfigEncryption;
import com.fastsitesoft.backuptool.config.entities.JobArchive;
import com.fastsitesoft.backuptool.config.entities.JobArchiveListing;
import com.fastsitesoft.backuptool.config.entities.JobArchiveListingState;
import com.fastsitesoft.backuptool.config.entities.JobError;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.config.entities.writers.JobArchiveListingStateWriter;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import com.fastsitesoft.backuptool.enums.BackupToolFSItemType;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolJobStatus;
import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.file.local.BackupItem;
import com.fastsitesoft.backuptool.utils.BackupFileArchiveOutputStream;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Output stream for backup files.  This class is responsible for chunking and
 * otherwise managing all values in the underling file-system.
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupFileOutput
{
    private static final Logger log
            = LogManager.getLogger(BackupFileOutput.class);

    public static BackupToolResult addBackupItems(
            final JobState job,
            final Deque<BackupItem> inPaths,
            final LinkOption linkOption,
            final Deque<JobArchive> archivePaths,
            final Pattern archivePathNameRegex,
            final BackupConfigChunk chunkConfig,
            final List<BackupToolNameComponentType> nameComponents,
            final BackupConfigEncryption encryptionType,
            final BackupToolCompressionType compressionType,
            final Boolean createListingFile,
            final String ignoreArchiveSuffix) throws BackupToolException
    {
        Deque<Path> paths = new ArrayDeque<>();

        for( BackupItem item : inPaths )
        {
            paths.add(item.getPath());
        }

        return addPaths(job, paths,linkOption,archivePaths, archivePathNameRegex, chunkConfig,
                nameComponents, encryptionType, compressionType, createListingFile, ignoreArchiveSuffix);
    }

    /**
     * Add a file to the current archive
     *
     * @param inPaths              Files/folders to be added
     * @param archives         List of paths to Tar files on the file-system, and the number of bytes that have been written to them.
     * @param archivePathNameRegex The regex used to name new archive files.  E.g. example_dir_([\d]+)\.btaf
     * @throws BackupToolException
     */
    public static BackupToolResult addPaths(
            final JobState job,
            final Deque<Path> inPaths,
            final LinkOption linkOption,
            final Deque<JobArchive> archives,
            final Pattern archivePathNameRegex,
            final BackupConfigChunk chunkConfig,
            final List<BackupToolNameComponentType> nameComponents,
            final BackupConfigEncryption encryptionType,
            final BackupToolCompressionType compressionType,
            final Boolean createListingFile,
            final String ignoreArchiveSuffix) throws BackupToolException
    {
        BackupToolResult res = null;

        if( archives == null )
        {
            log.debug("Archive list is null.");
        }

        if( archives.size() < 1 )
        {
            log.debug("Archive list is empty.");
        }

        if( inPaths == null )
        {
            log.debug("Path list is null");
        }

        if( inPaths.size() < 1 )
        {
            String errMsg = String.format("We will NOT make an empty archive '%s'!",
                    archives.getLast().getPath());

            log.debug(errMsg);

            throw new BackupToolException(errMsg);
        }

        // TODO : Check path type.  File? Folder? Link?

        Path currArchivePath = null;
        JobArchiveListingState currArchiveListingState = null;
        JobArchive currArchive = archives.peekLast();
        long currEntrySize;
        long fileSize;
        BackupFileArchiveOutputStream out = null;
        long currByteCount = 0;

        try
        {
            log.debug(String.format("inPaths size = '%d'", inPaths.size()));
            int currFileCount = 0;
            int currChunkCount = 0;

            // Loop through received files
            for( Path inPath : inPaths )
            {
                log.debug(String.format("addPaths() : #%d : Processing '%s'", currFileCount++, inPath));

                try
                {
                    // Default file size
                    fileSize = Files.size(inPath);

                    if( chunkConfig == null )
                    {
                        currArchivePath = currArchive.getPath();
                        currByteCount = currArchive.getByteCount();
                        currEntrySize = fileSize;
                    }
                    else
                    {
                        if( fileSize < (chunkConfig.getSizeBytes() - currByteCount) )
                        {
                            currEntrySize = fileSize;
                        }
                        else
                        {
                            // On second or later files in a series, make sure that
                            // the last bytes are removed from the current limit
                            currEntrySize = chunkConfig.getSizeBytes() - currByteCount;
                        }

                        if( currArchivePath == null )
                        {
                            // Most likely this is the first iteration of the file loop
                            // Get the first archive from the supplied list
                            if( currArchive.getByteCount() < chunkConfig.getSizeBytes() )
                            {
                                // Compared the chunk size limit with the values passed in
                                // with the archive file.
                                currArchivePath = currArchive.getPath();

                                currByteCount = currArchive.getByteCount();

                                log.debug(String.format("addPaths() : Continuing with archive '%s'", currArchivePath));
                            }
                            else
                            {
                                currArchivePath = IBackendFileSystem.nextName(currArchive.getPath(),
                                        archivePathNameRegex, nameComponents, true, ignoreArchiveSuffix);
                            }
                        }
                    }

                    // Get the listing file if available
                    if( currArchive.getListing() == null )
                    {
                        log.debug(String.format("Missing listing in archive '%s'",
                                currArchive.getPath() ));

                        // Create the listing file if needed
                        if( createListingFile )
                        {
                            Path currArchiveListingPath
                                    = JobArchiveListingState.getListingName(currArchivePath, false);
                            currArchiveListingState
                                    = JobArchiveListingState.from(UUID.randomUUID(), currArchiveListingPath);
                        }
                        else
                        {
                            log.debug(String.format("No listing file and cannot create for file '%s'",
                                    currArchive.getPath() ));
                        }

                    }
                    else
                    {
                        currArchiveListingState = currArchive.getListing();
                    }

                    // Can we write to the current archive?

                    // FIXME : Backup file naming convention.  Get name here.

                    // fileStream.setArchiveFile(currentBackupFile);
                    BackupFileCountingInputStream bis = null;

                    try
                    {
                        if( out == null )
                        {
                            out = new BackupFileArchiveOutputStream(
                                    new BufferedOutputStream(
                                            new FileOutputStream(currArchivePath.toFile(), true)),
                                    encryptionType,
                                    compressionType);

                            log.debug(String.format("addPaths() : Archive is now '%s'", currArchivePath));

                        }

                        if( Files.isDirectory(inPath) )
                        {
                            out.putArchiveEntry(inPath, (byte) '5', 0L, linkOption);
                            out.closeArchiveEntry();

                            // Record Listing
                            JobArchiveListing l = JobArchiveListing.from(inPath, BackupToolJobStatus.NONE,
                                    BackupToolJobDisposition.NONE, null, null, null, BackupToolFSItemType.DIRECTORY);
                            currArchiveListingState.getListing().add(l);
                        }
                        else if( fileSize == 0 )
                        {
                            // Let's make the empty file a special case
                            out.putArchiveEntry(inPath, null, currEntrySize, linkOption);
                            out.closeArchiveEntry();

                            log.debug(String.format("addPaths() : Adding empty entry: size %d : '%s'",
                                    currEntrySize, inPath));

                            // Record Listing
                            JobArchiveListing l = JobArchiveListing.from(inPath, BackupToolJobStatus.NONE,
                                    BackupToolJobDisposition.NONE, null, null, null, BackupToolFSItemType.GENERIC_FILE);
                            currArchiveListingState.getListing().add(l);
                        }
                        else
                        {
                            // Otherwise treat as a regular, non-empty file

                            bis = new BackupFileCountingInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream(inPath.toFile())));

                            int b;
                            long currChunkSize = 0;
                            boolean createNewEntry = true;

                            if( chunkConfig != null
                                    && chunkConfig.isEnabled() )
                            {
                                currChunkSize = chunkConfig.getSizeBytes();
                            }

                            if( currChunkSize > 0
                                    && currChunkSize < BackupConstants.DEFAULT_MINIMUM_CHUNK_SIZE )
                            {
                                // Chunk was defined but invalid
                                log.warn(String.format("Chunk size of '%d' is invalid. Using '%d' instead",
                                        currChunkSize, BackupConstants.DEFAULT_MINIMUM_CHUNK_SIZE));

                                currChunkSize = BackupConstants.DEFAULT_MINIMUM_CHUNK_SIZE;
                            }

                            while( (b = bis.read()) != -1 )
                            {
                                // Split the archive if necessary
                                if( currChunkSize > 0
                                        && currByteCount >= currChunkSize )
                                {
                                    log.debug( String.format(
                                            "Closing chunk #%d since chunk size is '%d' and the byte count is '%d'.",
                                            currChunkCount++, currChunkSize, currByteCount) );

                                    out.closeArchiveEntry();
                                    out.close();

                                    JobArchiveListingStateWriter.write(currArchiveListingState);

                                    currByteCount = 0;

                                    // New archive file needed
                                    currArchivePath = IBackendFileSystem.nextName(
                                            currArchivePath, archivePathNameRegex, nameComponents, true,
                                            ignoreArchiveSuffix);

                                    // Replace the last archive path if we have to
                                    if( archives.peekLast().getPath() == currArchivePath )
                                    {
                                        archives.removeLast();
                                    }

                                    if( createListingFile )
                                    {
                                        Path currArchiveListingPath
                                                = JobArchiveListingState.getListingName(currArchivePath, false);

                                        currArchiveListingState
                                                = JobArchiveListingState.from(
                                                UUID.randomUUID(), currArchiveListingPath);
                                    }
                                    else
                                    {
                                        log.warn(
                                                String.format(
                                                        "addPaths() : Not allowed to create listing file for new archive '%s'",
                                                        currArchivePath));

                                        currArchiveListingState = null;
                                    }

                                    currArchive = JobArchive.from(null,
                                            currArchiveListingState, currArchivePath, null, true, currByteCount,
                                            null, null, null);

                                    archives.addLast( currArchive );

                                    out = new BackupFileArchiveOutputStream(
                                            new BufferedOutputStream(
                                                    new FileOutputStream(currArchivePath.toFile())),
                                            encryptionType, compressionType);

                                    log.debug(String.format("addPaths() : Archive is now '%s'", currArchivePath));

                                    if( fileSize - bis.getCount() > chunkConfig.getSizeBytes() )
                                    {
                                        currEntrySize = chunkConfig.getSizeBytes();
                                    }
                                    else
                                    {
                                        currEntrySize = fileSize - bis.getCount();
                                    }

                                    // Signal a new entry
                                    createNewEntry = true;
                                }

                                if( createNewEntry )
                                {
                                    out.putArchiveEntry(inPath, null, currEntrySize, linkOption);
                                    createNewEntry = false;

                                    log.debug(String.format("addPaths() : Adding entry: size %d : '%s'",
                                                                currEntrySize, inPath));

                                    // Record Listing
                                    JobArchiveListing l = JobArchiveListing.from(inPath, BackupToolJobStatus.NONE,
                                            BackupToolJobDisposition.NONE, null, null, null, BackupToolFSItemType.GENERIC_FILE);
                                    currArchiveListingState.getListing().add(l);
                                }

                                out.write(b);
                                currByteCount++;
                            }

                            // Close the final entry
                            out.closeArchiveEntry();
                            JobArchiveListingStateWriter.write(currArchiveListingState);

                            log.debug(String.format("addPaths() : Closed final entry from file '%s'", inPath));
                        }
                    }
                    finally
                    {
                        if( bis != null )
                        {
                            bis.close();
                        }
                    }
                }
                catch( IOException ex )
                {
                    String currErr
                            = String.format("Error adding archive for '%s'.",
                            inPath);

                    if( job != null )
                    {
                        job.getErrors().add(
                                JobError.from(job.getJobId(),
                                        UUID.randomUUID(),
                                        BackupToolJobDisposition.ERROR,
                                        -1,
                                        currArchive != null ? currArchive.getPath() : null,
                                        null,
                                        inPath,
                                        currErr,
                                        currErr,
                                        Instant.now(),
                                        ex));
                    }

                    log.error(currErr, ex);
                    throw new BackupToolException(currErr, ex);
                }
            }
        }
        finally
        {
            if( out != null )
            {
                // NB : Trying to close the archive before writing out the promised data
                //      is more trouble than its worth
                //     out.closeArchiveEntry();
                try
                {
                    out.close();
                    JobArchiveListingStateWriter.write(currArchiveListingState);
                }
                catch( IOException ex)
                {
                    String currErr
                            = String.format("Error closing archive '%s'.",
                            currArchivePath);

                    log.error(currErr, ex);
                    throw new BackupToolException(currErr, ex);
                }
            }
        }

        res = new BackupToolResult(BackupToolResultStatus.SUCCESS);

        return res;
    }
}
