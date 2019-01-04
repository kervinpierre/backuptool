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

import com.fastsitesoft.backuptool.enums.FSSConfigMembersEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultCodeEnum;
import com.fastsitesoft.backuptool.enums.FSSValidateResultErrorEnum;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 * Represents the result of a validate() call in various API.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public final class FSSValidateConfigResult
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(FSSValidateConfigResult.class);

    /**
     * List of issues with the validated object
     */
    private final List<FSSValidateConfigError> errors;

    public static FSSValidateResultCodeEnum getStatus(FSSValidateConfigResult confRes) throws BackupToolException
    {
        FSSValidateResultCodeEnum res = FSSValidateResultCodeEnum.VALID;

        // calculate error from the array
        if( confRes == null )
        {
            throw new BackupToolException("Config Result is null");
        }

        if( confRes.getErrors() == null )
        {
            throw new BackupToolException("Config Result Error Array is null");
        }

        for( FSSValidateConfigError err : confRes.getErrors() )
        {
            if( err.getStatus() == FSSValidateResultCodeEnum.ERROR )
            {
                res = FSSValidateResultCodeEnum.ERROR;
                break;
            }
        }

        return res;
    }

    public List<FSSValidateConfigError> getErrors()
    {
        return errors;
    }
    
    public FSSValidateConfigResult(final List<FSSValidateConfigError> e )
    {
        errors = e;
    }

    public FSSValidateConfigResult( )
    {
        errors = new ArrayList<>();
    }

    /**
     * 
     * @param m
     * @param c
     * @param d
     * @param o
     * @param e 
     * @throws BackupToolException 
     */
    public void addError(FSSConfigMembersEnum m, FSSValidateResultCodeEnum c, 
                                    String d, Object o, FSSValidateResultErrorEnum e) throws BackupToolException
    {
        List<FSSValidateResultErrorEnum> errs = new ArrayList<>();
        errs.add(e);
        
        addError(m, c, d, o, errs);  
    }
    
    /**
     * Add an invalid object to the list.
     * 
     * @param m The member being validated
     * @param c The object's result code after validation.
     * @param d Textual description of the status
     * @param o The object value.
     * @param e
     * @throws BackupToolException
     */
    public void addError(FSSConfigMembersEnum m, FSSValidateResultCodeEnum c, 
                                    String d, Object o, List<FSSValidateResultErrorEnum> e) throws BackupToolException
    {
        FSSValidateConfigError currError = new FSSValidateConfigError(c, d, o, m, e);

        addError(currError);
    }
    
    /**
     * 
     * @param errs 
     * @throws BackupToolException
     */
    public void addError(List<FSSValidateConfigError> errs) throws BackupToolException
    {
        for( FSSValidateConfigError err : errs )
        {
            addError(err);
        }
    }

    /**
     * 
     * @param err 
     * @throws BackupToolException
     */
    public void addError(FSSValidateConfigError err) throws BackupToolException
    {
        if( err == null 
                || err.getStatus() == FSSValidateResultCodeEnum.NONE )
        {
            String errMsg = String.format("Error adding new validate config error '%s'", err);
            log.info(errMsg);
            
            throw new BackupToolException(errMsg);
        }
        
        errors.add(err);
    }
    
    /**
     * Return the String representation of this configuration objection
     * 
     * @return
     */
    @Override
    public String toString()
    {
        return toString(0);
    }
    
    public String toString(int indent)
    {
        String res;
        String indentStr = "";
        
        if( indent > 0 )
        {
            indentStr = StringUtils.repeat(" ", indent);
        }
        
        StringBuilder sb = new StringBuilder();
        FSSValidateResultCodeEnum currStatus = null;

        try
        {
            currStatus = getStatus(this);
        }
        catch( BackupToolException ex )
        {
            log.debug("Exceptiong getting status.", ex);
        }

        sb.append( String.format("%sFSSVALIDATERESULT\n", indentStr) );
        sb.append(String.format("%s  STATUS           : '%s'\n", indentStr, currStatus) );
        
        sb.append( String.format("%s  ERRORS :\n", indentStr) );
        for( FSSValidateConfigError err : errors )
        {
            sb.append( err.toString(indent+2) );
        }
        
        res = sb.toString();
        
        return res;
    }
}
