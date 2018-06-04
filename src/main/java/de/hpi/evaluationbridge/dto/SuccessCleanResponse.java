package de.hpi.evaluationbridge.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
class SuccessCleanResponse extends SuccessResponse<SuccessCleanResponse.CleanResponse> {

    @SuppressWarnings("WeakerAccess")
    @Getter
    @Setter
    public static class CleanResponse {
        private String url;
    }
}
