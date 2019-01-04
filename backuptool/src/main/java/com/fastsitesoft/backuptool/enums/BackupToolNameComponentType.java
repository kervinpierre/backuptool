package com.fastsitesoft.backuptool.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 7/12/15.
 */
public enum BackupToolNameComponentType
{
    NONE,
    ISO_TIMESTAMP,
    INTEGER_SEQUENCE,
    CHARACTER_SEQUENCE;

    private static final Logger log
            = LogManager.getLogger(BackupToolNameComponentType.class);

    public static BackupToolNameComponentType from( String s )
    {
        BackupToolNameComponentType res = NONE;
        try
        {
            String currV = StringUtils.upperCase(s);
            res = BackupToolNameComponentType.valueOf(currV);
        }
        catch (Exception ex)
        {
            log.error( String.format("Invalid name component type specified '%s'", s), ex);
        }

        return res;
    }
}
