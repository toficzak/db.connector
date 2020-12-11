package krzysztof.db.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import krzysztof.property.reader.PropertyReader;

public class DatabaseConnector {

  private static final Logger LOGGER = Logger.getLogger(DatabaseConnector.class.getSimpleName());

  private final PropertyReader propertyReader;
  private final String dbUserName;
  private final String dbUserPassword;
  private final String dbPort;
  private final String dbName;
  private final String dbHost;

  public DatabaseConnector() {
    this.propertyReader = new PropertyReader("config.properties", this.getClass().getClassLoader());

    this.dbUserName = propertyReader.getProperty("db.user");
    this.dbUserPassword = propertyReader.getProperty("db.password");
    this.dbPort = propertyReader.getProperty("db.port");
    this.dbName = propertyReader.getProperty("db.name");
    this.dbHost = propertyReader.getProperty("db.host");
  }

  public List<Map<String, String>> select(List<String> values, String entity,
      Optional<String> optWhere, Optional<String> optOrderBy, Optional<String> optLimit) {

    StringBuilder queryBuilder = new StringBuilder(String.format("select %s from %s",
        values.stream().collect(Collectors.joining(",")),
        entity));

    if (optWhere.isPresent()) {
      queryBuilder.append(String.format(" where %s", optWhere.get()));
    }

    if (optOrderBy.isPresent()) {
      queryBuilder.append(String.format(" order by %s", optOrderBy.get()));
    }

    if (optLimit.isPresent()) {
      queryBuilder.append(String.format(" limit %s", optLimit.get()));
    }

    try (Connection connection = this.generateConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());
        ResultSet resultSet = preparedStatement.executeQuery()) {

      List<Map<String, String>> result = new ArrayList<>();
      while (resultSet.next()) {
        Map<String, String> mappedEntity = new HashMap<>();
        result.add(mappedEntity);
        values.forEach(value -> {
          try {
            mappedEntity.put(value, resultSet.getString(value));
          } catch (SQLException e) {
            e.printStackTrace();
          }
        });
      }
      return result;
    } catch (Exception e) {
      LOGGER.warning(e.getMessage());
      throw new IllegalArgumentException(e.getMessage());
    }
  }


  public void update(String query) {
    try (Connection connection = this.generateConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      LOGGER.warning(e.getMessage());
    }
  }

  private String generateConnectionString() {
    return new StringBuilder("jdbc:postgresql://").append(dbHost).append(":").append(dbPort)
        .append("/").append(dbName).toString();
  }

  private Connection generateConnection() {
    try {
      return DriverManager.getConnection(this.generateConnectionString(), dbUserName,
          dbUserPassword);
    } catch (SQLException e) {
      String message = String.format("SQL State: %s%n%s", e.getSQLState(), e.getMessage());
      LOGGER.warning(message);
      throw new IllegalArgumentException(message);
    }
  }

}
