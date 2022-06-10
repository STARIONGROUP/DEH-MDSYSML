package MappingRules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class DstRequirementToHubRequirementMappingRuleTestFixture
{
    private IMagicDrawMappingConfigurationService mappingConfigurationService;
    private IHubController hubController;
    private DstRequirementToHubRequirementMappingRule mappingRule;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private MagicDrawRequirementCollection elements;
    private Package sysMLRequirementPackage0;
    private Class sysMLRequirement2;
    private Class sysMLRequirement1;
    private Class sysMLRequirement0;
    private Package sysMLRequirementPackage1;
    private Package sysMLRequirementPackage2;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private Package sysMLPackage;
    private cdp4common.engineeringmodeldata.Requirement requirement1;
    private IMagicDrawTransactionService transactionService;
    private IStereotypeService stereotypeService;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfigurationService = mock(IMagicDrawMappingConfigurationService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        this.stereotypeService = mock(IStereotypeService.class);
        when(this.stereotypeService.DoesItHaveTheStereotype(any(Element.class), any(Stereotypes.class))).thenReturn(false);
        this.SetupElements();
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        this.mappingRule = new DstRequirementToHubRequirementMappingRule(this.hubController, this.mappingConfigurationService, this.transactionService, this.stereotypeService);
    }

    @Test
    public void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        ArrayList<MappedRequirementRowViewModel> result = this.mappingRule.Transform(this.elements);
        assertEquals(3, result.size());
        assertNotNull(result.get(0).GetHubElement());
    }

    private void SetupElements()
    {
        this.elements = new MagicDrawRequirementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.requirement0 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement0.setShortName("REQS0");
        this.requirement1 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement1.setShortName("REQS1");
        
        RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
        requirementsSpecification.setShortName("MagicDrawRequirementPackage0");
        
        this.iteration.getRequirementsSpecification().add(requirementsSpecification);
        requirementsSpecification.getRequirement().add(this.requirement0);
        requirementsSpecification.getRequirement().add(this.requirement1);
        
        this.sysMLRequirement0 = mock(Class.class);
        when(this.sysMLRequirement0.getName()).thenReturn("MagicDrawRequirement0");
        this.sysMLRequirement1 = mock(Class.class);
        when(this.sysMLRequirement1.getName()).thenReturn("MagicDrawRequirement1");
        this.sysMLRequirement2 = mock(Class.class);
        when(this.sysMLRequirement2.getName()).thenReturn("MagicDrawRequirement2");
        
        this.sysMLPackage = mock(Package.class);
        when(this.sysMLPackage.getName()).thenReturn("MagicDrawPackage");
        this.sysMLRequirementPackage0 = mock(Package.class);
        when(this.sysMLRequirementPackage0.getName()).thenReturn("MagicDrawRequirementPackage0");
        this.sysMLRequirementPackage1 = mock(Package.class);
        when(this.sysMLRequirementPackage1.getName()).thenReturn("MagicDrawRequirementPackage1");
        this.sysMLRequirementPackage2 = mock(Package.class);
        when(this.sysMLRequirementPackage2.getName()).thenReturn("MagicDrawRequirementPackage2");
        ArrayList<Element> containedElements = new ArrayList<Element>();
        containedElements.add(this.sysMLRequirementPackage1);
        containedElements.add(this.sysMLRequirementPackage2);
        ArrayList<Element> containedRequirements0 = new ArrayList<Element>();
        containedRequirements0.add(this.sysMLRequirement1);
        containedRequirements0.add(this.sysMLRequirement2);
        ArrayList<Element> containedRequirements1 = new ArrayList<Element>();
        containedRequirements1.add(this.sysMLRequirement0);

        when(this.sysMLRequirement0.getOwner()).thenReturn(this.sysMLRequirementPackage2);
        when(this.sysMLRequirement1.getOwner()).thenReturn(this.sysMLRequirementPackage1);
        when(this.sysMLRequirement2.getOwner()).thenReturn(this.sysMLRequirementPackage1);
        when(this.sysMLPackage.getOwner()).thenReturn(null);
        when(this.sysMLRequirementPackage0.getOwner()).thenReturn(this.sysMLPackage);
        when(this.sysMLRequirementPackage1.getOwner()).thenReturn(this.sysMLRequirementPackage0);
        when(this.sysMLRequirementPackage2.getOwner()).thenReturn(this.sysMLRequirementPackage0);
        
        when(this.sysMLPackage.getOwnedElement()).thenReturn(Arrays.asList(this.sysMLRequirementPackage0));
        when(this.sysMLRequirementPackage0.getOwnedElement()).thenReturn(containedElements);
        when(this.sysMLRequirementPackage1.getOwnedElement()).thenReturn(containedRequirements0);
        when(this.sysMLRequirementPackage2.getOwnedElement()).thenReturn(containedRequirements1);
        
        this.elements.add(new MappedRequirementRowViewModel(this.requirement0, this.sysMLRequirement0, MappingDirection.FromDstToHub));
        this.elements.add(new MappedRequirementRowViewModel(this.sysMLRequirement1, MappingDirection.FromDstToHub));
        this.elements.add(new MappedRequirementRowViewModel(this.sysMLRequirement2, MappingDirection.FromDstToHub));
    }
}
