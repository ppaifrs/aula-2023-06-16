package app.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Request {

  private Map<String, String> queryParams = new HashMap<>();

  public void setQueryParams(Map<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  public Optional<String> getQueryParam(String key) {
    return Optional.ofNullable(queryParams.get(key));
  }
  
}
