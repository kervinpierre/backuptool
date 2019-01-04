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
import com.fastsitesoft.backuptool.enums.FSSBackupItemPathTypeEnum;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Build the correct PathMatcher from a supplied Backup Configuration Item.
 * 
 * @author kervin
 */
public class BackupConfigItemResolverMatcher
{
    private static final Logger log 
                                = LogManager.getLogger(BackupConfigItemResolverMatcher.class);
    
    public static PathMatcher getMatcher( BackupConfigItem item ) throws BackupToolException
    {
        PathMatcher res = null;
        
        if( item.getPathType() == FSSBackupItemPathTypeEnum.REGEX )
        {
            String currSyntax = String.format("regex:%s", item.getPathString());

            try
            {
                res = FileSystems.getDefault().getPathMatcher(currSyntax);
            }
            catch( PatternSyntaxException ex )
            {
                String errMsg = String.format(
                        "Invalid regular expression '%s'.  E.g. '*.log' is an invalid regular expression "
                                + "but '.*\\.log' is valid.", currSyntax);

                log.debug(errMsg, ex);
                throw new BackupToolException(errMsg, ex);
            }
        }
                
        return res;
    }

    /**
     * Match a path with an optional parent.
     *
     * The parent path is the ancestor used in relative matches, such as with children matchers.
     *
     * @param matcher
     * @param path
     * @param parent
     * @return
     */
    public static boolean matches( PathMatcher matcher, Path path, Path parent )
    {
        boolean res;

        Path currPath = path;

        if( parent != null )
        {
            currPath = parent.relativize(path);
        }

        res = matcher.matches(currPath);

        return res;
    }

    public static boolean matches( PathMatcher matcher, Path path )
    {
        boolean res;
        Path parent = null;

        res = matches(matcher, path, null);

        return res;
    }

    public static Map<BackupConfigDirectory, PathMatcher> getFolderMatcher( List<BackupConfigDirectory> item ) throws BackupToolException
    {
        Map<BackupConfigDirectory, PathMatcher> res = new HashMap<>();
        
        for( BackupConfigDirectory i : item )
        {
            res.put(i, getMatcher(i) );
        }
        
        return res;
    }
    
    public static Map<BackupConfigFile, PathMatcher> getFileMatcher( List<BackupConfigFile> item ) throws BackupToolException
    {
        Map<BackupConfigFile, PathMatcher> res = new HashMap<>();
        
        for( BackupConfigFile i : item )
        {
            res.put(i, getMatcher(i) );
        }
        
        return res;
    }
}
