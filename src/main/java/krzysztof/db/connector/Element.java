package krzysztof.db.connector;

public class Element {
  public String value;
  public String defaultValue;
  public boolean required;

  public static Element optionalValue() {
    return new Element(false);
  }

  public static Element requiredValue() {
    return new Element(true);
  }

  public static Element optionalValueWithDefault(String defaultValue) {
    return new Element(false, defaultValue);
  }

  public static Element requiredValueWithDefault(String defaultValue) {
    return new Element(true, defaultValue);
  }

  public boolean isOptionalAndEmpty() {
    return !this.required && (!this.hasValue() || this.hasDefaultValue());
  }

  public boolean hasValue() {
    return this.value != null && !this.value.isBlank();
  }

  public boolean hasDefaultValue() {
    return this.defaultValue != null && !this.defaultValue.isBlank();
  }

  private Element(boolean required) {
    super();
    this.required = required;
  }

  private Element(boolean required, String defaultValue) {
    super();
    this.required = required;
    this.defaultValue = defaultValue;
  }
}
