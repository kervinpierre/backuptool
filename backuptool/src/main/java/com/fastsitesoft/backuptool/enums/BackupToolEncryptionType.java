package com.fastsitesoft.backuptool.enums;

import com.fastsitesoft.backuptool.utils.BackupToolException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The type of encryption to use on the backup file.
 *
 * Created by kervin on 7/14/15.
 */
public enum BackupToolEncryptionType
{
    NONE,
    AES;

    private static final Logger log
            = LogManager.getLogger(BackupToolEncryptionType.class);

    public static BackupToolEncryptionType from(String c) throws BackupToolException
    {
        BackupToolEncryptionType currCS = null;

        try
        {
            String currV = StringUtils.upperCase(c);
            currCS = BackupToolEncryptionType.valueOf(currV);
        }
        catch (Exception ex)
        {
            String err = String.format("Invalid Encryption type specified '%s'", c);

            log.error( err, ex);

            throw new BackupToolException(err, ex);
        }

        return currCS;
    }
}
