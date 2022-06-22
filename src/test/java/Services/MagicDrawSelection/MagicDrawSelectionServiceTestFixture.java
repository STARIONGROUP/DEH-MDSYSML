package Services.MagicDrawSelection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementGroupRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class MagicDrawSelectionServiceTestFixture
{
    private IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    private MagicDrawSelectionService selectionService;
    private DomainOfExpertise domain;
    private Requirement requirement0;
    private Requirement requirement1;
    private Requirement requirement2;
    private RequirementsSpecification requirementsSpecification0;
    private RequirementsSpecification requirementsSpecification1;
    private ElementDefinition elementDefinition0;
    private Requirement requirement3;
    private RequirementsSpecification requirementsSpecification2;

    @BeforeEach
    void setUp() throws Exception
    {
        this.elementDefinitionBrowserViewModel = mock(IElementDefinitionBrowserViewModel.class);
        this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
        this.selectionService = new MagicDrawSelectionService(this.elementDefinitionBrowserViewModel, this.requirementBrowserViewModel);
        this.SetupHubElements();
    }
    
    private void SetupHubElements()
    {
        this.domain = new DomainOfExpertise();
        this.elementDefinition0 = new ElementDefinition();
        this.elementDefinition0.setOwner(this.domain);
        
        RequirementsGroup group0 = new RequirementsGroup();
        group0.setName("group0");
        group0.setOwner(this.domain);
        group0.setIid(UUID.randomUUID());
        
        RequirementsGroup group1 = new RequirementsGroup();
        group1.setName("group1");
        group1.getGroup().add(group1);
        group1.setOwner(this.domain);
        group1.setIid(UUID.randomUUID());
        
        RequirementsGroup group2 = new RequirementsGroup();
        group2.setName("group2");
        group2.setOwner(this.domain);
        group2.setIid(UUID.randomUUID());
        
        RequirementsGroup group3 = new RequirementsGroup();
        group3.setName("group3");
        group3.setOwner(this.domain);
        group3.setIid(UUID.randomUUID());
        
        this.requirement0 = new Requirement();
        this.requirement0.setName("requirement0");
        this.requirement0.setShortName("req0");
        this.requirement0.setGroup(group1);
        this.requirement0.setIid(UUID.randomUUID());

        this.requirement1 = new Requirement();
        this.requirement1.setName("requirement1");
        this.requirement1.setShortName("req1");
        this.requirement1.setGroup(group0);
        this.requirement1.setOwner(this.domain);  
        this.requirement1.setIid(UUID.randomUUID());
        
        this.requirement2 = new Requirement();
        this.requirement2.setName("requirement2");
        this.requirement2.setShortName("req2");
        this.requirement2.setOwner(this.domain);
        this.requirement2.setIid(UUID.randomUUID());
        
        this.requirement3 = new Requirement();
        this.requirement3.setName("requirement3");
        this.requirement3.setShortName("req3");
        this.requirement3.setGroup(group3);
        this.requirement3.setOwner(this.domain);
        this.requirement3.setIid(UUID.randomUUID());

        this.requirementsSpecification0 = new RequirementsSpecification();
        this.requirementsSpecification0.getGroup().add(group0);
        this.requirementsSpecification0.getRequirement().add(this.requirement0);
        this.requirementsSpecification0.getRequirement().add(this.requirement1);
        this.requirementsSpecification0.setOwner(this.domain);
        this.requirementsSpecification0.setIid(UUID.randomUUID());
        
        this.requirementsSpecification1 = new RequirementsSpecification();
        this.requirementsSpecification1.getRequirement().add(this.requirement2);
        this.requirementsSpecification1.getGroup().add(group2);
        this.requirementsSpecification1.setOwner(this.domain);
        this.requirementsSpecification1.setIid(UUID.randomUUID());
        
        this.requirementsSpecification2 = new RequirementsSpecification();
        this.requirementsSpecification2.getRequirement().add(this.requirement3);
        this.requirementsSpecification2.getGroup().add(group3);
        this.requirementsSpecification2.setOwner(this.domain);
        this.requirementsSpecification2.setIid(UUID.randomUUID());
    }

    @SuppressWarnings("unchecked")
    @Test
    void VerifyGetHubSelection()
    {
        assertEquals(0, this.selectionService.GetHubSelection().size());
        when(this.elementDefinitionBrowserViewModel.GetSelectedElements()).thenReturn((Collection)Arrays.asList(new ElementDefinitionRowViewModel(this.elementDefinition0, null)));
        
        RequirementSpecificationRowViewModel requirementSpecificationRowViewModel0 = new RequirementSpecificationRowViewModel(this.requirementsSpecification0, null);
        RequirementSpecificationRowViewModel requirementSpecificationRowViewModel1 = new RequirementSpecificationRowViewModel(this.requirementsSpecification1, null);
        RequirementSpecificationRowViewModel requirementSpecificationRowViewModel2 = new RequirementSpecificationRowViewModel(this.requirementsSpecification2, null);
        
        when(this.requirementBrowserViewModel.GetSelectedElements()).thenReturn(
                (Collection)Arrays.asList(requirementSpecificationRowViewModel2,
                        requirementSpecificationRowViewModel0.GetAllContainedRowsOfType(RequirementGroupRowViewModel.class).get(0), 
                        requirementSpecificationRowViewModel1.GetAllContainedRowsOfType(RequirementRowViewModel.class).get(0)));

        assertEquals(4, this.selectionService.GetHubSelection().size());
    }
}
