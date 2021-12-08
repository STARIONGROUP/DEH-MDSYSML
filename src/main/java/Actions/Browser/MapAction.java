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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.KeyStroke;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.NavigationService.INavigationService;
import Utils.ImageLoader.ImageLoader;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Tasks.Task;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementsSpecificationRowViewModel;
import Views.MagicDrawHubBrowserPanel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@link MapAction} is a {@link MDAction} that is to be added to the Cameo/Magic draw element browser context menu
 */
@SuppressWarnings("serial")
public class MapAction extends DefaultBrowserAction
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;

    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * The {@linkplain INavigationService}
     */
    private INavigationService navigationService;

    /**
     * The {@linkplain IMagicDrawUILogService}
     */
    private IMagicDrawUILogService uILogService;
        
    /**
     * Initializes a new {@linkplain MapAction}
     * 
     * @param hubController the {@linkplain IHubController} 
     * @param dstController the {@linkplain IDstController}
     * @param navigationService the {@linkplain INavigationService}
     */
    public MapAction(IHubController hubController, IDstController dstController, INavigationService navigationService, IMagicDrawUILogService uILogService) 
    {
        super("Map Selection", "Map the current selection", KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, true), null);
        this.uILogService = uILogService;
        this.setSmallIcon(ImageLoader.GetIcon("icon16.png"));
        this.dstController = dstController;
        this.hubController = hubController;
        this.navigationService = navigationService;
        this.hubController.GetIsSessionOpenObservable().subscribe(x -> this.updateState());
    }

    /**
     * Updates the enabled/disabled state of this action
     */
    @Override
    public void updateState()
    {
       this.SetIsEnabled(this.hubController.GetOpenIteration() != null);
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
        try
        {
            IDstMappingConfigurationDialogViewModel viewModel = AppContainer.Container.getComponent(IDstMappingConfigurationDialogViewModel.class);
            
            List<Element> elements = Arrays.asList(this.getTree().getSelectedNodes())
                    .stream()
                    .filter(x -> x.getUserObject() instanceof Element)
                    .map(x -> (Element)x.getUserObject())
                    .collect(Collectors.toList());

            viewModel.SetMappedElement(elements);
            
            if(!this.navigationService.ShowDialog(new DstMappingConfigurationDialog(), viewModel))
            {
                return;
            }
            
            List<MappedElementRowViewModel<? extends Thing, Class>> validMappedElements = viewModel.GetMappedElementCollection()
                    .stream()
                    .filter(x -> x.GetIsValid())
                    .collect(Collectors.toList());
                        
            StopWatch timer = StopWatch.createStarted();
            
            Task.Run(() -> this.MapSelectedElements(validMappedElements), boolean.class)
                .Observable()
                .subscribe(x -> 
                {
                    if(timer.isStarted())
                    {
                        timer.stop();
                    }

                    this.uILogService.Append(String.format("Mapping action is done in %s ms", timer.getTime(TimeUnit.MILLISECONDS), x.GetResult().booleanValue()));
                    
                }, x -> this.logger.catching(x));
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("MapAction actionPerformed has thrown an exception %s", exception));
        }
    }
    
    /**
     * Asynchronously maps the selected elements from the current tree
     * 
     * @param mappableElements The collection of {@linkplain MappedElementRowViewModel}  
     * @return a value indicating whether the mapping operation succeeded
     */
    private boolean MapSelectedElements(List<MappedElementRowViewModel<? extends Thing, Class>> mappableElements)
    {
        boolean result = true;
                
        MagicDrawBlockCollection mappedElements = new MagicDrawBlockCollection();
        
        mappedElements.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(ElementDefinition.class))
                .map(x -> (MappedElementDefinitionRowViewModel)x)
                .collect(Collectors.toList()));

        if(!mappedElements.isEmpty())
        {
            this.uILogService.Append(String.format("Mapping of %s Blocks in progress...", mappedElements.size()));
            result &= this.dstController.Map(mappedElements, MappingDirection.FromDstToHub);
        }
        
        MagicDrawRequirementCollection mappedRequirements = new MagicDrawRequirementCollection();
        
        mappedRequirements.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(RequirementsSpecification.class))
                .map(x -> (MappedRequirementsSpecificationRowViewModel)x)
                .collect(Collectors.toList()));

        if(!mappedRequirements.isEmpty())
        {
            this.uILogService.Append(String.format("Mapping of %s Requirements in progress...", mappedRequirements.size()));
            result &= this.dstController.Map(mappedRequirements, MappingDirection.FromDstToHub);
        }
        
        return result;
    }
}

