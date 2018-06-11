package de.hpi.evaluationbridge.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class CleanUrlResponse {

    private final String url;
}
