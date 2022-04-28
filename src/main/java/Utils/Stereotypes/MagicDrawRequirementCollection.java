/*
 * MagicDrawRequirementCollection.java
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
package Utils.Stereotypes;

import java.util.ArrayList;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Services.MappingEngineService.IMappableThingCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;

/**
 * The {@linkplain MagicDrawRequirementCollection} class represents a collection of {@linkplain MappedDstRequirementRowViewModel}.
 * The {@linkplain MagicDrawRequirementCollection} is usable by the {@linkplain MappingEngine} and 
 * each item of the collection represents a mapping in the {@linkplain MappingDirection.FromDstToHub}
 * 
 * The purpose of this {@linkplain MagicDrawRequirementCollection} is to type the requirements from Cameo/MagicDraw. 
 * e.g. Since the only obvious differences between Blocks and Requirements for instance are from their names and stereotypes, 
 * and there is no type difference in the MagicDraw API; they are both {@linkplain Class}.
 * 
 * Wrapping requirements this way solves the following problem,
 * The {@linkplain IMappingEngine} having to know the type of things to transform at runtime to be able to invoke the right rule; 
 * Meaning that defining a rule that takes as input a {@linkplain ArrayList} of any type isn't supported because of the java generic implementation.
 */
@SuppressWarnings("serial")
public class MagicDrawRequirementCollection extends ArrayList<MappedDstRequirementRowViewModel> implements IMappableThingCollection { }
