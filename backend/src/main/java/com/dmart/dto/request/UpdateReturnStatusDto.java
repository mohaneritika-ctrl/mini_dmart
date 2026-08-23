package com.dmart.dto.request;

import com.dmart.entity.ReturnStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReturnStatusDto {

    @NotNull(message = "Status is required")
    private ReturnStatus status;

    private String staffComment;
}