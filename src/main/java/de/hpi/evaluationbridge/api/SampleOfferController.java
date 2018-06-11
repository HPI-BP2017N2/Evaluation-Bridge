package de.hpi.evaluationbridge.api;

import de.hpi.evaluationbridge.dto.EmptySuccessResponse;
import de.hpi.evaluationbridge.dto.ErrorResponse;
import de.hpi.evaluationbridge.dto.SuccessResponse;
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
    public HttpEntity<Object> getSampleOffers(@PathVariable long shopID) throws SampleOfferDoesNotExistException {
        return new SuccessResponse<>(getSampleOffersService().getStoredSampleOffer(shopID)).withMessage("Successfully" +
                " fetched sample offer for shop " + shopID).send();
    }

    @RequestMapping(value = "/fetchPage/{pageID}", method = RequestMethod.GET)
    public HttpEntity<Object> fetchPage(@PathVariable String pageID) throws PageNotFoundException {
        return new SuccessResponse<>(getSampleOffersService().fetchPage(pageID)).withMessage("Successfully" +
                " fetched page with id " + pageID).send();
    }

    @RequestMapping(value = "/rootUrl/{shopID}", method = RequestMethod.GET)
    public HttpEntity<Object> getShopRootUrl(@PathVariable long shopID) throws SampleOfferDoesNotExistException {
        return new SuccessResponse<>(getSampleOffersService().getShopRootUrl(shopID)).withMessage("Successfully" +
                " resolved shop root url for shop " + shopID).send();
    }

    @RequestMapping(value = "/cleanUrl/{shopID}", method = RequestMethod.GET)
    public HttpEntity<Object> cleanUrl(@PathVariable long shopID, @RequestParam String url) {
        return new SuccessResponse<>(url).withMessage("Successfully cleaned shop root url for shop " + shopID).send();
    }
}
