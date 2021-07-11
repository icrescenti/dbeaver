/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Jun 30, 2021
 */
package org.jkiss.dbeaver.erd.ui.action;

import org.eclipse.jface.action.Action;
import org.jkiss.dbeaver.erd.ui.editor.ERDEditorEmbedded;
import org.jkiss.dbeaver.erd.ui.internal.ERDUIActivator;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.eclipse.swt.widgets.FileDialog;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.jkiss.utils.IOUtils;
import java.io.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.Log;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.dbeaver.erd.ui.model.DiagramLoader;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.model.virtual.DBVObject;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.plaf.TreeUI;

/**
 * Action to toggle diagram persistence
 */
public class DiagramImportAction extends Action {

    private static final Log log = Log.getLog(DiagramImportAction.class);

    private final ERDEditorEmbedded editor;
    private Shell shell;
    private final DBVObject virtualObject;

    public DiagramImportAction(ERDEditorEmbedded editor, Shell shell, DBVObject virtualObject) {
        super("Import diagram", DBeaverIcons.getImageDescriptor(UIIcon.IMPORT));
        setDescription("Import ERD file into a diagram");
        setToolTipText(getDescription());
        this.editor = editor;
        this.shell = shell;
        this.virtualObject = virtualObject;
    }

    @Override
    public void run() {
        try {
            String path = null;
            FileDialog dialog = new FileDialog( shell, SWT.OPEN );
            String[] filterExt = {"*.erd"};
            dialog.setFilterExtensions(filterExt);
            path = dialog.open();
            
            if ( path != null ) {
                DBVObject vObject = virtualObject;
                if (vObject == null) {
                    return;
                }
                Map<String, Object> diagramStateMap = new LinkedHashMap<>();
                vObject.setProperty("erd.diagram.state", diagramStateMap);

                try (final Reader isr = new InputStreamReader(new FileInputStream(path), GeneralUtils.UTF8_CHARSET)) {
                    StringWriter buf = new StringWriter();
                    IOUtils.copyText(isr, buf);
                    String diagramState = buf.toString(); 
                    diagramStateMap.put("serialized", diagramState);
                    
                    vObject.persistConfiguration();
                    editor.getCommandStack().markSaveLocation();
                }
                catch (Exception e) {
                    log.error("Error loading ER diagram from '" + path + "'", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error loading diagram", e);
        }
    }

}