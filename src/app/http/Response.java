package app.http;

import java.util.Map;

public class Response { // Value Object

  public static Response of(int status, String body) {
    return of(status, body, Map.of());
  }

  public static Response of(int status, String body, Map<String, String> headers) {
    return new Response(status, body, headers);
  }

  private final String body;
  private final int status;
  private final Map<String, String> headers;

  private Response(int status, String body, Map<String, String> headers) {
    this.body = body;
    this.status = status;
    this.headers = headers;
  }

  public String getBody() {
    return body;
  }

  public int getStatus() {
    return status;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
}
