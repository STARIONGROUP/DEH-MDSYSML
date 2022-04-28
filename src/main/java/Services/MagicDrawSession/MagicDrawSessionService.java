/*
 * MagicDrawSessionService.java
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
package Services.MagicDrawSession;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawSessionService} is the service providing easier access to the MagicDraw {@linkplain Sessions}s
 */
public class MagicDrawSessionService implements IMagicDrawSessionService
{
    /**
     * The {@linkplain ProjectEventListener} to monitor project open and closed in Cameo/MagicDraw
     */
    private final IMagicDrawProjectEventListener projectEventListener;
    
    /**
     * Gets the open Document ({@linkplain Project}) from the running instance of Cameo/MagicDraw
     * 
     * @return the {@linkplain Project}
     */
    @Override
    public Project GetProject() 
    {
        return this.projectEventListener.OpenDocument().Value();
    }
    
    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating the subscribers whenever the open document gets saved
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> SessionUpdated() 
    {
        return this.projectEventListener.ProjectSaved();
    }

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> HasAnyOpenSessionObservable()
    {
        return this.projectEventListener.HasOneDocumentOpen().Observable();
    }
    
    /**
     * Gets a value indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        return this.projectEventListener.HasOneDocumentOpen().Value().booleanValue();
    }
    
    public MagicDrawSessionService(IMagicDrawProjectEventListener projectEventListener)
    {
        this.projectEventListener = projectEventListener;
    }

    /**
     * Gets the open project element
     * 
     * @return a {@linkplain Collection} of {@linkplain Element}
     */
    @Override
    public Collection<Element> GetProjectElements()
    {
        return this.GetProject().getPrimaryModel().getPackagedElement().stream().map(Element.class::cast).collect(Collectors.toList());
    }
    
    /**
     * Gets the DataTypes element from the {@linkplain #GetProject()}
     * 
     * @return a {@linkplain Collection} of {@linkplain #DataType}
     */
    @Override
    public Collection<DataType> GetDataTypes()
    {
        Stereotype stereotype = StereotypeUtils.GetStereotype(this.GetProject(), Stereotypes.ValueType);
        
        List<Element> elements = StereotypesHelper.getExtendedElements(stereotype);
        
        return elements.stream()
                .filter(x -> x instanceof DataType)
                .map(x -> (DataType)x)
                .collect(Collectors.toList());
    }    

    /**
     * Gets the DataTypes element from the {@linkplain #GetProject()}
     * 
     * @return a {@linkplain Collection} of {@linkplain #DataType}
     */
    @Override
    public Collection<InstanceSpecification> GetUnits()
    {
        List<Element> units = StereotypesHelper.getExtendedElements(MDCustomizationForSysMLProfile.getInstance(this.GetProject()).getUnit());
                
        return units.stream()
                .filter(x -> x instanceof InstanceSpecification && StereotypeUtils.DoesItHaveTheStereotype(x, Stereotypes.Unit))
                .map(x -> (InstanceSpecification)x)
                .collect(Collectors.toList());
    }    
}
