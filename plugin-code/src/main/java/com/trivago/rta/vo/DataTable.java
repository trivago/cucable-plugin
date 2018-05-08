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

package com.trivago.rta.vo;


import java.util.ArrayList;
import java.util.List;

public class DataTable {
    private final List<List<String>> rows = new ArrayList<>();

    public void addRow(List<String> rowValues) {
        this.rows.add(rowValues);
    }

    public List<List<String>> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return "DataTable{" +
                "rows=" + rows +
                '}';
    }
}