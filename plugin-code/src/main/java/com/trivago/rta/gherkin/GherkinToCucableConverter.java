/*
 * Copyright 2017 trivago N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivago.rta.gherkin;

import gherkin.ast.DataTable;
import gherkin.ast.DocString;
import gherkin.ast.Examples;
import gherkin.ast.Node;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
class GherkinToCucableConverter {

    /**
     * Converts a list of Gherkin steps to Cucable steps including data tables.
     *
     * @param gherkinSteps a {@link Step} list.
     * @return a {@link com.trivago.rta.vo.Step} list.
     */
    List<com.trivago.rta.vo.Step> convertGherkinStepsToCucableSteps(final List<Step> gherkinSteps) {
        List<com.trivago.rta.vo.Step> steps = new ArrayList<>();

        for (Step gherkinStep : gherkinSteps) {
            com.trivago.rta.vo.Step step;
            com.trivago.rta.vo.DataTable dataTable = null;
            String docString = null;

            Node argument = gherkinStep.getArgument();
            if (argument instanceof DataTable) {
                dataTable = convertGherkinDataTableToCucableDataTable((DataTable) argument);
            } else if (argument instanceof DocString) {
                docString = ((DocString) argument).getContent();
            }

            String keywordAndName = gherkinStep.getKeyword().concat(gherkinStep.getText());
            step = new com.trivago.rta.vo.Step(keywordAndName, dataTable, docString);
            steps.add(step);
        }
        return steps;
    }

    /**
     * Converts a Gherkin data table to a Cucable data table.
     *
     * @param gherkinDataTable a {@link DataTable}.
     * @return a {@link com.trivago.rta.vo.DataTable}.
     */
    private com.trivago.rta.vo.DataTable convertGherkinDataTableToCucableDataTable(
            final DataTable gherkinDataTable) {

        com.trivago.rta.vo.DataTable dataTable = new com.trivago.rta.vo.DataTable();
        for (TableRow row : gherkinDataTable.getRows()) {
            List<TableCell> cells = row.getCells();
            List<String> rowValues = new ArrayList<>();
            for (TableCell cell : cells) {
                rowValues.add(cell.getValue());
            }
            dataTable.addRow(rowValues);
        }
        return dataTable;
    }

    /**
     * Converts a list of Gherkin tags to simple Cucable string tags.
     *
     * @param gherkinTags a {@link Tag} list.
     * @return a {@link String} list of tags.
     */
    List<String> convertGherkinTagsToCucableTags(final List<Tag> gherkinTags) {
        List<String> tags = new ArrayList<>();
        for (Tag gherkinTag : gherkinTags) {
            tags.add(gherkinTag.getName());
        }
        return tags;
    }

    /**
     * Converts a Gherkin example table to a map of columns (keys) and rows (values)
     *
     * @param exampleTable a Gherkin {@link Examples} instance.
     * @return a map where the keys are the column headers and the values are lists of strings.
     */
    Map<String, List<String>> convertGherkinExampleTableToCucableExampleMap(
            final Examples exampleTable,
            final List<String> includeScenarioTags,
            final List<String> excludeScenarioTags
    ) {
        Map<String, List<String>> exampleMap = new LinkedHashMap<>();

        List<TableCell> headerCells = exampleTable.getTableHeader().getCells();
        for (TableCell headerCell : headerCells) {
            exampleMap.put("<" + headerCell.getValue() + ">", new ArrayList<>());
        }
        Object[] columnKeys = exampleMap.keySet().toArray();

        List<TableRow> tableBody = exampleTable.getTableBody();
        for (TableRow tableRow : tableBody) {
            List<TableCell> cells = tableRow.getCells();
            for (int i = 0; i < cells.size(); i++) {
                String columnKey = (String) columnKeys[i];
                List<String> values = exampleMap.get(columnKey);
                values.add(cells.get(i).getValue());
            }
        }
        return exampleMap;
    }
}
