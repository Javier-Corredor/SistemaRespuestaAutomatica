package co.edu.uptc.RespuestaAutomatica.Entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa una pregunta frecuente (FAQ)")
public class QuestionEntity {
    @Schema(description = "Identificador único de la pregunta", example = "1")
    private Integer id;
    @Schema(description = "Texto de la pregunta", example = "¿Cuál es el horario de clases?")
    private String pregunta;
    @Schema(description = "Respuesta a la pregunta (vacía si sin responder)", example = "El horario es de 7:00 a 9:00")
    private String respuesta;
    @Schema(description = "Categoría de la pregunta (Académica, Inscripciones, Investigación, Otros)", example = "Académica")
    private String categoria;

    public QuestionEntity() {}

    public QuestionEntity(Integer id, String pregunta, String respuesta, String categoria) {
        this.id = id;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.categoria = categoria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
