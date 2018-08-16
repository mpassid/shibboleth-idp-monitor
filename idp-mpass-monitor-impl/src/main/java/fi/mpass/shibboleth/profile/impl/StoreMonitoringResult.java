/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.mpass.shibboleth.profile.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * This action stores results from {@link MonitoringResultContext} to the given {@link DataSource}.
 */
@SuppressWarnings("rawtypes")
public class StoreMonitoringResult extends AbstractProfileAction {
    
    /** The database table name storing monitoring results. */
    public static final String TABLE_NAME_MONITORING_RESULTS = "mpass_monitoring_result";

    /** The database table name storing monitoring step results. */
    public static final String TABLE_NAME_MONITORING_STEP_RESULTS = "mpass_monitoring_step_result";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoreMonitoringResult.class);
    
    /** JDBC data source for retrieving connections. */
    @NonnullAfterInit private DataSource dataSource;
    
    /** Number of times to retry a transaction if it rolls back. */
    @NonNegative private int transactionRetry;
    
    /** Error messages that signal a transaction should be retried. */
    @Nonnull @NonnullElements private Collection<String> retryableErrors;
    
    /** MonitoringResultContext to operate on. */
    @Nullable private MonitoringResultContext monitoringCtx;
    
    /**
     * Get the source datasource used to communicate with the database.
     * 
     * @return the data source;
     */
    @NonnullAfterInit public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get the source datasource used to communicate with the database.
     * 
     * @param source the data source;
     */
    public void setDataSource(@Nonnull final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        dataSource = Constraint.isNotNull(source, "DataSource cannot be null");
    }
    
    /**
     * Get the number of retries to attempt for a failed transaction.
     * 
     * @return number of retries
     */
    public int getTransactionRetries() {
        return transactionRetry;
    }
    
    /**
     * Set the number of retries to attempt for a failed transaction. Defaults to 3.
     * 
     * @param retries the number of retries
     */
    public void setTransactionRetries(@NonNegative final int retries) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        transactionRetry = (int) Constraint.isGreaterThanOrEqual(0, retries,
                "Timeout must be greater than or equal to 0");
    }

    /**
     * Get the error messages to check for classifying a driver error as retryable, generally indicating
     * a lock violation or duplicate insert that signifies a broken database.
     * 
     * @return retryable messages
     */
    @Nonnull @NonnullElements public Collection<String> getRetryableErrors() {
        return retryableErrors;
    }
    
    /**
     * Set the error messages to check for classifying a driver error as retryable, generally indicating
     * a lock violation or duplicate insert that signifies a broken database.
     * 
     * @param errors retryable messages
     */
    @SuppressWarnings("unchecked")
    public void setRetryableErrors(@Nullable @NonnullElements final Collection<String> errors) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        retryableErrors = new ArrayList(StringSupport.normalizeStringCollection(errors));
    }

    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        log.debug("Initializing");
        super.doInitialize();
        if (null == dataSource) {
            throw new ComponentInitializationException(getLogPrefix() + " No database connection provided");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        monitoringCtx = profileRequestContext.getSubcontext(MonitoringResultContext.class, false);
        if (monitoringCtx == null) {
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        final List<MonitoringSequenceResult> results = monitoringCtx.getResults();
        if (results == null || results.size() == 0) {
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        final List<MonitoringSequenceResult> results = monitoringCtx.getResults();
        final Map<Integer,Long> ids = new HashMap<>();
        int retries = getTransactionRetries();
        for (int i = 0; i < results.size(); i++) {
            boolean keepLoop = true;
            while (keepLoop) {
                try (final Connection dbConn = getConnection(false)) {
                    ids.put(i, store(dbConn, results.get(i)));
                    log.debug("New id put to the map");
                    dbConn.commit();
                    log.debug("Connection successfully committed");
                    keepLoop = false;
                } catch (final SQLException e) {
                    boolean retry = shouldRetry(e, retries);
                    if (retry) {
                        retries = retries - 1;
                        log.info("{} Retrying monitoring result storing operation", getLogPrefix());
                    } else {
                        ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
                        return;
                    }
                }
            }
            keepLoop = true;
            while (keepLoop) {
                try (final Connection dbConn = getConnection(false)) {
                    storeStep(dbConn, results.get(i), ids.get(i));
                    dbConn.commit();
                    keepLoop = false;
                } catch (final SQLException e) {
                    boolean retry = shouldRetry(e, retries);
                    if (retry) {
                        retries = retries - 1;
                        log.info("{} Retrying monitoring result step storing operation", getLogPrefix());
                    } else {
                        ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
                        return;
                    }
                }                
            }
        }
        ActionSupport.buildProceedEvent(profileRequestContext);
    }

    /**
     * Checks whether another attempt should be done after failed SQL event.
     * @param e The cause for the failed SQL event.
     * @param retries The amount of retries already run.
     * @return True if another attempt should be done, false otherwise.
     */
    protected boolean shouldRetry(final SQLException e, int retries) {
        boolean retry = false;
        if (getRetryableErrors() != null) {
            for (final String msg : getRetryableErrors()) {
                if (e.getSQLState() != null && e.getSQLState().contains(msg)) {
                    log.warn("{} Caught retryable SQL exception", getLogPrefix(), e);
                    retry = true;
                }
            }
        }
        if (retry) {
            if (retries - 1 < 0) {
                log.warn("{} Error retryable, but retry limit exceeded", getLogPrefix());
                return false;
            }
        } else {
            log.error("{} Caught SQL exception", getLogPrefix(), e);
            return false;
        }
        return true;
    }
    
    /**
     * Stores the given {@link MonitoringSequenceResult} without the attached sequence step results.
     * @param dbConn The database connection.
     * @param result The monitoring sequence result.
     * @return The identifier for the sequence result, generated by the database engine.
     * @throws SQLException If the storage operation fails.
     */
    protected synchronized long store(final Connection dbConn, final MonitoringSequenceResult result) 
            throws SQLException {
        final String insertResult = "INSERT INTO " + TABLE_NAME_MONITORING_RESULTS + 
                " (sourceId, startTime, endTime) VALUES (?,?,?)";
        final PreparedStatement statement = dbConn.prepareStatement(insertResult, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, result.getId());
        statement.setLong(2, result.getStartTime());
        statement.setLong(3, result.getEndTime());
        statement.executeUpdate();
        final ResultSet tableKeys = statement.getGeneratedKeys();
        if (tableKeys.next()) {
            long key = tableKeys.getLong("id");
            log.debug("Found key {}", key);
            return key;
        } else {
            log.error("Could not get the generated key after insert!");
            throw new SQLException("Could not get the generated key after insert!");
        }
    }

    /**
     * Stores the sequence step results from the given {@link MonitoringSequenceResult}.
     * @param dbConn The database connection.
     * @param result The monitoring sequence result, containing the sequence step results.
     * @param resultId The identifier for the already stored sequence result.
     * @throws SQLException If the storage operation fails.
     */
    protected void storeStep(final Connection dbConn, final MonitoringSequenceResult result, final Long resultId) 
            throws SQLException {
        log.debug("Starting to store step results");
        final List<MonitoringSequenceStepResult> steps = result.getStepResults();
        if (steps == null || steps.isEmpty()) {
            log.debug("No step results exists to be stored");
            return;
        }
        final String insertResult = "INSERT INTO " + TABLE_NAME_MONITORING_STEP_RESULTS + 
                " (resultId, phaseId, errorMessage, startTime, endTime) VALUES (?,?,?,?,?)";
        final PreparedStatement statement = dbConn.prepareStatement(insertResult);
        for (final MonitoringSequenceStepResult step : result.getStepResults()) {
            statement.setLong(1, resultId);
            statement.setInt(2, step.getPhaseId());
            if (step.getErrorMessage() != null) {
                statement.setString(3,  step.getErrorMessage());
            } else {
                statement.setString(3, "");
            }
            statement.setLong(4, step.getStartTime());
            statement.setLong(5, step.getEndTime());
            log.trace("Adding a batch to the statement");
            statement.addBatch();
            log.trace("Batch added");
        }
        log.trace("Executing the batch");
        statement.executeBatch();
        log.trace("Batch executed");
        statement.close();
    }
    
    /**
     * Obtain a connection from the data source.
     * 
     * <p>The caller must close the connection.</p>
     * 
     * @param autoCommit auto-commit setting to apply to the connection
     * 
     * @return a fresh connection
     * @throws SQLException if an error occurs
     */
    @Nonnull private Connection getConnection(final boolean autoCommit) throws SQLException {
        final Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(autoCommit);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return conn;
    }
}
