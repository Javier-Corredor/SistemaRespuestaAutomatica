package co.edu.uptc.RespuestaAutomatica.Service;

import co.edu.uptc.RespuestaAutomatica.Entities.QuestionEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaqService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FaqService.class);

    @Value("${faq.file.path:./data/faqs.json}")
    private String faqFilePath;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Object lock = new Object();

    @PostConstruct
    public void init() throws IOException {
        File f = new File(faqFilePath);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (!f.exists()) {
            // crear archivo vacío con lista vacía
            mapper.writeValue(f, new ArrayList<QuestionEntity>());
        }
    }

    private List<QuestionEntity> readAll() throws IOException {
        synchronized (lock) {
            File f = new File(faqFilePath);
            if (!f.exists()) return new ArrayList<>();
            byte[] bytes = Files.readAllBytes(f.toPath());
            if (bytes.length == 0) return new ArrayList<>();
            return mapper.readValue(bytes, new TypeReference<List<QuestionEntity>>(){});
        }
    }

    private void writeAll(List<QuestionEntity> list) throws IOException {
        synchronized (lock) {
            File f = new File(faqFilePath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, list);
        }
    }

    @Cacheable("faq_categories")
    public List<String> getCategories() {
        logger.debug("Obteniendo categorías de FAQ");
        List<String> cats = new ArrayList<>();
        cats.add("Académica");
        cats.add("Inscripciones");
        cats.add("Investigación");
        cats.add("Otros");
        return cats;
    }

    private boolean isValidCategory(String categoria) {
        if (categoria == null) return false;
        return getCategories().stream().anyMatch(c -> c.equalsIgnoreCase(categoria.trim()));
    }
    @Cacheable(value = "faqs_by_category", key = "#categoria != null ? #categoria.toLowerCase() : 'null'")
    public List<QuestionEntity> getByCategory(String categoria) throws IOException {
        if (!isValidCategory(categoria)) {
            throw new IllegalArgumentException("Categoria inválida. Categorías válidas: " + String.join(", ", getCategories()));
        }
        logger.debug("Leyendo preguntas de la categoría '{}'", categoria);
        List<QuestionEntity> all = readAll();
        return all.stream()
                .filter(q -> q.getCategoria() != null && q.getCategoria().equalsIgnoreCase(categoria.trim()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "faqs_search", key = "(#categoria != null ? #categoria.toLowerCase() : 'null') + '|' + (#keyword != null ? #keyword.toLowerCase() : '')")
    public List<QuestionEntity> search(String categoria, String keyword) throws IOException {
        if (!isValidCategory(categoria)) {
            throw new IllegalArgumentException("Categoria inválida. Categorías válidas: " + String.join(", ", getCategories()));
        }
        String k = keyword == null ? "" : keyword.toLowerCase();
        logger.debug("Buscando en categoría '{}' la palabra '{}'", categoria, k);
        return getByCategory(categoria).stream()
            .filter(q -> (q.getPregunta() != null && q.getPregunta().toLowerCase().contains(k))
                || (q.getRespuesta() != null && q.getRespuesta().toLowerCase().contains(k)))
            .collect(Collectors.toList());
    }

    @CacheEvict(value = {"faqs_by_category", "faqs_search", "faq_categories"}, allEntries = true)
    public QuestionEntity addQuestion(QuestionEntity q) throws IOException {
        if (q.getPregunta() == null || q.getPregunta().isBlank()) {
            throw new IllegalArgumentException("La pregunta es obligatoria");
        }
        if (q.getCategoria() == null || q.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        if (!isValidCategory(q.getCategoria())) {
            throw new IllegalArgumentException("Categoria inválida. Categorías válidas: " + String.join(", ", getCategories()));
        }
        logger.info("Añadiendo nueva pregunta en categoria '{}'", q.getCategoria());
        List<QuestionEntity> all = readAll();
        Optional<Integer> maxId = all.stream().map(QuestionEntity::getId).filter(id->id!=null).max(Comparator.naturalOrder());
        int nextId = maxId.map(i->i+1).orElse(1);
        q.setId(nextId);
        if (q.getRespuesta() == null) q.setRespuesta("");
        all.add(q);
        writeAll(all);
        return q;
    }

    public List<QuestionEntity> getUnanswered() throws IOException {
        List<QuestionEntity> all = readAll();
        return all.stream().filter(q -> q.getRespuesta() == null || q.getRespuesta().isBlank()).collect(Collectors.toList());
    }

    @CacheEvict(value = {"faqs_by_category", "faqs_search"}, allEntries = true)
    public QuestionEntity answerQuestion(Integer id, String respuesta) throws IOException {
        if (id == null) throw new IllegalArgumentException("Id es requerido");
        List<QuestionEntity> all = readAll();
        for (QuestionEntity q : all) {
            if (q.getId() != null && q.getId().equals(id)) {
                q.setRespuesta(respuesta == null ? "" : respuesta);
                writeAll(all);
                logger.info("Pregunta id={} respondida", id);
                return q;
            }
        }
        throw new IllegalArgumentException("Pregunta no encontrada");
    }
}
