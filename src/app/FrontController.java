package app;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import app.controller.DocumentoController;
import app.http.Request;
import app.http.Response;
import app.metadata.Controller;
import app.metadata.Path;

public class FrontController implements HttpHandler {

  private static Logger log = Logger.getLogger(FrontController.class.getName());

  private static class Pair {

    Class<?> controller;
    Map<String, Method> metodos;

  }

  private final HttpServer server;
  // documentos => DocumentosController => listar => método lista
  private Map<String, Pair> routing = new HashMap<>();


  public FrontController(HttpServer server) {
    log.info("Instanciando o FrontController");
    this.server = server;
    log.info("Criando o contexto /");
    this.server.createContext("/", this);
  }

  @Override // TODO: roteamento
  public void handle(HttpExchange exchange) throws IOException {
    //   /teste => ["", "teste"]
    //   /documentos/listar => ["", "documentos", "listar"]
    String path = exchange.getRequestURI().getPath()
      .substring(1);

    log.info("Atendendo requisição para " + path);

    List<String> split = Arrays.asList(path.split("/"));

    log.info("Requisição " + split);

    try (OutputStream out = exchange.getResponseBody()) {
      if (split.size() < 2) {

        log.warning("Rota não registrada");
        exchange.sendResponseHeaders(404, 0);

      } else {
        String rota = split.get(0); // documentos
        String metodo = split.get(1); // listar

        Pair pair = routing.get(rota);

        log.info("Pair " + pair);

        if (pair == null) {
          log.warning("Rota não registrada");
          exchange.sendResponseHeaders(404, 0);
        } else {

          Method m = pair.metodos.get(metodo); // listar?
        
          if (m == null) {
            log.warning("Rota não registrada");
            exchange.sendResponseHeaders(404, 0);
          } else {
            log.info("Método localizado " + m);
            try {
              Object objeto = pair.controller
                    .getDeclaredConstructor()
                    .newInstance(); // new DocumentoController
              Request req = new Request();

              String query = exchange.getRequestURI().getQuery(); // ?---->
              log.info("Query " + query);
              // req.setQueryParams(queryParams); // ?
              if (query != null) {
                String[] chaveValor = query.split("=");
                if (chaveValor.length >= 2)
                req.setQueryParams(Map.of(chaveValor[0], chaveValor[1]));
              }

              Response resp = (Response) m.invoke(objeto, req);
              String body = resp.getBody();

              log.info("Body da resposta " + body);
              log.info("Body length " + body.length());

              byte[] bytes = body.getBytes();

              int status = resp.getStatus();
              resp.getHeaders().forEach((key, value) -> { // de/para
                exchange.getResponseHeaders().add(key, value);
              });
              exchange.sendResponseHeaders(status, bytes.length);
              out.write(bytes);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
      out.flush();
    }
  }

  public void register(Class<?> clazz) {

    log.info("Tentando registrar " + clazz.getName());

    if (clazz.isAnnotationPresent(Controller.class)) {

      log.info("Registrando a classe " + clazz);
      
      String rota = clazz.getAnnotation(Controller.class).value();
      
      log.info("Registrando o endpoint " + rota);
      
      Map<String, Method> metodos = new HashMap<>();
      
      for (Method metodo : clazz.getMethods()) {
        if (metodo.isAnnotationPresent(Path.class)) {

          String caminho = metodo.getDeclaredAnnotation(Path.class).value();

          log.info("Registrando o método " + metodo.getName() + " para o caminho " + caminho);

          metodos.put(caminho, metodo); // "listar" => Método lista()
        }
      }

      Pair pair = new Pair();
      pair.controller = clazz;
      pair.metodos = metodos;

      // "documentos"=>DocumentoController
      //       // "listar" => método lista()
      this.routing.put(rota, pair);
    } else {
      log.warning("A classe " + clazz + " não possui annotation Controller");
    }
  }
}
