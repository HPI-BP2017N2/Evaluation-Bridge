package de.hpi.evaluationbridge.api;

import de.hpi.evaluationbridge.dto.*;
import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;
import de.hpi.evaluationbridge.exception.PageNotFoundException;
import de.hpi.evaluationbridge.exception.SampleOfferDoesNotExistException;
import de.hpi.evaluationbridge.service.ISampleOfferService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SampleOfferController {

    private final ISampleOfferService sampleOffersService;

    @RequestMapping(value = "/storeSampleOffers", method = RequestMethod.POST, produces = "application/json")
    public HttpEntity<Object> storeSampleOffers(@RequestBody List<Long> shops, @RequestParam int offerCount) {
        StringBuilder errors = new StringBuilder();
        shops.forEach(shopID -> {
            try {
                storeSampleOffers(shopID, offerCount);
            } catch (FetchProcessAlreadyRunningException e) {
                errors.append(e.getMessage()).append("\n");
            }
        });
        if (errors.length() > 0)
            return new ErrorResponse().withMessage("Error starting fetch process: \n" + errors.toString()).send
                    (HttpStatus.BAD_REQUEST);
        return new EmptySuccessResponse().withMessage("Fetch process startet for every shop!").send();

    }

    @RequestMapping(value = "/storeSampleOffers/{shopID}", method = RequestMethod.POST, produces = "application/json")
    public HttpEntity<Object> storeSampleOffers(
            @PathVariable long shopID,
            @RequestParam int offerCount) throws FetchProcessAlreadyRunningException {
        if (getSampleOffersService().isAlreadyFetchingShop(shopID))
            throw new FetchProcessAlreadyRunningException("Fetch process for shop " + shopID + " is already running.");
        getSampleOffersService().fetchSampleOffersForShop(shopID, offerCount);
        return new EmptySuccessResponse().withMessage("Triggered fetching sample offers for shop: " + shopID).send();
    }

    @RequestMapping(value = "/sampleOffers/{shopID}", method = RequestMethod.GET, produces = "application/json")
    public IdealoOffers getSampleOffers(@PathVariable long shopID) throws SampleOfferDoesNotExistException {
        return getSampleOffersService().getStoredSampleOffer(shopID).getIdealoOffers();
    }

    @RequestMapping(value = "/fetchPage/{pageID}", method = RequestMethod.GET)
    public String fetchPage(@PathVariable String pageID) throws PageNotFoundException {
        return getSampleOffersService().fetchPage(pageID);
    }

    @RequestMapping(value = "/rootUrl/{shopID}", method = RequestMethod.GET)
    public HttpEntity<Object> getShopRootUrl(@PathVariable long shopID) throws SampleOfferDoesNotExistException {
        String shopRootUrl = getSampleOffersService().getShopRootUrl(shopID);
        ShopRootUrlResponse response = new ShopRootUrlResponse(0L, shopID, "", shopRootUrl);
        return new SuccessResponse<>(response).withMessage("Successfully" +
                " resolved shop root url for shop " + shopID).send();
    }

    @RequestMapping(value = "/cleanUrl/{shopID}", method = RequestMethod.GET)
    public HttpEntity<Object> cleanUrl(@PathVariable long shopID, @RequestParam String url) {
        return new SuccessResponse<>(new CleanUrlResponse(url)).withMessage("Successfully cleaned shop root url for " +
                "shop " + shopID).send();
    }
}
