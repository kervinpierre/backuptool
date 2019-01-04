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
package com.fastsitesoft.backuptool;

import java.util.Date;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public interface IBackupBackendLockClass
{
    public static final String LOCKNAME = ".backuptoollock";
    
    public String GetPath();
    public String SetPath();
    
    public String Read();
    public void Write();
    public void Delete();
    
    public String GetOwnerName();
    public void SetOwnerName( String name );
    
    public Date GetAcquiredTime();
    public void SetAcquiredTime( Date atime );
}
