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
package com.fastsitesoft.backuptool.file.local;

import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigItem;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.FileTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * File-system items to be backed up.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupItem
{
    private static final Logger log 
                                = LogManager.getLogger(BackupItem.class);
    
    private final Path path;
    private final byte [] digest;
    private final FileTime lastModified;

    /**
     * Long representing the last time the file was modified on the File-system, when we read it last.
     * 
     * @return 
     */
    public FileTime getLastModified()
    {
        return lastModified;
    }

    /**
     * Digest for path when we read it last.
     * @return
     */
    public byte[] getDigest()
    {
        return digest;
    }

    public Path getPath()
    {
        return path;
    }

    /**
     * Private constructor.  Please use the from() method instead.
     * 
     * @param path 
     */
    private BackupItem(final Path path, final byte[] digest, final FileTime lastModified)
    {
        this.path = path;
        this.digest = digest;
        this.lastModified = lastModified;
    }

    public static BackupItem from(final Path path, final byte[] digest, final FileTime lastModified)
    {
        BackupItem res;

        res = new BackupItem(path, digest, lastModified);

        return res;
    }

    public static BackupItem from(final Path path, final byte[] digest) throws BackupToolException
    {

        FileTime currTime;

        try
        {
            currTime = Files.getLastModifiedTime(path);
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error getting last modified time from '%s'", path);

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        BackupItem res = new BackupItem(path, digest, currTime);

        return res;
    }

    /**
     * Create item, do not deal with digest.
     *
     * @param path
     * @return
     * @throws BackupToolException
     */
    public static BackupItem from(final Path path) throws BackupToolException
    {
        BackupItem res = from(path, null);

        return res;
    }

    /**
     * Merge the found directories and files into a single collection.
     *
     * @param foundDirs
     * @param foundFiles
     * @return
     * @throws BackupToolException
     */
    public static Map<Path, Deque<BackupItem>> mergeFoundItems(
                    final Map<Path, Map<Path, BackupConfigDirectory>> foundDirs,
                    final Map<Path, Map<Path, BackupConfigFile>> foundFiles )
                                                    throws BackupToolException
    {
        Map<Path, Deque<BackupItem>> res = new HashMap<>();

        for( Path currRootPath : foundDirs.keySet() )
        {
            Map<Path, BackupConfigDirectory> foundPath = foundDirs.get(currRootPath);
            Deque<BackupItem> currPaths = new ArrayDeque<>();
            for( Path currPath : foundPath.keySet() )
            {
                currPaths.add(BackupItem.from(currPath));
            }

            res.put( currRootPath, currPaths );
        }

        for( Path currRootPath : foundFiles.keySet() )
        {
            Map<Path, BackupConfigFile> foundPath = foundFiles.get(currRootPath);
            Deque<BackupItem> currPaths;
            if( (null == res.get(currRootPath)) )
            {
                currPaths = new ArrayDeque<>();
            }
            else
            {
                currPaths = res.get(currRootPath);
            }

            for( Path currPath : foundPath.keySet() )
            {
                currPaths.add(BackupItem.from(currPath));
            }

            res.putIfAbsent(currRootPath, currPaths);
        }

        return res;
    }
}
