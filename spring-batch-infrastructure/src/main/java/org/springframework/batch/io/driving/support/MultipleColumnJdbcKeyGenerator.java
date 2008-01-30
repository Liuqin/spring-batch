/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.io.driving.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.io.driving.DrivingQueryItemReader;
import org.springframework.batch.io.driving.KeyGenerator;
import org.springframework.batch.item.StreamContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>Jdbc implementation of the {@link KeyGenerator} interface that works for composite keys.
 * (i.e. keys represented by multiple columns)  A sql query to be used to return the keys and
 * a {@link RestartDataRowMapper} to map each row in the resultset to an Object must be set in 
 * order to work correctly.
 * </p>
 *
 * @author Lucas Ward
 * @see DrivingQueryItemReader
 * @since 1.0
 */
public class MultipleColumnJdbcKeyGenerator implements
		KeyGenerator {

	public static final String RESTART_KEY = "CompositeKeySqlDrivingQueryItemReader.key";

	private JdbcTemplate jdbcTemplate;

	private RestartDataRowMapper keyMapper = new ColumnMapRestartDataRowMapper();

	private String sql;

	private String restartSql;
	
	public MultipleColumnJdbcKeyGenerator() {
		super();
	}

	/**
	 * Construct a new ItemReader.
	 *
	 * @param jdbcTemplate
	 * @param sql - Sql statement that returns all keys to process.
	 * @param keyMapper - RowMapper that maps each row of the ResultSet to an object.
	 */
	public MultipleColumnJdbcKeyGenerator(JdbcTemplate jdbcTemplate,
			String sql){
		this();
		Assert.notNull(jdbcTemplate, "The JdbcTemplate must not be null.");
		Assert.hasText(sql, "The DrivingQuery must not be null or empty.");
		Assert.notNull(keyMapper, "The key RowMapper must not be null.");
		this.jdbcTemplate = jdbcTemplate;
		this.sql = sql;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.io.sql.scratch.AbstractDrivingQueryItemReader#retrieveKeys()
	 */
	public List retrieveKeys() {
		return jdbcTemplate.query(sql, keyMapper);
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.io.sql.scratch.AbstractDrivingQueryItemReader#restoreKeys(org.springframework.batch.restart.RestartData)
	 */
	public List restoreKeys(StreamContext streamContext) {

		Assert.state(keyMapper != null, "KeyMapper must not be null.");
		Assert.state(StringUtils.hasText(restartSql), "The RestartQuery must not be null or empty" +
		" in order to restart.");

		if (streamContext.getProperties() != null) {
			return jdbcTemplate.query(restartSql, keyMapper.createSetter(streamContext), keyMapper);
		}

		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.restart.Restartable#getRestartData()
	 */
	public StreamContext getKeyAsRestartData(Object key) {
		Assert.state(keyMapper != null, "RestartDataConverter must not be null.");
		return keyMapper.createRestartData(key);
	}

	/**
	 * Set the query to use to retrieve keys in order to restore the previous
	 * state for restart.
	 *
	 * @param restartQuery
	 */
	public void setRestartQuery(String restartQuery) {
		this.restartSql = restartQuery;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate, "The JdbcTemplate must not be null.");
		Assert.hasText(sql, "The DrivingQuery must not be null or empty.");
		Assert.notNull(keyMapper, "The key RowMapper must not be null.");
	}
	
	/**
	 * Set the {@link RestartDataRowMapper} to be used to map a resultset
	 * to keys.
	 * 
	 * @param keyMapper
	 */
	public void setKeyMapper(RestartDataRowMapper keyMapper) {
		this.keyMapper = keyMapper;
	}
	
	/**
	 * Set the sql statement used to generate the keys list.
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	/**
	 * Set the sql statement to be used to restore the keys list
	 * after a restart.
	 * 
	 * @param restartSql
	 */
	public void setRestartSql(String restartSql) {
		this.restartSql = restartSql;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
