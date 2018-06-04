package de.hpi.evaluationbridge.persistence;

import de.hpi.evaluationbridge.dto.IdealoOffers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sampleOffers")
@Getter
@RequiredArgsConstructor
@ToString
public class SampleOffer {

    private final IdealoOffers idealoOffers;

    @Id
    private final long shopID;

    private final String shopRootUrl;
}
