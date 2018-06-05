package de.hpi.evaluationbridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdealoOffer {

    @Transient
    private Document fetchedPage;

    private Property<String> ean, sku, han, brandName;

    private Property<Map<String, String>> titles, descriptions, urls;

    private Property<String[]> categoryPaths;

    private Property<Map<String, Integer>> prices;

    private Property<Map<String, List<String>>> imageUrls;

}
