package indi.atlantis.framework.chaconne;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CreatedSchemaUpdater
 * 
 * @author Fred Feng
 *
 * @version 1.0
 */
@Slf4j
public class CreatedSchemaUpdater implements SchemaUpdater {

	private final DataSource dataSource;

	public CreatedSchemaUpdater(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@PostConstruct
	@Override
	public void onCluserOnline() throws Exception {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			for (Map.Entry<String, String> entry : new HashMap<String, String>(Ddl.CreateScripts.ddls()).entrySet()) {
				if (!JdbcUtils.existsTable(connection, null, entry.getKey())) {
					JdbcUtils.update(connection, entry.getValue());
					log.info("Execute ddl: " + entry.getValue());
				}
			}
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

	@PreDestroy
	@Override
	public void onClusterOffline() {
	}

}