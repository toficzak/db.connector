package krzysztof.db.connector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectBuilder {
  private List<String> values;
  private String entity;
  private String where;
  private String orderBy;
  private String limit;
  private DatabaseConnector connector = new DatabaseConnector();

  public SelectBuilder(String entity) {
    this.entity = entity;
  }

  public SelectBuilder values(List<String> values) {
    this.values = values;
    return this;
  }

  public SelectBuilder values(Collection<Fields> values) {
    this.values = values.stream()
        .map(Fields::name)
        .collect(Collectors.toList());
    return this;
  }

  public SelectBuilder where(String where) {
    this.where = where;
    return this;
  }

  public SelectBuilder orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  public SelectBuilder limit(String limit) {
    this.limit = limit;
    return this;
  }

  public List<Map<String, String>> execute() {
    return connector.select(
        values,
        entity,
        Optional.ofNullable(this.where),
        Optional.ofNullable(this.orderBy),
        Optional.ofNullable(this.limit));
  }
}
