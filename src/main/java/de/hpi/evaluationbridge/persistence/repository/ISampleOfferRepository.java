package de.hpi.evaluationbridge.persistence.repository;

import de.hpi.evaluationbridge.persistence.SampleOffer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISampleOfferRepository extends MongoRepository<SampleOffer, Long> {

    Optional<SampleOffer> findByShopID(long shopID);
}
