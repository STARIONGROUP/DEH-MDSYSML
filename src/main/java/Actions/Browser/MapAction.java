/*
 * MapAction.java
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
package Actions.Browser;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;

import Enumerations.MappingDirection;
import Services.MagicDrawSelection.IMagicDrawSelectionService;
import Services.Mapping.IMapCommandService;
import Utils.ImageLoader.ImageLoader;
import Views.MagicDrawHubBrowserPanel;

/**
 * The {@link MapAction} is a {@link MDAction} that is to be added to the Cameo/Magic draw element browser context menu
 */
@SuppressWarnings("serial")
public class MapAction extends DefaultBrowserAction
{
    /**
     * The {@linkplain IMapCommandService}
     */
    private IMapCommandService mapCommandService;
    
    /**
     * The {@linkplain IMapCommandService}
     */
    private IMagicDrawSelectionService selectionService;

    /**
     * Initializes a new {@linkplain MapAction}
     * 
     * @param mapCommandService the {@linkplain IMapCommandService}
     * @param selectionService the {@linkplain IMagicDrawSelectionService}
     */
    public MapAction(IMapCommandService mapCommandService, IMagicDrawSelectionService selectionService) 
    {
        super("Map Selection", "Map the current selection", KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, true), null);
        this.setSmallIcon(ImageLoader.GetIcon("icon16.png"));
        this.selectionService = selectionService;
        this.mapCommandService = mapCommandService;
        this.mapCommandService.CanExecuteObservable().subscribe(x -> this.updateState());
    }

    /**
     * Updates the enabled/disabled state of this action
     * @param canExecute 
     */
    @Override
    public void updateState()
    {
       this.SetIsEnabled(this.mapCommandService.CanExecute());
    }
    
    /**
     * Sets a value indicating whether this action is enabled
     * 
     * @param shouldEnable a value switch that allows enabling this action
     */
    private void SetIsEnabled(boolean shouldEnable)
    {
        shouldEnable &= this.getTree() != null && this.getTree().getSelectedNodes().length > 0;
        this.setEnabled(shouldEnable);
    }

    /**
    * Commands the {@link MagicDrawHubBrowserPanel} to show or hide
    * 
    * @param actionEvent The {@link ActionEvent} that originated the action performed. This parameter is unused.
    */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {        
        this.selectionService.SetActiveBrowser(this.getTree());
        this.mapCommandService.MapSelection(MappingDirection.FromDstToHub);
    }
}

