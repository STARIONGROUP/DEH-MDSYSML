/*
 * CircularDependencyValidationService.java
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
package Services.ModelConsistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Reactive.ObservableCollection;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.Stereotypes;
import Utils.Tasks.ObservableTask;
import Utils.Tasks.Task;
import Utils.Tasks.TaskStatus;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;

/**
 * The {@linkplain CircularDependencyValidationService} verify model consistency in terms of circular dependency between Parts in a given {@linkplain Package}
 */
public class CircularDependencyValidationService implements ICircularDependencyValidationService
{
    /**
     * This current class logger
     */
    private Logger logger = LogManager.getLogger();

    /**
     * The {@linkplain IMagicDrawSessionService}
     */
    private final IMagicDrawSessionService sessionService;

    /**
     * The {@linkplain IMagicDrawUILogService}
     */
    private final IMagicDrawUILogService logService;

    /**
     * The {@linkplain IStereotypeService}
     */
    private final IStereotypeService stereotypeService;
    
    /**
     * A value indicating whether the validation process is in progress
     */
    private boolean isValidationInProgress;

    /**
     * The reference to the {@linkplain ObservableTask} that is responsible for the validation
     */
    private ObservableTask<Boolean> validationTask;
    
    /**
     * Backing field for {@linkplain #GetInvalidPaths()}
     */
    private ArrayListMultimap<Class, Collection<NamedElement>> invalidPaths = ArrayListMultimap.create();

    /**
     * Gets the {@linkplain Map} of all invalid path found when {@linkplain #Validate()}.
     * The Key represent one top node in a path and the associated value is the collection of path/node children of the key 
     */
    @Override
    public ArrayListMultimap<Class, Collection<NamedElement>> GetInvalidPaths()
    {
        return this.invalidPaths;
    }

    /**
     * Initializes a new {@linkplain CircularDependencyValidationService}
     * 
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param logService the {@linkplain IMagicDrawUILogService}
     */
    public CircularDependencyValidationService(IMagicDrawSessionService sessionService, IStereotypeService stereotypeService, IMagicDrawUILogService logService)
    {
        this.sessionService = sessionService;
        this.stereotypeService = stereotypeService;
        this.logService = logService;
        
        this.sessionService.HasAnyOpenSessionObservable().subscribe(x -> this.InitializeValidation(false));        
        this.sessionService.ProjectSaved().subscribe(x -> this.InitializeValidation(true));
    }

    /**
     * Initializes the process of validating the open SysML project
     * 
     * @param cancellationRequested a value indicating whether the {@linkplain #validationTask} can be cancelled if running
     */
    private void InitializeValidation(boolean cancellationRequested)
    {
        if(!this.sessionService.HasAnyOpenSession())
        {
            return;
        }
        
        if(this.isValidationInProgress && cancellationRequested && this.validationTask != null)
        {
            this.validationTask.Cancel();
        }
        else if (this.isValidationInProgress || (!cancellationRequested && this.validationTask != null))
        {
            return;
        }            
        
        this.isValidationInProgress = true;

        StopWatch timer = StopWatch.createStarted();
        
        this.validationTask = Task.Create(() -> this.Validate(), Boolean.class);
        
        this.validationTask.Observable()
            .subscribe(task -> WhenValidationIsFinished(timer, task));
        
        this.validationTask.Run();
    }

    /**
     * Occurs when the validation task is finished, also notifies the session service.
     * 
     * @param timer the ongoing timer
     * @param task the {@linkplain Task} that is finished
     */
    private void WhenValidationIsFinished(StopWatch timer, Task<Boolean> task)
    {
        try
        {
            if(timer.isStarted())
            {
                timer.stop();
            }
   
            if(task.GetStatus() == TaskStatus.Faulted)
            {
                this.logger.catching(task.GetException());
                this.logService.Append("Failure to detect circular dependency SysML model. %s", NotificationSeverity.ERROR, task.GetException());
                return;
            }
            
            if(task.GetStatus() == TaskStatus.Cancelled)
            {
                return;
            }

            String baseInfo = String.format("Detecting circular dependency on the SysML model took %s ms to complete", timer.getTime(TimeUnit.MILLISECONDS));
            
            if(this.invalidPaths.isEmpty())
            {
                this.logger.info(baseInfo);
            }
            else
            {
                this.logService.Append("%s. [%s] contains circular dependency", NotificationSeverity.WARNING, baseInfo, this.sessionService.GetProjectName());
            }                    
        }
        finally
        {
            this.isValidationInProgress = false;
            this.sessionService.GetSessionEvent().Value(true);
        }
    }
    
    /**
     * Validates the SysML model against any circular dependency
     * 
     * @return an assert
     */
    Boolean Validate()
    {
        this.invalidPaths.clear();
        
        final Set<Property> allPartProperties = new HashSet<>();

        for(Class element : this.sessionService.GetAllProjectElements().stream()
                .filter(x -> this.stereotypeService.DoesItHaveTheStereotype(x, Stereotypes.Block) 
                        && !((Class)x).getName().contains("RollUp"))
                .map(Class.class::cast)
                .collect(Collectors.toList()))
        {
            allPartProperties.addAll(element.getOwnedAttribute().stream().filter(x -> this.stereotypeService.IsPartProperty(x)).collect(Collectors.toList()));
        }
        
        ArrayList<MutablePair<Boolean, LinkedHashMap<String, NamedElement>>> nodes = new ArrayList<>();
        
        for (Property property : allPartProperties)
        {            
            nodes.clear();
            
            if(!(property.getObjectParent() instanceof Class))
            {
                continue;
            }            

            Class parentBlock = (Class) property.getObjectParent();
            
            nodes.add(MutablePair.of(false, new LinkedHashMap<String, NamedElement>()));            
            nodes.get(0).getRight().put(parentBlock.getID(), parentBlock);

            this.WalkThroughEachPath(nodes, property);
            
            for(LinkedHashMap<String, NamedElement> path : nodes.stream().filter(x -> x.getKey()).map(x -> x.getRight()).collect(Collectors.toList()))
            {
                this.invalidPaths.put(parentBlock, path.values().stream().skip(1).collect(Collectors.toList()));
            }
            
        }
        
        return this.invalidPaths.isEmpty();
    }

    /**
     * Recursively walk through each path until if finds again one dependency already encountered
     * 
     * @param nodes the {@linkplain Map} of id and {@linkplain NamedElement}
     * @param property the current Par {@linkplain Property}
     * @return a value indicating whether it has found recursion
     */
    private void WalkThroughEachPath(ArrayList<MutablePair<Boolean, LinkedHashMap<String, NamedElement>>> nodes, Property property)
    {
        if(property.getType() instanceof Class && this.stereotypeService.DoesItHaveTheStereotype(property.getType(), Stereotypes.Block))
        {
            Map<String, NamedElement> currentPath = nodes.get(nodes.size() - 1).getRight();
            currentPath.put(property.getID(), property);
            Class type = (Class)property.getType();

            if(currentPath.put(type.getID(), type) != null)
            {
                nodes.get(nodes.size() - 1).left = true;
                return;
            }
            
            if(!type.getOwnedAttribute().isEmpty())
            {
                List<Property> ownedAttributes = type.getOwnedAttribute().stream()
                            .filter(x -> this.stereotypeService.IsPartProperty(x))
                            .collect(Collectors.toList());
                
                Map<String, NamedElement> previousPath = new HashMap<>(currentPath);
                
                for (int index = 0; index < ownedAttributes.size(); index++)
                {
                    if(index > 0)
                    {
                        nodes.add(MutablePair.of(false, new LinkedHashMap<String, NamedElement>(previousPath)));
                    }
                    
                    this.WalkThroughEachPath(nodes, ownedAttributes.get(index));
                }
            }
        }
    }

    /**
     * Filters out the provided {@linkplain Collection} of {@linkplain Element}
     * 
     * @param elements the initial {@linkplain Collection} of {@linkplain Element} to filter
     * @return a {@linkplain Pair} where the left element is a map of element involved in any recursion and the right one is the original list filtered out
     */ 
    @Override
    public Pair<ArrayListMultimap<Class, Collection<NamedElement>>, Collection<Element>> FiltersInvalidElements(Collection<Element> elements)
    {
        ArrayListMultimap<Class, Collection<NamedElement>> invalidElements = ArrayListMultimap.create();
        
        for (Class block : elements.stream()
                .filter(x -> x instanceof Class)
                .map(x -> (Class)x)
                .collect(Collectors.toList()))
        {

            if(this.invalidPaths.keySet().contains(block))
            {
                for(Collection<NamedElement> path : this.invalidPaths.get(block))
                {
                    invalidElements.put(block, path);
                }
                elements.remove(block);
            }
        }
        
        
        return Pair.of(invalidElements, elements);
    }
    
    /**
     * Verifies that the Part {@linkplain Property} is not involved in any recursion, if so verifies that the provided {@linkplain RootRowViewModel} 
     * does not contains a row representing the {@linkplain Property}
     * 
     * @param rootRowViewModel the Part {@linkplain RootRowViewModel}
     * @param property the {@linkplain Property}
     * @returns an assert
     */
    @Override
    public boolean IsAlreadyPresent(RootRowViewModel rootRowViewModel, Property property)
    {
        if(!this.invalidPaths.isEmpty() && !this.invalidPaths.containsKey(property.getObjectParent()))
        {
            return false;
        }
        
        return this.IsAlreadyPresent(property, rootRowViewModel.GetContainedRows());
    }

    /**
     * 
     * Verifies that the Part {@linkplain Property} is not present in the provided {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     * does not contains a row representing the {@linkplain Property}
     * 
     * @param property the {@linkplain Property}
     * @param containedRows an {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     * @return an assert
     */
    @SuppressWarnings("unchecked")
    private boolean IsAlreadyPresent(Property property, ObservableCollection<IElementRowViewModel<?>> containedRows)
    {
        for (IElementRowViewModel<?> element : containedRows)
        {
            if(element.GetClassKind() == Stereotypes.PartProperty && 
                    Utils.Operators.Operators.AreTheseEquals(element.GetElement().getID(), property.getID()))
            {
                return true;
            }
            
            if(element instanceof IHaveContainedRows)
            {
                return this.IsAlreadyPresent(property, ((IHaveContainedRows<IElementRowViewModel<?>>) element).GetContainedRows());
            }
        }
        
        return false;
    }
}
