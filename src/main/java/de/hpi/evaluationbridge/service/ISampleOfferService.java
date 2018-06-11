package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;
import de.hpi.evaluationbridge.exception.PageNotFoundException;
import de.hpi.evaluationbridge.exception.SampleOfferDoesNotExistException;
import de.hpi.evaluationbridge.persistence.SampleOffer;

public interface ISampleOfferService {

    boolean isAlreadyFetchingShop(long shopID);

    void fetchSampleOffersForShop(long shopID, int offerCount) throws FetchProcessAlreadyRunningException;

    SampleOffer getStoredSampleOffer(long shopID) throws SampleOfferDoesNotExistException;

    String fetchPage(String pageID) throws PageNotFoundException;

    String getShopRootUrl(long shopID) throws SampleOfferDoesNotExistException;
}
