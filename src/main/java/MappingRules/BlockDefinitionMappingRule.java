/*
 * BlockDefinitionMappingRule.java
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import HubController.IHubController;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Reactive.ObservableCollection;
import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import cdp4common.commondata.ClassKind;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.ReferenceDataLibrary;
import cdp4dal.operations.ThingTransaction;
import cdp4dal.operations.ThingTransactionImpl;
import cdp4dal.operations.TransactionContextResolver;

/**
 * The {@linkplain BlockDefinitionMappingRule} is the mapping rule implementation for transforming {@linkplain Element} to {@linkplain ElementDefinition}
 */
public class BlockDefinitionMappingRule extends MappingRule<ObservableCollection<Element>, ArrayList<ElementDefinition>>
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;
    
    /**
     * The isLeaf category names where pair.item0 is the short name
     */
    private final Pair<String, String> isLeafCategoryNames = Pair.of("LEA", "isLeaf");
    
    /**
     * The isAbstract category names where pair.item0 is the short name
     */
    private final Pair<String, String> isAbstractCategoryNames = Pair.of("ABS", "isAbstract");
    
    /**
     * The isActive category names where pair.item0 is the short name
     */
    private final Pair<String, String> isActiveCategoryNames = Pair.of("ACT", "isActive");
    
    /**
     * The isEncapsulated category names where pair.item0 is the short name
     */
    private final Pair<String, String> isEncapsulatedCategoryNames = Pair.of("ENC", "isEncapsulated");

    /**
     * Initializes a new {@linkplain BlockDefinitionMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     */
    public BlockDefinitionMappingRule(IHubController hubController)
    {
        this.hubController = hubController;
    }
    
    /**
     * Transforms an {@linkplain ObservableCollection} of type {@linkplain Element} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain ObservableCollection} of type {@linkplain Element} to transform
     * @return the {@linkplain ArrayList} of {@linkplain ElementDefinition}
     */
    @Override
    public ArrayList<ElementDefinition> Transform(Object input)
    {
        try
        {           
            ObservableCollection<Element> elements = this.CastInput(input);
            
            List<Class> allBlocks = new ArrayList<Class>();
            
            for (Element element : elements)
            {          
                if(element instanceof Model)
                {
                    allBlocks = this.GetElementOfType(
                            elements.stream().flatMap(x -> x.getOwnedElement().stream())
                            .collect(Collectors.toList()), Class.class);
                }
                
                if(element instanceof Package)
                {
                    allBlocks = this.GetElementOfType(elements, Class.class);
                }
                            
            }
            
            allBlocks = this.GetElementOfType(elements, Class.class);
            
            return this.Map(allBlocks);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<ElementDefinition>();
        }
    }
    
    /**
     * Maps the provided collection of block
     * 
     * @param allBlocks the collection of {@linkplain Class} or block to map 
     * @return a collection of {@linkplain ElementDefinition}
     */
    private ArrayList<ElementDefinition> Map(List<Class> allBlocks)
    {
        ArrayList<ElementDefinition> result = new ArrayList<ElementDefinition>();
        
        for (Class block : allBlocks)
        {
            String shortName = block.getName().replaceAll("\\P{L}+", "");       
            ElementDefinition elementDefinition;
            
            Optional<ElementDefinition> optionalElementDefinition = this.hubController.GetOpenIteration().getElement().stream()
                    .filter(x -> x.getShortName().equals(shortName))
                    .findFirst();
            
            if(optionalElementDefinition.isPresent())
            {
                elementDefinition = optionalElementDefinition.get().clone(true);
            }
            else
            {
                elementDefinition = new ElementDefinition();
                elementDefinition.setName(block.getName());
                elementDefinition.setShortName(shortName);
            }
            
            this.MapCategories(elementDefinition, block);
            
            result.add(elementDefinition);
        }
        
        return result;
    }

    /**
     * Maps the applied categories to the block to the specified {@linkplain ElementDefinition}
     * 
     * @param elementDefinition the target {@linkplain ElementDefinition}
     * @param block the SysML block ({@linkplain Class}) instance
     */
    private void MapCategories(ElementDefinition elementDefinition, Class block)
    {        
        Boolean isEncapsulated = null;
        
        Optional<String> optionalIsEncapsulated = 
                StereotypesHelper.getStereotypePropertyValueAsString(block, block.getAppliedStereotypeInstance().getName() , this.isEncapsulatedCategoryNames.getRight())
                .stream().findFirst();
        
        if(optionalIsEncapsulated.isPresent())
        {
            isEncapsulated = Boolean.valueOf(optionalIsEncapsulated.get());
        }

        this.MapCategory(elementDefinition, this.isLeafCategoryNames, block.isLeaf(), false);
        this.MapCategory(elementDefinition, this.isAbstractCategoryNames, block.isAbstract(), true);
        this.MapCategory(elementDefinition, this.isActiveCategoryNames, block.isActive(), false);
        this.MapCategory(elementDefinition, this.isEncapsulatedCategoryNames, isEncapsulated, true);
    }

    /**
     * Maps the specified by short name {@linkplain Category} to the provided {@linkplain ElementDefinition}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} to update
     * @param categoryNames the {@linkplain Pair} of short name and name
     * @param value the {@linkplain Boolean} value from the current SysML block
     * @param shouldCreateTheCategory a value indicating whether the {@linkplain Category} should be created if it doesn't exist yet
     */
    private void MapCategory(ElementDefinition elementDefinition, Pair<String, String> categoryNames, Boolean value, boolean shouldCreateTheCategory)
    {
        Ref<Category> refCategory = new Ref<Category>(Category.class);
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> ((Category) x).getShortName().equals(categoryNames.getLeft()), refCategory) &&
                !(shouldCreateTheCategory && this.TryCreateCategory(categoryNames, refCategory)))
        {
            return;
        }

        if(value && elementDefinition.getCategory().indexOf(refCategory.Get()) == -1)
        {
            elementDefinition.getCategory().add(refCategory.Get());
        }
        else if(value != true && elementDefinition.getCategory().indexOf(refCategory.Get()) != -1)
        {
            elementDefinition.getCategory().remove(refCategory.Get());
        }
    }
    
    /**
     * Tries to create the category with the specified {@linkplain categoryShortName}
     * 
     * @param categoryNames the {@linkplain Pair} of short name and name
     * @param refCategory the {@linkplain Ref} of Category
     * 
     * @return a value indicating whether the category has been successfully created and retrieved from the cache
     */
    private boolean TryCreateCategory(Pair<String, String> categoryNames, Ref<Category> refCategory)
    {
        try
        {
            Category newCategory = new Category();
            newCategory.setName(categoryNames.getRight());
            newCategory.setShortName(categoryNames.getLeft());
            newCategory.setIid(UUID.randomUUID());
            newCategory.getPermissibleClass().addAll(Arrays.asList(ClassKind.ElementDefinition, ClassKind.ElementUsage));
            
            ReferenceDataLibrary rdl = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            rdl.getDefinedCategory().add(newCategory);
            
            ThingTransaction transaction = new ThingTransactionImpl(TransactionContextResolver.resolveContext(rdl), rdl);
            transaction.createOrUpdate(rdl);
            transaction.createOrUpdate(newCategory);
            
            this.hubController.TryWrite(transaction);
            ReferenceDataLibrary a = this.hubController.GetDehpOrModelReferenceDataLibrary();
            return this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getIid().compareTo(newCategory.getIid()) == 0, refCategory);
        }
        catch(Exception exception)
        {
            this.logger.error(String.format("Could not create the category with the shortname: %s, because %s", categoryNames.getRight(), exception));
            this.logger.catching(exception);
            return false;
        }
    }

    /**
     * Gets the contained or the specified element as a collection typed by the specified {@link TReturn}
     * 
     * @param <TReturn> The type to return
     * @param elements the collection of element to parse
     * @param clazz the {@link TReturn} class 
     * @return a {@linkplain List} of {@link TReturn}
     */
    @SuppressWarnings("unchecked")
    private <TReturn> List<TReturn> GetElementOfType(List<Element> elements, java.lang.Class<TReturn> clazz)
    {
        List<TReturn> result = new ArrayList<TReturn>();
        
        if(elements.stream().allMatch(x -> x instanceof Class))
        {
            return elements.stream().map(x -> (TReturn) x)
                    .collect(Collectors.toList());
        }
        
        for (Element element : elements)
        {
            for (Element containedElement : element.getOwnedElement())
            {
                if(containedElement.getClass().isAssignableFrom(clazz))
                {
                    result.add((TReturn)containedElement);
                }
            }
        }
        
        return result;
    }
}
