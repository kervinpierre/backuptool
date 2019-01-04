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
package com.fastsitesoft.backuptool.utils;

import com.fastsitesoft.backuptool.config.entities.BackupConfigEncryption;
import com.fastsitesoft.backuptool.enums.BackupToolCompressionType;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Output data to a Tar Stream which is also optionally GZip'ed and encrypted.
 *
 * Created by kervin on 7/10/15.
 */
public class BackupFileArchiveOutputStream extends OutputStream
{
    private static final Logger log
            = LogManager.getLogger(BackupFileArchiveOutputStream.class);

    private final OutputStream fileStream;
    private final TarArchiveOutputStream archiveStream;
    private final BackupToolCompressionType compressionType;
    private final BackupConfigEncryption encryptionType;

    public BackupFileArchiveOutputStream(final OutputStream out,
                                         final BackupConfigEncryption enc,
                                         final BackupToolCompressionType comp) throws BackupToolException
    {
        fileStream = out;
        compressionType = comp;
        encryptionType = enc;

        try
        {
            // Check configuration and build the correct output stream.
            OutputStream currStream = fileStream;
            if( enc != null && enc.getType() != null )
            {
                switch( enc.getType() )
                {
                    case AES:
                        // currStream = new AESStream(currStream);
                        break;

                    default:
                        break;
                }
            }

            if( comp != null )
            {
                switch( comp )
                {
                    case GZIP:
                        currStream = new GZIPOutputStream(currStream);
                        break;

                    default:
                        break;
                }
            }

            TarArchiveOutputStream taos
                    = new TarArchiveOutputStream(currStream);

            taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            taos.setAddPaxHeadersForNonAsciiNames(true);

            archiveStream = taos;
        }
        catch (IOException ex)
        {
            String errMsg = String.format("Error creating archive stream.");

            log.debug(errMsg, ex);
            throw new BackupToolException(errMsg, ex);
        }

    }

    @Override
    public void write(byte[] b) throws IOException
    {
        archiveStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        archiveStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        archiveStream.flush();
    }

    @Override
    public void close() throws IOException
    {
        archiveStream.close();
    }

    @Override
    public void write(int b) throws IOException
    {
        archiveStream.write(b);
    }

    public void putArchiveEntry(final Path p, final Byte linkFlag, LinkOption opts) throws BackupToolException
    {
        putArchiveEntry(p, linkFlag, null, opts);
    }

    public void putArchiveEntry(final Path p, final Byte linkFlag, final Long size, LinkOption opts) throws BackupToolException
    {
        PosixFileAttributes attributes = null;
        String ownerName = null;
        String groupName = null;
        String linkName = "";
        FileTime modTime = null;
        long mtime = 0;
        long currSize = 0;
        int uid = 0;
        int gid = 0;
        int devMajor = 0;
        int devMinor = 0;
        int mode = 0;

        try
        {
            if( size == null )
            {
                currSize = Files.size(p);
            }
            else
            {
                currSize = size;
            }

            attributes = Files.getFileAttributeView(p, PosixFileAttributeView.class).readAttributes();

            ownerName = attributes.owner().getName();
            groupName = attributes.group().getName();
            modTime   = attributes.lastModifiedTime();
            Set<PosixFilePermission> perms   = attributes.permissions();
            mode = FSSFilePermission.toOctal(perms);

            mtime = modTime.to(TimeUnit.MILLISECONDS);
            uid = (int) Files.getAttribute(p, "unix:uid");
            gid = (int) Files.getAttribute(p, "unix:gid");
        }
        catch (IOException ex)
        {
            throw new BackupToolException(
                    String.format("Failed retrieving attributes '%s'", p),ex);
        }

        putArchiveEntry(p, linkFlag, currSize, mtime, ownerName, groupName, linkName, uid, gid, mode,
                devMajor, devMinor, opts);
    }

    public void putArchiveEntry(final Path p,
                                final Byte linkFlag,
                                final long size,
                                final long modifiedTime,
                                final String userName,
                                final String groupName,
                                final String linkName,
                                final int userId,
                                final int groupId,
                                final int mode,
                                final int devMajor,
                                final int devMinor,
                                final LinkOption opts) throws BackupToolException
    {
        String currName = p.toString();

        try
        {
            TarArchiveEntry tae;

            if( linkFlag != null )
            {
                tae = new TarArchiveEntry(currName, linkFlag);
            }
            else
            {
                tae = new TarArchiveEntry(currName);
            }

            tae.setSize(size);
            tae.setUserName(userName);
            tae.setGroupName(groupName);
            tae.setLinkName(linkName);
            tae.setUserId(userId);
            tae.setGroupId(groupId);
            tae.setMode(mode);
            tae.setDevMajor(devMajor);
            tae.setDevMinor(devMinor);
            tae.setModTime( modifiedTime );

            archiveStream.putArchiveEntry(tae);
        }
        catch(IOException ex)
        {
            throw new BackupToolException(
                    String.format("Error adding archive '%s'", p), ex);
        }
    }

    public void closeArchiveEntry() throws IOException
    {
        archiveStream.closeArchiveEntry();
    }
}
