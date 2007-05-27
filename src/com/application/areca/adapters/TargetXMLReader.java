package com.application.areca.adapters;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveFilter;
import com.application.areca.ArchiveMedium;
import com.application.areca.RecoveryProcess;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileDateArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.LinkFilter;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalTGZMedium;
import com.application.areca.impl.IncrementalZip64Medium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.policy.DefaultFileSystemPolicy;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.postprocess.FileDumpPostProcessor;
import com.application.areca.postprocess.MailSendPostProcessor;
import com.application.areca.postprocess.MergePostProcessor;
import com.application.areca.postprocess.PostProcessor;
import com.application.areca.postprocess.ShellScriptPostProcessor;

/**
 * Adaptateur pour la s�rialisation / d�s�rialisation XML.
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
public class TargetXMLReader implements XMLTags {
    protected RecoveryProcess process;
    protected Node targetNode;
    protected MissingDataListener missingDataListener = null;
    
    public TargetXMLReader(Node targetNode, RecoveryProcess process) throws AdapterException {
        this.targetNode = targetNode;
        this.process = process;
    }
    
    public void setMissingDataListener(MissingDataListener missingDataListener) {
        this.missingDataListener = missingDataListener;
    }
    
    public FileSystemRecoveryTarget readTarget() throws IOException, AdapterException, ApplicationException {
        Node baseDir = targetNode.getAttributes().getNamedItem(XML_TARGET_BASEDIR);
        Node id = targetNode.getAttributes().getNamedItem(XML_TARGET_ID);
        Node uid = targetNode.getAttributes().getNamedItem(XML_TARGET_UID);        
        Node name = targetNode.getAttributes().getNamedItem(XML_TARGET_NAME);        
        
        if (id == null) {
            throw new AdapterException("Target ID not found : your target must have a '" + XML_TARGET_ID + "' attribute.");
        }   
        
        if (baseDir == null) {
            throw new AdapterException("Target source directory not found : your target must have a '" + XML_TARGET_BASEDIR + "' attribute.");
        }      
        
        String strUid = null;
        if (uid != null) {
            strUid = uid.getNodeValue();
        }
        
        FileSystemRecoveryTarget target = new FileSystemRecoveryTarget();
        target.setSourcePath(new File(baseDir.getNodeValue()));
        target.setId(Integer.parseInt(id.getNodeValue()));
        target.setUid(strUid);
        target.setProcess(process);

        if (name != null) {
            target.setTargetName(name.getNodeValue());
        }
        
        Node commentsNode = targetNode.getAttributes().getNamedItem(XML_TARGET_DESCRIPTION);
        if (commentsNode != null) {
            target.setComments(commentsNode.getNodeValue());
        }            
        
        NodeList children = targetNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            String child = children.item(i).getNodeName();
            if (child.equalsIgnoreCase(XML_FILTER_DIRECTORY)) {
                target.addFilter(this.readDirectoryArchiveFilter(children.item(i)));
            } else if (child.equalsIgnoreCase(XML_FILTER_FILEEXTENSION)) {
                target.addFilter(this.readFileExtensionArchiveFilter(children.item(i)));   
            } else if (child.equalsIgnoreCase(XML_FILTER_REGEX)) {
                target.addFilter(this.readRegexArchiveFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_FILESIZE)) {
                target.addFilter(this.readFileSizeArchiveFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_LINK)) {
                target.addFilter(this.readLinkFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_LOCKED)) {
                target.addFilter(this.readLockedFileFilter(children.item(i)));                  
            } else if (child.equalsIgnoreCase(XML_FILTER_FILEDATE)) {
                target.addFilter(this.readFileDateArchiveFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_MEDIUM)) {
                target.setMedium(this.readMedium(children.item(i), target), false);      
                target.getMedium().install();
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_DUMP)) {
                target.getPostProcessors().addPostProcessor(this.readDumpProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_EMAIL)) {
                target.getPostProcessors().addPostProcessor(this.readEmailProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_SHELL)) {
                target.getPostProcessors().addPostProcessor(this.readShellProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_MERGE)) {
                target.getPostProcessors().addPostProcessor(this.readMergeProcessor(children.item(i), target));                
            }
        }
        
        return target;
    }
    
    protected PostProcessor readDumpProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_DUMP_DIRECTORY);
        if (paramNode == null) {
            throw new AdapterException("Dump directory not found for File Dump Processor. A '" + XML_PP_DUMP_DIRECTORY + "' attribute must be set.");
        }          
        FileDumpPostProcessor pp = new FileDumpPostProcessor();
        pp.setDestinationFolder(new File(paramNode.getNodeValue()));
        return pp;
    }
    
    protected PostProcessor readShellProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_SHELL_SCRIPT);
        if (paramNode == null) {
            throw new AdapterException("Shell script file not found for Shell Processor. A '" + XML_PP_SHELL_SCRIPT + "' attribute must be set.");
        }          
        ShellScriptPostProcessor pp = new ShellScriptPostProcessor();
        pp.setCommand(paramNode.getNodeValue());
        return pp;
    }
    
    protected PostProcessor readMergeProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_MERGE_DELAY);
        if (paramNode == null) {
            throw new AdapterException("Merge delay not found for merge processor. A '" + XML_PP_MERGE_DELAY + "' attribute must be set.");
        }          
        MergePostProcessor pp = new MergePostProcessor();
        pp.setDelay(Integer.parseInt(paramNode.getNodeValue()));
        return pp;
    }
    
    protected PostProcessor readEmailProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        MailSendPostProcessor pp = new MailSendPostProcessor();
        
        Node recipientsNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_RECIPIENTS);
        if (recipientsNode == null) {
            throw new AdapterException("Recipient list not found for Email Processor. A '" + XML_PP_EMAIL_RECIPIENTS + "' attribute must be set.");
        }         
        Node smtpNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_SMTP);
        if (smtpNode == null) {
            throw new AdapterException("Smtp host not found for Email Processor. A '" + XML_PP_EMAIL_SMTP + "' attribute must be set.");
        }       
        
        Node userNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_USER);
        if (userNode != null) {
            pp.setUser(userNode.getNodeValue());
        }
        Node passwordNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_PASSWORD);
        if (passwordNode != null) {
            pp.setPassword(passwordNode.getNodeValue());
        }
        
        pp.setRecipients(recipientsNode.getNodeValue());
        pp.setSmtpServer(smtpNode.getNodeValue());        
        return pp;
    }
    
    protected ArchiveMedium readMedium(Node mediumNode, AbstractRecoveryTarget target) throws IOException, AdapterException, ApplicationException {
        Node typeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TYPE);
        if (typeNode == null) {
            throw new AdapterException("Medium type not found : your medium must have a '" + XML_MEDIUM_TYPE + "' attribute.");
        }           
        
        Node overwriteNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_OVERWRITE);
        boolean isOverwrite = (overwriteNode != null && overwriteNode.getNodeValue().equalsIgnoreCase("true"));    
        
        Node trackDirsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_DIRS);
        boolean trackDirs = (trackDirsNode != null && trackDirsNode.getNodeValue().equalsIgnoreCase("true"));   
        
        Node trackPermsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_PERMS);
        boolean trackPerms = (trackPermsNode != null && trackPermsNode.getNodeValue().equalsIgnoreCase("true"));   

        AbstractIncrementalFileSystemMedium medium;
        
        if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP)) {
            medium = new IncrementalZipMedium();                    
        } else if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP64)) {
            medium = new IncrementalZip64Medium();        
        } else if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_TGZ)) {
            medium = new IncrementalTGZMedium();                        
        } else if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_DIR)) {
            medium = new IncrementalDirectoryMedium();                
        }  else {
            throw new AdapterException("Unknown medium : " + typeNode.getNodeValue());
        }
        
        EncryptionPolicy encrArgs = readEncryptionPolicy(mediumNode, target);
        FileSystemPolicy storage = readFileSystemPolicy(mediumNode);
        medium.setFileSystemPolicy(storage);
        medium.setEncryptionPolicy(encrArgs);
        medium.setOverwrite(isOverwrite);
        medium.setTrackDirectories(trackDirs);
        medium.setTrackPermissions(trackPerms);        
        
        return medium;
    }
    
    protected FileSystemPolicy readFileSystemPolicy(Node mediumNode) throws IOException, AdapterException, ApplicationException {
        // First case : storage on local file system
        Node pathNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVEPATH);
        if (pathNode != null) {
            DefaultFileSystemPolicy policy = new DefaultFileSystemPolicy();
            policy.setBaseArchivePath(pathNode.getNodeValue());
            return policy;
        } 
        
        // Second case : storage on ftp host
        Node serverNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_HOST);
        Node portNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PORT);
        Node passivNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSIV);
        Node protocolNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PROTOCOL);
        Node implicitNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_IMPLICIT);
        Node loginNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_LOGIN);
        Node passwordNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_PASSWORD);
        Node dirNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FTP_REMOTEDIR);
        
        // No storage policy found
        if (serverNode == null && portNode == null && passivNode == null && loginNode == null && passwordNode == null && dirNode == null) {
            throw new AdapterException("Medium storage policy not found : your medium must have either a '" + XML_MEDIUM_ARCHIVEPATH + "' attribute or FTP attributes (" + XML_MEDIUM_FTP_HOST + ", " + XML_MEDIUM_FTP_LOGIN + ", " + XML_MEDIUM_FTP_PASSWORD + " ...)");            
        }
        
        // FTP policy initialization
        if (serverNode == null) {
            throw new AdapterException("FTP host not found : your medium must have a '" + XML_MEDIUM_FTP_HOST + "' attribute.");
        } 
        if (portNode == null) {
            throw new AdapterException("FTP remote port not found : your medium must have a '" + XML_MEDIUM_FTP_PORT + "' attribute.");
        } 
        if (loginNode == null) {
            throw new AdapterException("FTP login not found : your medium must have a '" + XML_MEDIUM_FTP_LOGIN + "' attribute.");
        } 
        if (passwordNode == null) {
            throw new AdapterException("FTP password not found : your medium must have a '" + XML_MEDIUM_FTP_PASSWORD + "' attribute.");
        } 
        if (dirNode == null) {
            throw new AdapterException("FTP remote directory not found : your medium must have a '" + XML_MEDIUM_FTP_REMOTEDIR + "' attribute.");
        } 

        FTPFileSystemPolicy policy = new FTPFileSystemPolicy();
        policy.setRemoteServer(serverNode.getNodeValue());
        policy.setRemotePort(Integer.parseInt(portNode.getNodeValue()));
        policy.setPassivMode(passivNode != null && passivNode.getNodeValue().equalsIgnoreCase("true"));
        if (protocolNode != null) {
            policy.setProtocol(protocolNode.getNodeValue());
            policy.setImplicit(implicitNode != null && implicitNode.getNodeValue().equalsIgnoreCase("true"));            
        }
        policy.setLogin(loginNode.getNodeValue());
        policy.setPassword(passwordNode.getNodeValue());
        policy.setRemoteDirectory(dirNode.getNodeValue());
        return policy;
    }
    
    protected EncryptionPolicy readEncryptionPolicy(Node mediumNode, AbstractRecoveryTarget target) throws IOException, AdapterException, ApplicationException {
        Node encryptedNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTED);
        boolean isEncrypted = (encryptedNode != null && encryptedNode.getNodeValue().equalsIgnoreCase("true"));   
        
        Node encryptionKeyNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONKEY);
        String encryptionKey = encryptionKeyNode != null ? encryptionKeyNode.getNodeValue() : null;   
        
        Node encryptionAlgoNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONALGO);
        String encryptionAlgo = encryptionAlgoNode != null ? encryptionAlgoNode.getNodeValue() : null;          
        
        if (isEncrypted && encryptionKey == null) { // No check for the encryptionAlgorithm because we use a default one if none is specified.
            if (this.missingDataListener != null) {
                Object[] encrData = (Object[])missingDataListener.missingEncryptionDataDetected(target);
                if (encrData != null) {
	                encryptionAlgo = (String)encrData[0];
	                encryptionKey = (String)encrData[1];
                }
            }
        }    
        
        if (isEncrypted && encryptionKey == null) { // Second check .... after missingDataListener invocation.
            throw new AdapterException("No encryption key found : your medium must have a '" + XML_MEDIUM_ENCRYPTIONKEY + "' attribute because it is encrypted (" + XML_MEDIUM_ENCRYPTED + " = true).");
        }
        
        EncryptionPolicy encrArgs = new EncryptionPolicy();
        encrArgs.setEncrypted(isEncrypted);
        encrArgs.setEncryptionAlgorithm(encryptionAlgo);
        encrArgs.setEncryptionKey(encryptionKey);
        
        return encrArgs;
    }
    
    protected FileDateArchiveFilter readFileDateArchiveFilter(Node filterNode) throws AdapterException {
        Node paramNode = filterNode.getAttributes().getNamedItem(XML_FILTER_PARAM);
        if (paramNode == null) {
            throw new AdapterException("Filter date not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg '2006_07_17').");
        }          
        FileDateArchiveFilter filter = new FileDateArchiveFilter();
        initFilter(filter, filterNode, paramNode);
        return filter;
    }
    
    protected FileSizeArchiveFilter readFileSizeArchiveFilter(Node filterNode) throws AdapterException {
        Node paramNode = filterNode.getAttributes().getNamedItem(XML_FILTER_PARAM);
        if (paramNode == null) {
            throw new AdapterException("Maximum size not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg '1024').");
        }          
        FileSizeArchiveFilter filter = new FileSizeArchiveFilter();
        initFilter(filter, filterNode, paramNode);
        return filter;
    }
    
    protected LinkFilter readLinkFilter(Node filterNode) throws AdapterException {     
        LinkFilter filter = new LinkFilter();
        initFilter(filter, filterNode, null);
        return filter;
    }
    
    protected LockedFileFilter readLockedFileFilter(Node filterNode) throws AdapterException {        
        LockedFileFilter filter = new LockedFileFilter();
        initFilter(filter, filterNode, null);
        return filter;
    }
    
    protected RegexArchiveFilter readRegexArchiveFilter(Node filterNode) throws AdapterException {    
        Node regexNode = filterNode.getAttributes().getNamedItem(XML_FILTER_RG_PATTERN);
        if (regexNode == null) {
            throw new AdapterException("Regex not found : your filter must have a '" + XML_FILTER_RG_PATTERN + "' attribute.");
        }          
        RegexArchiveFilter filter = new RegexArchiveFilter();
        initFilter(filter, filterNode, regexNode);
        return filter;
    }
    
    protected DirectoryArchiveFilter readDirectoryArchiveFilter(Node filterNode) throws AdapterException {
        Node directoryNode = filterNode.getAttributes().getNamedItem(XML_FILTER_DIR_PATH);
        if (directoryNode == null) {
            throw new AdapterException("Directory not found : your filter must have a '" + XML_FILTER_DIR_PATH + "' attribute.");
        }          
        DirectoryArchiveFilter filter = new DirectoryArchiveFilter();
        initFilter(filter, filterNode, directoryNode);
        return filter;
    }
    
    protected FileExtensionArchiveFilter readFileExtensionArchiveFilter(Node filterNode) throws AdapterException {
        FileExtensionArchiveFilter filter = new FileExtensionArchiveFilter();
        initFilter(filter, filterNode, null);
        
        NodeList children = filterNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            String nodeType = children.item(i).getNodeName();     
            if (nodeType.equalsIgnoreCase(XML_FILTER_EXTENSION)) {
                filter.addExtension(children.item(i).getChildNodes().item(0).getNodeValue());
            }
        }
        
        return filter;
    }    
    
    protected void initFilter(ArchiveFilter filter, Node filterNode, Node paramNode) {
        Node excludeNode = filterNode.getAttributes().getNamedItem(XML_FILTER_EXCLUDE);
        boolean isExclude = (excludeNode != null && excludeNode.getNodeValue().equalsIgnoreCase("true"));
        
        filter.setExclude(isExclude);
        if (paramNode != null) {
            filter.acceptParameters(paramNode.getNodeValue());
        }
    }
}