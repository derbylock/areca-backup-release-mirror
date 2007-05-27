package com.myJava.util.log;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.myJava.file.FileSystemManager;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class FileLogProcessor 
implements LogProcessor {
    
    private static SimpleDateFormat DF = new SimpleDateFormat("yy-MM-dd");
    
    /**
     *  Bool�en indiquant si on utilise un fichier unique ou si on utilise un fichier par jour
     */
    protected boolean uniqueFile;
    
    /**
     *  Chemin d'acc�s complet au fichier de log
     */
    private String fileName;
    
    /**
     * Process de nettoyage �ventuel de la log.
     */
    protected LogCleaner cleaner;
    
    private FileLogProcessor() {
        this.enableLogHistory(10);
    }
    
    public FileLogProcessor(String file) {
        this();
        this.fileName = file;
    }
    
    public FileLogProcessor(File file) {
        this();
        fileName = FileSystemManager.getAbsolutePath(file);
    }
    
    /**
     *  D�clenche l'historisation de la log.
     *  <BR>L'historique s'entend en jours.
     */
    public void enableLogHistory(int history) {
        this.uniqueFile = false;
        if (this.cleaner != null) {
            this.cleaner.stopTask();
        }
        
        this.cleaner = new LogCleaner(this, history);
        this.cleaner.startTask();
    }
    
    /**
     * Retourne le fichier de log courante
     */
    public String getCurrentLogFile() {
        if (! this.uniqueFile) {
            SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd");
            return this.fileName + "." + df.format(new Date());
        } else {
            return this.fileName;
        }
    }
    
    public void log(int level, String message, Throwable e, String source) {
        
        // Log compl�te :
        String logCt = LogHelper.format(level, message, source);
        
        // Ecriture de la log.
        try {
            String tgFile = this.fileName;
            
            // Gestion de l'historique de log
            if (! this.uniqueFile) {
                tgFile += "." + DF.format(new Date());
            }
            synchronized(this) { 
                Writer fw = FileSystemManager.getWriter(tgFile, true);
                fw.write("\n" + logCt);
                fw.flush();
                if (e != null) {
                    fw.write(" - ");
                    e.printStackTrace(new PrintWriter(fw, true));
                }
                fw.close();
            }
        } catch (Exception exc) {
            System.out.println(logCt);
            if (e != null) {
                e.printStackTrace();
            }
            System.out.println(" ");
            exc.printStackTrace();
        }
    }
    
    
    /**
     * Retourne le nom de base du fichier (sans la date, si on fonctionne en 
     * mode "historisation") 
     */
    public String getRootFileName() {
        return this.fileName;
    }
    
    /**
     * Retourne le r�pertoire de log
     */
    public File getLogDirectory() {
        if (this.fileName != null) {
            File f = new File(this.fileName);
            return FileSystemManager.getParentFile(f);
        } else {
            return null;
        }
    }
    
    /**
     * Efface le fichier de log.
     * <BR>Retourne true en cas de succ�s, false en cas d'�chec.
     */
    public synchronized boolean clearLog() {
        if (fileName != null) {
            File f = new File(fileName);
            return (FileSystemManager.delete(f));
        } else {
            return false;
        }
    }
}