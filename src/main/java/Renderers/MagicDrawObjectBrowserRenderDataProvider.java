/*
 * MagicDrawObjectBrowserRenderDataProvider.java
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
package Renderers;

import java.awt.Color;

import javax.swing.Icon;

import Utils.ImageLoader.ImageLoader;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.PropertyRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.Parameters.ActualFiniteStateRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.Parameters.OptionRowViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.Parameters.ParameterValueRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;
import ViewModels.ObjectBrowser.RenderDataProvider.ObjectBrowserRenderDataProvider;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import cdp4common.commondata.ClassKind;

/**
 * The {@linkplain MagicDrawObjectBrowserRenderDataProvider} is the override {@linkplain ObjectBrowserRenderDataProvider} for the {@linkplain MagicDrawObjectBrowser}
 */
public class MagicDrawObjectBrowserRenderDataProvider extends ObjectBrowserRenderDataProvider
{
    /**
     * Gets the specified row view model node name
     * 
     * @param rowViewModel the row view model to get the name from
     * @return a {@linkplain String}
     */
    @Override
    public String getDisplayName(Object rowViewModel)
    {
        if(rowViewModel instanceof ElementRowViewModel)
        {
            return ((ElementRowViewModel<?>)rowViewModel).GetName();
        }
        
        return "undefined";
    }
    
    /**
     * Gets an value indicating to the tree whether the display name for this object should use HTMLrendering
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean isHtmlDisplayName(Object rowViewModel)
    {
        return false;
    }

    /**
     * Gets the background color to be used for rendering this node. Returns
     * null if the standard table background or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Color getBackground(Object rowViewModel)
    {
        if (rowViewModel instanceof IRowViewModel)
        {
            if(((IRowViewModel)rowViewModel).GetIsSelected())
            {
                return new Color(104, 143, 184);
            }
            
            if(((IRowViewModel)rowViewModel).GetIsHighlighted())
            {
                return Color.YELLOW;
            }            
        }
        
        return Color.WHITE;
    }

    /**
     * Gets the foreground color to be used for rendering this node. Returns
     * null if the standard table foreground or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Color getForeground(Object rowViewModel)
    {
        return null;
    }

    /**
     * Gets a description for this object suitable for use in a tool tip. 
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain String}
     */
    @Override
    public String getTooltipText(Object rowViewModel)
    {
        return null;
    }

    /**
     * Gets the background color to be used for rendering this node. Returns
     * null if the standard table background or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Icon getIcon(Object rowViewModel)
    {
        if(rowViewModel instanceof ElementRowViewModel)
        {
            ElementRowViewModel<?> element = (ElementRowViewModel<?>)rowViewModel;
            
            if(element.GetClassKind() == Stereotypes.Package)
            {
                return ImageLoader.GetIcon(ImageLoader.ThingFolder, "parametergroup.png");
            }
            else if(element.GetClassKind() == Stereotypes.Block)
            {
                return ImageLoader.GetIcon(ClassKind.ElementDefinition);
            }
            else if(element.GetClassKind() == Stereotypes.Requirement)
            {
                return ImageLoader.GetIcon(ClassKind.Requirement);
            }
            else if(element.GetClassKind() == Stereotypes.PortProperty)
            {
                return ImageLoader.GetIcon(ClassKind.ElementUsage);
            }
            else if(element instanceof PropertyRowViewModel)
            {
                return ImageLoader.GetIcon(ClassKind.Parameter);
            }
        }
        
        return ImageLoader.GetIcon();
    }
}
