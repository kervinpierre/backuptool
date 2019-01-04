/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2014] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */

package com.fastsitesoft.backuptool.backend;

import com.fastsitesoft.backuptool.enums.BackupToolNameComponentType;
import com.fastsitesoft.backuptool.enums.StorageBackendSearchType;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main interface for accessing Backend File-Systems.
 * 
 * Examples of files that the backup application needs includes...
 * 
 * /{path}/{setName}/{timestamp}/
 * /{path}/{setName/{stateFiles}/
 * 
 * @author Kervin Pierre <info@sludev.com>
 * 
 */
public interface IBackendFileSystem
{
    static final Logger log
            = LogManager.getLogger(IBackendFileSystem.class);

    /**
     * Create a directory on the backend.  Maybe ignored if the backend does not
     * have the concept of directories or folders.
     * 
     * @param folder
     * @throws BackupToolException 
     */
    public void createFolder( IBackupFilePart folder ) throws BackupToolException;
    
    /**
     * Creates a file in a backend and returns the BackupFilePart for that new file.
     * 
     * @param file
     * @throws BackupToolException 
     */
    public void createFile( IBackupFilePart file ) throws BackupToolException;
    
    /**
     * Copy a file from one location to another.
     * 
     * @param src
     * @param dst 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public void copy( IBackupFilePart src, IBackupFilePart dst ) throws BackupToolException;
    
    /**
     * Permanently delete a backup file part.
     * 
     * @param file File Part to be deleted.
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    public boolean delete( IBackupFilePart file ) throws BackupToolException;
    
    public int deleteFolder(IBackupFilePart folder, boolean recurse) throws BackupToolException;
    
    /**
     * Check if a file exists
     * 
     * @param file 
     * @return  
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public boolean exists( IBackupFilePart file ) throws BackupToolException;
    
    /**
     * Return the size of the file in bytes.
     * 
     * @param file 
     * @return  The size of the file in bytes.
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    public long getContentSize( IBackupFilePart file ) throws BackupToolException;
    
    public long getLastModifiedDate(IBackupFilePart file) throws BackupToolException;
    
    /**
     * List all the children in a specified folder.
     * 
     * @param folder Folder to list.
     * @return  A list of children.
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    public List<IBackupFilePart> getChildren( IBackupFilePart folder ) throws BackupToolException;
    
    /**
     * Search a folder using specified search criteria.
     * 
     * @param folder
     * @param type
     * @return
     * @throws BackupToolException 
     */
    public List<IBackupFilePart> findFiles( IBackupFilePart folder, 
                                            StorageBackendSearchType type )
                                                    throws BackupToolException;
    /**
     * Find out of a file exists, and return an object.
     * 
     * @param file
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public IBackupFilePart resolveLocalFile( Path file ) throws BackupToolException;
    
    /**
     * Find out of a file exists, and return an object.
     * 
     * @param file
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public IBackupFilePart resolveRemoteFile( Path file ) throws BackupToolException;

    /**
     * Initialize the backend file-system.  
     * 
     * E.g. Including any providers added since the object's
     * construction, etc.
     * 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException
     */
    public void init() throws BackupToolException;

    Path nextTemplate(IBackupFilePart parentDir, Path template, Pattern matchPattern, boolean parentExists) throws BackupToolException;

    /**
     * Get the next valid name for a file-system object given a template and regex.
     *
     * @param template The path used as a template for generating the new.
     * @param nameRegex A regex applied to the template file that will result in the resulting path.
     * @param nameComponents A list of "components" or "parts" of the template to be modified.
     * @param ignoreSuffix A suffix, e.g. ".tmp" to ignore when matching the template
     * @return A newly generated path for a new file-system object
     * @throws BackupToolException
     */
    static Path nextName( final Path template,
                          final Pattern nameRegex,
                          final List<BackupToolNameComponentType> nameComponents,
                          final boolean useAbsolute,
                          final String ignoreSuffix)
            throws BackupToolException
    {
        Path res = null;

        if( template == null )
        {
            throw new BackupToolException("Name template cannot be null");
        }

        if( nameRegex == null )
        {
            throw new BackupToolException("Naming rule regex cannot be null");
        }

        String currName = template.getFileName().toString();

        if( ignoreSuffix != null && currName.endsWith(ignoreSuffix) )
        {
            currName = currName.substring(0, currName.length() - ignoreSuffix.length()).trim();
        }

        if( StringUtils.isBlank(currName) )
        {
            throw new BackupToolException(String.format("Invalid path template '%s'", template));
        }

        Matcher match = nameRegex.matcher(currName);
        if( !match.matches() )
        {
            throw new BackupToolException(String.format("Name Regex '%s' does not match file '%s'",
                    nameRegex, currName));
        }

        if( match.groupCount() < 1 )
        {
            throw new BackupToolException(String.format("Name Regex '%s' must have at lease 1 group",
                    nameRegex));
        }

        if( nameComponents == null || nameComponents.isEmpty() )
        {
            throw new BackupToolException("Name component list can not be empty.");
        }

        if( match.groupCount() > nameComponents.size() + 1 )
        {
            throw new BackupToolException(
                    String.format("Naming rule regex cannot have more groups ( '%d' )"
                                    + " than the provided name component list ( '%d' )",
                            match.groupCount(), nameComponents.size()));
        }

        StringBuilder resStr = new StringBuilder();
        resStr.append(
                Paths.get(
                        match.group(0).substring(0, match.start(1))
                ).toString());

        for( int i = 1; i <= match.groupCount(); i++ )
        {
            String groupStr = match.group(i);
            BackupToolNameComponentType currNameType = nameComponents.get(i - 1);

            String compStr = "";
            switch( currNameType )
            {
                case INTEGER_SEQUENCE:
                {
                    Long j = Long.parseLong(groupStr);
                    compStr = String.format("%05d", j + 1);
                }
                break;

                case ISO_TIMESTAMP:
                {
                    Instant currTime = Instant.now();
                    compStr = currTime.toString().replace(':', '-');
                }
                break;
            }

            resStr.append(compStr);

            int endIndex;
            if( i < match.groupCount() - 1 )
            {
                endIndex = match.start(i + 1);
            }
            else
            {
                endIndex = match.group(0).length();
            }
            resStr.append(match.group(0).substring(match.end(i), endIndex));
        }

        res = Paths.get(resStr.toString());

        if( useAbsolute )
        {
            res = template.getParent().resolve(res);
        }

        log.debug(String.format("nextName() : Generated '%s'", res));

        return res;
    }
}
