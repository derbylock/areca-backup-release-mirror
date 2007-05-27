package com.application.areca.launcher.gui.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.application.areca.launcher.gui.Application;

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
public class CTabFolderManager 
extends RefreshManager {
    private boolean parentRegistered = false;
    private CTabItem selected = null;

    public void registerTabItem(final CTabItem item) {
        item.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                unregisterRefreshable(getRefreshable(item));
            }
        });

        if (! parentRegistered) {
            parentRegistered = true;
            
            item.getParent().addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    handleSelection((CTabItem)event.item);
                }
            });
        }

        this.registerRefreshable(getRefreshable(item));
    }
    
    public void handleSelection(CTabItem item) {
        if (selected != null) {
            lostFocus(getRefreshable(selected));
        }

        selected = item; 
        getFocus(getRefreshable(selected));
    }

    public Refreshable getRefreshable(CTabItem item) {
        return (Refreshable)item.getControl();
    }
    
    public boolean references(CTabItem item) {
        return this.monitors.containsKey(getRefreshable(item).getRefreshableKey());
    }

    public void getFocus(Refreshable refreshable) {
        try {
            Application.getInstance().enableWaitCursor();
            super.getFocus(refreshable);
        } finally {
            Application.getInstance().disableWaitCursor();
        }
    }
}