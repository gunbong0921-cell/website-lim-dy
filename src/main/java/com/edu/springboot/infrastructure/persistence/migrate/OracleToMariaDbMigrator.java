package com.edu.springboot.infrastructure.persistence.migrate;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-shot JDBC copy. Not a Spring bean. Run via scripts/migrate-oracle-to-mariadb.ps1
 * or {@code gradlew migrateOracleToMariaDb}.
 */
public final class OracleToMariaDbMigrator {

	private static final String[] TABLES = {
		"HEXAQ_MEMBER",
		"HEXAQ_KAKAO_FRIEND",
		"HEXAQ_FREE_BOARD",
		"HEXAQ_QNA_BOARD",
		"HEXAQ_ARCHIVE_BOARD",
		"HEXAQ_BOARD_FILE",
		"HEXAQ_COMMENT",
		"HEXAQ_BOARD_LIKE"
	};

	private OracleToMariaDbMigrator() {
	}

	public static void main(String[] args) throws Exception {
		boolean force = hasForce(args);
		String oracleUrl = required("ORACLE_URL");
		String oracleUser = required("ORACLE_USERNAME");
		String oraclePassword = envOrEmpty("ORACLE_PASSWORD");
		String mariaUrl = required("MARIADB_URL");
		String mariaUser = required("MARIADB_USERNAME");
		String mariaPassword = envOrEmpty("MARIADB_PASSWORD");

		Class.forName("oracle.jdbc.OracleDriver");
		Class.forName("org.mariadb.jdbc.Driver");

		try (Connection oracle = DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword);
			Connection maria = DriverManager.getConnection(mariaUrl, mariaUser, mariaPassword)) {
			oracle.setReadOnly(true);
			maria.setAutoCommit(false);
			try (Statement st = maria.createStatement()) {
				st.execute("SET FOREIGN_KEY_CHECKS=0");
			}
			for (String table : TABLES) {
				copyTable(oracle, maria, table, force);
			}
			try (Statement st = maria.createStatement()) {
				st.execute("SET FOREIGN_KEY_CHECKS=1");
			}
			maria.commit();
			System.out.println("Migration finished.");
		}
	}

	private static void copyTable(Connection oracle, Connection maria, String table, boolean force)
		throws SQLException {
		long destCount = count(maria, table);
		if (destCount > 0) {
			if (!force) {
				throw new IllegalStateException(table + " already has " + destCount
					+ " rows. Pass --force (or MIGRATE_FORCE=true) to replace.");
			}
			try (Statement st = maria.createStatement()) {
				st.execute("DELETE FROM " + table);
			}
		}
		List<String> columns;
		int copied = 0;
		try (Statement select = oracle.createStatement();
			ResultSet rs = select.executeQuery("SELECT * FROM " + table)) {
			columns = columnNames(rs.getMetaData());
			if (columns.isEmpty()) {
				System.out.println(table + ": 0 rows");
				return;
			}
			String insertSql = insertSql(table, columns);
			try (PreparedStatement insert = maria.prepareStatement(insertSql)) {
				while (rs.next()) {
					for (int i = 1; i <= columns.size(); i++) {
						insert.setObject(i, jdbcValue(rs, i));
					}
					insert.addBatch();
					copied++;
					if (copied % 200 == 0) {
						insert.executeBatch();
					}
				}
				insert.executeBatch();
			}
		}
		resetAutoIncrement(maria, table);
		System.out.println(table + ": " + copied + " rows");
	}

	private static Object jdbcValue(ResultSet rs, int index) throws SQLException {
		Object value = rs.getObject(index);
		if (value == null) {
			return null;
		}
		if (value instanceof Clob clob) {
			long length = clob.length();
			if (length > Integer.MAX_VALUE) {
				throw new SQLException("CLOB too large at column " + index);
			}
			return length == 0 ? "" : clob.getSubString(1L, (int) length);
		}
		if (value instanceof BigDecimal decimal) {
			if (decimal.scale() <= 0 && decimal.precision() <= 18) {
				return decimal.longValue();
			}
			return decimal;
		}
		if (value instanceof Timestamp || value instanceof Number || value instanceof String) {
			return value;
		}
		if (value.getClass().getName().startsWith("oracle.sql.")) {
			Timestamp timestamp = rs.getTimestamp(index);
			return timestamp != null ? timestamp : rs.getString(index);
		}
		return value;
	}

	private static List<String> columnNames(ResultSetMetaData meta) throws SQLException {
		List<String> names = new ArrayList<>();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			names.add(meta.getColumnLabel(i).toLowerCase(Locale.ROOT));
		}
		return names;
	}

	private static String insertSql(String table, List<String> columns) {
		String cols = String.join(", ", columns);
		String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());
		return "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
	}

	private static long count(Connection connection, String table) throws SQLException {
		try (Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
			rs.next();
			return rs.getLong(1);
		}
	}

	private static void resetAutoIncrement(Connection maria, String table) throws SQLException {
		long next = 1L;
		try (Statement st = maria.createStatement();
			ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM " + table)) {
			if (rs.next()) {
				next = rs.getLong(1);
			}
		}
		try (Statement st = maria.createStatement()) {
			st.execute("ALTER TABLE " + table + " AUTO_INCREMENT = " + next);
		}
	}

	private static boolean hasForce(String[] args) {
		if ("true".equalsIgnoreCase(envOrEmpty("MIGRATE_FORCE"))) {
			return true;
		}
		for (String arg : args) {
			if ("--force".equals(arg)) {
				return true;
			}
		}
		return false;
	}

	private static String required(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Missing env " + name);
		}
		return value.trim();
	}

	private static String envOrEmpty(String name) {
		String value = System.getenv(name);
		return value == null ? "" : value;
	}
}
