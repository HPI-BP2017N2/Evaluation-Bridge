package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.dto.IdealoOffer;
import de.hpi.evaluationbridge.dto.IdealoOffers;
import de.hpi.evaluationbridge.exception.NotEnoughOffersException;
import de.hpi.evaluationbridge.persistence.SampleOffer;
import de.hpi.evaluationbridge.persistence.SamplePage;
import de.hpi.evaluationbridge.persistence.repository.ISampleOfferRepository;
import de.hpi.evaluationbridge.persistence.repository.ISamplePageRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class SampleOfferService implements ISampleOfferService {

    private final IdealoBridge idealoBridge;

    private final ISampleOfferRepository sampleOfferRepository;

    private final ISamplePageRepository samplePageRepository;

    private final Set<Long> fetchProcesses = new CopyOnWriteArraySet<>();

    @Override
    public boolean isAlreadyFetchingShop(long shopID) {
        return getFetchProcesses().contains(shopID);
    }

    @Override
    @Async("fetchSampleOffersThreadPool")
    public void fetchSampleOffersForShop(long shopID, int offerCount) {
        if (isAlreadyFetchingShop(shopID)) return;
        getFetchProcesses().add(shopID);
        log.info("Start fetching process for shop " + shopID + " and " + offerCount + " offer(s)");

        //fetch twice as much offers because some url might be unreachable
        IdealoOffers offers = getIdealoBridge().getSampleOffers(shopID, offerCount * 2);
        try {
            fetchPages(offers, offerCount, shopID);
            storeSamplePagesAndUpdateUrl(offers);
            getSampleOfferRepository().save(new SampleOffer(
                    offers,
                    shopID,
                    getIdealoBridge().resolveShopIDToRootUrl(shopID)));
        } catch (NotEnoughOffersException e) {
            log.error("Error fetching pages", e);
        }

        getFetchProcesses().remove(shopID);
    }

    private void storeSamplePagesAndUpdateUrl(IdealoOffers offers) {
        offers.forEach(offer -> {
            SamplePage page = getSamplePageRepository().save(new SamplePage(offer.getFetchedPage().html()));
            offer.get(OfferAttribute.URL).clear();
            offer.get(OfferAttribute.URL).add(page.getId());
        });
    }

    private void fetchPages(IdealoOffers offers, int targetOfferCount, long shopID) throws NotEnoughOffersException {
        int offerCounter = 0;
        for (Iterator<IdealoOffer> iterator = offers.iterator(); iterator.hasNext();) {
            IdealoOffer offer = iterator.next();
            if (offerCounter == targetOfferCount) {
                iterator.remove();
            } else {
                fetchHTMLForOffer(offer);
                if (offer.getFetchedPage() == null) iterator.remove();
                else {
                    offerCounter++;
                    log.info("Fetched page " + offerCounter + " of " + targetOfferCount + " for shop " + shopID);
                }
            }
            if (iterator.hasNext() && offerCounter < targetOfferCount) sleep(10000L);
        }
        if (offerCounter < targetOfferCount) throw new NotEnoughOffersException("Could not fetch as many offers as " +
                "requested!");
    }

    private void fetchHTMLForOffer(IdealoOffer offer) {
        List<String> urls = offer.get(OfferAttribute.URL);
        if (urls == null || urls.isEmpty()) return;
        try {
            Document fetchedPage = Jsoup
                    .connect(urls.get(0))
                    .userAgent("Mozilla/5.0 (compatible; HPI-BPN2-2017/2.1; https://hpi.de/naumann/teaching/bachelorprojekte/inventory-management.html)")
                    .get();
            offer.setFetchedPage(fetchedPage);
        } catch (IOException e) {
            log.error("Could not fetch page " + urls.get(0), e);
        }

    }

    private void sleep(long delayInMilliseconds) {
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException ignore) { /* we dont care */ }
    }
}
