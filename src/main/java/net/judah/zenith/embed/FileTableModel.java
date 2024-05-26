package net.judah.zenith.embed;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class FileTableModel extends DefaultTableModel {
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final List<File> files = new ArrayList<>();
    private final String[] columnNames = {"File", "Date Modified", "File Size"};

//    public FileTableModel(List<File> files) {
//    	super(files);
//        files.addAll(files);
//    }

    @Override
    public int getRowCount() {
        return files == null ? 0 : files.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        File file = files.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getName();
            case 1:
                return formatter.format(new Date(file.lastModified()));
            case 2:
                return file.length();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Long.class;
            default:
                return Object.class;
        }
    }
    
    public void addFile(File file) {
        if (file == null || !file.isFile()) {
        	System.err.println("Nope: " + file == null ? null : file.getAbsolutePath());
        	return;
        }
        files.add(file);
    	int row = getRowCount();
        fireTableRowsInserted(row, row);
    }
}