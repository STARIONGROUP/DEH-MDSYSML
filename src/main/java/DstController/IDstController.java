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

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappableThingCollection;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

/**
 * The {@linkplain IDstController} is the interface definition for the {@linkplain DstController}
 */
public interface IDstController extends IDstControllerBase
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
     * Gets a value indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain boolean}
     */
    boolean HasOneDocumentOpen();

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> HasOneDocumentOpenObservable();

    /**
     * Gets the open Document ({@linkplain Project}) from the running instance of Cameo/MagicDraw
     * 
     * @return the {@linkplain Project}
     */
    Project OpenDocument();

    /**
     * Gets The {@linkplain ObservableCollection} of dst map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    ObservableCollection<Thing> GetDstMapResult();
    
    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Class}
     */
    ObservableCollection<Class> GetHubMapResult();

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
    ObservableCollection<Class> GetSelectedHubMapResultForTransfer();

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
}
