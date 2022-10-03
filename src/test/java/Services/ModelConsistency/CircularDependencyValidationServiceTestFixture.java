/*
 * CircularDependencyValidationServiceTestFixture.java
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;

import Reactive.ObservableValue;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;

public class CircularDependencyValidationServiceTestFixture
{
    private IMagicDrawSessionService sessionService;
    private IStereotypeService stereotypeService;
    private IMagicDrawUILogService logService;
    private CircularDependencyValidationService service;
    private Collection<Element> elements = new ArrayList<>();
    private ObservableValue<Boolean> projectSavedObservable;
    private ObservableValue<Boolean> sessionOpenObservable;
    private ObservableValue<Boolean> sessionEventObservable;

    @BeforeEach
    public void Setup()
    {
        this.sessionService = mock(IMagicDrawSessionService.class);
        this.stereotypeService = mock(IStereotypeService.class);
        this.logService = mock(IMagicDrawUILogService.class);

        this.SetupElements();
        this.projectSavedObservable = new ObservableValue<Boolean>(false, Boolean.class);
        this.sessionOpenObservable = new ObservableValue<Boolean>(false, Boolean.class);
        this.sessionEventObservable = new ObservableValue<Boolean>(false, Boolean.class);
        
        when(this.sessionService.GetProjectName()).thenReturn("MODEL");
        when(this.sessionService.GetAllProjectElements()).thenReturn(this.elements);
        when(this.sessionService.ProjectSaved()).thenReturn(this.projectSavedObservable.Observable());
        when(this.sessionService.HasAnyOpenSessionObservable()).thenReturn(this.sessionOpenObservable.Observable());
        when(this.sessionService.GetSessionEvent()).thenReturn(this.sessionEventObservable);
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), any())).thenReturn(true);
        when(this.stereotypeService.IsPartProperty(any())).thenReturn(true);
                
        this.service = new CircularDependencyValidationService(this.sessionService, this.stereotypeService, this.logService);
    }

    private void SetupElements()
    {
        Package mainPackage = mock(Package.class);
        when(mainPackage.getName()).thenReturn("Package");
        ArrayList<Element> containedElements = new ArrayList<>();
        when(mainPackage.getOwnedElement()).thenReturn(containedElements);

        Class typeBlock = mock(Class.class);
        when(typeBlock.getName()).thenReturn("TypeBlock");
        ArrayList<Property> typeBlockProperties = new ArrayList<>();
        when(typeBlock.getOwnedAttribute()).thenReturn(typeBlockProperties);
        
        Property refProperty = mock(Property.class);
        when(this.stereotypeService.IsReferenceProperty(refProperty)).thenReturn(true);
        typeBlockProperties.add(refProperty);
        
        Property valProperty = mock(Property.class);
        when(this.stereotypeService.IsValueProperty(valProperty)).thenReturn(true);
        typeBlockProperties.add(valProperty);
        
        Property partPropertySameId = mock(Property.class);
        when(partPropertySameId.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.IsPartProperty(partPropertySameId)).thenReturn(true);
        typeBlockProperties.add(partPropertySameId);
        
        Property partPropertyOtherId = mock(Property.class);
        String id = UUID.randomUUID().toString();
        when(partPropertyOtherId.getID()).thenReturn(id);
        when(this.stereotypeService.IsPartProperty(partPropertyOtherId)).thenReturn(true);
        typeBlockProperties.add(partPropertyOtherId);
        
        Property valTypeProperty = mock(Property.class);
        when(this.stereotypeService.IsPartProperty(valTypeProperty)).thenReturn(false);
        typeBlockProperties.add(valTypeProperty);

        Class block = mock(Class.class);
        when(block.getName()).thenReturn("block");
        when(this.stereotypeService.DoesItHaveTheStereotype(block, Stereotypes.Block)).thenReturn(true);

        ArrayList<Property> properties = new ArrayList<>();
        Property referenceProperty = mock(Property.class);
        when(this.stereotypeService.IsReferenceProperty(referenceProperty)).thenReturn(true);
        properties.add(referenceProperty);

        Property valuePropertyDataType = mock(Property.class);
        when(valuePropertyDataType.getType()).thenReturn(mock(DataType.class));
        when(this.stereotypeService.IsValueProperty(valuePropertyDataType)).thenReturn(false);
        properties.add(valuePropertyDataType);

        Property partProperty = mock(Property.class);
        when(this.stereotypeService.IsPartProperty(partProperty)).thenReturn(true);
        when(partProperty.getType()).thenReturn(typeBlock);
        when(partProperty.getID()).thenReturn(id);
        properties.add(partProperty);
        
        Property valueProperty = mock(Property.class);
        when(this.stereotypeService.IsValueProperty(valuePropertyDataType)).thenReturn(true, false);
        properties.add(valueProperty);
        properties.add(valueProperty);

        Property partProperty2 = mock(Property.class);
        when(partProperty2.getType()).thenReturn(mock(Class.class));
        when(this.stereotypeService.IsValueProperty(partProperty2)).thenReturn(false);
        properties.add(partProperty2);

        when(block.getOwnedAttribute()).thenReturn(properties);

        Port port = mock(Port.class);
        ArrayList<Port> ports = new ArrayList<>();
        ports.add(port);
        when(block.getOwnedPort()).thenReturn(ports);

        Class requirement = mock(Class.class);
        when(requirement.getName()).thenReturn("requirement");
        when(this.stereotypeService.DoesItHaveTheStereotype(requirement, Stereotypes.Requirement)).thenReturn(true);

        containedElements.add(block);
        containedElements.add(requirement);

        this.elements.addAll(containedElements);
    }
    
    @Test
    public void VerifyValidationTask()
    {
        assertDoesNotThrow(() -> this.sessionOpenObservable.Value(true));
        when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
        assertDoesNotThrow(() -> this.sessionOpenObservable.Value(true));
        assertEquals(0, this.service.GetInvalidPaths().size());
    }
    
    @Test
    public void VerifyWhenThereIsCircularDependency()
    {
        when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
        Package mainPackage = mock(Package.class);
        when(mainPackage.getName()).thenReturn("Package");

        this.elements.add(mainPackage);
        ArrayList<Element> containedElements = new ArrayList<>();
        when(mainPackage.getOwnedElement()).thenReturn(containedElements);

        Class blockA = mock(Class.class);
        when(blockA.getName()).thenReturn("blockB");
        when(this.stereotypeService.DoesItHaveTheStereotype(blockA, Stereotypes.Block)).thenReturn(true);
        containedElements.add(blockA);
        
        ArrayList<Property> blockAProperties = new ArrayList<>();
        when(blockA.getOwnedAttribute()).thenReturn(blockAProperties);

        Class blockB = mock(Class.class);
        when(blockB.getName()).thenReturn("blockB");
        when(this.stereotypeService.DoesItHaveTheStereotype(blockB, Stereotypes.Block)).thenReturn(true);
        containedElements.add(blockB);
        
        ArrayList<Property> blockBProperties = new ArrayList<>();
        when(blockB.getOwnedAttribute()).thenReturn(blockBProperties);

        Property partPropertyA = mock(Property.class);
        when(partPropertyA.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyA.getType()).thenReturn(blockA);
        when(this.stereotypeService.IsPartProperty(partPropertyA)).thenReturn(true);
        when(partPropertyA.getObjectParent()).thenReturn(blockB);
        blockBProperties.add(partPropertyA);
        
        Property partPropertyB = mock(Property.class);
        when(partPropertyB.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyA.getType()).thenReturn(blockB);
        when(this.stereotypeService.IsPartProperty(partPropertyB)).thenReturn(true);
        when(partPropertyB.getObjectParent()).thenReturn(blockA);
        blockAProperties.add(partPropertyB);
        when(this.sessionService.GetAllProjectElements()).thenReturn(containedElements);
        
        assertFalse(this.service.Validate());
        assertDoesNotThrow(() -> this.projectSavedObservable.Value(true));
        assertEquals(1, this.service.GetInvalidPaths().size());
    }

    @Test
    public void VerifyWhenThereIsMoreThanOnePath()
    {
        when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
        
        ArrayList<Element> containedElements = new ArrayList<>();

        Class mission = mock(Class.class);
        when(mission.getName()).thenReturn("mission");
        when(mission.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(mission, Stereotypes.Block)).thenReturn(true);
        containedElements.add(mission);
        
        ArrayList<Property> missionProperties = new ArrayList<>();
        when(mission.getOwnedAttribute()).thenReturn(missionProperties);

        Class groundSegment = mock(Class.class);
        when(groundSegment.getName()).thenReturn("groundSegment");
        when(groundSegment.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(groundSegment, Stereotypes.Block)).thenReturn(true);
        containedElements.add(groundSegment);
        
        ArrayList<Property> groundSegmentProperties = new ArrayList<>();
        when(groundSegment.getOwnedAttribute()).thenReturn(groundSegmentProperties);
        
        Class antenna = mock(Class.class);
        when(antenna.getName()).thenReturn("antenna");
        when(antenna.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(antenna, Stereotypes.Block)).thenReturn(true);
        containedElements.add(antenna);
        
        ArrayList<Property> antennaProperties = new ArrayList<>();
        when(antenna.getOwnedAttribute()).thenReturn(antennaProperties);
        
        Class station0 = mock(Class.class);
        when(station0.getName()).thenReturn("station0");
        when(station0.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(station0, Stereotypes.Block)).thenReturn(true);
        containedElements.add(station0);
        
        ArrayList<Property> station0Properties = new ArrayList<>();
        when(station0.getOwnedAttribute()).thenReturn(station0Properties);
        
        Class station1 = mock(Class.class);
        when(station1.getName()).thenReturn("station1");
        when(station1.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(station1, Stereotypes.Block)).thenReturn(true);
        containedElements.add(station1);
        
        ArrayList<Property> station1Properties = new ArrayList<>();
        when(station1.getOwnedAttribute()).thenReturn(station1Properties);        

        Class station2 = mock(Class.class);
        when(station2.getName()).thenReturn("station2");
        when(station2.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.stereotypeService.DoesItHaveTheStereotype(station2, Stereotypes.Block)).thenReturn(true);
        containedElements.add(station2);
        
        ArrayList<Property> station2Properties = new ArrayList<>();
        when(station2.getOwnedAttribute()).thenReturn(station2Properties);
        
        Property partPropertyMission = mock(Property.class);
        when(partPropertyMission.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyMission.getType()).thenReturn(groundSegment);
        when(partPropertyMission.getObjectParent()).thenReturn(mission);
        missionProperties.add(partPropertyMission);
        
        Property partPropertyGroundSegment0 = mock(Property.class);
        when(partPropertyGroundSegment0.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyGroundSegment0.getType()).thenReturn(station0);
        when(partPropertyGroundSegment0.getObjectParent()).thenReturn(groundSegment);
        groundSegmentProperties.add(partPropertyGroundSegment0);        

        Property partPropertyGroundSegment1 = mock(Property.class);
        when(partPropertyGroundSegment1.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyGroundSegment1.getType()).thenReturn(station1);
        when(partPropertyGroundSegment1.getObjectParent()).thenReturn(groundSegment);
        groundSegmentProperties.add(partPropertyGroundSegment1);

        Property partPropertyGroundSegment2 = mock(Property.class);
        when(partPropertyGroundSegment2.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyGroundSegment2.getType()).thenReturn(station2);
        when(partPropertyGroundSegment2.getObjectParent()).thenReturn(groundSegment);
        groundSegmentProperties.add(partPropertyGroundSegment2);
        
        Property partPropertyStation0 = mock(Property.class);
        when(partPropertyStation0.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyStation0.getType()).thenReturn(antenna);
        when(partPropertyStation0.getObjectParent()).thenReturn(station0);
        groundSegmentProperties.add(partPropertyStation0);        

        Property partPropertyStation1 = mock(Property.class);
        when(partPropertyStation1.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyStation1.getType()).thenReturn(antenna);
        when(partPropertyStation1.getObjectParent()).thenReturn(station1);
        groundSegmentProperties.add(partPropertyStation1);

        Property partPropertyStation2 = mock(Property.class);
        when(partPropertyStation2.getID()).thenReturn(UUID.randomUUID().toString());
        when(partPropertyStation2.getType()).thenReturn(antenna);
        when(partPropertyStation2.getObjectParent()).thenReturn(station2);
        groundSegmentProperties.add(partPropertyStation2);
        
        when(this.sessionService.GetAllProjectElements()).thenReturn(containedElements);
        
        assertTrue(this.service.Validate());
        assertDoesNotThrow(() -> this.projectSavedObservable.Value(true));
        assertEquals(0, this.service.GetInvalidPaths().size());
    }
}
