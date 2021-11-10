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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.KeyStroke;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingEngineService.IMappableThingCollection;
import Utils.ImageLoader.ImageLoader;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import Utils.Tasks.Task;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedRequirementsSpecificationRowViewModel;
import Views.MagicDrawHubBrowserPanel;

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
     * the {@linkplain IHubController}
     */
    private IHubController hubController;
        
    /**
     * Initializes a new {@linkplain MapAction}
     * 
     * @param hubController the {@linkplain IHubController} 
     * @param dstController the {@linkplain IDstController}
     */
    public MapAction(IHubController hubController, IDstController dstController) 
    {
        super("Map Selection", "Map the current selection", KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, true), null);
        this.setSmallIcon(ImageLoader.GetIcon("icon16.png"));
        this.dstController = dstController;
        this.hubController = hubController;
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
            StopWatch timer = StopWatch.createStarted();
            
            Task.Run(() -> this.MapSelectedElements(), boolean.class)
                .Observable()
                .subscribe(x -> 
                {
                    if(timer.isStarted())
                    {
                        timer.stop();
                    }
                    
                    this.logger.error(String.format("Mapping action is done with success ? %s in %s ms", x.GetResult(), timer.getTime(TimeUnit.MILLISECONDS)));
                }, x -> this.logger.catching(x));
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("MapAction actionPerformed has thrown an exception %s", exception));
            throw exception;
        }
    }
    
    /**
     * Asynchronously maps the selected elements from the current tree
     * 
     * @return a value indicating whether the mapping operation succeeded
     */
    private boolean MapSelectedElements()
    {
        ArrayList<IMappableThingCollection> mappableElements = this.SortSelectedElements();
        
        boolean result = true;
        
        for (IMappableThingCollection elements : mappableElements.stream().filter(x -> !x.isEmpty()).collect(Collectors.toList()))
        {
            result &= this.dstController.Map(elements, MappingDirection.FromDstToHub);
        }

        return result;
    }

    /**
     * Sorts the selected element from the tree and return the correct sequence depending on what have been selected
     */
    private ArrayList<IMappableThingCollection> SortSelectedElements()
    {
        MagicDrawBlockCollection blocks = new MagicDrawBlockCollection();
        MagicDrawRequirementCollection requirements = new MagicDrawRequirementCollection();
        
        List<Element> elements = Arrays.asList(this.getTree().getSelectedNodes())
                .stream()
                .filter(x -> x.getUserObject() instanceof Element)
                .map(x -> (Element)x.getUserObject())
                .collect(Collectors.toList());

        this.SortElement(elements, blocks, requirements);
        
        return new ArrayList<IMappableThingCollection>(Arrays.asList(blocks, requirements));
    }

    /**
     * Parses and sorts the provided {@linkplain Element} collection and add these to the respective out collections.
     * Can be recursive.
     * 
     * @param elements the element collection to sort
     * @param blocks the Block collection
     * @param requirements the requirements collection
     */
    private void SortElement(Collection<Element> elements, MagicDrawBlockCollection blocks, MagicDrawRequirementCollection requirements)
    {
        for (Element element : elements)
        {
            if (element instanceof Class)
            {
                Class classElement = (Class)element;
                
                if(StereotypeUtils.DoesItHaveTheStereotype(classElement, Stereotypes.Block))
                {
                    blocks.add(new MappedElementDefinitionRowViewModel(classElement, MappingDirection.FromDstToHub));
                }
                else if(StereotypeUtils.DoesItHaveTheStereotype(classElement, Stereotypes.Requirement))
                {
                    requirements.add(new MappedRequirementsSpecificationRowViewModel(classElement, MappingDirection.FromDstToHub));
                }
            }
            else if(element instanceof Package)
            {
                Package packageElement = (Package)element;
                
                if(!elements.stream().anyMatch(x -> packageElement.getOwnedElement().stream().anyMatch(e -> e.equals(x))))
                {
                    this.SortElement(packageElement.getOwnedElement(), blocks, requirements);
                }
            }
        }
    }    
}

