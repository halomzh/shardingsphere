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

import org.apache.shardingsphere.test.integration.cases.assertion.dql.DQLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.engine.param.IntegrateTestParameters;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public final class AdditionalDQLIT extends BaseDQLIT {
    
    private final DQLIntegrateTestCaseAssertion assertion;
    
    public AdditionalDQLIT(final String parentPath, final DQLIntegrateTestCaseAssertion assertion, final String ruleType,
                           final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{2} -> {3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestEnvironment.getInstance().isRunAdditionalTestCases() ? IntegrateTestParameters.getParametersWithAssertion(IntegrateTestCaseType.DQL) : Collections.emptyList();
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(connection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (
                Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(String.format(getSql(), assertion.getSQLValues().toArray()))) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection)
            throws SQLException, ParseException {
        try (
                Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                ResultSet resultSet = statement.executeQuery(String.format(getSql(), assertion.getSQLValues().toArray()))) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection)
            throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(connection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(connection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            assertTrue("Not a DQL statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray())));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a DQL statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            assertTrue("Not a DQL statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray())));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a DQL statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
}
