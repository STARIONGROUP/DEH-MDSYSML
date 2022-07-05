/*
 * MappingListViewAction.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Actions.ToolBar;


import java.awt.event.ActionEvent;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jidesoft.docking.DockingManager;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.MainFrame;

import App.AppContainer;
import Utils.ImageLoader.ImageLoader;
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;
import Views.MagicDrawHubBrowserPanel;
import Views.MagicDrawMappingListViewPanel;

/**
 * The {@linkplain MappingListViewAction} is the {@link MDAction} that shows the {@linkplain MappingListView}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappingListViewAction extends MDAction
{ 
    /**
     * The current class logger
     */
    private transient Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain MagicDrawMappingListView}
     */
    private MagicDrawMappingListViewPanel mappingListView;

    /**
     * Initializes a new {@link LocalExchangeHistoryAction}
     */
    public MappingListViewAction()
    {
        super("View the Mapping List View", "View the Mapping List View", null, null);
        this.setLargeIcon(ImageLoader.GetIcon("icon16.png"));
        this.mappingListView = new MagicDrawMappingListViewPanel();
        this.mappingListView.SetDataContext(AppContainer.Container.getComponent(IMappingListViewViewModel.class));
    }

    /**
     * Commands the {@link MagicDrawHubBrowserPanel} to show or hide
     * 
     * @param actionEvent The {@link ActionEvent} that originated the action performed. This parameter is unused.
     */
     @Override
     public void actionPerformed(ActionEvent actionEvent)
     {            
         try
         {
             Application applicationInstance = Application.getInstance();
             MainFrame mainFrame = applicationInstance.getMainFrame();
             DockingManager dockingManager = mainFrame.getDockingManager();
             Collection<String> allFrames = dockingManager.getAllFrames();

             for (String string : allFrames)
             {
                 this.logger.debug(String.format("FRAME => [%s]", string));
             }

             boolean isPanelPresent = false;

             for(String key : allFrames)
             {
                 if(key.equals(this.mappingListView.GetPanelDockKey()))
                 {
                     this.mappingListView.ShowHide(dockingManager);
                     isPanelPresent = true;
                 }
             }

             if(!isPanelPresent)
             {
                 dockingManager.addFrame(this.mappingListView);
             }
         }
         catch (Exception exception) 
         {
             this.logger.error(String.format("LocalExchangeHistoryAction actionPerformed has thrown an exception %s %n %s", exception.toString(), exception.getStackTrace()));
             throw exception;
         }
     }
}
