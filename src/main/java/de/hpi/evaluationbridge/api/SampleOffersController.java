package de.hpi.evaluationbridge.api;

import de.hpi.evaluationbridge.dto.EmptySuccessResponse;
import de.hpi.evaluationbridge.exception.FetchProcessAlreadyRunningException;
import de.hpi.evaluationbridge.service.ISampleOffersService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SampleOffersController {

    private final ISampleOffersService sampleOffersService;

    @RequestMapping(value = "/storeSampleOffers/{shopID}", method = RequestMethod.POST, produces = "application/json")
    public HttpEntity<Object> storeSampleOffers(
            @PathVariable long shopID,
            @RequestParam int offerCount) throws FetchProcessAlreadyRunningException {
        getSampleOffersService().fetchSampleOffersForShop(shopID, offerCount);
        return new EmptySuccessResponse().withMessage("Triggered fetching sample offers for shop: " + shopID).send();
    }
}
