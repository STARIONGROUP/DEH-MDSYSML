/*
 * DirectedRelationshipsToBinaryRelationshipsMappingRule.java
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import App.AppContainer;
import DstController.IDstController;
import HubController.IHubController;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Services.Stereotype.IStereotypeService;
import Utils.Operators.Operators;
import Utils.Stereotypes.DirectedRelationshipType;
import Utils.Stereotypes.MagicDrawRelatedElementCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;

/**
 * The {@linkplain DirectedRelationshipsToBinaryRelationshipsMappingRule} is the {@linkplain MappingRule} that maps {@linkplain DirectedRelationship}s to {@linkplain BinaryRelationShip}
 */
public class DirectedRelationshipsToBinaryRelationshipsMappingRule extends DstToHubBaseMappingRule<MagicDrawRelatedElementCollection, ArrayList<BinaryRelationship>>
{
    /**
     * The result of this mapping rule
     */
    private ArrayList<BinaryRelationship> result = new ArrayList<>();
    
    /**
     * The {@linkplain IDstController}
     */
    IDstController dstController;

    /**
     * Initializes a new {@linkplain DirectedRelationshipsToBinaryRelationshipsMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public DirectedRelationshipsToBinaryRelationshipsMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, IStereotypeService stereotypeService)
    {
        super(hubController, mappingConfiguration, stereotypeService);
    }
    
    /**
     * Transforms an {@linkplain MagicDrawRelatedElementCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain MagicDrawRelatedElementCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<BinaryRelationship> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.Map(this.CastInput(input));
            return new ArrayList<>(this.result);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.result.clear();
        }
    }

    /**
     * Maps the provided collection of  {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain MappedElementRowViewModel} to map
     */
    private void Map(MagicDrawRelatedElementCollection elements)
    {
        for (MappedElementRowViewModel<? extends Thing, ? extends Class> sourceElement : elements)
        {
            for (DirectedRelationship relationship : sourceElement.GetDstElement().get_directedRelationshipOfSource())
            {
                Optional<MappedElementRowViewModel<? extends Thing, ? extends Class>> optionalTargetElement = elements.stream()
                        .filter(x -> relationship.getTarget().stream().anyMatch(e -> Operators.AreTheseEquals(e.getID(), x.GetDstElement().getID())))
                        .findFirst();
                
                if(!optionalTargetElement.isPresent())
                {
                    continue;
                }
                
                this.logger.debug(String.format("Relationship being mapped : [%s] => [%s]", sourceElement.GetHubElement().getName(), optionalTargetElement.get().GetHubElement().getName()));
                
                DirectedRelationshipType relationshipType = DirectedRelationshipType.From(relationship.getHumanType());
                
                if(relationshipType == null)
                {
                    this.logger.info(String.format("Skipping mapping of relationship %s because its stereotype is not supported.", relationship.getHumanType()));
                    continue;
                }
                
                if(this.DoesRelationshipAlreadyExists(relationshipType, sourceElement.GetHubElement(), optionalTargetElement.get().GetHubElement()))
                {
                    continue;
                }
                
                this.result.add(this.CreateBinaryRelationship(relationshipType, sourceElement, optionalTargetElement.get()));
            }
        }
    }

    /**
     * Create a {@linkplain BinaryRelationship}
     * 
     * @param sourceMappedElement the {@linkplain MappedElementRowViewModel} source
     * @param targetMappedElement the {@linkplain MappedElementRowViewModel} target
     * @return a {@linkplain BinaryRelationship}
     */
    private BinaryRelationship CreateBinaryRelationship(DirectedRelationshipType relationshipType, MappedElementRowViewModel<? extends Thing, ? extends Class> sourceMappedElement, 
            MappedElementRowViewModel<? extends Thing, ? extends Class> targetMappedElement)
    {
        BinaryRelationship relationship = new BinaryRelationship();
        relationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        relationship.setIid(UUID.randomUUID());
        relationship.setName(String.format("%s → %s", this.GetElementName(sourceMappedElement), this.GetElementName(targetMappedElement)));
        relationship.setSource(sourceMappedElement.GetHubElement());
        relationship.setTarget(targetMappedElement.GetHubElement());
        this.MapCategory(relationship, relationshipType.name(), ClassKind.BinaryRelationship);
        return relationship;
    }

    /**
     * Gets the name of the provided {@linkplain Class} from the {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementRowViewModel}
     */
    private String GetElementName(
            MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElement)
    {
        return mappedElement.GetDstElement() instanceof NamedElement 
                ? ((NamedElement)mappedElement.GetDstElement()).getName()
                : mappedElement.GetDstElement().getID();
    }

    /**
     * Verifies that no relationship already exists between the provided {@linkplain Class} source and the {@linkplain Class} target in the Hub
     * @param relationshipType 
     * 
     * @param relationshipType the stereotype reprented by {@linkplain DirectedRelationship} of the current relationship
     * @param source the {@linkplain Thing} source
     * @param target the {@linkplain Thing} target
     * @return a {@linkplain boolean}
     */
    private boolean DoesRelationshipAlreadyExists(DirectedRelationshipType relationshipType, Thing source, Thing target)
    {
        Predicate<? super BinaryRelationship> predicate = x -> Operators.AreTheseEquals(x.getTarget().getIid(), target.getIid()) 
                && Operators.AreTheseEquals(x.getSource().getIid(), source.getIid())
                && x.getCategory().stream().anyMatch(c -> Operators.AreTheseEquals(c.getName(), relationshipType.name()));
        
        return this.hubController.GetOpenIteration().getRelationship().stream()
                    .filter(x -> x instanceof BinaryRelationship)
                    .map(x -> (BinaryRelationship)x)
                    .anyMatch(predicate)
                || this.dstController.GetMappedDirectedRelationshipToBinaryRelationships().stream().anyMatch(predicate);
    }    
}
