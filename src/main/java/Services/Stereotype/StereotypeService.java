/*
 * StereotypeService.java
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
package Services.Stereotype;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javafmi.modeldescription.v2.Unit;

import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.magicdraw.sysml.util.SysMLUtilities;
import com.nomagic.requirements.util.RequirementUtilities;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import App.AppContainer;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;

/**
 * The {@linkplain Stereotype} service provides a layer of abstraction between operations around {@linkplain Stereotype} and the adapter.
 * This service plays the previous role of the {@linkplain StereotypeUtils}. It has been implemented for testability of the adapter
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class StereotypeService implements IStereotypeService
{
	/**
	* Singleton implementation of this service.
	*
	* MagicDraw API exposes static helpers for Stereotype which are used in different places such as RowViewModels, which should not depend on services.
	* This convenient property, is short for resolving the service in the {@linkplain AppContainer}
	*/
	private static IStereotypeService current;

	/**
	* Sets the final {@linkplain #Current} property
	*/
	static
	{
		current = new StereotypeService(AppContainer.Container.getComponent(IMagicDrawSessionService.class));
	}

	/**
	* Gets the {@linkplain current}
	*
	* @return instance the {@linkplain IStereotypeService}
	*/
	public static IStereotypeService Current()
	{
		return StereotypeService.current;
	}

	/**
	* Sets the {@linkplain current}.
	* Use this setter only for test purpose.
	*
	* @param instance the {@linkplain IStereotypeService}
	*/
	public static void SetCurrent(IStereotypeService instance)
	{
		StereotypeService.current = instance;
	}
    
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IMagicDrawSessionService}
     */
    private IMagicDrawSessionService sessionService;

    /**
     * Initializes a new {@linkplain ISessionService}
     * 
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     */
    public StereotypeService(IMagicDrawSessionService sessionService)
    {
        this.sessionService = sessionService;
    }
    
    /**
     * Sets the stereotype property value of the provided {@linkplain Element} with the provided {@linkplain Object} value
     *
     * @param element the stereotyped {@linkplain Element}
     * @param stereotype the {@linkplain Stereotypes} that applies to the {@linkplain Element} and which also defines the provided provided property name
     * @param propertyName the property name
     * @param value the value
     */
    @Override
    public void SetStereotypePropertyValue(Element element, Stereotypes stereotype, String propertyName, Object value)
    {
        StereotypesHelper.setStereotypePropertyValue(element, this.GetStereotype(stereotype), propertyName, value);
    }
    
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    @Override
    public Stereotype GetStereotype(Enum<?> stereotype)
    {
        return this.GetStereotype(stereotype.name());
    }

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain String}
     * 
     * @param stereotypeName the {@linkplain String} stereotype name
     * @return a {@linkplain Stereotype}
     */
    private Stereotype GetStereotype(String stereotypeName)
    {
        Collection<Stereotype> allStereoType = StereotypesHelper.getAllStereotypes(this.sessionService.GetProject());
        Optional<Stereotype> optionalStereotype = allStereoType.stream().filter(x -> AreTheseEquals(x.getName(), stereotypeName, true)).findFirst();
          
        if(optionalStereotype.isPresent())
        {
            return optionalStereotype.get();
        }
  
        return null;
    }

    /**
     * Verifies that the provided {@linkplain Element} has the specified stereotype
     * 
     * @param stereotype the expected {@linkplain Stereotypes}
     * @param element the {@linkplain Element} to test
     * @return a value indicating whether the {@linkplain Element} has the specified stereotype
     */
    @Override
    public boolean DoesItHaveTheStereotype(Element element, Stereotypes stereotype)
    {
        try
        {
            return StereotypesHelper.hasStereotypeOrDerived(element, stereotype.name());
        }
        catch(IllegalArgumentException exception)
        {
            this.logger.catching(exception);
            return false;
        }
    }

    /**
     * Gets the {@linkplain Stereotype} that is applied to the provided property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain Stereotype}
     */
    @Override
    public Stereotype GetPropertyStereotype(Property property)
    {
        if(this.DoesItHaveTheStereotype(property, Stereotypes.PartProperty))
        {
            return this.GetStereotype(property, Stereotypes.PartProperty);
        }
        if(this.DoesItHaveTheStereotype(property, Stereotypes.ValueProperty))
        {
            return this.GetStereotype(property, Stereotypes.ValueProperty);
        }
        if(this.DoesItHaveTheStereotype(property, Stereotypes.ReferenceProperty))
        {
            return this.GetStereotype(property, Stereotypes.ReferenceProperty);
        }
        
        return null;
    }
    
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param namedElement the {@linkplain Element}
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    @Override
    public Stereotype GetStereotype(Element namedElement, Enum<?> stereotype)
    {
        return this.GetStereotype(namedElement, stereotype.name());
    }

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain String}
     * 
     * @param element the {@linkplain Element}
     * @param stereotypeName the {@linkplain String} stereotype name
     * @return a {@linkplain Stereotype}
     */
    private Stereotype GetStereotype(Element element, String stereotypeName)
    {
        return StereotypesHelper.getAppliedStereotypeByString(element, stereotypeName);
    }   
    
    /**
     * Gets all the {@linkplain Stereotype} applied to the specified {@linkplain Element}
     * 
     * @param element the {@linkplain Element}
     * @return a {@linkplain Collection} of {@linkplain Stereotype}
     */
    @Override
    public Collection<Stereotype> GetAllStereotype(Element element)
    {
        return MDCustomizationForSysMLProfile.getInstance(element).getAllStereotypes();
    }
    
    /**
     * Applies the {@linkplain Stereotype} specified by the provided {@linkplain Stereotypes} to the provided {@linkplain Element} 
     * 
     * @param element the {@linkplain Element}
     * @param stereotype the {@linkplain Stereotypes}
     */
    @Override
    public void ApplyStereotype(Element element, Stereotypes stereotype)
    {
        this.ApplyStereotype(element, this.GetStereotype(stereotype));
    }
    
    /**
     * Applies the {@linkplain Stereotype} specified by the provided {@linkplain Stereotypes} to the provided {@linkplain Element} 
     * 
     * @param element the {@linkplain Element}
     * @param stereotype the {@linkplain Stereotype}
     */
    @Override
    public void ApplyStereotype(Element element, Stereotype stereotype)
    {
        StereotypesHelper.addStereotype(element, stereotype);
    }
    
    /**
     * Gets a value indicating whether the provided {@linkplain Element} has the stereotype property isEncapsulated and returns the value
     * 
     * @param element the {@linkplain Element}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean IsEncapsulated(Element element)
    {
        return SysMLUtilities.isEncapsulated(element);
    }
    
    /**
     * Gets a value indicating whether the provided {@linkplain Property}
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean IsPartProperty(Property property)
    {
        return MDCustomizationForSysMLProfile.isPartProperty(property);
    }

    /**
     * Gets a value indicating whether the provided {@linkplain Property}
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean IsValueProperty(Property property)
    {
        return MDCustomizationForSysMLProfile.isValueProperty(property);
    }
    
    /**
     * Gets a value indicating whether the provided {@linkplain Property} is a reference property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
	@Override
	public boolean IsReferenceProperty(Property property)
	{
		return MDCustomizationForSysMLProfile.isReferenceProperty(property);
	}
    
    /**
     * Gets value representation string out of the specified {@linkplain LiteralNumericValue}
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    @Override
    public String GetValueRepresentation(Property property)
    {         
        String unit = this.GetUnitRepresention(property);
        
        String valueString = this.GetValueFromProperty(property);
        
        return String.format("%s%s", valueString, unit == null ? String.format(" %s", this.GetTypeRepresentation(property)) : String.format(" [%s]", unit));
    }
    
    /**
     * Gets the default value from the specified {@linkplain Property} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    @Override
    public String GetValueFromProperty(Property property)
    {
        ValueSpecification value = property.getDefaultValue();
        String valueString = "";
        
        if(value instanceof LiteralInteger)
        {
            valueString = String.valueOf(((LiteralInteger)value).getValue());
        }
        else if(value instanceof LiteralReal)
        {
            valueString = String.valueOf(((LiteralReal)value).getValue());
        }
        else if(value instanceof LiteralUnlimitedNatural)
        {
            valueString = String.valueOf(((LiteralUnlimitedNatural)value).getValue());
        }
        else if(value instanceof LiteralBoolean)
        {
            valueString = String.valueOf(((LiteralBoolean)value).isValue());
        }
        else if(value instanceof LiteralString)
        {
            valueString = ((LiteralString)value).getValue();
        }
        
        return valueString;
    }
    
    /**
     * Gets the type of the provided value as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    @Override
    public String GetTypeRepresentation(Property property)
    {
        Type type = property.getType();
        
        if(type != null)
        {
            return type.getName();
        }
        
        return " ";
    }

    /**
     * Gets the {@linkplain Unit} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    @Override
    public String GetUnitRepresention(Property property)
    {
        if(property.getType() == null)
        {
            return null;
        }
        
        Stereotype stereotype = StereotypesHelper.getAppliedStereotypeByString(property.getType(), "unit");

        if(stereotype == null)
        {
            return null;
        }
        
        List<String> unit = StereotypesHelper.getStereotypePropertyValueAsString(property.getType(), stereotype, "unit");
        
        if(unit != null && !unit.isEmpty())
        {
            return unit.get(0);
        }
        
        return null;
    }

    /**
     * Gets the DataTypes element from the {@linkplain #GetProject()}
     * 
     * @return a {@linkplain Collection} of {@linkplain #DataType}
     */
    @Override
    public Collection<DataType> GetDataTypes()
    {
        Stereotype stereotype = this.GetStereotype(Stereotypes.ValueType);
        
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
        List<Element> units = StereotypesHelper.getExtendedElements(MDCustomizationForSysMLProfile.getInstance(this.sessionService.GetProject()).getUnit());
                
        return units.stream()
                .filter(x -> x instanceof InstanceSpecification && this.DoesItHaveTheStereotype(x, Stereotypes.Unit))
                .map(x -> (InstanceSpecification)x)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the requirement text out of the provided SysML requirement
     * 
     * @param requirement the {@linkplain Element} requirement
     * @return a {@linkplain String}
     */
    @Override
    public String GetRequirementText(Element requirement)
    {
        return RequirementUtilities.getRequirementText(requirement);
    }

    /**
     * Gets the requirement text out of the provided SysML requirement
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    @Override
    public String GetRequirementId(Class requirement)
    {
        return AppContainer.Container.getComponent(IMagicDrawTransactionService.class).GetRequirementId(requirement);
    }
}
