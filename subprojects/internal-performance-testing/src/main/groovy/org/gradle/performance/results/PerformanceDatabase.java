/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.performance.results;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PerformanceDatabase {
    private final String databaseName;
    private final List<ConnectionAction<Void>> databaseInitializers;
    private Connection connection;

    public PerformanceDatabase(String databaseName, ConnectionAction<Void>... schemaInitializers) {
        this.databaseName = databaseName;
        this.databaseInitializers = Arrays.asList(schemaInitializers);
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(String.format("Could not close datastore '%s'.", getUrl()), e);
            } finally {
                connection = null;
            }
        }
    }

    public <T> T withConnection(ConnectionAction<T> action) throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(getUrl(), getUserName(), getPassword());
            for (ConnectionAction<Void> initializer : databaseInitializers) {
                try {
                    initializer.execute(connection);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 90096) {
                        System.out.println("Not enough permissions to migrate the performance database. This is okay if you are only trying to read.");
                    } else {
                        connection.close();
                        connection = null;
                        throw e;
                    }
                }
            }
        }
        return action.execute(connection);
    }

    public String getUrl() {
        String defaultUrl = "jdbc:h2:" + System.getProperty("user.home") + "/.gradle-performance-test-data";
        String baseUrl = System.getProperty("org.gradle.performance.db.url", defaultUrl);
        StringBuilder url = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("/")) {
            url.append('/');
        }
        url.append(databaseName);
        return url.toString();
    }

    public String getUserName() {
        return System.getProperty("org.gradle.performance.db.username", "sa");
    }

    public String getPassword() {
        return System.getProperty("org.gradle.performance.db.password", "");
    }
}
