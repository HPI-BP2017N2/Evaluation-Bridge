package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;

public interface ISampleOffersService {

    void fetchSampleOffersForShop(long shopID, int offerCount) throws FetchProcessAlreadyRunningException;
}
