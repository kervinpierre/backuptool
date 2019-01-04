/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
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
package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.enums.FSSFilePermissionType;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for working with file permissions.
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public final class FSSFilePermission
{
    private final FSSFilePermissionType type;
    private final Set<PosixFilePermission> perms;

    private static final int OWNER_READ_FILEMODE = 0400;
    private static final int OWNER_WRITE_FILEMODE = 0200;
    private static final int OWNER_EXECUTE_FILEMODE = 0100;
    private static final int GROUP_READ_FILEMODE = 0040;
    private static final int GROUP_WRITE_FILEMODE = 0020;
    private static final int GROUP_EXECUTE_FILEMODE = 0010;
    private static final int OTHERS_READ_FILEMODE = 0004;
    private static final int OTHERS_WRITE_FILEMODE = 0002;
    private static final int OTHERS_EXECUTE_FILEMODE = 0001;

    public FSSFilePermissionType getType()
    {
        return type;
    }

    public Set<PosixFilePermission> getPerms()
    {
        return perms;
    }

    public FSSFilePermission(String p)
    {
        int tempInt;

        try
        {
            tempInt = Integer.parseInt(p, 8);
        }
        catch( NumberFormatException ex )
        {
            // Since we can't parse the argument as an octal, treat it as a permission string
            tempInt = Integer.MAX_VALUE;
        }

        type  = FSSFilePermissionType.JAVA_POSIX_FS;
        if( tempInt == Integer.MAX_VALUE )
        {
            perms = PosixFilePermissions.fromString(p);
        }
        else
        {
            perms = new HashSet<>();
            fromOctal(tempInt, perms);
        }
    }
    
    public FSSFilePermission(FSSFilePermissionType t)
    {
        this(t, new HashSet<>());
    }

    public FSSFilePermission(FSSFilePermissionType t, Set<PosixFilePermission> p)
    {
        type = t;
        perms = p;
    }

    public String toString(int indent)
    {
        String res = "";
        
        for(PosixFilePermission p : perms)
        {
            res += String.format("%s, ", p.toString());
        }
        
        return res;
    }

    @Override
    public String toString()
    {
        return toString(0);
    }

    /**
     * Convert a Java PosixFilePermissions string to our permissions.
     * E.g. "rwxr-x---" for permissions 0750 ( in octal )
     * 
     * @link https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/PosixFilePermissions.html#fromString(java.lang.String)
     * @param p String describing permssions. 
     */
    public static FSSFilePermission fromJavaFSString(String p)
    {
        FSSFilePermission res
                = new FSSFilePermission(FSSFilePermissionType.JAVA_POSIX_FS,
                        PosixFilePermissions.fromString(p));
        return res;
    }

    public static void fromOctal(int p, Set<PosixFilePermission> permSet)
    {
        permSet.clear();

        if( (p & OWNER_READ_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OWNER_READ);
        }

        if( (p & OWNER_WRITE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OWNER_WRITE);
        }

        if( (p & OWNER_EXECUTE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if( (p & GROUP_READ_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.GROUP_READ);
        }

        if( (p & GROUP_WRITE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.GROUP_WRITE);
        }

        if( (p & GROUP_EXECUTE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if( (p & OTHERS_READ_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OTHERS_READ);
        }

        if( (p & OTHERS_WRITE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OTHERS_WRITE);
        }

        if( (p & OTHERS_EXECUTE_FILEMODE) != 0 )
        {
            permSet.add(PosixFilePermission.OTHERS_EXECUTE);
        }
    }

    public static FSSFilePermission fromOctal(int p)
    {
        Set<PosixFilePermission> res = new HashSet<>();
        fromOctal(p, res);

        FSSFilePermission resPerm = new FSSFilePermission(FSSFilePermissionType.JAVA_POSIX_FS,
                res);

        return resPerm;
    }

    public int toOctal()
    {
        return toOctal(perms);
    }

    public static int toOctal(Set<PosixFilePermission> permSet)
    {
        int result = 0;
        for (PosixFilePermission currBit : permSet)
        {
            switch (currBit) 
            {
            case OWNER_READ:
                result |= OWNER_READ_FILEMODE;
                break;
            case OWNER_WRITE:
                result |= OWNER_WRITE_FILEMODE;
                break;
            case OWNER_EXECUTE:
                result |= OWNER_EXECUTE_FILEMODE;
                break;
            case GROUP_READ:
                result |= GROUP_READ_FILEMODE;
                break;
            case GROUP_WRITE:
                result |= GROUP_WRITE_FILEMODE;
                break;
            case GROUP_EXECUTE:
                result |= GROUP_EXECUTE_FILEMODE;
                break;
            case OTHERS_READ:
                result |= OTHERS_READ_FILEMODE;
                break;
            case OTHERS_WRITE:
                result |= OTHERS_WRITE_FILEMODE;
                break;
            case OTHERS_EXECUTE:
                result |= OTHERS_EXECUTE_FILEMODE;
                break;
            }
        }
        return result;
    }

}
