package com.company;

import javax.swing.*;
import java.io.IOException;

public class FormatTableTask extends SwingWorker<Void, Integer> {

    private ExcelFile file;

    public FormatTableTask(ExcelFile file) {
        this.file = file;
    }
    @Override
    protected Void doInBackground() throws IOException {
        file.removeDuplicates();
        return null;
    }
}
