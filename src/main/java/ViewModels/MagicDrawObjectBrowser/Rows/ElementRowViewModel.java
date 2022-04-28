/*
 * ClassRowViewModel.java
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
package ViewModels.MagicDrawObjectBrowser.Rows;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain ElementRowViewModel} is the row view model that represents any {@linkplain Class} in the {@linkplain MagicDrawObjectBrowser}
 * 
 * @param TElement the type of {@linkplain Element} this row view model represents
 */
public abstract class ElementRowViewModel<TElement extends Element> implements IElementRowViewModel<TElement>
{
    /**
     * The current class Logger
     */
    protected final Logger Logger = LogManager.getLogger();
    
    /**
     * The value indicating whether this row should be highlighted as "selected for transfer"
     */
    private boolean isSelected;
    
    /**
     * Switches between the two possible values for the {@linkplain isSelected}
     * 
     * @return the new {@linkplain boolean} value
     */
    @Override
    public boolean SwitchIsSelectedValue()
    {
        return this.isSelected = !this.isSelected;
    }
    
    /**
     * Sets a value whether this row is selected
     * 
     * @param isSelected the {@linkplain boolean} value
     */
    @Override
    public void SetIsSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
    
    /**
     * Gets a value indicating whether this row should be highlighted as "selected for transfer"
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsSelected()
    {
        return this.isSelected;
    }
    
    /**
     * The value indicating whether this row should be highlighted in the tree
     */
    private boolean isHighlighted;
    
    /**
     * Sets a value whether this row is highlighted
     * 
     * @param isHighlighted the {@linkplain boolean} value
     */
    @Override
    public void SetIsHighlighted(boolean isHighlighted)
    {
        this.isHighlighted = isHighlighted;
    }
    
    /**
     * Gets a value indicating whether this row should be highlighted in the tree
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsHighlighted()
    {
        return this.isHighlighted;
    }
    
    /**
     * The name of the {@linkplain Element} represented by this row view model
     */
    private TElement element;
    
    /**
     * Gets the name of the {@linkplain Element} represented by this row view model
     * 
     * @return the represented {@linkplain Element}
     */
    @Override
    public TElement GetElement()
    {
        return this.element;
    }

    /**
     * The name of the {@linkplain Element} represented by this row view model
     */
    private String name;
    
    /**
     * Gets the name of the {@linkplain Element} represented by this row view model
     * 
     * @return
     */
    public String GetName()
    {
        return this.name;
    }
    
    /**
     * Sets the name of this row view model
     * 
     * @param name the new name
     */
    protected void SetName(String name)
    {
        this.name = name;
    }

    
    /**
     * The {@linkplain IRowViewModel} parent of this row view model
     */
    private IRowViewModel parent;

    /**
     * Gets the parent row view model of the current row
     * 
     * @return an {@linkplain IRowViewModel}
     */
    @Override
    public IRowViewModel GetParent()
    {
        return this.parent;
    }

    /**
     * A value indicating whether the current row is expanded
     */
    private boolean isExpanded;
    
    /**
     * Gets a value indicating whether the current row is expanded
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean GetIsExpanded()
    {
        return this.isExpanded;
    }
    
    /**
     * Sets a value indicating whether the current row is expanded
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public void SetIsExpanded(boolean isExpanded)
    {
        this.isExpanded = isExpanded;
    }

    /**
     * Initializes a new {@linkplain ElementRowViewModel}
     * 
     * @param parent the {@linkplain IElementRowViewModel} parent view model of this row view model
     * @param element the {@linkplain TElement} {@linkplain Element} which is represented
     */
    public ElementRowViewModel(IElementRowViewModel<?> parent, TElement element)
    {
        this.element = element;
        this.parent = parent;
        this.UpdateProperties();
    }
    
    /**
     * Updates this view model properties
     * 
     * @param name the name of this row view model
     */ 
    protected void UpdateProperties(String name)
    {
        this.name = name;
    }
    
    /**
     * Updates this view model properties
     */ 
    public void UpdateProperties()
    {
        if(element != null)
        {
            if(this.element instanceof Package)
            {
                this.name = ((Package)this.element).getName();
            }
            else if(this.element instanceof Class)
            {
                this.name = ((Class)this.element).getName();
            }
            else if(this.element instanceof Property)
            {
                this.name = ((Property)this.element).getName();
                Type type = ((Property)this.element).getType();
                
                if(StringUtils.isBlank(this.name) && type != null)
                {
                    this.name = String.format(": %s", type.getName());
                }
            }
            
            if(StringUtils.isBlank(this.name))
            {
                this.name = this.element.getHumanName();
            }
        }
    }
    /**
     * Updates the represented {@linkplain Element} with the specified one
     * 
     * @param element the new {@linkplain Element}
     * @param shouldHighlight a value indicating whether the highlight should be updated
     */
    @SuppressWarnings("unchecked")
    public void UpdateElement(Element element, boolean shouldHighlight)
    {
        this.element = (TElement)element;
        this.UpdateProperties();
        
        if(shouldHighlight)
        {
            this.isHighlighted = true;
        }

        if(this instanceof IHaveContainedRows)
        {
            IHaveContainedRows<?> thisAsParent = (IHaveContainedRows<?>)this;
            
            thisAsParent.GetContainedRows().clear();
            thisAsParent.ComputeContainedRows();
        }
    }
}
