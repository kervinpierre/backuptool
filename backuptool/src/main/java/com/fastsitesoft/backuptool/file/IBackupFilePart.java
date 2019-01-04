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

import com.fastsitesoft.backuptool.backend.IBackendFileSystem;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.net.URI;
import java.nio.file.Path;


/**
 * Part or whole BackupFile on disk.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public interface IBackupFilePart extends Comparable
{
    /**
     * Returns the protocol scheme for the file part.
     * 
     * @return 
     */
    public String getScheme();
    
    /**
     * Return the attached file-system for this object.
     * 
     * @return 
     */
    public IBackendFileSystem getFS();
    
    /**
     * The URI related to the current file.
     * 
     * @return 
     */
    public URI getUri();
    
    /**
     * The object path on its file-system
     * @return 
     */
    public Path getPath();
}
