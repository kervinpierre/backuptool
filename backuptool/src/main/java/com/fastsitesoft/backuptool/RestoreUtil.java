package com.fastsitesoft.agent.backup;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Misc. utilities for aiding in restoring backups.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class RestoreUtil
{
    private static final Logger log 
                                = LogManager.getLogger(RestoreUtil.class);
    
    public static void restore(File backupFolderPath, File outputDirectory)
    {
        String tmpStr = backupFolderPath.getAbsolutePath();
        File srcDirectory = new File(tmpStr);

        File tmpOutputDirectory = new File(outputDirectory.getAbsolutePath() + File.separator + "tmp" + File.separator);
//
//        copyToOutputFolder(srcDirectory, tmpOutputDirectory);
//
//        generateDecrypt(tmpOutputDirectory);
//        mergingFiles(tmpOutputDirectory);
//        unTarFile(tmpOutputDirectory);
//        unZipFile(tmpOutputDirectory);
//
//        copyResultToOutputFolder(tmpOutputDirectory, outputDirectory);
    }

    /**
     * Copy Result folder to output folder
     * 
     * @param outputFile
     * @param outputFolderPath
     * @throws Exception
     */
    public static void copyResultToOutputFolder(File tmpOutputDirectory, File outputDirectory) throws Exception
    {
        FileUtils.copyDirectory(tmpOutputDirectory, outputDirectory);
        deleteFile(tmpOutputDirectory);
    }

    /**
     * This method will delete file/directory recursive
     * 
     * @param file
     * @throws Exception
     */
    public static void deleteFile(File file) throws Exception
    {
        if (file.isDirectory())
        {
            FileUtils.deleteDirectory(file);
        }
        else
        {
            file.delete();
        }
    }

    /**
     * Copy all file to output folder for further procession
     * 
     * @param srcDirectory
     * @param tmpOutputDirectory
     * @throws Exception
     */
    public static void copyToOutputFolder(File srcDirectory, File tmpOutputDirectory) throws Exception
    {
        if (tmpOutputDirectory.exists())
        {
            deleteFile(tmpOutputDirectory);
        }
        tmpOutputDirectory.mkdirs();
        // copy all files to output folder
        if (srcDirectory.isDirectory())
        {
            FileUtils.copyDirectory(srcDirectory, tmpOutputDirectory, new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.isFile();
                }
            });
        }
        else
        {
            FileUtils.copyFileToDirectory(srcDirectory, tmpOutputDirectory);
        }
    }

    /**
     * Merging splitted files for procesion
     * 
     * @param outputFile
     * @return
     * @throws Exception
     */
    public static String mergingFiles(File tmpOutputDirectory) throws Exception
    {
        String mergedOutputFilePath = "";
        File fileList[] = tmpOutputDirectory.listFiles();
        int totalFile = fileList.length;
        if (totalFile > 1)
        {
            // Need to merge all files in sequence
            String mergedFileName = getMergedFileName(fileList[0].getName());
            String outputFileName = getFileName(fileList[0].getName());
            mergedOutputFilePath = tmpOutputDirectory.getAbsolutePath() + File.separator + outputFileName + ".tar.gz";
            FileOutputStream mergedOutputStream = new FileOutputStream(mergedOutputFilePath);
            FileInputStream splittedFileInputStream = null;

            for (int sequence = 0; sequence < totalFile; sequence++)
            {
                File splittedFile = new File(tmpOutputDirectory.getAbsolutePath() + File.separator + mergedFileName + sequence + ".tar.gz");
                splittedFileInputStream = new FileInputStream(splittedFile);

                readWriteChunk(splittedFileInputStream, mergedOutputStream);

                splittedFileInputStream.close();
                splittedFileInputStream = null;
            }

            mergedOutputStream.flush();
            mergedOutputStream.close();

            removeAllSplittedFile(fileList);
        }
        return mergedOutputFilePath;
    }

    private static void readWriteChunk(InputStream is, OutputStream os) throws Exception
    {
        byte[] buffer = new byte[1024];

        int readCount = 0;

        while ((readCount = is.read(buffer)) != -1)
        {
            os.write(buffer, 0, readCount);
        }
    }

    /**
     * remove all splitted files
     * 
     * @param fileList
     */
    public static void removeAllSplittedFile(File[] fileList)
    {
        for (File file : fileList)
        {
            file.delete();
        }
    }

    /**
     * Ger merged file name
     * 
     * @param fileName
     * @return
     */
    public static String getMergedFileName(String fileName)
    {
        String mergedFileName = "";
        String nameToken[] = fileName.split("-");
        StringBuffer sb = new StringBuffer();
        if (nameToken.length >= 4)
        {
            for (int index = 0; index <= nameToken.length - 2; index++)
            {
                sb.append(nameToken[index] + "-");
            }
        }
        mergedFileName = sb.toString();
        return mergedFileName;
    }

    /**
     * Get file name
     * 
     * @param fileName
     * @return
     */
    public static String getFileName(String fileName)
    {
        String mergedFileName = "";
        String nameToken[] = fileName.split("-");
        StringBuffer sb = new StringBuffer();
        if (nameToken.length >= 4)
        {
            for (int index = 0; index <= nameToken.length - 4; index++)
            {
                sb.append(nameToken[index] + "-");
            }
        }
        mergedFileName = sb.substring(0, sb.length() - 1).toString();
        return mergedFileName;
    }

    /**
     * Perform decryption
     * 
     * @param outputFile
     * @throws Exception
     */
    public static void generateDecrypt(File tmpOutputDirectory) throws Exception
    {
        File fileList[] = tmpOutputDirectory.listFiles();
        for (File encryptedFile : fileList)
        {
            File newDecryptedFile = new File(encryptedFile.getAbsolutePath() + "_dec");
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] output = null;
            try
            {
//                SecretKey skey = new SecretKeySpec(BackupConstants.SEC_KEY.getBytes("UTF-8"), "AES");
//                AlgorithmParameterSpec paramSpec = new IvParameterSpec(BackupConstants.iv);
//                Cipher dcipher = Cipher.getInstance(BackupConstants.ALGORITHUM);

             //   dcipher.init(Cipher.DECRYPT_MODE, skey, paramSpec);

                fin = new FileInputStream(encryptedFile);
                fout = new FileOutputStream(newDecryptedFile);

                byte[] readByte = FileUtils.readFileToByteArray(encryptedFile);

//                output = dcipher.update(readByte, 0, readByte.length);

                fout.write(output);
                fout.flush();
                fout.close();
                fin.close();

                encryptedFile.delete();

                newDecryptedFile.renameTo(encryptedFile);
            }
            catch (IOException e)
            {
                throw e;
            }
            finally
            {
                try
                {
                    if (fin != null)
                        fin.close();
                    if (fout != null)
                        fout.close();
                }
                catch (IOException ex)
                {
                    throw ex;
                }
            }
        }
    }

    /**
     * It will Untar Tar file
     * 
     * @param outputFolder
     * @throws Exception
     */
    public static void unTarFile(File tmpOutputDirectory) throws Exception
    {
        File[] fileList = tmpOutputDirectory.listFiles();
        for (File inputFile : fileList)
        {
            /* read TAR File into TarArchiveInputStream */
            TarArchiveInputStream tarArchiveIS = new TarArchiveInputStream(new FileInputStream(inputFile));
            /* To read individual TAR file */
            TarArchiveEntry entry = null;
            String individualFiles;
            int offset;
            FileOutputStream outputFile = null;
            /* Create a loop to read every single entry in TAR file */
            while ((entry = tarArchiveIS.getNextTarEntry()) != null)
            {
                /* Get the name of the file */
                individualFiles = entry.getName();
                /* Get Size of the file and from a byte array for the size */
                byte[] content = new byte[(int) entry.getSize()];
                offset = 0;
                /* Some SOP statements to check progress */
                System.out.println("File Name in TAR File is: " + individualFiles);
                System.out.println("Size of the File is: " + entry.getSize());
                System.out.println("Byte Array length: " + content.length);
                /* read file from the archive into byte array */
                tarArchiveIS.read(content, offset, content.length - offset);

                /* Define OutputStream for writing the file */
                File tempfile = new File(tmpOutputDirectory + File.separator + individualFiles);
                System.out.println("File : " + tempfile.getAbsolutePath());
                System.out.println("Exists : " + tempfile.exists());
                System.out.println("Is Directory : " + tempfile.isDirectory());
                if (!tempfile.exists() && content.length == 0)
                {
                    tempfile.mkdirs();
                }
                else
                {
                    outputFile = new FileOutputStream(tempfile);
                    IOUtils.write(content, outputFile);
                    outputFile.close();
                }
            }
            /* Close TarAchiveInputStream */
            tarArchiveIS.close();
            inputFile.delete();
        }
    }

    /**
     * It will unzip file
     * 
     * @param outputFolder
     * @throws Exception
     */
    public static void unZipFile(File tmpOutputDirectory) throws Exception
    {
        File fileList[] = tmpOutputDirectory.listFiles();
        for (File compressedFile : fileList)
        {
            if (compressedFile.isDirectory())
            {
                unZipFile(compressedFile);
            }
            else
            {
                deCompressFile(compressedFile);
            }
        }
    }

    /**
     * It will uncompress file
     * 
     * @param compressedFile
     * @throws Exception
     */
    public static void deCompressFile(File compressedFile) throws Exception
    {
        File outputfile = new File(compressedFile.getAbsoluteFile() + "_unzip");
        FileOutputStream outputFileStream = new FileOutputStream(outputfile);
        GZIPInputStream gzipis = new GZIPInputStream(new FileInputStream(compressedFile));

        readWriteChunk(gzipis, outputFileStream);

        outputFileStream.flush();
        outputFileStream.close();

        gzipis.close();

        compressedFile.delete();

        outputfile.renameTo(compressedFile);
    }
}
