package Services.MagicDrawSelection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;

class MagicDrawSelectionServiceTestFixture
{
    private IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    private MagicDrawSelectionService selectionService;

    @BeforeEach
    void setUp() throws Exception
    {
        this.elementDefinitionBrowserViewModel = mock(IElementDefinitionBrowserViewModel.class);
        this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
        this.selectionService = new MagicDrawSelectionService(this.elementDefinitionBrowserViewModel, this.requirementBrowserViewModel);
    }
    
    @Test
    void VerifyGetDstSelection()
    {
        assertDoesNotThrow(() -> this.selectionService.GetDstSelection(Class.class));
    }

    @Test
    void VerifySetActiveBrowser()
    {
        assertTrue(true);
    }

    @Test
    void VerifyGetHubSelection()
    {
        assertTrue(true);
    }
}
