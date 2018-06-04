package de.hpi.evaluationbridge.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "samplePages")
@Getter
@RequiredArgsConstructor
public class SamplePage {

    @Id
    private final String id;

    private final String html;

    public SamplePage(String html) {
        this.html = html;
        this.id = null;
    }
}
