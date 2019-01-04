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
package com.fastsitesoft.backuptool.backend;

import com.fastsitesoft.backuptool.enums.StorageBackendSearchType;
import com.fastsitesoft.backuptool.file.BackupFilePart;
import com.fastsitesoft.backuptool.file.IBackupFilePart;
import com.fastsitesoft.backuptool.utils.BackupToolException;
import com.sludev.commons.vfs2.provider.azure.AzConstants;
import com.sludev.commons.vfs2.provider.azure.AzFileProvider;
import com.sludev.commons.vfs2.provider.s3.SS3Constants;
import com.sludev.commons.vfs2.provider.s3.SS3FileProvider;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents various backend services used to store 'Backup' objects.
 * 
 * Responsible for storing and retrieving BackupItem and BackupFileChuck data
 * basically.
 * 
 * Contains multiple file-systems.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class StorageBackendFileSystem implements IBackendFileSystem
{
    private static final Logger log 
                                = LogManager.getLogger(StorageBackendFileSystem.class);

    private final String user;
    private final String password;
    private final String defaultScheme;
    private final String authority;
    private final HashMap<String, FileProvider> providers;
    
    private final DefaultFileSystemManager fileManager;
    private final FileSystemOptions fileSystemOpts;
    
    /**
     * Returns the user/account id for the backend.
     * 
     * @return 
     */
    protected String getUser()
    {
        return user;
    }

    /**
     * Creates a new Storage Backend Object.  Used for accessing and general operations on a storage backend.
     * 
     * @param user The username used for the backend authentication if any.
     * @param password The password used for backend authentication, if any.
     * @param authority The 'authority' ( e.g. hostname or "hostname:port" combination used to access the backend
     * @param defaultScheme The protocol scheme used to identify the backend.
     * @throws BackupToolException 
     */
    private StorageBackendFileSystem(String user, String password, String authority, String defaultScheme)
                                    throws BackupToolException
    {
        this.user = user;
        this.password = password;
        this.defaultScheme = defaultScheme;
        this.authority = authority;
        
        this.providers = new HashMap<>();
        this.fileManager = new DefaultFileSystemManager();
        this.fileSystemOpts = new FileSystemOptions();
        
        addProvider("file");
        if( StringUtils.isNoneBlank(this.defaultScheme) )
        {
            addProvider(this.defaultScheme);
        }
    }

    public static StorageBackendFileSystem from(String user, String password, URI url)
            throws BackupToolException
    {
        StorageBackendFileSystem res;

        String defaultScheme = url.getScheme();
        String authority = StringUtils.defaultString(url.getAuthority());
        String container;

        try
        {
            container = url.getPath().split("/", 2)[0].trim();
        }
        catch(Exception ex)
        {
            String errMsg = String.format("Error parsing storage URI '%s'", url);

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

        res = from(user, password, authority, defaultScheme);

        return res;
    }

    public static StorageBackendFileSystem from(String user, String password, String authority, String defaultScheme)
            throws BackupToolException
    {
        StorageBackendFileSystem res = new StorageBackendFileSystem(user, password, authority, defaultScheme);

        return res;
    }

    /**
     * Initializes the current backend object.
     * 
     * @throws BackupToolException
     */
    @Override
    public void init() throws BackupToolException
    {          
          
        try 
        {
            // fileManager.addProvider(AzConstants.AZSBSCHEME, new AzFileProvider());
            //fileManager.addProvider("file", new DefaultLocalFileProvider());
            
            for(Entry<String,FileProvider> pr : providers.entrySet())
            {
                fileManager.addProvider(pr.getKey(), pr.getValue());
            }
            
            fileManager.init();

            StaticUserAuthenticator auth = new StaticUserAuthenticator("", user, password);
         
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(fileSystemOpts, auth);
        }
        catch (FileSystemException ex)
        {
            String errMsg = "Error initializing VFS filesystem manager.";
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
    }

    /**
     * Copy a Backup File Part to and from a File-System.
     * 
     * @param src
     * @param dst
     * @throws BackupToolException 
     */
    @Override
    public void copy( IBackupFilePart src, IBackupFilePart dst) throws BackupToolException
    {
        if( src == null || dst == null )
        {
            String errMsg = String.format("Error copying '%s' to '%s'", src == null ? "null" : src.getUri(),
                    dst == null ? "null" : dst.getUri());

            log.debug(errMsg);

            throw new BackupToolException(errMsg);
        }

        log.debug(String.format("copying '%s' to '%s'", src.getUri(), dst.getUri()));

        String uriSrc;
        String uriDst;
        
        FileObject foSrc;
        FileObject foDst;
        
        uriSrc = src.getUri().toASCIIString();
        uriDst = dst.getUri().toASCIIString();
        
        try
        {
            foSrc = fileManager.resolveFile(uriSrc, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving source uri '%s'", uriSrc);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            foDst = fileManager.resolveFile(uriDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving destination uri '%s'", uriDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            foDst.copyFrom(foSrc, Selectors.SELECT_SELF);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error copying '%s' to destination uri '%s'", uriSrc, uriDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
    }

    @Override
    public boolean delete(IBackupFilePart file) throws BackupToolException
    {
        String fileDst;
        boolean res = false;
        
        FileObject foDst;
        
        fileDst = file.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            res = foDst.delete();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling createFile() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }

    @Override
    public int deleteFolder(IBackupFilePart folder, boolean recurse) throws BackupToolException
    {
        int res = -1;
        
        String fileDst;
        
        FileObject foDst;
        
        fileDst = folder.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            if( recurse )
            { 
                res = foDst.delete(Selectors.SELECT_ALL);
            }
            else
            {
                res = foDst.delete(Selectors.SELECT_SELF);
            }
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling deleteFolder() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }

    @Override
    public boolean exists(IBackupFilePart file) throws BackupToolException
    {
        boolean res = false;
        
        String fileDst;
        
        FileObject foDst;
        
        fileDst = file.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            res = foDst.exists();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error checking exist() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }

    @Override
    public long getContentSize(IBackupFilePart file) throws BackupToolException
    {
        long res = 0;
        
        String fileDst;
        
        FileObject foDst;
        
        fileDst = file.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            FileContent fo = foDst.getContent();
            res = fo.getSize();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error checking getContent() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }

    @Override
    public long getLastModifiedDate(IBackupFilePart file) throws BackupToolException
    {
        long res = 0;
        
        String fileDst;
        
        FileObject foDst;
        
        fileDst = file.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            FileContent fo = foDst.getContent();
            res = fo.getLastModifiedTime();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error checking getContent() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }
    
    @Override
    public void createFile(IBackupFilePart file) throws BackupToolException
    {
        String fileDst;
        
        FileObject foDst;
        
        fileDst = file.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            foDst.createFile();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling createFile() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
    }
    
    
    @Override
    public void createFolder(IBackupFilePart folder) throws BackupToolException
    {
        String fileDst;
        
        FileObject foDst;
        
        fileDst = folder.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving file uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            foDst.createFolder();
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling createFolder() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
    }
    
    @Override
    public List<IBackupFilePart> findFiles(IBackupFilePart folder, 
                                           StorageBackendSearchType type)
                                                    throws BackupToolException
    {
        List<IBackupFilePart> res = null;
        
        String fileDst;
        
        FileObject foDst;
        
        fileDst = folder.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving folder uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            res = new ArrayList<>();
            
            FileSelector currSelector = fromSearchType(type);
            
            FileObject[] children = foDst.findFiles(currSelector);
            for( FileObject child : children )
            {
                FileName currName = child.getName();
                String currPath = currName.getPath();
                IBackupFilePart currChildObj = resolveLocalFile(Paths.get(currPath));
                res.add(currChildObj);
            }
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling findFiles() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        return res;
    }
    
    @Override
    public List<IBackupFilePart> getChildren(IBackupFilePart folder) throws BackupToolException
    {
        List<IBackupFilePart> res = null;
       
        String fileDst;
        
        FileObject foDst;
        
        fileDst = folder.getUri().toASCIIString();
        
        try
        {
            foDst = fileManager.resolveFile(fileDst, fileSystemOpts);
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error resolving folder uri '%s'", fileDst);
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
        try
        {
            res = new ArrayList<>();
            
            FileObject[] children = foDst.getChildren();
            for( FileObject child : children )
            {
                FileName currName = child.getName();
                String currPath = currName.getPath();
                IBackupFilePart currChildObj = resolveLocalFile(Paths.get(currPath));
                res.add(currChildObj);
            }
        }
        catch (FileSystemException ex)
        {
            String errMsg = String.format("Error calling getChildren() on '%s'", fileDst );
            
            log.debug(errMsg, ex);
            
            throw new BackupToolException(errMsg, ex);
        }
        
       return res;
    }
    
    final protected void addProvider(String scheme) throws BackupToolException
    {
        switch(scheme)
        {
            case AzConstants.AZSBSCHEME:
                {
                    addProvider(scheme, new AzFileProvider());
                    if( StringUtils.isBlank(user) || StringUtils.isBlank(password) )
                    {
                        String errMsg = "Azure backend with empty username and/or password.";

                        log.debug(errMsg);

                        throw new BackupToolException(errMsg);
                    }
                }
                break;
                
            case SS3Constants.S3SCHEME:
                {
                    addProvider(scheme, new SS3FileProvider());
                    if( StringUtils.isBlank(user) || StringUtils.isBlank(password) )
                    {
                        String errMsg = "S3 backend with empty username and/or password.";

                        log.debug(errMsg);

                        throw new BackupToolException(errMsg);
                    }
                }
                break;
                
            case "file":
                {
                    addProvider(scheme, new DefaultLocalFileProvider());
                }
                break;
                
            default:
                throw new BackupToolException( String.format("Invalid scheme '%s'", scheme) );
        }
    }
    
    final protected void addProvider(String scheme, FileProvider prov)
    {
        providers.put(scheme, prov);
    }
    
    /**
     * Given a scheme and a path, use the provider to from a valid URI.
     * 
     * Set providers require account names, containers, etc in their URLs, so this is
     * very provider specific.
     * 
     * @param currScheme
     * @param currPath
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public URI resolveURI(String currScheme, Path currPath) throws BackupToolException
    {
        URI res = null;

        if( currPath == null )
        {
            log.debug("Path cannot be null");

            throw new BackupToolException("Path cannot be null");
        }

        switch(currScheme)
        {
            case "file":
                {
                    try
                    {
                        res = currPath.toUri();
                    }
                    catch( IllegalArgumentException ex )
                    {
                        String errMsg
                                = String.format("Invalid path for URI.create() '%s'", currPath);

                        log.debug(errMsg, ex);

                        throw new BackupToolException(errMsg, ex);
                    }
                }
                break;
                
            case AzConstants.AZSBSCHEME:
                {
                    String newPath = currPath.toString();
                    newPath = StringUtils.prependIfMissing(newPath, "/");

                    try
                    {
                        res = new URI(AzConstants.AZSBSCHEME,
                                authority,
                                newPath,
                                null);
                    }
                    catch( URISyntaxException ex )
                    {
                        log.debug("Error creating Azure URL", ex);
                    }
                }
                break;
                
            case SS3Constants.S3SCHEME:
                {
                    String newPath = currPath.toString();
                    newPath = StringUtils.prependIfMissing(newPath, "/");

                    try
                    {
                        res = new URI(SS3Constants.S3SCHEME,
                                authority,
                                newPath,
                                null);
                    }
                    catch( URISyntaxException ex )
                    {
                        log.debug("Error creating S3 URL", ex);
                    }
                }
                break;
                
            default:
                throw new BackupToolException( 
                        String.format("Scheme '%s' is invalid or unknown", currScheme) );
        }
        
        return res;
    }
    
    /**
     * Creates a new file part related to this storage object and a particular
     * provider instance.
     * 
     * @param currScheme
     * @param currPath
     * @return 
     * @throws com.fastsitesoft.backuptool.utils.BackupToolException 
     */
    public IBackupFilePart resolve(String currScheme, Path currPath) throws BackupToolException
    {
        URI currUri = resolveURI(currScheme, currPath);
        if( currUri == null )
        {
            String errMsg = String.format("URI cannot be null for '%s' and '%s'",
                    currScheme, currPath);

            log.debug(errMsg);

            throw new BackupToolException(errMsg);
        }
        
        BackupFilePart res = new BackupFilePart(currScheme, this, currUri, currPath );
        
        return res;
    }
    
    /**
     * Creates a local file part; on the local file-system.
     * 
     * @param currPath
     * @return 
     */
    @Override
    public IBackupFilePart resolveLocalFile(Path currPath) throws BackupToolException
    {
        IBackupFilePart res = resolve("file", currPath);
                
        return res;
    }
    
    /**
     * Creates a remote file part; on the default remote file-system.
     * 
     * @param currPath
     * @return 
     */
    @Override
    public IBackupFilePart resolveRemoteFile(Path currPath) throws BackupToolException
    {
        IBackupFilePart res = resolve(defaultScheme, currPath);
                
        return res;
    }

    private FileSelector fromSearchType(StorageBackendSearchType type)
    {
        FileSelector res = null;
        
        switch(type)
        {
            case SELECT_SELF:
                res = Selectors.SELECT_SELF;
                break;
        }
        
        return res;
    }

    /**
     * Use the template argument to decide how to create the next file-system object name.
     *
     * If there are file-system objects existing in the parent directory, then the last in the natural sort order is returned.
     *
     * Otherwise the template argument itself is returned to the caller.
     *
     * This allows us to always get the next name for a new file-system object in a parent directory.
     *
     * @param parentDir  The parent directory to search for the file-system objects.
     * @param template   The template to return if the parent directory is empty.
     * @return The last file-system object in sorted order.
     * @throws BackupToolException
     */
    public Path nextTemplate( final IBackupFilePart parentDir,
                              final Path template,
                              final Pattern matchPattern,
                              final boolean requireParent ) throws BackupToolException
    {
        Path res = template;

        if( parentDir == null )
        {
            throw new BackupToolException("Parent directory cannot be null");
        }

        if( exists(parentDir) == false )
        {
            if( requireParent )
            {
                throw new BackupToolException(String.format("Parent directory '%s' must exist.",
                        parentDir.getUri()));
            }
            else
            {
                return res;
            }
        }

        IBackupFilePart currTemp;

        List<IBackupFilePart> children = getChildren(parentDir);
        if( children == null || children.size() < 1 )
        {
            // No file-system child objects in the parent directory.
            // Return the original template argument.
            return res;
        }

        Collections.sort(children);
        Collections.reverse(children);

        if( matchPattern != null )
        {
            for( IBackupFilePart part : children )
            {
                String p = part.getPath().getFileName().toString();
                if(matchPattern.matcher(p).matches())
                {
                    res = part.getPath();
                    break;
                }
            }
        }
        else
        {
            currTemp = children.get(children.size() - 1);
            res = currTemp.getPath();
        }


        return res;
    }
}
