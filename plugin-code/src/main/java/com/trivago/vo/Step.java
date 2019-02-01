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

package com.trivago.vo;

public class Step {
    private final DataTable dataTable;
    private final String docString;
    private String name;

    public Step(final String name, final DataTable dataTable, final String docString) {
        this.name = name;
        this.dataTable = dataTable;
        this.docString = docString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public String getDocString() {
        return docString;
    }
}