import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

public class DocumentManager {
    private final Map<String, Document> storage = new HashMap<>();
    private int idCounter = 0;

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            String id = "doc-" + idCounter++;
            document.setId(id);
        }
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> matchedDocuments = new ArrayList<>(storage.values());

        if (request.getTitlePrefixes() != null) {
            matchedDocuments.removeIf(doc -> !request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> doc.getTitle() != null && doc.getTitle().startsWith(prefix)));
        }

        if (request.getContainsContents() != null) {
            matchedDocuments.removeIf(doc -> !request.getContainsContents().stream()
                    .anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content)));
        }

        if (request.getAuthorIds() != null) {
            matchedDocuments.removeIf(doc -> doc.getAuthor() == null ||
                    !request.getAuthorIds().contains(doc.getAuthor().getId()));
        }

        if (request.getCreatedFrom() != null) {
            matchedDocuments.removeIf(doc -> doc.getCreated() == null ||
                    doc.getCreated().isBefore(request.getCreatedFrom()));
        }

        if (request.getCreatedTo() != null) {
            matchedDocuments.removeIf(doc -> doc.getCreated() == null ||
                    doc.getCreated().isAfter(request.getCreatedTo()));
        }

        return matchedDocuments;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}