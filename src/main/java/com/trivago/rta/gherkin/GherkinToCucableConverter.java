/*
 * Copyright 2017 trivago GmbH
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
import gherkin.ast.Node;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GherkinToCucableConverter {

    /**
     * Converts a list of Gherkin steps to Cucable steps including data tables.
     *
     * @param gherkinSteps a {@link Step} list.
     * @return a {@link com.trivago.rta.vo.Step} list.
     */
    List<com.trivago.rta.vo.Step> convertGherkinStepsToCucableSteps(
            final List<Step> gherkinSteps
    ) {
        List<com.trivago.rta.vo.Step> steps = new ArrayList<>();

        for (Step gherkinStep : gherkinSteps) {

            com.trivago.rta.vo.Step step;
            String dataTableString = "";

            Node argument = gherkinStep.getArgument();
            if (argument instanceof DataTable) {
                dataTableString = convertGherkinDataTableToString((DataTable) argument);
                System.out.println("Data Table found");
            }

            String keywordAndName = gherkinStep.getKeyword().concat(gherkinStep.getText());

            step = new com.trivago.rta.vo.Step(keywordAndName, dataTableString);
            steps.add(step);
        }
        return steps;
    }

    private String convertGherkinDataTableToString(DataTable dataTable) {
        final String DATA_TABLE_SEPARATOR = "|";

        String dataTableString = "";
        for (TableRow row : dataTable.getRows()) {
            List<String> rowStrings = new ArrayList<>();
            for (TableCell cell : row.getCells()) {
                rowStrings.add(cell.getValue());
            }
            if (rowStrings.size() > 0) {
                String rowString = DATA_TABLE_SEPARATOR
                        .concat(String.join(DATA_TABLE_SEPARATOR, rowStrings))
                        .concat(DATA_TABLE_SEPARATOR);
                dataTableString = dataTableString.concat(rowString);
            }
        }
        return dataTableString;
    }

    /**
     * Converts a list of Gherkin tags to Cucable string tags.
     *
     * @param gherkinTags a {@link Tag} list.
     * @return a {@link String} list of tags.
     */
    List<String> convertGherkinTagsToCucableTags(final List<Tag> gherkinTags) {
        List<String> featureTags = new ArrayList<>();
        for (Tag gherkinTag : gherkinTags) {
            featureTags.add(gherkinTag.getName());
        }
        return featureTags;
    }

}
