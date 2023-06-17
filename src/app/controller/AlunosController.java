package app.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import app.http.Request;
import app.http.Response;
import app.metadata.Controller;
import app.metadata.Path;

@Controller("alunos")
public class AlunosController {
  
  private List<Aluno> alunos;

  public AlunosController() {
    Aluno a1 = new Aluno(123, "Victor Pithan");
    Aluno a2 = new Aluno(456, "Milena Henriques");
    Aluno a3 = new Aluno(789, "Geraldo Júnior"); // UTF-16
    alunos = Arrays.asList(a1, a2, a3);
  }

  @Path("todos")
  public Response listar(Request request) {
    String body = alunos.stream().map(a ->
          a.matricula() + ": " + a.nome()).collect(Collectors.joining(";"));

    return Response.of(200, body, Map.of("Content-Type", "text/plain; charset=UTF-8"));
  }

  @Path("matricula")
  public Response getMatricula(Request request) {
    Optional<String> body = alunos.stream()
    .filter(a -> a.matricula().toString().equals(request.getQueryParam("id").get()))
    .map(a -> a.matricula() + ": " + a.nome())
    .findAny();

    if (body.isPresent()) {
      return Response.of(200, body.get(), Map.of("Content-Type", "text/plain; charset=UTF-8"));
    } else {
      return Response.of(404, "", Map.of("Content-Type", "text/plain; charset=UTF-8"));
    }
  }

}
