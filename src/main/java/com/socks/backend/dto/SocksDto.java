package com.socks.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocksDto {

    private Long id;

    private String color;

    private Integer count;

    private Double cottonPercent;
}
