/*
 * DirectedRelationshipsToBinaryRelationshipsMappingRuleTestFixture.java
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
package MappingRules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRelatedElementCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.EngineeringModelSetup;
import cdp4common.sitedirectorydata.ModelReferenceDataLibrary;
import cdp4common.sitedirectorydata.SiteDirectory;

public class DirectedRelationshipsToBinaryRelationshipsMappingRuleTestFixture
{
    private DirectedRelationshipsToBinaryRelationshipsMappingRule rule;
    private IHubController hubController;
    private IStereotypeService stereotypeService;
    private IMagicDrawMappingConfigurationService configurationService;
    private ArrayList<MappedElementRowViewModel<? extends Thing, ? extends Class>> mappedElements;
    private IDstController dstController;
    private Abstraction dstRelationship1;
    private BinaryRelationship element0ToRequirement0;
    private Category category;

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("resource")
    @BeforeEach
    void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.configurationService = mock(IMagicDrawMappingConfigurationService.class);
        this.stereotypeService = mock(IStereotypeService.class);
        
        this.rule = new DirectedRelationshipsToBinaryRelationshipsMappingRule(this.hubController, this.configurationService, this.stereotypeService);
        
        this.dstController = mock(IDstController.class);
        when(this.dstController.GetMappedDirectedRelationshipToBinaryRelationships()).thenReturn(new ObservableCollection<BinaryRelationship>());
        
        this.rule.dstController = dstController;
        
        this.SetupElements();
        
        Iteration iteration = new Iteration();
        iteration.getRelationship().add(element0ToRequirement0);
        when(this.hubController.GetOpenIteration()).thenReturn(iteration);
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(), any())).thenReturn(false);
    }

    private void SetupElements()
    {
        ElementDefinition element0 = new ElementDefinition(UUID.randomUUID(), null, null);
        Requirement requirement0 = new Requirement(UUID.randomUUID(), null, null);
        Requirement requirement1 = new Requirement(UUID.randomUUID(), null, null);
        Requirement requirement2 = new Requirement(UUID.randomUUID(), null, null);

        this.category = new Category();
        this.category.setName("Satisfy");
                
        this.element0ToRequirement0 = new BinaryRelationship();
        this.element0ToRequirement0.setSource(element0);
        this.element0ToRequirement0.setTarget(requirement0);
        this.element0ToRequirement0.getCategory().add(this.category);
        
        element0.getRelationships().add(this.element0ToRequirement0);
        requirement0.getRelationships().add(this.element0ToRequirement0);        
        
        Class class0 = mock(Class.class);
        when(class0.getID()).thenReturn(UUID.randomUUID().toString());
        when(class0.getName()).thenReturn("class0");
        when(class0.get_directedRelationshipOfSource()).thenReturn(Arrays.asList());
        Class dstRequirement0 = mock(Class.class);
        when(dstRequirement0.getID()).thenReturn(UUID.randomUUID().toString());
        when(dstRequirement0.getName()).thenReturn("class0");
        when(dstRequirement0.get_directedRelationshipOfSource()).thenReturn(Arrays.asList());
        Class dstRequirement1 = mock(Class.class);
        when(dstRequirement1.getID()).thenReturn(UUID.randomUUID().toString());
        when(dstRequirement0.get_directedRelationshipOfSource()).thenReturn(Arrays.asList());
        when(dstRequirement1.getName()).thenReturn("class0");
        Class dstRequirement2 = mock(Class.class);
        when(dstRequirement2.getID()).thenReturn(UUID.randomUUID().toString());
        when(dstRequirement0.get_directedRelationshipOfSource()).thenReturn(Arrays.asList());
        when(dstRequirement2.getName()).thenReturn("class0");
        
        Abstraction dstRelationship0 = mock(Abstraction.class);
        when(dstRelationship0.getTarget()).thenReturn(new ArrayList<>(Arrays.asList(dstRequirement2)));
        when(dstRelationship0.getSource()).thenReturn(new ArrayList<>(Arrays.asList(dstRequirement1)));
        when(dstRequirement2.get_directedRelationshipOfSource()).thenReturn(new ArrayList<>(Arrays.asList(dstRelationship0)));
        when(dstRequirement1.get_directedRelationshipOfTarget()).thenReturn(new ArrayList<>(Arrays.asList(dstRelationship0)));
        when(dstRelationship0.getHumanType()).thenReturn("Satisfy");
        
        this.dstRelationship1 = mock(Abstraction.class);
        when(this.dstRelationship1.getTarget()).thenReturn(new ArrayList<>(Arrays.asList(dstRequirement0)));
        when(this.dstRelationship1.getSource()).thenReturn(new ArrayList<>(Arrays.asList(dstRequirement1)));
        when(dstRequirement0.get_directedRelationshipOfTarget()).thenReturn(new ArrayList<>(Arrays.asList(this.dstRelationship1)));
        when(dstRequirement1.get_directedRelationshipOfTarget()).thenReturn(new ArrayList<>(Arrays.asList(this.dstRelationship1)));
        when(dstRelationship0.getHumanType()).thenReturn("Satisfy");
        
        ObservableCollection<Abstraction> mappedRelationships = new ObservableCollection<Abstraction>();
        mappedRelationships.add(dstRelationship0);
        
        when(this.dstController.GetMappedBinaryRelationshipsToDirectedRelationships()).thenReturn(mappedRelationships);
        
        this.mappedElements = new ArrayList<MappedElementRowViewModel<? extends Thing, ? extends Class>>();
        
        this.mappedElements.add(new MappedElementDefinitionRowViewModel(element0, class0, MappingDirection.FromHubToDst));
        this.mappedElements.add(new MappedRequirementRowViewModel(requirement0, dstRequirement0, MappingDirection.FromHubToDst));
        this.mappedElements.add(new MappedRequirementRowViewModel(requirement1, dstRequirement1, MappingDirection.FromHubToDst));
        this.mappedElements.add(new MappedRequirementRowViewModel(requirement2, dstRequirement2, MappingDirection.FromHubToDst));
    }

    @Test
    void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.rule.Transform(this.mappedElements));        
        MagicDrawRelatedElementCollection input = new MagicDrawRelatedElementCollection();
        input.addAll(this.mappedElements);
        EngineeringModelSetup engineeringModelSetup = new EngineeringModelSetup();
        ModelReferenceDataLibrary modelReferenceDataLibrary = new ModelReferenceDataLibrary();
        engineeringModelSetup.getRequiredRdl().add(modelReferenceDataLibrary);
        when(this.hubController.GetDehpOrModelReferenceDataLibrary()).thenReturn(modelReferenceDataLibrary);
        ArrayList<BinaryRelationship> result = this.rule.Transform(input);
        assertEquals(1, result.size());
        SiteDirectory siteDirectory = new SiteDirectory();
        siteDirectory.getModel().add(engineeringModelSetup);
        
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(), any())).thenAnswer(x -> 
        {
            @SuppressWarnings("unchecked")
            Ref<Category> refCategory = (Ref<Category>)x.getArgument(1, Ref.class);
            refCategory.Set(this.category);
            return true;
        });
        
        result = this.rule.Transform(input);
        assertEquals(1, result.size());
    }
}
