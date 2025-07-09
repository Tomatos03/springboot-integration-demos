package com.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : Tomatos
 * @date : 2025/7/9
 */
@Schema(description = "This is description of SecondDTO")
@AllArgsConstructor
@Getter
@Setter
public class SecondDTO {
    @Schema(description = "Test filed 0")
    private int filed0;
    @Schema(description = "Test filed 1")
    private Integer filed1;
}
