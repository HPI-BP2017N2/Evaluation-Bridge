package de.hpi.evaluationbridge.service;

import de.hpi.evaluationbridge.dto.IdealoOffers;
import de.hpi.evaluationbridge.dto.ShopIDToRootUrlResponse;
import de.hpi.evaluationbridge.properties.IdealoBridgeConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Component
public class IdealoBridge {

    private final RestTemplate oAuthRestTemplate;

    private final IdealoBridgeConfig properties;

    @Retryable(
            value = { HttpClientErrorException.class },
            backoff = @Backoff(delay = 3000, multiplier = 5))
    IdealoOffers getSampleOffers(long shopID, int maxOffers) {
        return getOAuthRestTemplate().getForObject(getSampleOffersURI(shopID, maxOffers), IdealoOffers.class);
    }

    @Retryable(
            value = { HttpClientErrorException.class },
            backoff = @Backoff(delay = 3000))
    public String resolveShopIDToRootUrl(long shopID) {
        return getOAuthRestTemplate().getForObject(getShopIDToRootUrlURI(shopID), ShopIDToRootUrlResponse.class).getShopUrl();
    }

    private URI getShopIDToRootUrlURI(long shopID) {
        return UriComponentsBuilder.fromUriString(getProperties().getApiUrl())
                .path(getProperties().getShopIDToRootUrlRoute() + shopID)
                .build()
                .encode()
                .toUri();
    }

    private URI getSampleOffersURI(long shopID, int maxOffers) {
        return UriComponentsBuilder.fromUriString(getProperties().getApiUrl())
                .path(getProperties().getSampleOffersRoute() + shopID)
                .queryParam("maxCount", maxOffers)
                .build()
                .encode()
                .toUri();
    }
}
