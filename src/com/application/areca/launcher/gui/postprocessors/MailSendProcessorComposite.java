package com.application.areca.launcher.gui.postprocessors;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ApplicationException;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.postprocess.MailSendPostProcessor;
import com.application.areca.postprocess.PostProcessor;

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
public class MailSendProcessorComposite extends AbstractProcessorComposite {

    private Text txtRecipients;
    private Text txtSmtp;
    private Text txtUser;
    private Text txtPassword;
    private Button btnTest;
    
    public MailSendProcessorComposite(Composite composite, PostProcessor proc, ProcessorEditionWindow window) {
        super(composite, proc, window);
        this.setLayout(new GridLayout(2, false));
        
        // Recipients
        Label lblRecipients = new Label(this, SWT.NONE);
        lblRecipients.setText(RM.getLabel("procedition.recipients.label"));
        
        txtRecipients = new Text(this, SWT.BORDER);
        txtRecipients.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        window.monitorControl(txtRecipients);
        
        // SMTP
        Label lblSmtp = new Label(this, SWT.NONE);
        lblSmtp.setText(RM.getLabel("procedition.smtp.label"));
        
        txtSmtp = new Text(this, SWT.BORDER);
        txtSmtp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        window.monitorControl(txtSmtp);
        
        // User
        Label lblUser = new Label(this, SWT.NONE);
        lblUser.setText(RM.getLabel("procedition.user.label"));
        
        txtUser = new Text(this, SWT.BORDER);
        txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        window.monitorControl(txtUser);
        
        // Password
        Label lblPassword = new Label(this, SWT.NONE);
        lblPassword.setText(RM.getLabel("procedition.password.label"));
        
        txtPassword = new Text(this, SWT.BORDER);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        window.monitorControl(txtPassword);
        
        // Test
        new Label(this, SWT.NONE);
        btnTest = new Button(this, SWT.PUSH);
        btnTest.setText(RM.getLabel("procedition.smtp.test"));
        btnTest.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnTest.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                
                try {
                    MailSendPostProcessor testProc = new MailSendPostProcessor();
                    initProcessor(testProc);
                    testProc.sendMail("Areca mail report test", "Areca mail report test successfull !", ps);
                    Application.getInstance().showInformationDialog(baos.toString(), RM.getLabel("procedition.ok.label"));                
                } catch (ApplicationException e1) {
                    Application.getInstance().showErrorDialog(e1.getMessage() + "\n\n" + baos.toString(), RM.getLabel("procedition.error.label"));
                }
            }
        });
        
        if (proc != null) {
            MailSendPostProcessor mProc = (MailSendPostProcessor)proc;
            txtRecipients.setText(mProc.getRecipients());
            txtSmtp.setText(mProc.getSmtpServer());
            txtUser.setText(mProc.getUser());
            txtPassword.setText(mProc.getPassword());
        }
    }

    public void initProcessor(PostProcessor proc) {
        MailSendPostProcessor mProc = (MailSendPostProcessor)proc;
        mProc.setRecipients(txtRecipients.getText());
        mProc.setSmtpServer(txtSmtp.getText());
        mProc.setUser(txtUser.getText());
        mProc.setPassword(txtPassword.getText());
    }
    
    public boolean validateParams() {
        this.window.resetErrorState(txtRecipients);
        this.window.resetErrorState(txtSmtp);
        
        if (txtRecipients.getText() == null || txtRecipients.getText().trim().length() == 0) {
            this.window.setInError(txtRecipients);
            this.btnTest.setEnabled(false);
            return false;
        }
        
        if (txtSmtp.getText() == null || txtSmtp.getText().trim().length() == 0) {
            this.window.setInError(txtSmtp);
            this.btnTest.setEnabled(false);
            return false;
        }

        this.btnTest.setEnabled(true);
        return true;
    }
}