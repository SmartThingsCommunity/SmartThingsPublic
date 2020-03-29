/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sample.myapp;

import org.sample.numberutils.Numbers;

public class Main {

    public static void main(String... args) {
        new Main().printAnswer();
    }

    public void printAnswer() {
        String output = " The answer is " + Numbers.add(19, 23);
        System.out.println(output);
    }
}
