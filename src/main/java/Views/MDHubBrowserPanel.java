/*
 * azeaf.java
 *
 * Copyright (c) 2015-2019 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of CDP4-SDKJ Community Edition
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
import Service.NavigationService.INavigationService;
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

@SuppressWarnings("serial")
public class MDHubBrowserPanel extends DockableFrame implements IView<IHubBrowserPanelViewModel>
{
    public static final String PanelDockKey = "DEH MagicDraw Adapter - HubBrowserPanel";
    
    private boolean isVisibleInTheDock;
    
    protected INavigationService navigationService;

    private IHubBrowserPanelViewModel dataContext;
    
    private HubBrowserPanel hubBrowserPanel;
    
    /**
     * Initializes a new {@linkplain MDHubBrowserPanel}
     */
    public MDHubBrowserPanel()
    {
        //initialize();
        setKey(PanelDockKey);
        setTabTitle("Hub Browser");
        setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(JFrame.HIDE_ON_CLOSE);
        this.hubBrowserPanel = new HubBrowserPanel();
        getRootPane().getContentPane().add(this.hubBrowserPanel);
    }

    /**
     * Show or Hide this {@link MDHubBrowserPanel}
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

    @Override
    public void Bind(IHubBrowserPanelViewModel viewModel)
    {
        this.hubBrowserPanel.ConnectButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                if(dataContext.ConnectButtonAction())
                {
                    hubBrowserPanel.ConnectButton.setText("Disconnect");
                }
            }
        });        
    }
    
    @Override
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (IHubBrowserPanelViewModel)viewModel;   
        this.Bind(dataContext);
    }

    @Override
    public IHubBrowserPanelViewModel GetDataContext()
    {
        return this.dataContext;
    }
}
