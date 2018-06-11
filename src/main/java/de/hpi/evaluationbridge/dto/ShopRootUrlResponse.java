package de.hpi.evaluationbridge.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShopRootUrlResponse {

    private final long oracleShopId;

    private final long shopId;

    private final String shopName;

    private final String shopUrl;
}
