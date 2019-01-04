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
package com.fastsitesoft.backuptool;

import com.fastsitesoft.backuptool.utils.BackupToolException;

import java.io.*;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decorating OutputStream.  Counts bytes and also allows swapping underlying stream.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class BackupFileCountingInputStream extends InputStream
{
    private static final Logger log 
                 = LogManager.getLogger(BackupFileCountingInputStream.class);

    private final InputStream fileStream;
    private int readBytes;
    
    /**
     * Decorate an existing input stream from the file-system before backing it up
     * 
     */
    public BackupFileCountingInputStream(final InputStream in)
    {
        readBytes = 0;
        fileStream = in;
    }

    public int getCount()
    {
        int res = 0;
        
        res = readBytes;
        
        return res;
    }


    @Override
    public int read() throws IOException
    {
        int res;

        res = fileStream.read();
        readBytes++;

        return res;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        int res;

        res = fileStream.read(b);
        readBytes++;

        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int res;

        res = fileStream.read(b, off, len);
        readBytes++;

        return res;
    }

    @Override
    public long skip(long n) throws IOException
    {
        long res;

        res = fileStream.skip(n);

        return res;
    }

    @Override
    public int available() throws IOException
    {
        int res;

        res = fileStream.available();

        return res;
    }

    @Override
    public void close() throws IOException
    {
        fileStream.close();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        fileStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException
    {
        fileStream.reset();
    }

    @Override
    public boolean markSupported()
    {
        boolean res;

        res = fileStream.markSupported();

        return res;
    }

}
