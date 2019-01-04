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
import com.fastsitesoft.backuptool.enums.BackupConfigItemResolverWalkerAction;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fastsitesoft.backuptool.enums.BackupToolJobDisposition;
import com.fastsitesoft.backuptool.enums.FSSBackupItemBackupOption;
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Walk the local File-System and match files based on patterns
 *
 * @author kervin
 */
public class BackupConfigItemResolverWalker extends SimpleFileVisitor<Path>
{
    private static final Logger log
            = LogManager.getLogger(BackupConfigItemResolverWalker.class);

    private final List<BackupConfigDirectory> baseFolderItems;
    private final List<BackupConfigFile> baseFileItems;
    private final Map<Path, BackupConfigDirectory> foundFolderPaths;
    private final Map<Path, BackupConfigFile> foundFilePaths;
    private final Map<BackupConfigDirectory, PathMatcher> baseFolderMatchers;
    private final Map<BackupConfigFile, PathMatcher> baseFileMatchers;
    private final List<JobError> errors;
    /**
     * A stack of ancestor folder paths.  Along with their configuration item if any
     */
    private final Deque<Pair<Path, BackupConfigDirectory>> ancestorDirStack;
    
    /**
     * Stack of ancestor folder item matchers
     */
    private final Map<BackupConfigDirectory, Map<BackupConfigDirectory, PathMatcher>> childrenFolderMatchers;
    private final Map<BackupConfigDirectory, Map<BackupConfigFile, PathMatcher>> childrenFileMatchers;

    private final boolean findTargets;

    private final int maxDepth;
    private final Path start;

    private final UUID jobId;

    /**
     * Stack of default actions
     */
    private final Deque<BackupConfigItemResolverWalkerAction> defaultAction;

    private final Set<FileVisitOption> visitorOptions;

    /**
     * Only find the backup target files and folders. Do not return their
     * children.
     *
     * @return
     */
    public boolean isFindTargets()
    {
        return findTargets;
    }

    public List<JobError> getErrors()
    {
        return errors;
    }

    public Map<Path, BackupConfigDirectory> getFoundFolderPaths()
    {
        return foundFolderPaths;
    }

    public Map<Path, BackupConfigFile> getFoundFilePaths()
    {
        return foundFilePaths;
    }

    /**
     * Construct a new finder object.
     *
     * @param folderItems The base Folder items to search for. Some may be regular expressions
     * which would return multiple actual paths.
     * @param fileItems
     * @param findTargets Find only targets, and not their children
     * @param start
     * @param options
     * @param maxDepth
     */
    private BackupConfigItemResolverWalker(
            final List<BackupConfigDirectory> folderItems,
            final List<BackupConfigFile> fileItems,
                                          final boolean findTargets,
                                          final Path start,
            final Set<FileVisitOption> options,
            final int maxDepth,
            final List<JobError> errors,
            final UUID jobId)
    {
        this.findTargets = findTargets;
        this.baseFileItems = fileItems;
        this.baseFolderItems = folderItems;
        this.jobId = jobId;

        if( errors == null )
        {
            this.errors = new ArrayList<>();
        }
        else
        {
            this.errors = errors;
        }

        this.foundFolderPaths = new HashMap<>();
        this.foundFilePaths = new HashMap<>();
        
        if (options == null)
        {
            this.visitorOptions = new HashSet<>();
            this.visitorOptions.add(FileVisitOption.FOLLOW_LINKS);
        }
        else
        {
            this.visitorOptions = options;
        }

        this.start = start;
        this.maxDepth = maxDepth;
        this.baseFolderMatchers = new HashMap<>();
        this.baseFileMatchers = new HashMap<>();
        this.childrenFileMatchers = new HashMap<>();
        this.childrenFolderMatchers = new HashMap<>();

        /**
         * By default all File-System objects are excluded unless they're
         * matched by a rule.
         */
        this.defaultAction = new ArrayDeque<>();

        // TODO : Build the matchers from the baseItems.

        this.ancestorDirStack = new ArrayDeque<>();
    }

    public static BackupConfigItemResolverWalker from(
            final List<BackupConfigDirectory> folderItems,
            final List<BackupConfigFile> fileItems,
            final boolean findTargets,
            final Path start,
            final Set<FileVisitOption> options,
            final int maxDepth,
            final List<JobError> errors,
            final UUID jobId)
    {
        BackupConfigItemResolverWalker res;

        res = new BackupConfigItemResolverWalker( folderItems,
                fileItems,
                findTargets,
                start, options, maxDepth, errors, jobId);

        return res;
    }

    /**
     * Wrapper for traversing the file system.
     *
     * 1. Folder exclude base matchers with no children have highest precedence.
     *
     * @return start path
     * @throws IOException
     */
    public Path walkFileTree() throws IOException, BackupToolException
    {
        Path res;

        foundFolderPaths.clear();
        foundFilePaths.clear();
        baseFolderMatchers.clear();
        baseFileMatchers.clear();
        
        baseFolderMatchers.putAll(BackupConfigItemResolverMatcher.getFolderMatcher(baseFolderItems));
        baseFileMatchers.putAll(BackupConfigItemResolverMatcher.getFileMatcher(baseFileItems));
        
        this.defaultAction.push(BackupConfigItemResolverWalkerAction.EXCLUDE);

        res = Files.walkFileTree(start, visitorOptions, maxDepth, this);

        if( log.isDebugEnabled() )
        {
            for( Path currPath : foundFolderPaths.keySet() )
            {
                BackupConfigDirectory bcf = foundFolderPaths.get(currPath);
                String bcfStr = bcf==null?"(null)":bcf.getPathString();

                log.debug(String.format("Found Folder : '%s'\n    Matched by '%s'\n",
                        currPath, bcfStr ));
            }

            for( Path currPath : foundFilePaths.keySet() )
            {
                BackupConfigFile bcf = foundFilePaths.get(currPath);
                String bcfStr = bcf==null?"(null)":bcf.getPathString();

                log.debug(String.format("Found File : '%s'\n    Matched by '%s'\n",
                        currPath, bcfStr ));
            }
        }

        return res;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
    {
        FileVisitResult res = null;

        log.debug(String.format("Visit file failed '%s'", file), exc);

        getErrors().add(
                    JobError.from(jobId,
                            UUID.randomUUID(),
                            BackupToolJobDisposition.ERROR,
                            -1,
                            null,
                            null,
                            file,
                            "Visit file failed",
                            null,
                            Instant.now(),
                            exc));

        /**
         * Check that this path doesn't match an exclude matcher.  If it does, then ignore it and continue.
         */

        if( Files.exists(file, LinkOption.NOFOLLOW_LINKS) )
        {
            if( Files.isDirectory(file) || Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS) )
            {
                BackupConfigDirectory currItem = matchFolder(file.toAbsolutePath(), baseFolderMatchers, ancestorDirStack,
                        childrenFolderMatchers, childrenFileMatchers);
                if( currItem != null && currItem.isExclude() )
                {
                    // Ignore excluded directories
                    res = FileVisitResult.CONTINUE;
                    log.debug(String.format("Continue on folder '%s' due to exclude item '%s'", file, currItem));
                }
            }
            else
            {
                BackupConfigFile currItem = matchFile(file.toAbsolutePath(), baseFileMatchers, ancestorDirStack,
                        childrenFolderMatchers, childrenFileMatchers);
                if( currItem != null && currItem.isExclude() )
                {
                    // Ignore excluded files
                    res = FileVisitResult.CONTINUE;
                    log.debug(String.format("Continue on file '%s' due to exclude item '%s'", file, currItem));
                }
            }
        }
        else
        {
            // Did the file get deleted while working?
            log.warn(String.format("File does not exist while walking '%s'. Was it just deleted?", file));
        }

        if( res == null )
        {
            throw exc;
        }

        return res;
    }

    /**
     * Visit a file-system file object.
     *
     * @param file
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        FileVisitResult res = FileVisitResult.CONTINUE;
        Path currAbsolutePath = file.toAbsolutePath();

        // Set the default action with this file to the folder default action
        BackupConfigItemResolverWalkerAction currentAction
                                                = defaultAction.peek();

        log.debug(String.format("Visiting file '%s'\n", file));

        BackupConfigFile item = matchFile(currAbsolutePath,baseFileMatchers,ancestorDirStack,
                childrenFolderMatchers,childrenFileMatchers);

        if (item != null)
        {
            if (item.isExclude())
            {
                currentAction = BackupConfigItemResolverWalkerAction.EXCLUDE;
            }
            else
            {
                currentAction = BackupConfigItemResolverWalkerAction.INCLUDE;
            }
        }

        switch (currentAction)
        {
            case INCLUDE:
                foundFilePaths.put(file, item);
                log.debug(String.format("Adding '%s' to found files for '%s'\n",
                        file, item==null?"null":item.getPathString()));
                break;

            case EXCLUDE:
                break;

            default:
            {
                String errMsg = String.format("Invalid action '%s' for file '%s'",
                        currentAction, file);
                log.debug(errMsg);
                throw new IOException(errMsg);
            }
        }

        return res;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
        FileVisitResult res   = FileVisitResult.CONTINUE;
        Path currAbsolutePath = dir.toAbsolutePath();

        BackupConfigItemResolverWalkerAction currentItemAction
                = BackupConfigItemResolverWalkerAction.NONE;
        BackupConfigItemResolverWalkerAction currentDefaultAction
                = BackupConfigItemResolverWalkerAction.NONE;

        Pair<Path,BackupConfigDirectory> resPair = null;

        log.debug(String.format("Visiting directory '%s'\n    Default action is '%s'\n",
                dir, defaultAction.peek()));

        // Check for base matches then ancestors.
        // This allows base items to overrule ancestors
        
        // Do a base item match.  This gives base matches precedence, which
        // is crucial for overruling inferred rules
        BackupConfigDirectory currItem = matchFolder(currAbsolutePath, baseFolderMatchers, ancestorDirStack,
                childrenFolderMatchers, childrenFileMatchers);

        /**
         * Process the chosen item...
         */
        if( currItem == null )
        {
            log.debug(String.format("No matchers found for '%s'\n",
                    dir));
        }
        else
        {
            if( currItem.isExclude() )
            {
                // Prune this path, if it has no child baseItems.
                // But some excludes have children we should evaluate
                resPair = Pair.of(dir, currItem);

                currentDefaultAction = BackupConfigItemResolverWalkerAction.EXCLUDE;
                currentItemAction = BackupConfigItemResolverWalkerAction.EXCLUDE;

                List<BackupConfigDirectory> currChildFolders = currItem.getChildFolders();
                List<BackupConfigFile> currChildFiles = currItem.getChildFiles();
                if( (currChildFolders == null || currChildFolders.isEmpty() )
                        && (currChildFiles == null || currChildFiles.isEmpty()) )
                {
                    // This is an exclude with no child matchers
                    // We can skip tree here
                    if( currItem.getBackupOptions().contains(FSSBackupItemBackupOption.BREAK_TRAVERSAL) )
                    {
                        // The BREAK_TRAVERSAL option allows us to skip an entire subtree.  This is NOT
                        // on by default because it may cause us to skip over files that would have otherwise
                        // been matched by other base matchers.
                        res = FileVisitResult.SKIP_SUBTREE;
                    }
                }
            }
            else
            {
                // Since we have an item match.  All descendant items are included
                // by default.
                currentDefaultAction = BackupConfigItemResolverWalkerAction.INCLUDE;
                currentItemAction = BackupConfigItemResolverWalkerAction.INCLUDE;

                resPair = Pair.of(dir, currItem);
            }
        }

        if( res == FileVisitResult.CONTINUE )
        {
            /**
             * Include this folder in the found paths list only if we have an
             * "include" status.
             */
            switch( currentItemAction )
            {
                case INCLUDE:
                    foundFolderPaths.put(dir, currItem);
                    log.debug(String.format("Adding '%s' to found folders for '%s'\n",
                            dir, (currItem == null) ? "null" : currItem.getPathString()));
                    break;

                case EXCLUDE:
                    break;

                default:
                {
                    switch( defaultAction.peek() )
                    {
                        case INCLUDE:
                            foundFolderPaths.put(dir, currItem);
                            log.debug(String.format("Default Action : Adding '%s' to found folders for '%s'\n",
                                    dir, currItem == null ? "null" : currItem.getPathString()));
                            break;

                        case EXCLUDE:
                            break;
                    }
                }
            }

            if( resPair == null )
            {
                // We always push the current directory onto the stack
                ancestorDirStack.push(Pair.of(dir, null));
            }
            else
            {
                ancestorDirStack.push(resPair);
            }

            // Keep the default action for descendants
            if( currentDefaultAction == BackupConfigItemResolverWalkerAction.NONE )
            {
                currentDefaultAction = defaultAction.peek();
                defaultAction.push(currentDefaultAction);
            }
            else
            {
                defaultAction.push(currentDefaultAction);
            }
        }

        log.debug(String.format("Leaving preVisitDirectory().\n"
                + "    Top of the Default Action stack is now '%s'. Size '%d'.  Visit Result '%s'",
                defaultAction.peek(), defaultAction.size(), res));

        return res;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
    {
        FileVisitResult res = FileVisitResult.CONTINUE;

        if( exc == null )
        {
            log.debug(String.format("postVisitDirectory() : Completed visiting '%s'\n", dir));
        }
        else
        {
            log.warn(String.format("postVisitDirectory() : Completed visiting '%s' with exception\n", dir));
        }

        BackupConfigItemResolverWalkerAction currAction = defaultAction.pop();

        log.debug(String.format("Top of the Default Action stack is now '%s' after popping '%s'. Size is '%d'\n",
                defaultAction.peek(), currAction, defaultAction.size()));

        // Are we changing hierarchy level?
        if( ancestorDirStack.size() > 0 )
        {
            Pair<Path,BackupConfigDirectory> lastDirPair = ancestorDirStack.peek();
            Path lastDir = lastDirPair.getLeft();

            if( dir.equals(lastDir) )
            {
                // Remove ourselves from the visiting stack
                ancestorDirStack.pop();
            }
            else
            {
                log.warn(String.format("Last visited directory mismatch. Expected '%s', but found '%s'",
                        dir, lastDir));
            }
        }

        return res;
    }

    protected static BackupConfigFile matchFile( final Path file,
                         final Map<BackupConfigFile, PathMatcher> baseFileMatchers,
                         final Deque<Pair<Path, BackupConfigDirectory>> ancestorDirStack,
                         final Map<BackupConfigDirectory, Map<BackupConfigDirectory, PathMatcher>> childrenFolderMatchers,
                         final Map<BackupConfigDirectory, Map<BackupConfigFile, PathMatcher>> childrenFileMatchers ) throws IOException
    {
        BackupConfigFile res;

        res = matchBaseFileItem(file, baseFileMatchers);
        if (res == null)
        {
            // Can we match ancestor matchers?
            try
            {
                res = (BackupConfigFile) matchAncestorItem(BackupConfigFile.class, file, ancestorDirStack,
                        childrenFolderMatchers, childrenFileMatchers);
            }
            catch( BackupToolException ex )
            {
                String errMsg = String.format("Call matchAncestorItem() failed for file '%s'", file);

                log.debug(errMsg, ex);

                // This is all the API allows us to throw
                throw new IOException(errMsg, ex);
            }
        }

        return res;
    }

    /**
     * Check for base matches then ancestors.  This allows base items to overrule ancestors
     *
     * Do a base item match.  This gives base matches precedence, which is crucial for overruling inferred rules
     * @param dir The directory path to be matched.
     * @param baseFolderMatchers
     * @param ancestorDirStack
     * @param childrenFolderMatchers
     * @param childrenFileMatchers
     * @return The first Folder Configuration Item that matches the supplied directory.
     * @throws IOException
     */
    protected static BackupConfigDirectory matchFolder(final Path dir,
                              final Map<BackupConfigDirectory, PathMatcher> baseFolderMatchers,
                              final Deque<Pair<Path, BackupConfigDirectory>> ancestorDirStack,
                              final Map<BackupConfigDirectory, Map<BackupConfigDirectory, PathMatcher>> childrenFolderMatchers,
                              final Map<BackupConfigDirectory, Map<BackupConfigFile, PathMatcher>> childrenFileMatchers) throws IOException
    {
        BackupConfigDirectory res;
        BackupConfigDirectory baseItem;
        BackupConfigDirectory ancestorItem;

        try
        {
            baseItem = matchBaseFolderItem(dir, baseFolderMatchers);
        }
        catch( BackupToolException ex )
        {
            String errMsg = String.format("Call matchBaseFolderItem() failed for directory '%s'", dir);

            log.debug(errMsg, ex);

            // This is all the API allows us to throw
            throw new IOException(errMsg, ex);
        }

        res = baseItem;

        if(baseItem == null)
        {
            // We do have ancestor item matchers.
            try
            {
                ancestorItem = (BackupConfigDirectory)matchAncestorItem(BackupConfigDirectory.class, dir,
                        ancestorDirStack,
                        childrenFolderMatchers, childrenFileMatchers);
            }
            catch( BackupToolException ex )
            {
                String errMsg = String.format("Call matchAncestorItem() failed for directory '%s'", dir);

                log.debug(errMsg, ex);

                // This is all the API allows us to throw
                throw new IOException(errMsg, ex);
            }

            if( ancestorItem != null )
            {
                // Ancestor matched
                res = ancestorItem;
            }
        }
        else
        {
            // Base item match was found
            res = baseItem;
        }

        return res;
    }

    /**
     * Return the first Backup Config Directory item that matches this path.  Exclude matchers have precedence.
     *
     * Base matcher item regexes should only batch the base of the directories to be backed up.  And not its
     * decedents.
     *
     * @param p
     * @return
     */
    protected static BackupConfigDirectory matchBaseFolderItem(Path p,
                                                            Map<BackupConfigDirectory, PathMatcher> baseFolderMatchers) throws BackupToolException
    {
        BackupConfigDirectory res = null;

        log.debug(String.format("matchBaseFolderItem() for '%s'\n", p));

        for (BackupConfigDirectory i : baseFolderMatchers.keySet())
        {
            PathMatcher currPM = baseFolderMatchers.get(i);

            if( currPM == null )
            {
                // We may have a literal folder configuration
                if( i.getPathType() != FSSBackupItemPathTypeEnum.REGEX )
                {
                    // In this case, we simply compare the literal paths
                    Path tempPath = Paths.get(i.getPathString());
                    if (p.equals(tempPath))
                    {
                        if (i.isExclude())
                        {
                            // No need to continue searching
                            res = i;

                            break;
                        }
                        else if (res == null)
                        {
                            // Keep searching because excludes have precedence over
                            // includes.
                            res = i;
                        }
                    }
                }
            }
            else if (currPM.matches(p))
            {

                if (i.isExclude())
                {
                    // No need to continue searching
                    res = i;

                    break;
                }
                else if (res == null)
                {
                    // Keep searching because excludes have precedence over
                    // includes.
                    res = i;
                }
            }
        }

        log.debug( String.format("matchBaseFolderItem() for '%s' returning '%s'\n",
                                            p, res==null?"null":res.getPathString()) );

        return res;
    }

    protected static BackupConfigFile matchBaseFileItem(Path p, Map<BackupConfigFile, PathMatcher> baseFileMatchers)
    {
        BackupConfigFile res = null;

        log.debug(String.format("matchBaseFileItem() for '%s'\n", p));

        for (BackupConfigFile i : baseFileMatchers.keySet())
        {
            PathMatcher currPM = baseFileMatchers.get(i);

            if( currPM == null )
            {
                // We may have a literal file configuration
                if( i.getPathType() != FSSBackupItemPathTypeEnum.REGEX )
                {
                    // In this case, we simply compare the literal paths
                    Path tempPath = Paths.get(i.getPathString());
                    if (p.equals(tempPath))
                    {
                        if (i.isExclude())
                        {
                            // No need to continue searching
                            res = i;

                            break;
                        }
                        else if (res == null)
                        {
                            // Keep searching because excludes have precendence over
                            // includes.
                            res = i;
                        }
                    }
                }
            }
            else
            {
                if( currPM.matches(p))
                {
                    if( i.isExclude() )
                    {
                        // No need to continue searching
                        res = i;

                        break;
                    }
                    else if( res == null )
                    {
                        // Keep searching because excludes have precendence over
                        // includes.
                        res = i;
                    }
                }
            }
        }

        log.debug( String.format("matchBaseFileItem() for '%s' returning '%s'\n",
                p, res==null?"null":res.getPathString()) );

        return res;
    }
    
    /**
     * Return the first child of the configuration items in this item's ancestry that matches.
     *
     * @param clazz
     * @param p
     * @param ancestorDirStack
     * @param childrenFolderMatchers
     * @param childrenFileMatchers
     * @return First child item that matches.  Base matchers are not tested.  And there is no exclude
     * precedence in the hierarchy, but there is among sibling children matchers.
     * @throws BackupToolException
     */
    protected static BackupConfigItem matchAncestorItem(final Class clazz, final Path p,
                         final Deque<Pair<Path, BackupConfigDirectory>> ancestorDirStack,
                         final Map<BackupConfigDirectory, Map<BackupConfigDirectory, PathMatcher>> childrenFolderMatchers,
                         final Map<BackupConfigDirectory, Map<BackupConfigFile, PathMatcher>> childrenFileMatchers) throws BackupToolException
    {
        BackupConfigItem res = null;

        log.debug(String.format("matchAncestorItem() for '%s' of class '%s'\n", p, clazz));

        for (Iterator<Pair<Path,BackupConfigDirectory>> i = ancestorDirStack.descendingIterator();
                i.hasNext();)
        {
            // 1. Check for 'i' in base matchers array

            // 2. If it isn't in there build then add it
            // 3. Match on i
            // 4. Stop only on an exclude match or the end of the list
            // 5. Do the same with child matchers on each level
            Pair<Path, BackupConfigDirectory> currPair = i.next();
            BackupConfigDirectory currItem = currPair.getRight();
            Path itemPath = currPair.getLeft();

            if( currItem == null )
            {
                // We have an ancestor, but it does not match any matchers.
                // E.g. A base directory, etc.
                continue;
            }

            // We don't try to match with the ancestor's base match.
            //
            // Just the relative children matches.  The base match has already
            // matched an ancestor, so what's the point?

            // Match the child item's children
            Path relPath = itemPath.relativize(p);
            BackupConfigItem childRes
                    = matchChildrenItem(clazz,relPath,currItem,childrenFolderMatchers,childrenFileMatchers);

            if( childRes != null )
            {
                // No 'exclude' precedence here.  The first match is returned.
                res = childRes;
                break;
            }
        }

        log.debug( String.format("matchAncestorItem() for '%s' returning '%s'\n",
                p, res==null?"null":res.getPathString()) );

        return res;
    }

    /**
     * Match the children on a patcher match item.
     *
     * The first matching item is returned unless there is an 'exclude' match in the list.  Then the exclude match has precedence.
     *
     * @param clazz The type of item we're trying to match.  This can be files, folders or <code>null</code> for both.
     * @param p Path to match with.  This path should always be relative as we're dealing with a child matcher. E.g. using <code>Path.relativize()</code>
     * @param item The Folder Configuration Item that holds the children matchers.
     * @param childrenFolderMatchers A cache of child folder matchers
     * @param childrenFileMatchers A cache of child file matchers.
     * @return The matching Configuration Item.  Or <code>null</code> if none is found.
     * @throws BackupToolException
     */
    protected static BackupConfigItem matchChildrenItem(final Class clazz, final Path p,
                           final BackupConfigDirectory item,
                           final Map<BackupConfigDirectory, Map<BackupConfigDirectory, PathMatcher>> childrenFolderMatchers,
                           final Map<BackupConfigDirectory, Map<BackupConfigFile, PathMatcher>> childrenFileMatchers)
            throws BackupToolException
    {
        BackupConfigItem res = null;

        // We only match relative links with child matchers.
        if( p == null || p.isAbsolute() )
        {
            String errMsg = String.format("Invalid path for child match '%s'.  Path should be relative and not null.", p);

            log.debug(errMsg);

            throw new BackupToolException(errMsg);
        }

        // Start with trying to match the file configurations.
        // If a class isn't specified, then both types are tried.
        if( clazz == null || clazz == BackupConfigFile.class )
        {
            List<BackupConfigFile> currChildFiles = item.getChildFiles();
            if (currChildFiles != null && currChildFiles.size() > 0)
            {
                Map<BackupConfigFile, PathMatcher> currChildenFileMatchers
                        = childrenFileMatchers.get(item);
                if (currChildenFileMatchers == null)
                {
                    // This child item doesn't have a Path Matchers in cache yet
                    currChildenFileMatchers
                            = BackupConfigItemResolverMatcher.getFileMatcher(currChildFiles);

                    // Cache the children matchers
                    childrenFileMatchers.put(item, currChildenFileMatchers);
                }

                // Match against all children matchers
                for(BackupConfigFile currChildItem : currChildenFileMatchers.keySet())
                {
                    if( currChildItem.getPathType() == FSSBackupItemPathTypeEnum.REGEX )
                    {
                        PathMatcher currChildMatcher = currChildenFileMatchers.get(currChildItem);

                        // Match with a relative matcher to the base matcher which is our ancestor
                        if( BackupConfigItemResolverMatcher.matches(currChildMatcher, p ) )
                        {

                            if (currChildItem.isExclude())
                            {
                                // No need to continue searching
                                res = currChildItem;
                                break;
                            }
                            else if (res == null)
                            {
                                // Keep searching because excludes have precendence over
                                // includes.
                                res = currChildItem;
                            }
                        }
                    }
                    else
                    {
                        // In this case, we simply compare the literal paths
                        Path tempPath = Paths.get(item.getPathString());
                        if (p.equals(tempPath))
                        {
                            if (item.isExclude())
                            {
                                // No need to continue searching
                                res = item;
                                break;
                            }
                            else if (res == null)
                            {
                                // Keep searching because excludes have precendence over
                                // includes.
                                res = item;
                            }
                        }
                    }
                }

            }
        }

        // Do the same above, but for the folder class
        if( res == null && (clazz == null || clazz == BackupConfigDirectory.class) )
        {
            List<BackupConfigDirectory> currChildren = item.getChildFolders();
            if (currChildren != null && currChildren.size() > 0)
            {
                Map<BackupConfigDirectory, PathMatcher> currChildenFolderMatchers
                        = childrenFolderMatchers.get(item);
                if (currChildenFolderMatchers == null)
                {
                    // This child item doesn't have a Path Matchers in cache yet
                    currChildenFolderMatchers
                            = BackupConfigItemResolverMatcher.getFolderMatcher(currChildren);

                    // Cache the children matchers
                    childrenFolderMatchers.put(item, currChildenFolderMatchers);
                }

                // Match against all children matchers
                for (BackupConfigDirectory currChildItem : currChildenFolderMatchers.keySet())
                {
                    if( currChildItem.getPathType() == FSSBackupItemPathTypeEnum.REGEX )
                    {
                        PathMatcher currChildMatcher = currChildenFolderMatchers.get(currChildItem);

                        if( BackupConfigItemResolverMatcher.matches(currChildMatcher, p ) )
                        {

                            if( currChildItem.isExclude() )
                            {
                                // No need to continue searching
                                res = currChildItem;
                                break;
                            }
                            else if( res == null )
                            {
                                // Keep searching because excludes have precendence over
                                // includes.
                                res = currChildItem;
                            }
                        }
                    }
                    else
                    {
                        // In this case, we simply compare the literal paths
                        Path tempPath = Paths.get(item.getPathString());
                        if (p.equals(tempPath))
                        {
                            if (item.isExclude())
                            {
                                // No need to continue searching
                                res = item;
                                break;
                            }
                            else if (res == null)
                            {
                                // Keep searching because excludes have precendence over
                                // includes.
                                res = item;
                            }
                        }
                    }
                }

            }
        }

        return res;
    }
}
