package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.dto.IdealoOffer;
import de.hpi.evaluationbridge.dto.IdealoOffers;
import de.hpi.evaluationbridge.exception.NotEnoughOffersException;
import de.hpi.evaluationbridge.exception.PageNotFoundException;
import de.hpi.evaluationbridge.exception.SampleOfferDoesNotExistException;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.util.*;
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

        //fetch twice as much offers because some urls might be unreachable
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

    @Override
    public SampleOffer getStoredSampleOffer(long shopID) throws SampleOfferDoesNotExistException {
        Optional<SampleOffer> sampleOffer = getSampleOfferRepository().findByShopID(shopID);
        return sampleOffer.orElseThrow(() -> new SampleOfferDoesNotExistException("No sample offer for shop " + shopID));
    }

    @Override
    @Cacheable(value = "pages")
    public String fetchPage(String pageID) throws PageNotFoundException {
        Optional<SamplePage> samplePage = getSamplePageRepository().findById(pageID);
        return samplePage.orElseThrow(() -> new PageNotFoundException("Page with id " + pageID + " does not exists.")).getHtml();
    }

    @Override
    @Cacheable(value = "rootUrls")
    public String getShopRootUrl(long shopID) throws SampleOfferDoesNotExistException {
        Optional<SampleOffer> sampleOffer = getSampleOfferRepository().findByShopID(shopID);
        return sampleOffer.orElseThrow(() -> new SampleOfferDoesNotExistException("No sample offer for shop " +
                shopID)).getShopRootUrl();
    }

    private void storeSamplePagesAndUpdateUrl(IdealoOffers offers) {
        offers.forEach(offer -> {
            SamplePage page = getSamplePageRepository().save(new SamplePage(offer.getFetchedPage().html()));
            offer.getUrls().getValue().clear();
            offer.getUrls().getValue().put("0", "http://localhost:5221/fetchPage/" + page.getId());
        });
    }

    private void fetchPages(IdealoOffers offers, int targetOfferCount, long shopID) throws NotEnoughOffersException {
        int offerCounter = 0;
        for (Iterator<IdealoOffer> iterator = offers.iterator(); iterator.hasNext();) {
            IdealoOffer offer = iterator.next();
            if (offerCounter == targetOfferCount) {
                iterator.remove();
            } else {
                try {
                    fetchHTMLForOffer(offer);
                } catch (SSLHandshakeException e) {
                    throw new NotEnoughOffersException("Can not fetch any site of shop " + shopID);
                }
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

    private void fetchHTMLForOffer(IdealoOffer offer) throws SSLHandshakeException {
        List<String> urls = new LinkedList<>(offer.getUrls().getValue().values());
        if (urls.isEmpty()) return;
        try {
            Document fetchedPage = Jsoup
                    .connect(urls.get(0))
                    .userAgent("Mozilla/5.0 (compatible; HPI-BPN2-2017/2.1; https://hpi.de/naumann/teaching/bachelorprojekte/inventory-management.html)")
                    .get();
            offer.setFetchedPage(fetchedPage);
        }catch (SSLHandshakeException e) {
            throw e;
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
