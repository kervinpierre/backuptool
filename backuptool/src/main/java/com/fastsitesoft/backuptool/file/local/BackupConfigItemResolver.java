/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.fastsitesoft.backuptool.file.local;

import com.fastsitesoft.backuptool.config.entities.BackupConfigFile;
import com.fastsitesoft.backuptool.config.entities.BackupConfigDirectory;
import com.fastsitesoft.backuptool.config.entities.BackupConfigItem;
import com.fastsitesoft.backuptool.config.entities.JobError;
import com.fastsitesoft.backuptool.config.entities.JobState;
import com.fastsitesoft.backuptool.constants.BackupConstants;
import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatus;
import com.fastsitesoft.backuptool.enums.BackupToolResultStatusDetail;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.utils.BackupItemInvalidException;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.fastsitesoft.backuptool.utils.BackupToolResult;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Walk the local File-System and match files based on patterns
 * 
 * @author kervin
 */
public class BackupConfigItemResolver 
                    implements Callable<BackupToolResult>
{
    private static final Logger log 
                                = LogManager.getLogger(BackupConfigItemResolver.class);
    
    private final boolean findTargets;
    private final List<BackupConfigDirectory> folderItems;
    private final List<BackupConfigFile> fileItems;
    
    private final Map<Path,Map<Path,BackupConfigDirectory>> foundFolders;
    private final Map<Path,Map<Path,BackupConfigFile>> foundFiles;

    private final Path cwd;
    
    private final Map<BackupConfigDirectory, Path> baseFolderPaths;
    private final Map<BackupConfigFile, Path> baseFilePaths;
    
    private final Map<BackupConfigDirectory, Path> callFolderPaths;
    private final Map<BackupConfigFile, Path> callFilePaths;
    
    private final Set<FileVisitOption> visitorOptions;

    private final JobState job;
    
    /**
     * Only find the backup target files and folders.  Do not return their children.
     * 
     * @return 
     */
    public boolean isFindTargets()
    {
        return findTargets;
    }

    public List<BackupConfigFile> getFileItems()
    {
        return fileItems;
    }

    /**
     * Returns a Map of the folders found during the last files-system walk.
     *
     * The key of the Map is the base path of the walk.  The value on that key
     * will be the Map of all Paths along with the configuration that caused
     * them to be included.
     *
     * @return A Map of base paths along with their matched file-system paths.
     */
    public Map<Path, Map<Path, BackupConfigDirectory>> getFoundFolders()
    {
        return foundFolders;
    }

    public Map<Path, Map<Path, BackupConfigFile>> getFoundFiles()
    {
        return foundFiles;
    }

    public Path getCwd()
    {
        return cwd;
    }

    public JobState getJob()
    {
        return job;
    }

    /**
     * Construct a new finder object.
     * 
     * @param cwd The current working directory in case we have relative paths to resolve.
     * @param findTargets Find only targets, and not their children
     */
    private BackupConfigItemResolver(
                final JobState job,
                final List<BackupConfigFile> fileItems,
                final List<BackupConfigDirectory> folderItems,
                final Path cwd,
                final boolean findTargets )
    {
        this.job = job;

        this.findTargets = findTargets;
        this.fileItems       = fileItems;
        this.folderItems = folderItems;
        
        if( cwd == null )
        {
            this.cwd    = Paths.get("").toAbsolutePath();
        }
        else
        {
            this.cwd    = cwd;
        }

        this.baseFolderPaths  = new HashMap<>();
        this.baseFilePaths  = new HashMap<>();
        
        this.callFolderPaths  = new ConcurrentHashMap<>();
        this.callFilePaths  = new ConcurrentHashMap<>();
        
        this.visitorOptions = new HashSet<>();
        
        this.visitorOptions.add(FileVisitOption.FOLLOW_LINKS);
        this.foundFolders = new HashMap<>();
        this.foundFiles = new HashMap<>();
    }

    public static BackupConfigItemResolver from(
            final JobState job,
            final List<BackupConfigFile> fileItems,
            final List<BackupConfigDirectory> folderItems,
            final Path cwd,
            final boolean findTargets )
    {
        BackupConfigItemResolver res;

        res = new BackupConfigItemResolver(job, fileItems, folderItems, cwd, findTargets);

        return res;
    }

    /**
     * Get the set of base paths from the items list.
     */
    private void resolveBasePath() throws BackupItemInvalidException
    {
        baseFolderPaths.clear();
        
        Path currPath;
        
        // Resolve for folders
        if( folderItems != null )
        {
            for( BackupConfigDirectory currItem : folderItems )
            {
                if( currItem.getPathType() == FSSBackupItemPathTypeEnum.REGEX )
                {
                    currPath = resolveBasePath(currItem, getCwd());
                }
                else
                {
                    currPath = Paths.get(currItem.getPathString());
                }

                if( baseFolderPaths.containsKey(currItem) )
                {
                    String errMsg = String.format("Duplicate config item '%s'", currItem);

                    throw new BackupItemInvalidException(errMsg);
                }
                else
                {
                    baseFolderPaths.put(currItem, currPath);
                }
            }
        }
        
        // Now for files
        if( fileItems != null )
        {
            for( BackupConfigFile currItem : fileItems )
            {
                if( currItem.getPathType() == FSSBackupItemPathTypeEnum.REGEX )
                {
                    currPath = resolveBasePath(currItem, getCwd());
                }
                else
                {
                    currPath = Paths.get(currItem.getPathString());
                }

                if( baseFilePaths.containsKey(currItem) )
                {
                    String errMsg = String.format("Duplicate config item '%s'", currItem);

                    throw new BackupItemInvalidException(errMsg);
                }
                else
                {
                    baseFilePaths.put(currItem, currPath);
                }
            }
        }
    }
    
    public static Path resolveBasePath(BackupConfigItem item, Path cwdPath) throws BackupItemInvalidException
    {
        Path res;
        
        Path currItemPath = Paths.get(item.getPathString());
        
        res = resolveBasePath(currItemPath, cwdPath);
        
        return res;
    }
    
    public static Path resolveBasePath(Path path ) throws BackupItemInvalidException
    {
        return resolveBasePath(path, Paths.get("").toAbsolutePath());
    }
    
    /**
     * Return the highest existing basepath of a regex path.
     * 
     * @param path A regex absolute path
     * @param cwd The current working directory for resolving paths
     * 
     * @return The Path representing the highest existing base path.
     * @throws com.fastsitesoft.backuptool.utils.BackupItemInvalidException
     */
    public static Path resolveBasePath(Path path, Path cwd) throws BackupItemInvalidException
    {
        Path res = null;
        
        Path absolutePath = path;
        
        if( cwd != null )
            absolutePath = cwd.resolve(path);
        
        // Loop ancestor folders in path, from root folder to base folder

        for (int i = 1; i <= absolutePath.getNameCount(); ++i) 
        {
            Path currPath = absolutePath.subpath(0, i);
            currPath = absolutePath.getRoot().resolve(currPath);
            
            if( Files.notExists(currPath) )
            {
                // Check that the last name part contains regex characters
                //     If it doesn't, then the path is simply invalid
                //     If it does, then that's our regex to evaluate
                Path currPathName = absolutePath.getName(i-1);
                if( StringUtils.containsNone(currPathName.toString(), BackupConstants.REGEX_SPECIAL_CHARS) )
                {
                    String errMsg = String.format(
                            "resolveBasePath() failed for '%s' in '%s'\nAncestor folder in the path does not exist.", 
                            path, cwd);
                    
                    log.debug(errMsg);
                    
                    throw new BackupItemInvalidException(errMsg);
                }
                else
                {
                    // Might a regex...
                    // Or a folder with a "-" in the name, which is common
                    break;
                }
            }
            
            res = currPath.toAbsolutePath();
        }
        
        return res;
    }

    @Override
    public BackupToolResult call() throws Exception
    {
        BackupToolResult res = null;
        
        resolveBasePath();

        res = deduplicateConfigs(baseFolderPaths, baseFilePaths, callFolderPaths, callFilePaths);

        // A set for temporarily storing all unique base file and folder paths.


        res = deduplicateFolders(callFolderPaths, callFilePaths);

        try
        {
            res = walkFileTree(callFolderPaths, callFilePaths, findTargets, foundFolders, foundFiles);
        }
        catch( FileSystemLoopException ex )
        {

            String errMsg = String.format("Error walking file-system.  Loop detected");

            // A file-system loop was detected
            res = new BackupToolResult(BackupToolResultStatus.FAIL,
                    BackupToolResultStatusDetail.FILESYSTEMLOOPDETECTED,
                    errMsg, ex);
        }

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }

    /**
     * Make sure folders across "file" and "folder" paths do not share a common ancestor.
     * If they do, swap out these descendant paths with their ancestor.
     *
     * TODO : deduplicateFolders() needs lots of unit testing.
     */
    protected static BackupToolResult deduplicateFolders(Map<BackupConfigDirectory, Path> callFldrs,
                                                  Map<BackupConfigFile, Path> callFiles)
    {
        BackupToolResult res = null;

        Set<Path> totalFolders = new HashSet<>();
        totalFolders.addAll(callFldrs.values());
        totalFolders.addAll(callFiles.values());

        for( Path currPath : totalFolders )
        {
            for( Path innerPath : totalFolders )
            {
                if( currPath.equals(innerPath) )
                {
                    continue;
                }

                if( currPath.startsWith( innerPath ) )
                {
                    for( BackupConfigDirectory fldr : callFldrs.keySet() )
                    {
                        if( callFldrs.get(fldr).equals(currPath) )
                        {
                            // This path needs to be swapped out
                            callFldrs.put(fldr, innerPath);
                        }
                    }
                    for( BackupConfigFile file : callFiles.keySet() )
                    {
                        if( callFiles.get(file).equals(currPath) )
                        {
                            // This path needs to be swapped out
                            callFiles.put(file, innerPath);
                        }
                    }
                }
                else if( innerPath.startsWith(currPath) )
                {
                    for( BackupConfigDirectory fldr : callFldrs.keySet() )
                    {
                        if( callFldrs.get(fldr).equals(innerPath) )
                        {
                            // This path needs to be swapped out
                            callFldrs.put(fldr, currPath);
                        }
                    }
                    for( BackupConfigFile file : callFiles.keySet() )
                    {
                        if( callFiles.get(file).equals(innerPath) )
                        {
                            // This path needs to be swapped out
                            callFiles.put(file, currPath);
                        }
                    }
                }
            }
        }

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }

    /**
     * Make sure we don't have redundant base folders.
     *
     * That is, we need to make sure no 2 folders in callPaths is an
     * ancestor of the other.
     *
     * If they are, then the descendant should be removed and replaced with
     * with the ancestor in the Map.  That way we don't search the same
     * ancestor twice for different items.
     *
     * TODO : Confirm the following method does that.
     */
    protected BackupToolResult deduplicateConfigs( Map<BackupConfigDirectory, Path> baseFlders,
                                                   Map<BackupConfigFile, Path> baseFiles,
                                                   Map<BackupConfigDirectory, Path> callFldrs,
                                                  Map<BackupConfigFile, Path> callFile)
    {
        BackupToolResult res = null;

        for( BackupConfigDirectory currBaseFolder : baseFlders.keySet() )
        {
            Path currBasePath = baseFlders.get(currBaseFolder);
            boolean found = false;

            for( BackupConfigDirectory currFolder : callFldrs.keySet() )
            {
                if( currBaseFolder.equals(currFolder ) )
                {
                    // Config item already added
                    found = true;
                    continue;
                }

                Path currPath = callFldrs.get(currFolder);

                if( currPath.equals(currBasePath) )
                {
                    continue;
                }
                else if( currPath.startsWith( currBasePath ) )
                {
                    // Path should be replaced with ancestor
                    callFldrs.replace(currFolder, currBasePath);
                }
                else if( currBasePath.startsWith(currPath) )
                {
                    // Use the ancestor path moving on
                    currBasePath = currPath;
                }
            }

            if( !found )
            {
                // Add the item to the new map
                callFldrs.put(currBaseFolder, currBasePath);
            }
        }

        // Do the same for the base file configurations.
        for( BackupConfigFile currBaseFile : baseFiles.keySet() )
        {
            boolean found = false;
            Path currBasePath = baseFiles.get(currBaseFile);

            // We need a folder, if we have a literal file path.
            // Regexes have already been normalized to a folder_path+regex
            // hopefully if the regex was in the file name
            //
            // FIXME : Handle regexes NOT in the filename
            if( currBaseFile.getPathType() != FSSBackupItemPathTypeEnum.REGEX )
            {
                currBasePath = currBasePath.getParent();
            }

            for( BackupConfigFile currFile : callFile.keySet() )
            {
                if( currBaseFile.equals(currFile ) )
                {
                    // Config item already added
                    found = true;
                    continue;
                }

                Path currPath = callFile.get(currFile);

                if( currPath.equals(currBasePath) )
                {
                    continue;
                }
                else if( currPath.startsWith( currBasePath ) )
                {
                    // Path should be replaced with ancestor
                    callFile.replace(currFile, currBasePath);
                }
                else if( currBasePath.startsWith(currPath) )
                {
                    // Use the ancestor path moving on
                    currBasePath = currPath;
                }
            }

            if( !found )
            {
                // Add the item to the new map
                callFile.put(currBaseFile, currBasePath);
            }
        }

        return res;
    }

    protected BackupToolResult walkFileTree(Map<BackupConfigDirectory, Path> callFldr,
                                            Map<BackupConfigFile, Path> callFile,
                                            boolean ft,
                                            Map<Path,Map<Path,BackupConfigDirectory>> foundFldr,
                                            Map<Path,Map<Path,BackupConfigFile>> foundFile) throws IOException, BackupToolException
    {
        BackupToolResult res = null;

        Set<Path> totalFolders = new HashSet<>();
        totalFolders.addAll(callFldr.values());
        totalFolders.addAll(callFile.values());

        /**
         * TODO : Unit-test use-case where totalFolders is greater than 1
         */

        // Loop all the unique base folders, processing any files and folders
        // related to that path
        for( Path currPath : totalFolders )
        {
            log.debug(String.format("Starting walk on base folder '%s'", currPath));

            List<BackupConfigDirectory> currFolder = new ArrayList<>();
            List<BackupConfigFile> currFile = new ArrayList<>();

            // Find all the items that key to the current path
            for( BackupConfigDirectory i : callFldr.keySet() )
            {
                if( callFldr.get(i).equals(currPath) )
                {
                    currFolder.add(i);
                }
            }

            // Find all the items that key to the current path
            for( BackupConfigFile i : callFile.keySet() )
            {
                if( callFile.get(i).equals(currPath) )
                {
                    currFile.add(i);
                }
            }

            res = walkFileTree(currFolder, currFile, currPath, ft, foundFldr, foundFile);
        }

        return res;
    }

    protected BackupToolResult walkFileTree(List<BackupConfigDirectory> currFolder,
                                            List<BackupConfigFile> currFile,
                                            Path currPath,
                                            boolean ft,
                                            Map<Path,Map<Path,BackupConfigDirectory>> foundFldr,
                                            Map<Path,Map<Path,BackupConfigFile>> foundFile)
            throws IOException, BackupToolException
    {
        if( currFile == null )
        {
            String errMsg
                    = String.format("Missing file array argument for '%s'", currPath);

            throw new BackupToolException(errMsg);
        }

        if( currFolder == null )
        {
            String errMsg
                    = String.format("Missing folder array argument for '%s'", currPath);

            throw new BackupToolException(errMsg);
        }

        if( log.isDebugEnabled() )
        {
            StringBuilder sb = new StringBuilder();

            sb.append("\nDirs...\n");
            for( BackupConfigDirectory dir : currFolder )
            {
                sb.append(String.format("'%s'\n", dir.getPathString()));
            }

            sb.append("\nFiles...\n");
            for( BackupConfigFile file : currFile )
            {
                sb.append(String.format("'%s'\n", file.getPathString()));
            }

            log.debug(sb.toString());
        }

        BackupToolResult res = null;

        UUID currJobId = new UUID(0,0);
        if( getJob() == null )
        {
            log.warn("getJob() returned null.");
        }
        else
        {
            currJobId = getJob().getJobId();
        }

        // Walk the current path with the corresponding item keys
        BackupConfigItemResolverWalker walker
                = BackupConfigItemResolverWalker.from(
                            currFolder,
                            currFile,
                            ft,
                            currPath,
                            null,
                            Integer.MAX_VALUE,
                            null,
                            currJobId );

        try
        {
            walker.walkFileTree();
        }
        catch( Exception ex )
        {
            if( getJob() != null )
            {
                JobState.mergeErrors(getJob(), walker.getErrors());
            }

            throw ex;
        }

        foundFldr.put(currPath, walker.getFoundFolderPaths());
        foundFile.put(currPath, walker.getFoundFilePaths());

        if( res == null )
        {
            res = new BackupToolResult(BackupToolResultStatus.SUCCESS);
        }

        return res;
    }
}
