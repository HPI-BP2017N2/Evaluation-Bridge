package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;

public interface ISampleOfferService {

    boolean isAlreadyFetchingShop(long shopID);

    void fetchSampleOffersForShop(long shopID, int offerCount) throws FetchProcessAlreadyRunningException;
}
