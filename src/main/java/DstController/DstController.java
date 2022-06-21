/*
 * DstController.java
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
package DstController;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Usage;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.InterfaceRealization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;

import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.BlockToElementMappingRule;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.HistoryService.IMagicDrawLocalExchangeHistoryService;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MagicDrawTransaction.Clones.ClonedReferenceElement;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.StreamExtensions;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.HubRelationshipElementsCollection;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRelatedElementCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.ChangeKind;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Definition;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.ShortNamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.PossibleFiniteState;
import cdp4common.engineeringmodeldata.PossibleFiniteStateList;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.ValueSet;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.SpecializedQuantityKind;
import cdp4common.types.ContainerList;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

/**
 * The {@linkplain DstController} is a class that manage transfer and connection to attached running instance of Cameo/MagicDraw
 */
public final class DstController implements IDstController
{
    /**
     * Gets this running DST adapter name
     */
    public static final String THISTOOLNAME = "DEH-MDSYSML";

    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IMappingEngine} instance
     */
    private final IMappingEngineService mappingEngine;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * The {@linkplain IMagicDrawUILogService} instance
     */
    private IMagicDrawUILogService logService;

    /**
     * The {@linkplain IMappingConfigurationService} instance
     */
    private IMagicDrawMappingConfigurationService mappingConfigurationService;

    /**
     * The {@linkplain IMagicDrawLocalExchangeHistoryService} instance
     */
    private final IMagicDrawLocalExchangeHistoryService exchangeHistory;
    
    /**
     * The {@linkplain IMagicDrawSessionService} instance
     */
    private final IMagicDrawSessionService sessionService;
    
    /**
     * The {@linkplain IMagicDrawTransactionService} instance
     */
    private final IMagicDrawTransactionService transactionService;

    /**
     * The {@linkplain IStereotypeService}
     */
    private IStereotypeService stereotypeService;

    /**
     * A value indicating whether the {@linkplain DstController} should load mapping when the HUB session is refresh or reloaded
     */
    private boolean isHubSessionRefreshSilent;
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> hubMapResult = new ObservableCollection<>();
    
    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> GetHubMapResult()
    {
        return this.hubMapResult;
    }    
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> dstMapResult = new ObservableCollection<>();

    /**
     * Gets The {@linkplain ObservableCollection} of DST map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> GetDstMapResult()
    {
        return this.dstMapResult;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedHubMapResultForTransfer}
     */    
    private ObservableCollection<Class> selectedHubMapResultForTransfer = new ObservableCollection<>(Class.class);
    
    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the Cameo/MagicDraw
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    @Override
    public ObservableCollection<Class> GetSelectedHubMapResultForTransfer()
    {
        return this.selectedHubMapResultForTransfer;
    }
    
    /**
     * Backing field for {@linkplain GetSelectedDstMapResultForTransfer}
     */
    private ObservableCollection<Thing> selectedDstMapResultForTransfer = new ObservableCollection<>(Thing.class);
    
    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    @Override
    public ObservableCollection<Thing> GetSelectedDstMapResultForTransfer()
    {
        return this.selectedDstMapResultForTransfer;
    }
    
    /**
     * The private collection of mapped {@linkplain BinaryRelationship} to {@linkplain DirectedRelationship}
     */
    private ObservableCollection<Abstraction> mappedBinaryRelationshipsToDirectedRelationships = new ObservableCollection<>();
    
    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain DirectedRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain DirectedRelationship}s
     */
    @Override
    public ObservableCollection<Abstraction> GetMappedBinaryRelationshipsToDirectedRelationships()
    {
        return this.mappedBinaryRelationshipsToDirectedRelationships;
    }
    
    /**
     * The private collection of mapped {@linkplain Traces} to  {@linkplain BinaryRelationship}
     */
    private ObservableCollection<BinaryRelationship> mappedDirectedRelationshipsToBinaryRelationships = new ObservableCollection<>();

    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     */
    @Override
    public ObservableCollection<BinaryRelationship> GetMappedDirectedRelationshipToBinaryRelationships()
    {
        return this.mappedDirectedRelationshipsToBinaryRelationships;
    }
    
    /**
     * Backing field for {@linkplain GeMappingDirection}
     */
    private ObservableValue<MappingDirection> currentMappingDirection = new ObservableValue<>(MappingDirection.FromDstToHub, MappingDirection.class);

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    @Override
    public Observable<MappingDirection> GetMappingDirection()
    {
        return this.currentMappingDirection.Observable();
    }

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection CurrentMappingDirection()
    {
        return this.currentMappingDirection.Value();
    }

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    @Override
    public MappingDirection ChangeMappingDirection()
    {
        this.currentMappingDirection.Value(
                this.currentMappingDirection.Value() == MappingDirection.FromDstToHub 
                ? MappingDirection.FromHubToDst
                : MappingDirection.FromDstToHub);
        
        return this.currentMappingDirection.Value();
    }
    
    /**
     * Initializes a new {@linkplain DstController}
     * 
     * @param mappingEngine the {@linkplain IMappingEngine} instance
     * @param HubController the {@linkplain IHubController} instance
     * @param logService the {@linkplain IMagicDrawUILogService} instance
     * @param mappingConfigurationService the {@linkplain IMagicDrawMappingConfigurationService} instance
     * @param sessionService the {@linkplain IMagicDrawSessionService} instance
     * @param exchangeHistory the {@linkplain IMagicDrawLocalExchangeHistoryService} instance
     * @param transactionService the {@linkplain IMagicDrawTransactionService} instance
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public DstController(IMappingEngineService mappingEngine, IHubController hubController, IMagicDrawUILogService logService, 
            IMagicDrawMappingConfigurationService mappingConfigurationService, IMagicDrawSessionService sessionService,
            IMagicDrawLocalExchangeHistoryService exchangeHistory, IMagicDrawTransactionService transactionService, IStereotypeService stereotypeService)
    {
        this.mappingEngine = mappingEngine;
        this.hubController = hubController;
        this.logService = logService;
        this.mappingConfigurationService = mappingConfigurationService;
        this.sessionService = sessionService;
        this.exchangeHistory = exchangeHistory;
        this.transactionService = transactionService;
        this.stereotypeService = stereotypeService;
        
        this.InitializeObservables();
    }

    /**
     * Initializes this {@linkplain DstController} {@linkplain Observable}s
     */
    private void InitializeObservables()
    {            
        this.sessionService.SessionUpdated().subscribe(x -> this.ReloadMapping());
        this.hubController.GetSessionEventObservable().subscribe(x -> this.ReloadMapping());
        
        this.hubController.GetIsSessionOpenObservable().subscribe(isSessionOpen -> this.WhenAnySessionCloses(isSessionOpen));
        this.sessionService.HasAnyOpenSessionObservable().subscribe(isSessionOpen -> this.WhenAnySessionCloses(isSessionOpen));

        this.GetDstMapResult().ItemsAdded().subscribe(x -> this.MapRelationships(MappingDirection.FromDstToHub));
        this.GetHubMapResult().ItemsAdded().subscribe(x -> this.MapRelationships(MappingDirection.FromHubToDst));
    }

    /**
     * Gracefully clears mapping collections when a session is closed
     * 
     * @param isSessionOpen a value indicating whether the emitted value indicates that a session is open
     */
    private void WhenAnySessionCloses(boolean isSessionOpen)
    {
        if(!isSessionOpen)
        {
            this.ResetAllRelatedMappingCollections();
        }
    }

    /**
     * Reset all mapped things collections
     */
    private void ResetAllRelatedMappingCollections()
    {
        this.transactionService.Clear();
        this.dstMapResult.clear();
        this.hubMapResult.clear();
        this.selectedDstMapResultForTransfer.clear();
        this.selectedHubMapResultForTransfer.clear();
        this.mappedDirectedRelationshipsToBinaryRelationships.clear();
        this.mappedBinaryRelationshipsToDirectedRelationships.clear();
    }

    /**
     * Reloads the saved mapping and applies the mapping rule to the loaded things
     */
    private void ReloadMapping()
    {
        if(this.isHubSessionRefreshSilent)
        {
            return;
        }

        this.LoadMapping();
    }
    
    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     * 
     * @return the number of mapped things loaded
     */
    @Override
    public void LoadMapping()
    {
        this.ResetAllRelatedMappingCollections();
        
        StopWatch timer = StopWatch.createStarted();
        
        Collection<IMappedElementRowViewModel> things = this.mappingConfigurationService.LoadMapping(this.sessionService.GetProjectElements()
                .stream()
                .filter(Class.class::isInstance)
                .map(Class.class::cast)
                .collect(Collectors.toList()));
        
        MagicDrawBlockCollection allMappedMagicDrawElement = new MagicDrawBlockCollection();
        MagicDrawRequirementCollection allMappedMagicDrawRequirements = new MagicDrawRequirementCollection();
        
        HubElementCollection allMappedHubElement = new HubElementCollection();
        HubRequirementCollection allMappedHubRequirements = new HubRequirementCollection();
        
        things.stream()
            .filter(x -> x.GetMappingDirection() == MappingDirection.FromDstToHub)
            .forEach(x -> SortMappedElementByType(allMappedMagicDrawElement, allMappedMagicDrawRequirements, x));
        
        things.stream()
            .filter(x -> x.GetMappingDirection() == MappingDirection.FromHubToDst)
            .forEach(x -> SortMappedElementByType(allMappedHubElement, allMappedHubRequirements, x));
        
        boolean result = true;
                
        if(!allMappedMagicDrawElement.isEmpty())
        {
            result &= this.Map(allMappedMagicDrawElement, MappingDirection.FromDstToHub);
        }
        if(!allMappedMagicDrawRequirements.isEmpty())
        {
            result &= this.Map(allMappedMagicDrawRequirements, MappingDirection.FromDstToHub);
        }
        if(!allMappedHubElement.isEmpty())
        {
            result &= this.Map(allMappedHubElement, MappingDirection.FromHubToDst);
        }
        if(!allMappedHubRequirements.isEmpty())
        {
            result &= this.Map(allMappedHubRequirements, MappingDirection.FromHubToDst);
        }
        
        timer.stop();
        
        if(!result)
        {
            this.logService.Append(String.format("Could not map %s saved mapped things for some reason, check the log for details", things.size()), NotificationSeverity.ERROR);
            things.clear();
            return;
        }
    
        this.logService.Append(String.format("Loaded %s saved mapping, done in %s ms", things.size(), timer.getTime(TimeUnit.MILLISECONDS)));
    }

    /**
     * Sorts the {@linkplain IMappedElementRowViewModel} and adds it to the relevant collection of one of the two provided
     * 
     * @param allMappedElement the {@linkplain Collection} of {@linkplain MappedElementDefinitionRowViewModel}
     * @param allMappedRequirements the {@linkplain Collection} of {@linkplain MappedDstRequirementRowViewModel}
     * @param mappedRowViewModel the {@linkplain IMappedElementRowViewModel} to sort
     */
    private void SortMappedElementByType(ArrayList<MappedElementDefinitionRowViewModel> allMappedElement,
            ArrayList<MappedRequirementRowViewModel> allMappedRequirements, IMappedElementRowViewModel mappedRowViewModel)
    {
        if(mappedRowViewModel.GetTThingClass().isAssignableFrom(ElementDefinition.class))
        {
            allMappedElement.add((MappedElementDefinitionRowViewModel) mappedRowViewModel);
        }
        else if(mappedRowViewModel.GetTThingClass().isAssignableFrom(Requirement.class))
        {
            allMappedRequirements.add((MappedRequirementRowViewModel) mappedRowViewModel);
        }
    }
    
    /**
     * Maps the traces/BinaryRelationship from either the {@linkplain #dstMapResult} or the {@linkplain #hubMapResult} depending on the provided {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @SuppressWarnings("unchecked")
    private boolean MapRelationships(MappingDirection mappingDirection)
    {
        IMappableThingCollection input = null;
        
        if(mappingDirection == MappingDirection.FromDstToHub)
        {
            input = new MagicDrawRelatedElementCollection();
            ((ArrayList<MappedElementRowViewModel<? extends Thing, ? extends Class>>) input).addAll(this.dstMapResult);
        }
        else if(mappingDirection == MappingDirection.FromHubToDst)
        {
            input = new HubRelationshipElementsCollection();
            ((ArrayList<MappedElementRowViewModel<? extends Thing, ? extends Class>>) input).addAll(this.hubMapResult);
        }

        return this.MapRelationships(input, mappingDirection);
    }
    
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @SuppressWarnings("unchecked")
    private boolean MapRelationships(IMappableThingCollection input, MappingDirection mappingDirection)
    {
        this.logService.Append(String.format("Mapping of Relationships from %s mappedElements in progress...", ((Collection<?>)input).size()));
        Ref<ArrayList<?>> output = new Ref<>(null);
        Ref<Boolean> result = new Ref<>(Boolean.class, false);
        
        if(this.TryMap(input, output, result))
        {
            if(mappingDirection == MappingDirection.FromDstToHub)
            {
                ArrayList<BinaryRelationship> mappedBinaryRelationship = (ArrayList<BinaryRelationship>)output.Get();
                this.mappedDirectedRelationshipsToBinaryRelationships.removeIf(x -> mappedBinaryRelationship.stream().anyMatch(r -> AreTheseEquals(x.getIid(), r.getIid())));
                this.mappedDirectedRelationshipsToBinaryRelationships.addAll(mappedBinaryRelationship);
                this.logService.Append("%s Binary Relationships were mapped from MagicDraw Traces", output.Get().size());
            }
            else if(mappingDirection == MappingDirection.FromHubToDst)
            {
                ArrayList<Abstraction> mappedTraces = (ArrayList<Abstraction>)output.Get();
                this.mappedBinaryRelationshipsToDirectedRelationships.removeIf(x -> mappedTraces.stream().anyMatch(t -> AreTheseEquals(t.getID(), x.getID())));
                this.mappedBinaryRelationshipsToDirectedRelationships.addAll(mappedTraces);
                this.logService.Append("%s MagicDraw Traces were mapped from Binary Relationships", output.Get().size());
            }
        }
        
        return result.Get();
    }

    /**
     * Tries to map the provided {@linkplain IMappableThingCollection}
     * 
     * @param input the {@linkplain IMappableThingCollection}
     * @param output the {@linkplain ArrayList} output of whatever mapping rule returns
     * @param result the result to return {@linkplain #Map(IMappableThingCollection, MappingDirection)} from in case the mapping fails
     * @return a value that is true when the {@linkplain IMappableThingCollection} mapping succeed
     */
    private boolean TryMap(IMappableThingCollection input, Ref<ArrayList<?>> output, Ref<Boolean> result)
    {
        if(input.isEmpty())
        {
            result.Set(true);
        }
        
        Object outputAsObject = this.mappingEngine.Map(input);

        if(outputAsObject instanceof ArrayList<?>)
        {
            output.Set((ArrayList<?>)outputAsObject);
        }
        
        return output.HasValue();
    }
    
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean Map(IMappableThingCollection input, MappingDirection mappingDirection)
    {
        Ref<ArrayList<?>> output = new Ref<>(null);
        Ref<Boolean> result = new Ref<>(Boolean.class, false);
        
        if(this.TryMap(input, output, result))
        {
            ArrayList<MappedElementRowViewModel<DefinedThing, Class>> resultAsCollection = 
                    (ArrayList<MappedElementRowViewModel<DefinedThing, Class>>) output.Get();
            
            if(!resultAsCollection.isEmpty())
            {
                if (mappingDirection == MappingDirection.FromDstToHub
                        && resultAsCollection.stream().allMatch(x -> x.GetHubElement() instanceof Thing))
                {
                    this.dstMapResult.removeIf(x -> resultAsCollection.stream()
                            .anyMatch(d -> AreTheseEquals(d.GetHubElement().getIid(), x.GetHubElement().getIid())));
        
                    this.selectedDstMapResultForTransfer.clear();                
                    return this.dstMapResult.addAll(resultAsCollection);
                }
                else if (mappingDirection == MappingDirection.FromHubToDst
                        && resultAsCollection.stream().allMatch(x -> x.GetDstElement() instanceof Class))
                {
                    this.hubMapResult.removeIf(x -> resultAsCollection.stream()
                            .anyMatch(d -> AreTheseEquals(d.GetDstElement().getID(), x.GetDstElement().getID())));
    
                    this.selectedHubMapResultForTransfer.clear();
                    return this.hubMapResult.addAll(resultAsCollection);
                }            
            }
        }
        
        return result.Get();
    }
    
    /**
     * Adds or removes available BinaryRelationship for transfer to the Hub
     */
    private void AddOrRemoveBinaryRelationshipForTransfer()
    {
        this.selectedHubMapResultForTransfer.removeIf(x -> x instanceof BinaryRelationship);

        List<BinaryRelationship> transferableBinaryRelationship = this.mappedDirectedRelationshipsToBinaryRelationships.stream()
                .filter(x -> this.selectedDstMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getIid(), x.getTarget().getIid()))
                        && this.selectedDstMapResultForTransfer.stream().anyMatch(m -> AreTheseEquals(m.getIid(), x.getSource().getIid())))
                .collect(Collectors.toList());
        
        this.selectedDstMapResultForTransfer.addAll(transferableBinaryRelationship);
        
    }
    
    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean Transfer()
    {
        boolean result;
        
        switch(this.CurrentMappingDirection())
        {
            case FromDstToHub:
                result = this.TransferToHub();
                break;
            case FromHubToDst:
                result = this.TransferToDst();
                break;
            default:
                result = false;
                break;        
        }
        
        this.LoadMapping();
        return result;
    }
    
    /**
     * Transfers all the {@linkplain Class} contained in the {@linkplain huMapResult} to the DST
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean TransferToDst()
    {
        try
        {
            boolean result = this.transactionService.Commit(() -> PrepareThingsForTransfer());

            this.logService.Append(String.format("Transfered %s elements to MagicDraw/Cameo", this.selectedHubMapResultForTransfer.size()), result);
            
            if(!this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary())
            {
                this.logService.Append("Saving the mapping configuration in progress...");

                this.isHubSessionRefreshSilent = true;
                Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
    
                Iteration iterationClone = iterationTransaction.getLeft();
                ThingTransaction transaction = iterationTransaction.getRight();
                this.mappingConfigurationService.PersistExternalIdentifierMap(transaction, iterationClone);
                transaction.createOrUpdate(iterationClone);
                
                this.hubController.Write(transaction);
                result &= this.hubController.Refresh();
                this.mappingConfigurationService.RefreshExternalIdentifierMap();
            }
            
            this.selectedHubMapResultForTransfer.clear();
            this.logService.Append("Reloading the mapping configuration in progress...");
            this.isHubSessionRefreshSilent = false;
            return result & this.hubController.Refresh();
        }
        catch (Exception exception)
        {
            this.logService.Append(exception.toString(), exception);
            return false;
        }
        finally
        {
            this.selectedHubMapResultForTransfer.clear();
            this.isHubSessionRefreshSilent = false;
        }
    }
    
    /**
     * Prepares all the element from {@linkplain #GetSelectedHubMapResult()} for transfer
     */
    private void PrepareThingsForTransfer()
    {
        for (Class element : this.selectedHubMapResultForTransfer)
        {
            Class reference = element;
            
            if(this.transactionService.IsCloned(element))
            {
                reference = this.transactionService.GetClone(element).GetOriginal();
            }
        
            if(this.stereotypeService.DoesItHaveTheStereotype(reference, Stereotypes.Block))
            {
                this.PrepareBlocks(element);
            }
            else if(this.stereotypeService.DoesItHaveTheStereotype(reference, Stereotypes.Requirement))
            {
                this.PrepareRequirement(element);
            }
        }
        
        this.PrepareDirectedRelationShip();
        this.PrepareStates();
    }

    /**
     * Prepare the {@linkplain State}
     */
    private void PrepareStates()
    {
        if(this.transactionService.GetStatesModifiedRegions().isEmpty())
        {
            return;
        }
        
        Package model = this.sessionService.GetModel();
        
        StateMachine stateMachineModel = StreamExtensions.OfType(model.getOwnedElement().stream(), StateMachine.class)
                .filter(x -> AreTheseEquals(x.getName(), "Model"))
                .findFirst()
                .orElseGet(() -> 
                {
                     StateMachine newStateMachine = this.transactionService.Create(StateMachine.class, "Model");
                     model.getOwnedElement().add(newStateMachine);
                     return newStateMachine;
                });
        
        Region mainRegion = stateMachineModel.getRegion().stream()
                .findFirst()
                .orElse(this.transactionService.Create(Region.class, ""));
        
        for (Entry<State, List<Pair<Region, ChangeKind>>> stateAndModifiedRegions : this.transactionService.GetStatesModifiedRegions())
        {
            for (Pair<Region, ChangeKind> regionAndModification : stateAndModifiedRegions.getValue())
            {
                switch(regionAndModification.getRight())
                {
                    case CREATE:
                        stateAndModifiedRegions.getKey().getRegion().add(regionAndModification.getLeft());
                        break;
                    case DELETE:
                        this.transactionService.Delete(regionAndModification.getLeft());
                        break;
                    default:
                        break;
                }
            }
            
            if(StreamExtensions.OfType(mainRegion.getOwnedElement().stream(), State.class)
                    .noneMatch(x -> AreTheseEquals(x.getID(), stateAndModifiedRegions.getKey().getID())))
            {
                mainRegion.getOwnedElement().add(stateAndModifiedRegions.getKey());
            }
            
            for (Dependency dependency : StreamExtensions.OfType(stateAndModifiedRegions.getKey().get_directedRelationshipOfTarget(), Dependency.class))
            {
                dependency.setOwner(model);
                model.get_relationshipOfRelatedElement().add(dependency);
                dependency.getSupplier().clear();
                dependency.getSupplier().add(stateAndModifiedRegions.getKey());
            }
        }
    }

    /**
     * Prepares {@linkplain Abstraction} relationships to be transfered to MagicDraw
     */
    private void PrepareDirectedRelationShip()
    {
        List<Abstraction> transferableRelationships = this.mappedBinaryRelationshipsToDirectedRelationships.stream()
                .filter(x -> this.selectedHubMapResultForTransfer.stream().anyMatch(m -> x.getTarget().stream().anyMatch(t -> AreTheseEquals(m.getID(), t.getID())))
                        && this.selectedHubMapResultForTransfer.stream().anyMatch(m -> x.getSource().stream().anyMatch(t -> AreTheseEquals(m.getID(), t.getID()))))
                .collect(Collectors.toList());
        
        this.logService.Append("Processing %s Relationships", transferableRelationships.size());
        
        for (Abstraction relationship : transferableRelationships)
        {
            if(this.sessionService.GetProject().getPrimaryModel().get_relationshipOfRelatedElement().stream()
                    .noneMatch(x -> AreTheseEquals(x.getID(), relationship)))
            {
                this.sessionService.GetProject().getPrimaryModel().get_relationshipOfRelatedElement().add(relationship);
                relationship.setOwner(this.sessionService.GetProject().getPrimaryModel());
                this.exchangeHistory.Append(relationship, ChangeKind.CREATE);
            }
        }
    }

    /**
     * Prepares the specified {@linkplain Class} requirement for transfer
     * 
     * @param element the {@linkplain Class} element
     */
    private void PrepareRequirement(Class element)
    {
        if(this.transactionService.IsCloned(element))
        {
            Class original = this.transactionService.GetClone(element).GetOriginal();
            this.transactionService.SetRequirementId(original, element);
            this.transactionService.SetRequirementText(original, element);
            original.setName(element.getName());
            this.exchangeHistory.Append(element, ChangeKind.UPDATE);
        }
        else
        {
            this.UpdateRequirementPackage(element);
        }
    }

    /**
     * Prepares the specified {@linkplain Class} block for transfer
     * 
     * @param element the {@linkplain Class} element
     */
    private void PrepareBlocks(Class element)
    {
        if(this.transactionService.IsCloned(element))
        {
            Class original = this.transactionService.GetClone(element).GetOriginal();
            
            this.UpdateElementParameters(element, original);
            this.exchangeHistory.Append(element, ChangeKind.UPDATE);
        }
        else
        {
            this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(element);
            this.exchangeHistory.Append(element, ChangeKind.CREATE);
        }
        
        this.PrepareInterfaces(element);
        this.UpdateElementPortsRelationships(element);
    }

    /**
     * Updates the ports relationships for the provided {@linkplain Class}
     * 
     * @param element the {@linkplain Class} element
     */
    private void UpdateElementPortsRelationships(Class element)
    {
        for (Usage usageRelationship : element.getOwnedPort().stream()
                .flatMap(x -> x.get_relationshipOfRelatedElement().stream())
                .filter(x -> x instanceof Usage)
                .map(x -> (Usage)x)
                .filter(x -> x.getSource().stream().anyMatch(r -> AreTheseEquals(x.getID(), x.getOwner().getID())))
                .collect(Collectors.toList()))
        {
            Element portDefinition = usageRelationship.getOwner();
            portDefinition.get_relationshipOfRelatedElement().remove(usageRelationship);
            this.sessionService.GetProject().getPrimaryModel().get_relationshipOfRelatedElement().add(usageRelationship);
        }
    }

    /**
     * Updates the provided original {@linkplain Class} attributes with the cloned one
     * 
     * @param clone the cloned {@linkplain Class}
     * @param original the original {@linkplain Class}
     */
    private void UpdateElementParameters(Class clone, Class original)
    {
        for (Property property : clone.getOwnedAttribute().stream().collect(Collectors.toList()))
        {
            try
            {
                Optional<Property> optionalOriginalProperty = original.getOwnedAttribute().stream()
                        .filter(x -> AreTheseEquals(x.getID(), property.getID()))
                        .findFirst();
                
                if(optionalOriginalProperty.isPresent())
                {
                    ModelElementsManager.getInstance().removeElement(optionalOriginalProperty.get());
                }
                
                original.getOwnedAttribute().add(property);    
            }
            catch(Exception exception)
            {
                this.logger.catching(Level.INFO, exception);
                this.logger.error(String.format("Removing/Adding the property %s from %s threw an error", property.getName(), clone.getName()));
            }
            
            this.exchangeHistory.Append(property, ChangeKind.UPDATE);
        }
    }

    /**
     * Prepares all the {@linkplain Interface}s that the provided {@linkplain Class} ports use
     * 
     * @param element the {@linkplain Class}
     */
    private void PrepareInterfaces(Class element)
    {
        for (Port port : element.getOwnedPort().stream().filter(x -> x.getType() != null).collect(Collectors.toList()))
        {
            Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship> relationships = port.getType().get_relationshipOfRelatedElement();
            
            List<Usage> usagesRelationships = Utils.StreamExtensions.OfType(relationships.stream(), Usage.class).collect(Collectors.toList());
            
            for (Interface interfaceToAdd : Utils.StreamExtensions.OfType(usagesRelationships.stream()
                    .flatMap(x -> x.getTarget().stream()), Interface.class)
                    .filter(x -> this.transactionService.IsNew(x))
                    .collect(Collectors.toList()))
            {
                if(this.sessionService.GetProject().getPrimaryModel().getOwnedElement().stream().noneMatch(x -> AreTheseEquals(x.getID(), interfaceToAdd.getID())))
                {
                    this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(interfaceToAdd);
                    this.exchangeHistory.Append(interfaceToAdd, ChangeKind.CREATE);
                }
            }
            
            for (Usage usage : usagesRelationships)
            {
                if(this.sessionService.GetProject().getPrimaryModel().getOwnedElement().stream().noneMatch(x -> AreTheseEquals(x.getID(), usage.getID())))
                {
                    this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(usage);
                }                
            }
            
            for (Interface interfaceToAdd : Utils.StreamExtensions.OfType(relationships.stream(), InterfaceRealization.class)
                    .map(x -> x.getContract())
                    .filter(x -> this.transactionService.IsNew(x))
                    .collect(Collectors.toList()))
            {
                if(this.sessionService.GetProject().getPrimaryModel().getOwnedElement().stream().noneMatch(x -> AreTheseEquals(x.getID(), interfaceToAdd.getID())))
                {
                    this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(interfaceToAdd);
                    this.exchangeHistory.Append(interfaceToAdd, ChangeKind.CREATE);
                }                
            }
        }
        
        this.PrepareInterfacesForChildren(element);
    }

    /**
     * Prepares all the {@linkplain Interface}s that the provided {@linkplain Class} children ports use
     * 
     * @param element the {@linkplain Class}
     */
    private void PrepareInterfacesForChildren(Class element)
    {
        for (Class childComponent : element.eContents().stream()
                .filter(x -> x instanceof Class)
                .map(x -> (Class)x)
                .collect(Collectors.toList()))
        {
            this.PrepareInterfaces(childComponent);
        }
    }
    
    /**
     *  Updates the provided requirement owning {@linkplain Package}s
     * 
     * @param requirement the {@linkplain Class} requirement
     */
    private void UpdateRequirementPackage(Class requirement)
    {        
        Package container = (Package)requirement.eContainer();
        Boolean containerIsCloned = null;
        
        while(container != null && !(containerIsCloned = this.transactionService.IsCloned(container)) 
                && container.eContainer() instanceof Package 
                && !(AreTheseEquals(((Package)container.eContainer()).getName(), "Model", true)))
        {
            container = (Package)container.eContainer();
        }

        final Package containerToUpdate = container;
        
        if(containerToUpdate == null)
        {
            return;
        }        

        if(containerIsCloned.booleanValue())
        {
            this.UpdateRequirementPackage(containerToUpdate);
            this.exchangeHistory.Append(containerToUpdate, ChangeKind.UPDATE);
        }
        else
        {
            if(!this.sessionService.GetProject().getPrimaryModel().getOwnedElement().removeIf(x -> AreTheseEquals(((MDObject) x).getID(), containerToUpdate.getID())))
            {
                this.exchangeHistory.Append(containerToUpdate, ChangeKind.CREATE);
            }
            
            this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(containerToUpdate);

            this.exchangeHistory.Append(requirement, ChangeKind.CREATE);
        }
    }
    
    /**
     * Updates the provided cloned {@linkplain Package}
     * 
     * @param containerToUpdate the {@linkplain Package}
     */
    private void UpdateRequirementPackage(Package containerToUpdate)
    {
        ClonedReferenceElement<Package> requirementPkgCloneReference = this.transactionService.GetClone(containerToUpdate);
        
        this.UpdateChildrenOfType(requirementPkgCloneReference.GetOriginal().getOwnedElement(), 
                requirementPkgCloneReference.GetClone().getOwnedElement());
    }

    /**
     * Adds the new {@linkplain #TElement} contained in the cloned collection to the original collection
     * 
     * @param <TElement> the type of {@linkplain Element} the collections contains
     * @param originalCollection the original collection
     * @param clonedCollection the cloned collection
     */
    private <TElement extends Element> void UpdateChildrenOfType(Collection<TElement> originalCollection,
            Collection<TElement> clonedCollection)
    {
        for (TElement elementToAdd : new ArrayList<>(clonedCollection))
        {
            if(originalCollection.stream().noneMatch(x -> AreTheseEquals(elementToAdd.getID(), x.getID())))
            {
                originalCollection.add(elementToAdd);
                
                if(elementToAdd instanceof NamedElement)
                {
                    this.exchangeHistory.Append((NamedElement) elementToAdd, ChangeKind.CREATE);
                }
            }
        }
    }

    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean TransferToHub()
    {
        try
        {
            this.isHubSessionRefreshSilent = true;
            Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
            Iteration iterationClone = iterationTransaction.getLeft();
            ThingTransaction transaction = iterationTransaction.getRight();
            
            if(!this.hubController.TrySupplyAndCreateLogEntry(transaction))
            {
                this.logService.Append("Transfer to the HUB aborted!", NotificationSeverity.WARNING);
                return true;
            }            
            
            this.PrepareThingsForTransfer(iterationClone, transaction);

            this.mappingConfigurationService.PersistExternalIdentifierMap(transaction, iterationClone);
            transaction.createOrUpdate(iterationClone);
            
            this.hubController.Write(transaction);
            this.mappingConfigurationService.RefreshExternalIdentifierMap();
            boolean result = this.hubController.Refresh();
            this.UpdateParameterValueSets();
            this.isHubSessionRefreshSilent = false;
            return result && this.hubController.Refresh();
        }
        catch (Exception exception)
        {
            this.logService.Append(exception.toString(), NotificationSeverity.ERROR);
            this.logger.catching(exception);
            return false;
        }
        finally
        {
            this.selectedDstMapResultForTransfer.clear();
            this.isHubSessionRefreshSilent = false;
        }
    }
    
    /**
     * Updates the {@linkplain ValueSet} with the new values
     * 
     * @return a value indicating whether the operation went OK
     * @return a {@linkplain Pair} of a value indicating whether the transaction has been committed with success
     * and a string of the exception if any
     * @throws TransactionException
     */
    public void UpdateParameterValueSets() throws TransactionException
    {
        Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
        Iteration iterationClone = iterationTransaction.getLeft();
        ThingTransaction transaction = iterationTransaction.getRight();

        List<Parameter> allParameters = this.dstMapResult.stream()
                .filter(x -> x.GetHubElement() instanceof ElementDefinition)
                .flatMap(x -> ((ElementDefinition)x.GetHubElement()).getParameter().stream())
                .collect(Collectors.toList());
        
        for(Parameter parameter : allParameters)
        {
            Ref<Parameter> refNewParameter = new Ref<>(Parameter.class);
            
            if(this.hubController.TryGetThingById(parameter.getIid(), refNewParameter))
            {
                Parameter newParameterCloned = refNewParameter.Get().clone(false);
    
                for (int index = 0; index < newParameterCloned.getValueSet().size(); index++)
                {
                    ParameterValueSet clone = newParameterCloned.getValueSet().get(index).clone(false);
                    this.UpdateValueSet(clone, parameter.getValueSet().get(index));
                    transaction.createOrUpdate(clone);
                }
    
                transaction.createOrUpdate(newParameterCloned);
            }
        }
        
        transaction.createOrUpdate(iterationClone);
        this.hubController.Write(transaction);
    }    

    /**
     * Updates the specified {@linkplain ParameterValueSetBase}
     * 
     * @param clone the {@linkplain ParameterValueSetBase} to update
     * @param valueSet the {@linkplain ValueSet} that contains the new values
     */
    private void UpdateValueSet(ParameterValueSetBase clone, ValueSet valueSet)
    {
        this.exchangeHistory.Append(clone, valueSet);
        
        clone.setManual(valueSet.getManual());
        clone.setValueSwitch(valueSet.getValueSwitch());
    }

    /**
     * Prepares all the {@linkplain Thing}s that are to be updated or created
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private void PrepareThingsForTransfer(Iteration iterationClone, ThingTransaction transaction) throws TransactionException
    {
        this.AddOrRemoveBinaryRelationshipForTransfer();
        ArrayList<Thing> thingsToTransfer = new ArrayList<>(this.selectedDstMapResultForTransfer);
        
        Predicate<? super MappedElementRowViewModel<? extends Thing, ? extends Class>> selectedMappedElement = 
                x -> this.selectedDstMapResultForTransfer.stream().anyMatch(t -> t.getIid().equals(x.GetHubElement().getIid()));
                
        Collection<Relationship> relationships = this.dstMapResult.stream()
                .filter(selectedMappedElement)
                .flatMap(x -> x.GetRelationships().stream())
                .collect(Collectors.toList());
        
        this.logService.Append("Processing %s relationship(s)", relationships.size() + 
                this.selectedDstMapResultForTransfer.stream().filter(x -> x instanceof BinaryRelationship).count());
        
        thingsToTransfer.addAll(relationships);
                
        for (Thing thing : thingsToTransfer)
        {
            switch(thing.getClassKind())
            {
                case ElementDefinition:
                    this.PrepareElementDefinitionForTransfer(iterationClone, transaction, (ElementDefinition)thing);
                    break;
                case RequirementsSpecification:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, (RequirementsSpecification)thing);
                    break;
                case Requirement:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, thing.getContainerOfType(RequirementsSpecification.class));
                    break;
                case BinaryRelationship:
                    this.AddOrUpdateIterationAndTransaction((BinaryRelationship)thing, iterationClone.getRelationship(), transaction);
                    break;
                default:
                    break;
            }
            
            if(thing.getContainer() == null)
            {
                this.logService.Append("%s thing %s has a null container", thing.getClassKind(), NotificationSeverity.ERROR, thing.getUserFriendlyName());
            }
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} to prepare
     * @throws TransactionException
     */
    private void PrepareElementDefinitionForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            ElementDefinition elementDefinition) throws TransactionException
    {
        this.AddOrUpdateIterationAndTransaction(elementDefinition, iterationClone.getElement(), transaction);
        
        for (ElementUsage elementUsage : elementDefinition.getContainedElement())
        {
            this.AddOrUpdateIterationAndTransaction(elementUsage.getElementDefinition(), iterationClone.getElement(), transaction);
            this.PrepareDefinition(transaction, elementUsage.getElementDefinition());
            this.AddOrUpdateIterationAndTransaction(elementUsage, elementDefinition.getContainedElement(), transaction);
        }
        
        if(transaction.getAddedThing().stream().anyMatch(x -> AreTheseEquals(x.getIid(), elementDefinition.getIid())))
        {
            this.PrepareDefinition(transaction, elementDefinition);
        }

        for(Parameter parameter : elementDefinition.getParameter())
        {            
            transaction.createOrUpdate(parameter);            
            this.PrepareStates(iterationClone, transaction, parameter);
        }
    }

    /**
     * Prepares the {@linkplain ActualFiniteState} from the provided parameter state dependency
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param parameter the {@linkplain Parameter}
     * @throws TransactionException 
     */
    private void PrepareStates(Iteration iterationClone, ThingTransaction transaction, Parameter parameter) throws TransactionException
    {
        if(parameter.getStateDependence() == null)
        {
            return;
        }
        
        for (PossibleFiniteStateList possibleFiniteStateList : parameter.getStateDependence().getPossibleFiniteStateList())
        {
            this.AddOrUpdateIterationAndTransaction(possibleFiniteStateList, iterationClone.getPossibleFiniteStateList(), transaction);
            
            for (PossibleFiniteState possibleFiniteState : possibleFiniteStateList.getPossibleState())
            {
                transaction.createOrUpdate(possibleFiniteState);
            }
        }
        
        this.AddOrUpdateIterationAndTransaction(parameter.getStateDependence(), iterationClone.getActualFiniteStateList(), transaction);
    }

    /**
     * Prepares any transferable {@linkplain Definition} from the provided {@linkplain ElementDefinition}
     * 
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} that can contain a transferable {@linkplain Definition}
     * @throws TransactionException
     */
    private void PrepareDefinition(ThingTransaction transaction, ElementDefinition elementDefinition) throws TransactionException
    {
        Optional<Definition> definition = elementDefinition.getDefinition().stream()
                                                .filter(x -> AreTheseEquals(x.getLanguageCode(), BlockToElementMappingRule.MDIID))
                                                .findFirst();
        
        if(definition.isPresent())
        {
            this.AddOrUpdateIterationAndTransaction(definition.get(), elementDefinition.getDefinition(), transaction);
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} to prepare
     * @throws TransactionException
     */
    private void PrepareRequirementForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            RequirementsSpecification requirementsSpecification) throws TransactionException
    {        
        this.AddOrUpdateIterationAndTransaction(requirementsSpecification, iterationClone.getRequirementsSpecification(), transaction);
        
        ContainerList<RequirementsGroup> groups = requirementsSpecification.getGroup();
        
        this.RegisterRequirementsGroups(transaction, groups);
        
        for(Requirement requirement : requirementsSpecification.getRequirement())
        {
            transaction.createOrUpdate(requirement);
            
            for (Definition definition : requirement.getDefinition())
            {
                transaction.createOrUpdate(definition);
            }
        }
    }

    /**
     * Registers the {@linkplain RequirementsGroup} to be created or updated
     * 
     * @param transaction
     * @param groups
     * @throws TransactionException
     */
    private void RegisterRequirementsGroups(ThingTransaction transaction, ContainerList<RequirementsGroup> groups) throws TransactionException
    {
        for(RequirementsGroup requirementsGroup : groups)
        {
            transaction.createOrUpdate(requirementsGroup);
            
            if(!requirementsGroup.getGroup().isEmpty())
            {
                this.RegisterRequirementsGroups(transaction, requirementsGroup.getGroup());
            }
        }
    }

    /**
     * Updates the {@linkplain ThingTransaction} and the {@linkplain ContainerList} with the provided {@linkplain Thing}
     * 
     * @param <T> the Type of the current {@linkplain Thing}
     * @param thing the {@linkplain Thing}
     * @param containerList the {@linkplain ContainerList} of {@linkplain Thing} typed as T
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private <T extends Thing> void AddOrUpdateIterationAndTransaction(T thing, ContainerList<T> containerList, ThingTransaction transaction) throws TransactionException
    {
        try
        {
            if(thing.getContainer() == null || containerList.stream().noneMatch(x -> x.getIid().equals(thing.getIid())))
            {
                containerList.add(thing);
                this.exchangeHistory.Append(thing, ChangeKind.CREATE);
            }
            else
            {
                this.exchangeHistory.Append(thing, ChangeKind.UPDATE);
            }
                        
            transaction.createOrUpdate(thing);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Adds or Removes all {@linkplain TElement} from/to the relevant selected things to transfer
     * depending on whether the {@linkplain ClassKind} was specified
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    @Override
    public void AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind classKind, boolean shouldRemove)
    {
        if(classKind == null)
        {
            this.AddOrRemoveAllFromSelectedHubMapResultForTransfer(shouldRemove);
        }
        else
        {
            this.AddOrRemoveAllFromSelectedDstMapResultForTransfer(classKind, shouldRemove);
        }
    }
    
    /**
     * Adds or Removes all {@linkplain Thing} from/to the relevant selected things to transfer
     * 
     * @param classKind the {@linkplain ClassKind} of the {@linkplain Thing}s to add or remove depending on which impact view it has been called from
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedDstMapResultForTransfer(ClassKind classKind, boolean shouldRemove)
    {
        Predicate<? super Thing> predicateClassKind = x -> x.getClassKind() == classKind;
        
        this.selectedDstMapResultForTransfer.removeIf(predicateClassKind);
        
        if(!shouldRemove)
        {
            this.selectedDstMapResultForTransfer.addAll(
                    this.dstMapResult.stream()
                        .map(MappedElementRowViewModel::GetHubElement)
                        .filter(predicateClassKind)
                        .collect(Collectors.toList()));
        }
    }

    /**
     * Adds or Removes all {@linkplain Class} from/to the relevant selected things to transfer
     * 
     * @param shouldRemove a value indicating whether the things are to be removed
     */
    private void AddOrRemoveAllFromSelectedHubMapResultForTransfer(boolean shouldRemove)
    {
        this.selectedHubMapResultForTransfer.clear();
        
        if(!shouldRemove)
        {
            List<? extends Class> elements = this.hubMapResult.stream()
                    .map(MappedElementRowViewModel::GetDstElement)
                    .collect(Collectors.toList());
            
            this.selectedHubMapResultForTransfer.addAll(elements);
        }
    }

    /**
     * Tries to get the corresponding element based on the provided {@linkplain DefinedThing} name or short name. 
     * 
     * @param <TElement> the type of {@linkplain Element} to query
     * @param thing the {@linkplain DefinedThing} that can potentially match a {@linkplain #TElement} 
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain Element} has been found
     */
    @Override
    public <TElement extends NamedElement> boolean TryGetElementByName(DefinedThing thing, Ref<TElement> refElement)
    {
        return this.TryGetElementBy(x -> x instanceof NamedElement
                && (AreTheseEquals(thing.getName(), ((NamedElement)x).getName(), true)
                || AreTheseEquals(thing.getShortName(), ((NamedElement)x).getName(), true)), refElement);
    }
        
    /**
     * Tries to get the corresponding element that has the provided Id
     * 
     * @param <TElement> the type of {@linkplain Element} to query
     * @param elementId the {@linkplain String} id of the searched element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain Element} has been found
     */
    @Override
    public <TElement extends NamedElement> boolean TryGetElementById(String elementId, Ref<TElement> refElement)
    {
        return this.TryGetElementBy(x -> AreTheseEquals(elementId, x.getID()), refElement);
    }
    
    /**
     * Tries to get the corresponding element that answer to the provided {@linkplain Predicate}
     * 
     * @param <TElement> the type of {@linkplain NamedElement} to query
     * @param predicate the {@linkplain Predicate} to verify in order to match the element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain #TElement} has been found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends NamedElement> boolean TryGetElementBy(Predicate<TElement> predicate, Ref<TElement> refElement)
    {
        Optional<TElement> element = this.sessionService.GetProjectElements().stream()
                .filter(x -> refElement.GetType().isInstance(x))
                .map(x -> (TElement)x)
                .filter(predicate)
                .findFirst();
        
        if(element.isPresent() && refElement.GetType().isInstance(element.get()))
        {
            refElement.Set(element.get());
        }
        
        return refElement.HasValue();
    }

    /**
     * Tries to get a {@linkplain InstanceSpecification} unit that matches the provided {@linkplain MeasurementUnit}
     * 
     * @param unit the {@linkplain MeasurementUnit} of reference
     * @param refDataType the {@linkplain Ref} of {@linkplain InstanceSpecification}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean TryGetUnit(MeasurementUnit unit, Ref<InstanceSpecification> refUnit)
    {
        for (InstanceSpecification dataType :  this.stereotypeService.GetUnits())
        {            
            if(this.VerifyNames(unit, dataType.getName()))
            {
                refUnit.Set(dataType);
                break;
            }
        }
        
        return refUnit.HasValue();
    }
    
    /**
     * Tries to get a {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param parameterType the {@linkplain ParameterType} of reference
     * @param scale the {@linkplain MeasurementScale} of reference
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean TryGetDataType(ParameterType parameterType, MeasurementScale scale, Ref<DataType> refDataType)
    {
        Ref<QuantityKind> refGeneral = new Ref<>(QuantityKind.class);
        
        if(parameterType instanceof SpecializedQuantityKind)
        {
            refGeneral.Set(((SpecializedQuantityKind)parameterType).getGeneral());
        }

        this.stereotypeService.GetDataTypes().stream()
                .filter(x -> this.VerifyNames(parameterType, scale, x) || (refGeneral.HasValue() && this.VerifyNames(refGeneral.Get(), scale, x)))
                .findFirst()
                .ifPresent(x -> refDataType.Set(x));
        
        return refDataType.HasValue();
    }

    /**
     * Verifies that the provided {@linkplain ParameterType} and {@linkplain DataType} have matching names
     * 
     * @param thing the {@linkplain ParameterType}
     * @param scale the {@linkplain MeasurementScale} of reference
     * @return a {@linkplain boolean}
     */
    private boolean VerifyNames(ParameterType thing, MeasurementScale scale, DataType dataType)
    {        
        if(thing instanceof QuantityKind && scale != null && StringUtils.isNotBlank(scale.getName()) && dataType.getName().contains("["))
        {
            return this.VerifyQuantityKindNameAndUnit((QuantityKind)thing, scale, dataType);
        }
        else if(scale == null)
        {
            return this.VerifyNames(thing, dataType.getName());
        }
        
        return false;
    }

    /**
     * Verifies that the provided {@linkplain ParameterType} and {@linkplain DataType} have matching names
     * 
     * @param <TThing> the type of the thing used to constraint the thing to be a {@linkplain NamedThing} and {@linkplain ShortNamedThing}
     * @param thing the {@linkplain #TThing} of reference
     * @param dataTypeName the {@linkplain String} {@linkplain DataType} name
     * @return a {@linkplain boolean}
     */
    private <TThing extends NamedThing & ShortNamedThing> boolean VerifyNames(TThing thing, String dataTypeName)
    {
        return AreTheseEquals(dataTypeName, thing.getName(), true) 
              || AreTheseEquals(dataTypeName, thing.getShortName(), true);
    }

    /**
     * Verifies that the provided {@linkplain ParameterType} and {@linkplain DataType} have matching names, 
     * and also that the provided {@linkplain MeasurementScale} matches the {@linkplain DataType} unit
     * 
     * @param quantityKind the {@linkplain QuantityKind} parameter type
     * @param scale the {@linkplain MeasurementScale} of reference
     * @param dataType the {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    private boolean VerifyQuantityKindNameAndUnit(QuantityKind quantityKind, MeasurementScale scale, DataType dataType)
    {
        try
        {
            String[] dataTypeNameAndUnit = dataType.getName().split("[\\[\\]]");
                        
            return this.VerifyNames(quantityKind, dataTypeNameAndUnit[0]) && this.VerifyNames(scale, dataTypeNameAndUnit[1]);
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
    }
}
