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

package com.trivago.exceptions.properties;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WrongOrMissingPropertiesExceptionTest {

    @Test
    public void testErrorOnePropertyMessage() {
        List<String> properties = new ArrayList<>();
        properties.add("OneProperty");
        WrongOrMissingPropertiesException exception = new WrongOrMissingPropertiesException(properties);
        assertThat(
                exception.getMessage(),
                is("Property not specified correctly in the configuration section of your pom file: [OneProperty]")
        );
    }

    @Test
    public void testErrorMultiplePropertiesMessage() {
        List<String> properties = new ArrayList<>();
        properties.add("OneProperty");
        properties.add("AnotherProperty");
        WrongOrMissingPropertiesException exception = new WrongOrMissingPropertiesException(properties);
        assertThat(
                exception.getMessage(),
                is("Properties not specified correctly in the configuration section of your pom file: [OneProperty, AnotherProperty]")
        );
    }
}
