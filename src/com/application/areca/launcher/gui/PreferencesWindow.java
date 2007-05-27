package com.application.areca.launcher.gui;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ResourceManager;
import com.application.areca.launcher.gui.common.SavePanel;
import com.myJava.file.FileTool;

/**
 * <BR>
 * @author Stephane BRUNEL
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
public class PreferencesWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Combo langCombo;
    private Button openLastWorkspace;
    private Button openDefaultWorkspace;
    private Text defaultWorkspace;
    private Text defaultArchiveStorage;
    private Button displayReport;

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 20;
        composite.setLayout(layout);
        
        GridData mainData1 = new GridData();
        mainData1.grabExcessHorizontalSpace = true;
        mainData1.widthHint = computeWidth(600);
        mainData1.horizontalAlignment = SWT.FILL;
        mainData1.verticalAlignment = SWT.FILL;
        
        Group grpDisp = new Group(composite, SWT.NONE);
        grpDisp.setLayoutData(mainData1);
        grpDisp.setText(RM.getLabel("preferences.appearence.title"));
        GridLayout layDisp = new GridLayout();
        layDisp.numColumns = 2;
        grpDisp.setLayout(layDisp);
        buildAppearenceComposite(grpDisp);
        
        GridData mainData2 = new GridData();
        mainData2.grabExcessHorizontalSpace = true;
        mainData2.horizontalAlignment = SWT.FILL;
        mainData2.verticalAlignment = SWT.FILL;
        
        Group grpStart = new Group(composite, SWT.NONE);
        grpStart.setLayoutData(mainData2);
        grpStart.setText(RM.getLabel("preferences.startup.title"));
        GridLayout layStart = new GridLayout();
        layStart.numColumns = 3;
        grpStart.setLayout(layStart);
        buildStartupComposite(grpStart);
        
        GridData mainData3 = new GridData();
        mainData3.grabExcessHorizontalSpace = true;
        mainData3.horizontalAlignment = SWT.FILL;
        mainData3.verticalAlignment = SWT.FILL;
        
        Group grpArchives = new Group(composite, SWT.NONE);
        grpArchives.setLayoutData(mainData3);
        grpArchives.setText(RM.getLabel("preferences.workspace.title"));
        GridLayout layArchives = new GridLayout();
        layArchives.numColumns = 3;
        grpArchives.setLayout(layArchives);
        buildArchivesComposite(grpArchives);
        
        GridData saveData = new GridData();
        saveData.grabExcessHorizontalSpace = true;
        saveData.horizontalAlignment = SWT.FILL;
        saveData.verticalAlignment = SWT.FILL;
        
        SavePanel pnlSave = new SavePanel(this);
        pnlSave.buildComposite(composite).setLayoutData(saveData);
        
        composite.pack();
        return composite;
    }
    
    private void buildAppearenceComposite(Composite parent) {
        Label lblLang = new Label(parent, SWT.NONE);
        lblLang.setText(RM.getLabel("preferences.lang.label"));
        
        langCombo = new Combo(parent, SWT.READ_ONLY);
        fillLangCombo();
        monitorControl(langCombo);
    }
    
    private void buildStartupComposite(Composite parent) {
        GridData dtLast = new GridData();
        dtLast.horizontalSpan = 3;
        openLastWorkspace = new Button(parent, SWT.RADIO);
        openLastWorkspace.setText(RM.getLabel("preferences.lastworkspace.label"));
        openLastWorkspace.setLayoutData(dtLast);
        
        openDefaultWorkspace = new Button(parent, SWT.RADIO);
        openDefaultWorkspace.setText(RM.getLabel("preferences.opendefaultworkspace.label"));
        int startupMode = ArecaPreferences.getStartupMode();
        if (startupMode == ArecaPreferences.UNDEFINED || startupMode == ArecaPreferences.LAST_WORKSPACE_MODE) {
            openLastWorkspace.setSelection(true);
        } else {
            openDefaultWorkspace.setSelection(true);
        }
        monitorControl(openDefaultWorkspace);
        
        defaultWorkspace = new Text(parent, SWT.BORDER);
        GridData ldWs = new GridData();
        ldWs.grabExcessHorizontalSpace = true;
        ldWs.horizontalAlignment = SWT.FILL;
        defaultWorkspace.setLayoutData(ldWs);
        defaultWorkspace.setText(ArecaPreferences.getDefaultWorkspace());
        monitorControl(defaultWorkspace);
        
        Button btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(defaultWorkspace.getText(), PreferencesWindow.this);
                if (path != null) {
                    defaultWorkspace.setText(path);
                }
            }
        });
    }
    
    private void buildArchivesComposite(Composite parent) {
        Label lblDefaultStorage = new Label(parent, SWT.NONE);
        lblDefaultStorage.setText(RM.getLabel("preferences.defaultstorage.label"));
        
        defaultArchiveStorage = new Text(parent, SWT.BORDER);
        GridData ldWs = new GridData();
        ldWs.grabExcessHorizontalSpace = true;
        ldWs.horizontalAlignment = SWT.FILL;
        defaultArchiveStorage.setLayoutData(ldWs);
        defaultArchiveStorage.setText(ArecaPreferences.getDefaultArchiveStorage());
        monitorControl(defaultArchiveStorage);
        
        Button btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(defaultArchiveStorage.getText(), PreferencesWindow.this);
                if (path != null) {
                    defaultArchiveStorage.setText(path);
                }
            }
        });
        
        displayReport = new Button(parent, SWT.CHECK);
        displayReport.setText(RM.getLabel("preferences.displayreport.label"));
        displayReport.setSelection(ArecaPreferences.getDisplayReport());
        monitorControl(displayReport);
    }
    
    private void fillLangCombo() {
        try {
            URL url = ClassLoader.getSystemResource("languages");
            FileTool tool = new FileTool();
            String[] lges = tool.getInputStreamRows(url.openStream(), true); 
        
            for (int i=0; i<lges.length; i++) {
                langCombo.add(lges[i]);
                
                if (lges[i].equals(ArecaPreferences.getLang())) {
                    langCombo.select(i);
                }
            }
        } catch (Exception ex) {
            this.application.handleException(ex);
        }
    }

    public String getTitle() {
        return RM.getLabel("preferences.dialog.title");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {       
        String lang = (String)langCombo.getItem(langCombo.getSelectionIndex());
        ArecaPreferences.setLang(lang);
        ArecaPreferences.setStartupMode(openLastWorkspace.getSelection() ? ArecaPreferences.LAST_WORKSPACE_MODE : ArecaPreferences.DEFAULT_WORKSPACE_MODE);
        ArecaPreferences.setDefaultWorkspace(defaultWorkspace.getText());
        ArecaPreferences.setDefaultArchiveStorage(defaultArchiveStorage.getText());
        ArecaPreferences.setDisplayReport(displayReport.getSelection());
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}