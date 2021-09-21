/*
 * MDHubBrowserPanel.java
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
package Views;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;

import App.AppContainer;
import Services.NavigationService.INavigationService;
import Utils.ImageLoader.ImageLoader;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import ViewModels.Interfaces.IViewModel;
import Views.Interfaces.IView;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.junit.Ignore;

/**
 * The {@linkplain MDHubBrowserPanel} is the {@linkplain HubBrowserPanel} for the MagicDraw / Cameo software
 */
@SuppressWarnings("serial")
public class MDHubBrowserPanel extends DockableFrame implements IView<IHubBrowserPanelViewModel>
{
    /**
     * The key that identify this view in the dock layout manager of the MagicDraw / Cameo software
     */
    public static final String PanelDockKey = "DEH MagicDraw Adapter - HubBrowserPanel";
    
    /**
     * An assert whether this view is visible
     */
    private boolean isVisibleInTheDock = true;
    
    /**
     * The {@link INavigationService}
     */
    protected INavigationService navigationService;

    /**
     * The {@link IHubBrowserPanelViewModel} as the data context of this view
     */
    private IHubBrowserPanelViewModel dataContext;
    
    /**
     * The {@linkplain HubBrowserPanel} this view wraps
     */
    private HubBrowserPanel hubBrowserPanel;
    
    /**
     * Initializes a new {@linkplain MDHubBrowserPanel}
     */
    public MDHubBrowserPanel()
    {
        setKey(PanelDockKey);
        setTabTitle("Hub Browser");
        setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(CLOSE_ACTION_TO_HIDE);
        this.hubBrowserPanel = new HubBrowserPanel();
        getRootPane().getContentPane().add(this.hubBrowserPanel);
    }

    /**
     * Show or Hide this {@link MDHubBrowserPanel}
     * 
     * @param dockingManager The {@link DockingManager} that is allowed to hide or show this frame
     */
    public void ShowHide(DockingManager dockingManager)
    {
        if(this.isVisibleInTheDock)
        {
            dockingManager.hideFrame(PanelDockKey);
            this.isVisibleInTheDock = false;
        }
        else
        {
            dockingManager.showFrame(PanelDockKey);
            this.isVisibleInTheDock = true;
        }
    }
    
    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
        this.hubBrowserPanel.ConnectButton().addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                if(!dataContext.GetIsConnected())
                {
                    Boolean connectionDialogResult= dataContext.Connect();
                    if(connectionDialogResult != null && connectionDialogResult)
                    {
                        hubBrowserPanel.ConnectButton().setText("Disconnect");
                    }
                }
                else
                {
                    dataContext.Disconnect();
                    hubBrowserPanel.ConnectButton().setText("Connect");
                }
            }
        });
        
        this.hubBrowserPanel.getHubBrowserHeader().SetDataContext(this.dataContext.GetHubBrowserHeaderViewModel());
        this.hubBrowserPanel.GetElementDefinitionBrowser().SetDataContext(this.dataContext.GetElementDefinitionBrowserViewModel());
        this.hubBrowserPanel.GetRequirementBrowser().SetDataContext(this.dataContext.GetRequirementBrowserViewModel());
    }
    
    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @Override
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (IHubBrowserPanelViewModel)viewModel;   
        this.Bind();
    }

    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public IHubBrowserPanelViewModel GetDataContext()
    {
        return this.dataContext;
    }
}
