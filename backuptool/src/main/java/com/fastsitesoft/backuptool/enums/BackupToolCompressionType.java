package com.fastsitesoft.backuptool.enums;

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;

/**
 * The type of compression to use on the archive file.
 *
 * Created by kervin on 7/14/15.
 */
public enum BackupToolCompressionType
{
    NONE,
    GZIP;

    public static BackupToolCompressionType from(String t) throws BackupToolException
    {
        BackupToolCompressionType res;

        if ( StringUtils.isBlank(t))
        {
            throw new BackupToolException(
                    String.format("Error setting compression type '{0}'",
                            t));
        }

        res = BackupToolCompressionType.valueOf(t.trim().toUpperCase());

        return res;
    }
}
