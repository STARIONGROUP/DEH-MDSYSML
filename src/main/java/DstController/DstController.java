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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListener;
import com.nomagic.magicdraw.uml.ElementImpl;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappingEngineService;
import cdp4common.engineeringmodeldata.ElementDefinition;
import io.reactivex.Observable;

/**
 * The {@linkplain DstController} is a class that manage transfer and connection to attached running instance of Cameo/MagicDraw
 */
public final class DstController implements IDstController
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ProjectEventListener} to monitor project open and closed in Cameo/MagicDraw
     */
    private final MDSysMLProjectEventListener projectEventListener = new MDSysMLProjectEventListener();

    /**
     * The {@linkplain IMappingEngine} instance
     */
    private IMappingEngineService mappingEngine;

    /**
     * Gets the open Document ({@linkplain Project}) from the running instance of Cameo/MagicDraw
     * 
     * @return the {@linkplain Project}
     */
    @Override
    public Project OpenDocument() 
    {
        return this.projectEventListener.OpenDocumentObservable.Value();
    }

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> HasOneDocumentOpenObservable()
    {
        return this.projectEventListener.HasOneDocumentOpenObservable.Observable();
    }
    
    /**
     * Gets a value indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean HasOneDocumentOpen()
    {
        return this.projectEventListener.HasOneDocumentOpenObservable.Value().booleanValue();
    }
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<ElementDefinition> dstMapResult = new ObservableCollection<ElementDefinition>(ElementDefinition.class);

    /**
     * Gets The {@linkplain ObservableCollection} of dst map result
     */
    @Override
    public ObservableCollection<ElementDefinition> GetDstMapResult()
    {
        return this.dstMapResult;
    }
    
    /**
     * Initializes a new {@linkplain DstController}
     * 
     * @param mappingEngine the {@linkplain IMappingEngine} instance
     */
    public DstController(IMappingEngineService mappingEngine)
    {
        this.mappingEngine = mappingEngine;
        Application applicationInstance = Application.getInstance();
        applicationInstance.addProjectEventListener(this.projectEventListener);
    }
    
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain Collection} of {@linkplain Object} to map
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @Override
    public boolean Map(ObservableCollection<?> input)
    {        
        Object resultAsObject = this.mappingEngine.Map(input);
        
        if(resultAsObject instanceof Collection<?>)
        {
            ArrayList<?> resultAsCollection = (ArrayList<?>) resultAsObject;
            
            if(!resultAsCollection.isEmpty())
            {
                if(resultAsCollection.stream().allMatch(ElementDefinition.class::isInstance))
                {
                    this.dstMapResult.addAll(resultAsCollection.stream().map(ElementDefinition.class::cast).collect(Collectors.toList()));
                    return true;
                }
            }
        }
        
        return false;
    }
}
