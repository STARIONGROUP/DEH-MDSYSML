/*
 * IDstController.java
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

import java.util.Collection;
import java.util.function.Predicate;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappableThingCollection;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.ParameterType;
import io.reactivex.Observable;

/**
 * The {@linkplain IDstController} is the interface definition for the {@linkplain DstController}
 */
public interface IDstController extends IDstControllerBase<Class>
{
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    boolean Map(IMappableThingCollection input, MappingDirection mappingDirection);


    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean TransferToHub();

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    Observable<MappingDirection> GetMappingDirection();

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    MappingDirection ChangeMappingDirection();

    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    ObservableCollection<Thing> GetSelectedDstMapResultForTransfer();
    
    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the Cameo/MagicDraw
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    ObservableCollection<NamedElement> GetSelectedHubMapResultForTransfer();

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    MappingDirection CurrentMappingDirection();

    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean Transfer();

    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     * 
     * @return the number of mapped things loaded
     */
    void LoadMapping();

    /**
     * Tries to get the corresponding element that answer to the provided {@linkplain Predicate}
     * 
     * @param <TElement> the type of {@linkplain #TDstElement} to query
     * @param predicate the {@linkplain Predicate} to verify in order to match the element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain #TElement} has been found
     */
    <TElement extends NamedElement> boolean TryGetElementBy(Predicate<TElement> predicate, Ref<TElement> refElement);
    
    /**
     * Tries to get the corresponding element that has the provided Id
     * 
     * @param <TElement> the type of {@linkplain #TDstElement} to query
     * @param elementId the {@linkplain String} id of the searched element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain #TElement} has been found
     */
    <TElement extends NamedElement> boolean TryGetElementById(String elementId, Ref<TElement> refElement);
    
    /**
     * Tries to get the corresponding element based on the provided {@linkplain DefinedThing} name or short name. 
     * 
     * @param <TElement> the type of {@linkplain #TDstElement} to query
     * @param thing the {@linkplain DefinedThing} that can potentially match a {@linkplain #TElement} 
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain #TElement} has been found
     */
    <TElement extends NamedElement> boolean TryGetElementByName(DefinedThing thing, Ref<TElement> refElement);

    /**
     * Transfers all the {@linkplain Class} contained in the {@linkplain huMapResult} to the DST
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean TransferToDst();
    
    /**
     * Tries to get a {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param parameterType the {@linkplain ParameterType} of reference
     * @param scale the {@linkplain MeasurementScale} of reference
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    boolean TryGetDataType(ParameterType parameterType, MeasurementScale scale, Ref<DataType> refDataType);

    /**
     * Tries to get a {@linkplain InstanceSpecification} unit that matches the provided {@linkplain MeasurementUnit}
     * 
     * @param unit the {@linkplain MeasurementUnit} of reference
     * @param refDataType the {@linkplain Ref} of {@linkplain InstanceSpecification}
     * @return a {@linkplain boolean}
     */
    boolean TryGetUnit(MeasurementUnit unit, Ref<InstanceSpecification> refUnit);

    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     */
    ObservableCollection<BinaryRelationship> GetMappedDirectedRelationshipToBinaryRelationships();

    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain DirectedRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain DirectedRelationship}s
     */
    ObservableCollection<Abstraction> GetMappedBinaryRelationshipsToDirectedRelationships();


    /**
     * Pre-maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and return the map result
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @return a {@linkplain Collection} of {@linkplain MappedElementRowViewModel}
     */
    Collection<MappedElementRowViewModel<DefinedThing, Class>> PreMap(IMappableThingCollection input);
}
