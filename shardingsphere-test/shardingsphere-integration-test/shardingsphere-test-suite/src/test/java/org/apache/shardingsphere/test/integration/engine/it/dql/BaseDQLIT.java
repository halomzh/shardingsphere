/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.engine.it.dql;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.dql.DQLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.engine.it.SingleIT;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;
import org.apache.shardingsphere.test.integration.env.schema.SchemaEnvironmentManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseDQLIT extends SingleIT {
    
    protected BaseDQLIT(final String parentPath, final DQLIntegrateTestCaseAssertion assertion, final String ruleType,
                        final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
    }
    
    @BeforeClass
    public static void insertData() throws IOException, JAXBException, SQLException, ParseException {
        SchemaEnvironmentManager.createDatabases();
        SchemaEnvironmentManager.dropTables();
        SchemaEnvironmentManager.createTables();
        for (DatabaseType each : IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().keySet()) {
            insertData(each);
        }
    }
    
    private static void insertData(final DatabaseType databaseType) throws SQLException, ParseException, IOException, JAXBException {
        for (String each : IntegrateTestEnvironment.getInstance().getRuleTypes()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(each), ActualDataSourceBuilder.createActualDataSources(each, databaseType)).load();
        }
    }
    
    @AfterClass
    public static void clearData() throws IOException, JAXBException {
        SchemaEnvironmentManager.dropDatabases();
    }
    
    protected final void assertResultSet(final ResultSet resultSet) throws SQLException {
        List<DataSetColumn> expectedColumns = new LinkedList<>();
        for (DataSetMetadata each : getDataSet().getMetadataList()) {
            expectedColumns.addAll(each.getColumns());
        }
        assertMetaData(resultSet.getMetaData(), expectedColumns);
        assertRows(resultSet, getDataSet().getRows());
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<DataSetColumn> expectedColumns) throws SQLException {
        // TODO fix shadow
        if ("shadow".equals(getRuleType())) {
            return;
        }
        // Unconfigured Table doesn't have column info, should skip check column info
        if (0 == actualMetaData.getColumnCount()) {
            return;
        }
        assertThat(actualMetaData.getColumnCount(), is(expectedColumns.size()));
        int index = 1;
        for (DataSetColumn each : expectedColumns) {
            assertThat(actualMetaData.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        ResultSetMetaData actualMetaData = actualResultSet.getMetaData();
        while (actualResultSet.next()) {
            int index = 1;
            assertTrue("Size of actual result set is different with size of expected dat set rows.", count < expectedDatSetRows.size());
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    if (!NOT_VERIFY_FLAG.equals(each)) {
                        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(index)), is(each));
                        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(actualMetaData.getColumnLabel(index))), is(each));
                    }
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                    assertThat(String.valueOf(actualResultSet.getObject(actualMetaData.getColumnLabel(index))), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
