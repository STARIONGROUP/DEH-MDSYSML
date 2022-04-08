/*
 * OpenBrowserPanelAction.java
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
import DstController.IDstController;
import Utils.ImageLoader.ImageLoader;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewPanelViewModel;
import Views.MagicDrawHubBrowserPanel;
import Views.MagicDrawImpactViewPanel;

/**
 * The {@link OpenHubBrowserPanelAction} is a {@link MDAction} that can be added to one toolbar in Cameo or MagicDraw
 */
@SuppressWarnings("serial")
public class OpenHubBrowserPanelAction extends MDAction
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@link MagicDrawHubBrowserPanel} instance
     */
    private MagicDrawHubBrowserPanel hubBrowserPanel;

    /**
     * The {@link MagicDrawImpactViewPanel} instance
     */
    private MagicDrawImpactViewPanel impactViewPanel;
    
    /**
     * Initializes a new {@link OpenHubBrowserPanelAction}
     */
    public OpenHubBrowserPanelAction()
    {
         super("Hub Browser", "Open/Close the Hub Browser Panels", null, null);
         this.setLargeIcon(ImageLoader.GetIcon("icon16.png"));

         this.InitializesPanels();
         
         AppContainer.Container.getComponent(IDstController.class).HasOneDocumentOpenObservable().subscribe(x -> 
         {
             if(!x.booleanValue())
             {
                 this.InitializesPanels();
             }
         });
    }

    /**
     * Initializes the Hub panels
     */
    private void InitializesPanels()
    {
        this.hubBrowserPanel = new MagicDrawHubBrowserPanel();
        this.hubBrowserPanel.SetDataContext(AppContainer.Container.getComponent(IHubBrowserPanelViewModel.class));

        this.impactViewPanel = new MagicDrawImpactViewPanel();
        this.impactViewPanel.SetDataContext(AppContainer.Container.getComponent(IMagicDrawImpactViewPanelViewModel.class));
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
            
            boolean isHubBrowserPanelPresent = false, 
                    isImpactViewPanelPresent = false;
            
            for(String key : allFrames)
            {
                if(key == this.hubBrowserPanel.GetPanelDockKey())
                {
                    this.hubBrowserPanel.ShowHide(dockingManager);
                    isHubBrowserPanelPresent = true;
                }
                else if(key == this.impactViewPanel.GetPanelDockKey())
                {
                    this.impactViewPanel.ShowHide(dockingManager);
                    isImpactViewPanelPresent = true;
                }
            }
            
            if(!isImpactViewPanelPresent)
            {
                dockingManager.addFrame(this.impactViewPanel);
            }
            
            if(!isHubBrowserPanelPresent)
            {
                dockingManager.addFrame(this.hubBrowserPanel);
            }
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("OpenHubBrowserPanelAction actionPerformed has thrown an exception %s %n %s", exception.toString(), exception.getStackTrace()));
            throw exception;
        }
    }
}
