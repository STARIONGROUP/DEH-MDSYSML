package Actions.ToolBar;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.mockito.ArgumentMatchers.*;

import java.awt.event.ActionEvent;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OpenHubBrowserPanelActionTestFixture
{
    @Before
    public void setUp() throws Exception
    {        
    }

    @Ignore
    @Test
    public void test()
    {
        OpenHubBrowserPanelAction action = new OpenHubBrowserPanelAction();
        assertDoesNotThrow(() -> action.actionPerformed(any(ActionEvent.class)));
    }

}
