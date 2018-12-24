package com.company.controller;

import com.company.tasks.FormatTableTask;
import com.company.tasks.MaxPriceTableTask;
import com.company.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTest {

    @Mock
    private View view;
    @InjectMocks
    private Controller controller;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void viewAddToTextAreaInvokedWhenOnMessage() throws Exception {
        controller.onMessage("test");
        TimeUnit.SECONDS.sleep(2);
        verify(view).addTextToTextArea("test");
    }

    @Test
    public void propertyChangeProgressThenSetProgressBarValue() {
        PropertyChangeEvent event = new PropertyChangeEvent(new Object(), "progress", 0, 1);
        controller.propertyChange(event);
        verify(view).setProgressBarValue(1);
    }

    @Test
    public void propertyChangeFormatTableTaskDone() {
        FormatTableTask task = mock(FormatTableTask.class);
        PropertyChangeEvent event = new PropertyChangeEvent(task, "state", SwingWorker.StateValue.STARTED,
                SwingWorker.StateValue.DONE);
        JButton button = mock(JButton.class);
        when(view.getRemoveDuplicatesButton()).thenReturn(button);

        controller.propertyChange(event);
        verify(view).addTextToTextArea("Преобразование таблицы окончено.");
        verify(view).hideProgressBar();
        verify(button).setText("Удалить дубликаты");
        verify(view).enableAllButtons();
    }

    @Test
    public void propertyChangeMaxPriceTableTaskDone() {
        MaxPriceTableTask task = mock(MaxPriceTableTask.class);
        PropertyChangeEvent event = new PropertyChangeEvent(task, "state", SwingWorker.StateValue.STARTED,
                SwingWorker.StateValue.DONE);
        JButton button = mock(JButton.class);
        when(view.getProcessButton()).thenReturn(button);

        controller.propertyChange(event);
        verify(view).addTextToTextArea("Постановка цен закончена.");
        verify(view).hideProgressBar();
        verify(button).setText("Поставить цены");
        verify(view).enableAllButtons();
    }
}