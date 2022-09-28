/*
 * ICircularDependencyValidationService.java
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
package Services.ModelConsistency;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;

/**
 * The {@linkplain ICircularDependencyValidationService} is the interface definition for the {@linkplain CircularDependencyValidationService}
 */
public interface ICircularDependencyValidationService
{

    /**
     * Verifies that the Part {@linkplain Property} is not involved in any recursion, if so verifies that the provided {@linkplain RootRowViewModel} 
     * does not contains a row representing the {@linkplain Property}
     * 
     * @param rootRowViewModel the Part {@linkplain RootRowViewModel}
     * @param property the {@linkplain Property}
     * @returns an assert
     */
    boolean IsAlreadyPresent(RootRowViewModel rootRowViewModel, Property property);

    /**
     * Filters out the provided {@linkplain Collection} of {@linkplain Element}
     * 
     * @param elements the initial {@linkplain Collection} of {@linkplain Element} to filter
     * @return a {@linkplain Pair} where the left element is a map of element involved in any recursion and the right one is the original list filtered out
     */
    Pair<ArrayListMultimap<Class, Collection<NamedElement>>, Collection<Element>> FiltersInvalidElements(Collection<Element> elements);

    /**
     * Gets the {@linkplain Map} of all invalid path found when {@linkplain #Validate()}.
     * The Key represent one top node in a path and the associated value is the collection of path/node children of the key 
     */
    ArrayListMultimap<Class, Collection<NamedElement>> GetInvalidPaths();
}
