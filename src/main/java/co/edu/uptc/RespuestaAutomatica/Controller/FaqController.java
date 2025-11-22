package co.edu.uptc.RespuestaAutomatica.Controller;

import co.edu.uptc.RespuestaAutomatica.Entities.QuestionEntity;
import co.edu.uptc.RespuestaAutomatica.Service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/faq")
@Tag(name = "FAQ Controller", description = "Preguntas frecuentes por categoría")
public class FaqController {

    @Autowired
    private FaqService faqService;

    @Operation(summary = "Listar categorías")
    @GetMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> categories() {
        return faqService.getCategories();
    }

    @Operation(summary = "Obtener preguntas por categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de preguntas"),
        @ApiResponse(responseCode = "400", description = "Categoria inválida")
    })
    @GetMapping(path = "/{categoria}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuestionEntity> byCategory(@PathVariable String categoria) throws IOException {
        return faqService.getByCategory(categoria);
    }

    @Operation(summary = "Buscar dentro de una categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultados de búsqueda"),
        @ApiResponse(responseCode = "400", description = "Categoria inválida")
    })
    @GetMapping(path = "/{categoria}/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuestionEntity> search(@PathVariable String categoria, @RequestParam(required = false) String q) throws IOException {
        return faqService.search(categoria, q);
    }

    @Operation(summary = "Añadir pregunta (usuario)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pregunta creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public QuestionEntity addQuestion(@RequestBody QuestionEntity q) throws IOException {
        return faqService.addQuestion(q);
    }

    @Operation(summary = "Listar preguntas sin responder (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de preguntas sin responder"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping(path = "/unanswered", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public List<QuestionEntity> unanswered() throws IOException {
        return faqService.getUnanswered();
    }

    @Operation(summary = "Responder una pregunta (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pregunta respondida"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PostMapping(path = "/{id}/answer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionEntity answer(@PathVariable Integer id, @RequestBody(required = true) java.util.Map<String,String> body) throws IOException {
        String respuesta = body.get("respuesta");
        return faqService.answerQuestion(id, respuesta);
    }
}
