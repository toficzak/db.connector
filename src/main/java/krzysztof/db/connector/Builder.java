package krzysztof.db.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class Builder {

  public final String entityName;
  protected final boolean addCreated;
  protected final boolean addModified;

  private static final String INSERT_TEMPLATE = "insert into %s(%s) values (%s)";
  protected Map<Fields, Element> elements = new HashMap<>();

  public Builder(String entityName) {
    this.entityName = entityName;
    this.addCreated = true;
    this.addModified = true;
  }

  public Builder(String entityName, boolean addCreated, boolean addModified) {
    this.entityName = entityName;
    this.addCreated = addCreated;
    this.addModified = addModified;
  }

  public int persist() {
    checkRequiredParamteres();
    return this.insert();
  }

  public Builder set(Fields field, String value) {
    this.elements.get(field).value = value;
    return this;
  }

  public Builder set(Fields field, int value) {
    this.elements.get(field).value = Integer.toString(value);
    return this;
  }

  public Builder set(Fields field, boolean value) {
    this.elements.get(field).value = Boolean.toString(value);
    return this;
  }

  public Builder set(Fields field, long value) {
    this.elements.get(field).value = Long.toString(value);
    return this;
  }

  private int insert() {
    int entityId = IdProvider.getAndIncrement();

    List<String> params = new ArrayList<>();
    List<String> values = new ArrayList<>();

    for (Entry<Fields, Element> element : this.elements.entrySet()) {
      if (element.getValue().isOptionalAndEmpty()) {
        continue;
      }
      if (element.getValue().hasValue()) {
        values.add(element.getValue().value);
      } else if (element.getValue().hasDefaultValue()) {
        values.add(element.getValue().defaultValue);
      } else {
        throw new IllegalStateException("No value for parameter: " + element.getKey().name());
      }
      params.add(element.getKey().name());
    }
    params.add("id");
    values.add(Integer.toString(entityId));

    if (this.addCreated) {
      params.add("created");
      values.add("now()");
    }

    if (this.addModified) {
      params.add("modified");
      values.add("now()");
    }

    String paramString = params.stream().collect(Collectors.joining(","));
    String valueString = values.stream()
        .map(value -> "'" + value + "'")
        .collect(Collectors.joining(","));

    String query = String.format(INSERT_TEMPLATE, this.entityName, paramString, valueString);
    new DatabaseConnector().update(query);
    return entityId;
  }

  private void checkRequiredParamteres() {
    List<String> emptyRequiredValues = new ArrayList<>();
    this.elements.entrySet().stream()
        .filter(element -> element.getValue().required
            && element.getValue().value != null
            && element.getValue().value.isBlank()
            && element.getValue().defaultValue.isBlank())
        .map(Entry::getKey)
        .map(Fields::name)
        .forEach(emptyRequiredValues::add);

    if (!emptyRequiredValues.isEmpty()) {
      String emptyParams = emptyRequiredValues.stream()
          .collect(Collectors.joining(","));
      String errorMessage = String.format("Given required parameters are empty: %s.", emptyParams);
      throw new IllegalStateException(errorMessage);
    }
  }
}
