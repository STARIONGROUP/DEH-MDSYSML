/*
 * MagicDrawProjectEventListener.java
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
package Services.MagicDrawSession;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListener;

import Reactive.ObservableValue;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawProjectEventListener} is a {@linkplain ProjectEventListener}. It listens for changes happening in Cameo/MagicDraw on the open project
 */
public final class MagicDrawProjectEventListener implements ProjectEventListener, IMagicDrawProjectEventListener
{
    /**
     * The {@linkplain Application} instance
     */
    private Application application = Application.getInstance();
    
    /**
     * Backing field for {@linkplain HasOneDocumentOpen}
     */
    private final ObservableValue<Boolean> hasOneDocumentOpen = new ObservableValue<>(false, Boolean.class);
    
    /**
     * Gets a reactive value indicating if Cameo/MagicDraw has an open document
     */
    @Override
    public ObservableValue<Boolean> HasOneDocumentOpen()
    {
        return this.hasOneDocumentOpen;
    }

    /**
     * Backing field for {@linkplain ProjectSaved}
     */
    private final ObservableValue<Boolean> projectSaved = new ObservableValue<>(false, Boolean.class);

    /**
     * Gets a reactive value indicating that the open document has been saved
     */
    @Override
    public Observable<Boolean> ProjectSaved()
    {
        return this.projectSaved.Observable();
    }
    
    /**
     * Backing field for {@linkplain OpenDocument}
     */
    private final ObservableValue<Project> openDocument = new ObservableValue<>(Project.class);

    /**
     * Gets an {@linkplain ObservableValue} of type {@linkplain Project}
     */
    @Override
    public ObservableValue<Project> OpenDocument()
    {
        return this.openDocument;
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawProjectEventListener}
     */
    public MagicDrawProjectEventListener()
    {
        this.application.addProjectEventListener(this);
    }
    
    /**
     * Occurs when the project gets saved
     * 
     * @param project the current open project
     * @param isSaved a value indicating whether the project has been saved
     */
    @Override
    public void projectSaved(Project project, boolean isSaved) 
    {
        this.openDocument.Value(this.application.getProject());
        this.projectSaved.Value(isSaved);
    }
    
    /**
     * Occurs when the project gets closed
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectClosed(Project project)
    {
        this.hasOneDocumentOpen.Value(false);
        this.openDocument.Value(null);
    }

    /**
     * Occurs when a project is created
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectCreated(Project project)
    {
        this.hasOneDocumentOpen.Value(true);
        this.openDocument.Value(project);
    }

    /**
     * Occurs when the project gets open
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectOpened(Project project)
    {
        this.openDocument.Value(project);
        this.hasOneDocumentOpen.Value(true);
    }

    /**
     * Occurs when the project gets open from the user interface
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable}
     */
    @Override
    public void projectOpenedFromGUI(Project project)
    {
        this.openDocument.Value(project);
        this.hasOneDocumentOpen.Value(true);
    }

    /**
     * Occurs when the project gets replaced by another one
     * Fires the {@linkplain HasOneDocumentOpenObservable} and the {@linkplain OpenDocumentObservable} 
     */
    @Override
    public void projectReplaced(Project project, Project project2)
    {
        this.openDocument.Value(project2);
        this.hasOneDocumentOpen.Value(true);
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
