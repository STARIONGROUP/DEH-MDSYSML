package Services.MappingConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import io.reactivex.Observable;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;

class MagicDrawMappingConfigurationServiceTestFixture
{
    private IHubController hubController;
    private IMagicDrawTransactionService transactionService;
    private IStereotypeService stereotypeService;
    private MagicDrawMappingConfigurationService service;
    private Class block0;
    private ElementDefinition elementDefinition;
    private Requirement requirement;
    private Class dstRequirement;

    @BeforeEach
    void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.stereotypeService = mock(IStereotypeService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, false));
        
        this.service = new MagicDrawMappingConfigurationService(this.hubController, this.transactionService, this.stereotypeService);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.service.LoadMapping(Collections.emptyList()));
        
        this.block0 = mock(Class.class);
        when(this.block0.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.dstRequirement = mock(Class.class);
        when(this.dstRequirement.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.elementDefinition = new ElementDefinition();
        this.elementDefinition.setIid(UUID.randomUUID());
        
        this.requirement = new Requirement();
        this.requirement.setIid(UUID.randomUUID());
        
        ExternalIdentifier externalIdentifier0 = new ExternalIdentifier();
        externalIdentifier0.Identifier = this.block0.getID();
        externalIdentifier0.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier1 = new ExternalIdentifier();
        externalIdentifier1.Identifier = this.block0.getID();
        externalIdentifier1.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier2 = new ExternalIdentifier();
        externalIdentifier0.Identifier = this.dstRequirement.getID();
        externalIdentifier0.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier3 = new ExternalIdentifier();
        externalIdentifier1.Identifier = this.dstRequirement.getID();
        externalIdentifier1.MappingDirection = MappingDirection.FromDstToHub;

        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier0, this.elementDefinition.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier1, this.elementDefinition.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier2, this.requirement.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier3, this.requirement.getIid()));
        
        assertEquals(0, this.service.LoadMapping(Arrays.asList(block0, dstRequirement)).size());
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Block))).thenReturn(true);
        
        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenAnswer(x ->
        {
            Ref<?> ref = x.getArgument(1, Ref.class);
            UUID id = x.getArgument(0, UUID.class);
            
            if(ref.GetType() == Requirement.class)
            {
                ((Ref<Requirement>)ref).Set(this.requirement);
            }
            else
            {
                ((Ref<ElementDefinition>)ref).Set(this.elementDefinition);
            }
            
            return true;
        });
        
        assertNotEquals(0, this.service.LoadMapping(Arrays.asList(block0, dstRequirement)).size());
        
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Block))).thenReturn(false);
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Requirement))).thenReturn(true);

        assertNotEquals(0, this.service.LoadMapping(Arrays.asList(dstRequirement)).size());
    }

    @Test
    void VerifyCreateExternalIdentifierMapStringStringBoolean()
    {
        assertNotNull(this.service.CreateExternalIdentifierMap("name", "model", false));
    }
}
