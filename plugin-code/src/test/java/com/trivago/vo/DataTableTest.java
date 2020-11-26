package com.trivago.vo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DataTableTest {
    @Test
    public void validDataTableTest() {
        DataTable dataTable = new DataTable();

        List<String> row1 = new ArrayList<>();
        row1.add("value1");
        row1.add("value2");
        row1.add("value3");

        List<String> row2 = new ArrayList<>();
        row2.add("value4");
        row2.add("");
        row2.add("value6");

        dataTable.addRow(row1);
        dataTable.addRow(row2);

        List<List<String>> values = dataTable.getRows();
        assertThat(values.size(), is(2));
        assertThat(values.get(0).size(), is(3));
        assertThat(values.get(1).size(), is(3));
    }
}
