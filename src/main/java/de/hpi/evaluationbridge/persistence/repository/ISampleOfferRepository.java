package de.hpi.evaluationbridge.persistence.repository;

import de.hpi.evaluationbridge.persistence.SampleOffer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISampleOfferRepository extends MongoRepository<SampleOffer, Long> {

    SampleOffer findByShopID(long shopID);
}
