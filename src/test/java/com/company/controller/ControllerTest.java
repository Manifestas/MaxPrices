package com.company.controller;

import com.company.view.View;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ControllerTest {

    @Test
    public void viewAddToTextAreaInvokedWhenOnMessage() {
        View view = mock(View.class);
        Controller controller = new Controller(view);
        controller.onMessage("test");
        verify(view).addTextToTextArea("test");
    }
}