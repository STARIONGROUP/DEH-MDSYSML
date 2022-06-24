/*
 * MagicDrawBasePanel.java
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

import javax.swing.JPanel;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;

import Services.NavigationService.INavigationService;
import ViewModels.Interfaces.IViewModel;
import Views.Interfaces.IView;

/**
 * The {@linkplain MagicDrawBasePanel} is the base class for all MagicDraw/Cameo panels such as the Impact view
 * 
 * @param <TViewModel> the type of the view model the inheriting view belongs to
 * @param <TView> the type of the view that is enclosed by the inheriting v iew
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public abstract class MagicDrawBasePanel<TViewModel extends IViewModel, TView extends JPanel> extends DockableFrame implements IView<TViewModel>
{
    /**
     * The key that identify this view in the dock layout manager of the MagicDraw / Cameo software
     */
    private String panelDockKey;

    /**
     * Gets the value of this associated unique panel dock key
     * 
     * @return a {@linkplain String} containing the key
     */
    public String GetPanelDockKey()
    {
        return this.panelDockKey;
    }
    
    /**
     * An assert whether this view is visible
     */
    private boolean isVisibleInTheDock = true;
    
    /**
     * The {@link INavigationService}
     */
    protected transient INavigationService navigationService;

    /**
     * The {@link TViewModel} as the data context of this view
     */
    protected transient TViewModel dataContext;
    
    /**
     * The {@linkplain TView} this view wraps
     */
    protected TView view;
    
    /**
     * Initializes a new {@linkplain MagicDrawBasePanel}
     * 
     * @param panelDockKey the key to identify the panel
     */
    protected MagicDrawBasePanel(String panelDockKey)
    {
        this.panelDockKey = panelDockKey;
        this.setKey(this.panelDockKey);
    }
    
    /**
     * Show or Hide this {@link MagicDrawBasePanel}
     * 
     * @param dockingManager The {@link DockingManager} that is allowed to hide or show this frame
     */
    public void ShowHide(DockingManager dockingManager)
    {
        if(this.isVisibleInTheDock)
        {
            dockingManager.hideFrame(this.panelDockKey);
            this.isVisibleInTheDock = false;
        }
        else
        {
            dockingManager.showFrame(this.panelDockKey);
            this.isVisibleInTheDock = true;
        }
    }
    
    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @SuppressWarnings("unchecked")
    @Override
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (TViewModel)viewModel;   
        this.Bind();
    }

    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public TViewModel GetDataContext()
    {
        return this.dataContext;
    }
}
