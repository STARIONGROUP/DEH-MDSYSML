package MappingRules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class HubRequirementToDstRequirementMappingRuleTestFixture
{

    private IMagicDrawMappingConfigurationService mappingConfigurationService;
    private IDstController dstController;
    private IHubController hubController;
    private HubRequirementToDstRequirementMappingRule mappingRule;
    private HubRequirementCollection elements;
    private Iteration iteration;
    private RequirementsSpecification requirementsSpecification0;
    private RequirementsSpecification requirementsSpecification1;
    private DomainOfExpertise domain;
    private RequirementsGroup requirementsGroup2;
    private RequirementsGroup requirementsGroup1;
    private RequirementsGroup requirementsGroup0;
    private cdp4common.engineeringmodeldata.Requirement requirement5;
    private cdp4common.engineeringmodeldata.Requirement requirement4;
    private cdp4common.engineeringmodeldata.Requirement requirement3;
    private cdp4common.engineeringmodeldata.Requirement requirement2;
    private cdp4common.engineeringmodeldata.Requirement requirement1;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private Category systemNonFunctionalInterfaceRequirementCategory;
    private Category systemNonFunctionalRequirementCategory;
    private Category systemFunctionalRequirementCategory;
    private Category systemFunctionalInterfaceRequirementCategory;
    private IMagicDrawTransactionService transactionService;
    private IStereotypeService stereotypeService;

    @BeforeEach
    void Setup()
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        this.mappingConfigurationService = mock(IMagicDrawMappingConfigurationService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        this.stereotypeService = mock(IStereotypeService.class);
        
        this.SetupElements();
        
        this.mappingRule = new HubRequirementToDstRequirementMappingRule(this.hubController, this.mappingConfigurationService, this.transactionService, this.stereotypeService);
        this.mappingRule.dstController = this.dstController;
    }
    
    @Test
    void VerifyTransform()
    {
        when(this.transactionService.Create(any(Stereotypes.class), any(String.class)))
                    .thenAnswer(x -> 
                    {     
                        Class requirement = mock(Class.class);
                        when(requirement.getName()).thenReturn(x.getArgument(1, String.class));
                        when(requirement.getID()).thenReturn(UUID.randomUUID().toString());
                        return requirement;
                    });
        
        when(this.transactionService.Create(any(java.lang.Class.class), any(String.class)))
                    .thenAnswer(x -> 
                    {
                        Package packageMocked = mock(Package.class);
                        when(packageMocked.getName()).thenReturn(x.getArgument(1, String.class));
                        BasicEList<Element> containedElement = new BasicEList<>();
                        when(packageMocked.getOwnedElement()).thenReturn(containedElement);
                        when(packageMocked.getID()).thenReturn(UUID.randomUUID().toString());
                        return packageMocked;
                    });
                    
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        ArrayList<MappedRequirementRowViewModel> result = this.mappingRule.Transform(this.elements);
        assertEquals(6, result.size());
        assertNotNull(result.get(0).GetDstElement());
        Package requirement2Container = (Package)result.get(2).GetDstElement().eContainer();
        assertSame(result.get(1).GetDstElement().eContainer(), requirement2Container);
    }

    private void SetupElements()
    {
        this.elements = new HubRequirementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.requirementsSpecification0 = new RequirementsSpecification();
        this.requirementsSpecification0.setShortName("REQS0");
        this.requirementsSpecification0.setName("REQS0");
        this.requirementsSpecification1 = new RequirementsSpecification();
        this.requirementsSpecification1.setShortName("REQS1");
        this.requirementsSpecification1.setName("REQS1");
        
        this.requirementsGroup0 = new RequirementsGroup();
        this.requirementsGroup0.setName("group0");
        this.requirementsGroup0.setShortName("group0");
        this.requirementsGroup1 = new RequirementsGroup();
        this.requirementsGroup1.setName("group1");
        this.requirementsGroup1.setShortName("group1");
        this.requirementsGroup2 = new RequirementsGroup();
        this.requirementsGroup2.setName("group2");
        this.requirementsGroup2.setShortName("group2");
        
        this.requirementsSpecification0.getGroup().add(requirementsGroup0);
        this.requirementsGroup0.getGroup().add(requirementsGroup1);
        
        this.requirement0 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement0.setName("requirement0");
        this.requirement0.setShortName("REQ0");
        this.requirement1 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement1.setName("requirement1");
        this.requirement1.setShortName("REQ1");
        this.requirement2 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement2.setName("requirement2");
        this.requirement2.setShortName("REQ2");
        this.requirement3 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement3.setName("requirement3");
        this.requirement3.setShortName("REQ3");
        this.requirement4 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement4.setName("requirement4");
        this.requirement4.setShortName("REQ4");
        this.requirement5 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement5.setName("requirement5");
        this.requirement5.setShortName("REQ5");
        
        Definition definition0 = new Definition();
        definition0.setContent("definition0");
        definition0.setLanguageCode("en");
        this.requirement0.getDefinition().add(definition0);

        Definition definition1 = new Definition();
        definition1.setContent("definition1");
        definition1.setLanguageCode("en");
        this.requirement1.getDefinition().add(definition1);

        Definition definition2 = new Definition();
        definition2.setContent("definition2");
        definition2.setLanguageCode("en");
        this.requirement2.getDefinition().add(definition2);

        Definition definition3 = new Definition();
        definition3.setContent("definition3");
        definition3.setLanguageCode("en");
        this.requirement3.getDefinition().add(definition3);

        Definition definition4 = new Definition();
        definition4.setContent("definition4");
        definition4.setLanguageCode("en");
        this.requirement4.getDefinition().add(definition4);

        Definition definition5 = new Definition();
        definition5.setContent("definition5");
        definition5.setLanguageCode("en");
        this.requirement5.getDefinition().add(definition5);
        
        
        this.systemNonFunctionalRequirementCategory = new Category();
        this.systemNonFunctionalRequirementCategory.setName("SystemNonFunctionalRequirement");        
        this.systemNonFunctionalInterfaceRequirementCategory = new Category();
        this.systemNonFunctionalInterfaceRequirementCategory.setName("SystemNonFunctionalInterfaceRequirement");        
        this.systemFunctionalInterfaceRequirementCategory = new Category();
        this.systemFunctionalInterfaceRequirementCategory.setName("SystemFunctionalInterfaceRequirement");        
        this.systemFunctionalRequirementCategory = new Category();
        this.systemFunctionalRequirementCategory.setName("SystemFunctionalRequirement");

        this.requirement0.getCategory().add(this.systemFunctionalInterfaceRequirementCategory);
        this.requirement1.getCategory().add(this.systemNonFunctionalInterfaceRequirementCategory);
        this.requirement2.getCategory().add(this.systemNonFunctionalRequirementCategory);
        this.requirement3.getCategory().add(this.systemFunctionalRequirementCategory);
        this.requirement3.getCategory().add(this.systemNonFunctionalInterfaceRequirementCategory);
        
        this.requirement0.setGroup(this.requirementsGroup1);
        this.requirement1.setGroup(this.requirementsGroup2);
        this.requirement2.setGroup(this.requirementsGroup2);
        
        this.requirementsSpecification0.getRequirement().add(this.requirement0);
        this.requirementsSpecification1.getRequirement().add(this.requirement1);
        this.requirementsSpecification1.getRequirement().add(this.requirement2);
        this.requirementsSpecification1.getRequirement().add(this.requirement3);
        this.requirementsSpecification1.getRequirement().add(this.requirement4);
        this.requirementsSpecification1.getRequirement().add(this.requirement5);
        
        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification0);
        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification1);
                
        this.elements.add(new MappedRequirementRowViewModel(this.requirement0, null, MappingDirection.FromHubToDst));
        this.elements.add(new MappedRequirementRowViewModel(this.requirement1, null, MappingDirection.FromHubToDst));
        this.elements.add(new MappedRequirementRowViewModel(this.requirement2, null, MappingDirection.FromHubToDst));
        this.elements.add(new MappedRequirementRowViewModel(this.requirement3, null, MappingDirection.FromHubToDst));
        this.elements.add(new MappedRequirementRowViewModel(this.requirement4, null, MappingDirection.FromHubToDst));        
        this.elements.add(new MappedRequirementRowViewModel(this.requirement5, null, MappingDirection.FromHubToDst));
    }
}
