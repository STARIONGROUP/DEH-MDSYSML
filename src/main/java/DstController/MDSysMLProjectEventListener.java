/*
 * MDSysMLProjectEventListener.java
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
package DstController;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListener;

import Reactive.ObservableValue;

/**
 * The {@linkplain MDSysMLProjectEventListener} is a {@linkplain ProjectEventListener}. It listens for changes happening in Cameo/MagicDraw on the open project
 */
public final class MDSysMLProjectEventListener implements ProjectEventListener
{
    /**
     * Gets a reactive value indicating if Cameo/MagicDraw has an open document
     */
    protected final ObservableValue<Boolean> HasOneDocumentOpenObservable = new ObservableValue<Boolean>(false, Boolean.class);

    /**
     * Gets a reactive value indicating that the open document has been saved
     */
    protected final ObservableValue<Boolean> projectSavedObservable = new ObservableValue<Boolean>(false, Boolean.class);
    
    /**
     * Gets a reactive {@linkplain Project} of type {@linkplain ObservableValue} of type {@linkplain Project}
     */
    protected final ObservableValue<Project> OpenDocumentObservable = new ObservableValue<Project>(Project.class);
    
    /**
     * Occurs when the project gets saved
     * 
     * @param project the current open project
     * @param isSaved a value indicating whether the project has been saved
     */
    @Override
    public void projectSaved(Project project, boolean isSaved) 
    {
        this.projectSavedObservable.Value(isSaved);
    }
    
    /**
     * Occurs when the project gets closed
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectClosed(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(false);
        this.OpenDocumentObservable.Value(null);
    }

    /**
     * Occurs when a project is created
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectCreated(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(true);
        this.OpenDocumentObservable.Value(project);
    }

    /**
     * Occurs when the project gets open
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectOpened(Project project)
    {
        this.OpenDocumentObservable.Value(project);
        this.HasOneDocumentOpenObservable.Value(true);
    }

    /**
     * Occurs when the project gets open from the user interface
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectOpenedFromGUI(Project project)
    {
        this.OpenDocumentObservable.Value(project);
        this.HasOneDocumentOpenObservable.Value(true);
    }

    /**
     * Occurs when the project gets replaced by another one
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable} 
     */
    @Override
    public void projectReplaced(Project project, Project project2)
    {
        this.OpenDocumentObservable.Value(project2);
        this.HasOneDocumentOpenObservable.Value(true);
    }

    /**
     * Occurs when the project gets deactivated. Unused
     */
    @Override
    public void projectDeActivated(Project project) { }

    /**
     * Occurs when the project gets activated. Unused
     */
    @Override
    public void projectActivated(Project project) { }

    /**
     * Occurs when the project gets activated from the user interface. Unused
     */
    @Override
    public void projectActivatedFromGUI(Project project) { }
    
    /**
     * Occurs when the project gets pre activated. Unused
     */
    @Override
    public void projectPreActivated(Project project) { }

    /**
     * Occurs when the project gets pre closed. Unused
     */
    @Override
    public void projectPreClosed(Project project) { }

    /**
     * Occurs when the project gets pre closed final. Unused
     */
    @Override
    public void projectPreClosedFinal(Project project) { }

    /**
     * Occurs when the project gets pre deactivated. Unused
     */
    @Override
    public void projectPreDeActivated(Project project) { }

    /**
     * Occurs when the project gets pre replaced. Unused
     */
    @Override
    public void projectPreReplaced(Project project, Project project2) { }

    /**
     * Occurs when the project gets pre saved. Unused
     */
    @Override
    public void projectPreSaved(Project project, boolean isSaved) { }
}
