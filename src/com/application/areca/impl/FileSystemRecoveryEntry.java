package com.application.areca.impl;

import java.io.File;

import com.application.areca.RecoveryEntry;
import com.application.areca.Utils;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;

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
public class FileSystemRecoveryEntry implements RecoveryEntry {
    private File file;
    private File rootDirectory;
    private short status;
    private long size;
    private String name;
    
    public FileSystemRecoveryEntry(File rootDirectory, File file, short status, long size) {
        this.rootDirectory = rootDirectory;            
        this.file = file;
        this.status = status;
        this.size = size;
        this.name = Utils.extractShortFilePath(this.getFile(), this.getRootDirectory());
    }
    
    public FileSystemRecoveryEntry(File rootDirectory, File file, short status) {
    	this(rootDirectory, file, status, 0);
    }
    
    public FileSystemRecoveryEntry(File rootDirectory, File file) {
    	this(rootDirectory, file, STATUS_NOT_STORED);
    }
    
    public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public short getStatus() {
		return status;
	}

	public File getFile() {
        return this.file;
    }
    
    public File getRootDirectory() {
        return this.rootDirectory;
    }
    
    public String toString() {
        return getName();
    }

	public String getName() {
	    return name;
	}
	
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof FileSystemRecoveryEntry)) {
            return false;
        } else {
            FileSystemRecoveryEntry other = (FileSystemRecoveryEntry)obj;
            return
            	EqualsHelper.equals(other.name, this.name);
        }
    }
    
    public int hashCode() {
        int hash = HashHelper.initHash(this);
        hash = HashHelper.hash(hash, this.name);
        return hash;
    }
}