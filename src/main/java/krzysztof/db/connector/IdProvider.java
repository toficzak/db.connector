package krzysztof.db.connector;

public enum IdProvider {
  INSTANCE;

  private static int id = 1;

  public static int getAndIncrement() {
    return IdProvider.id++;
  }
}
