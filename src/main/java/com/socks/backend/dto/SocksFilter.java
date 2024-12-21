package com.socks.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SocksFilter {

    protected String color;

    protected Integer equalCount;

    protected Integer moreThenCount;

    protected Integer lessThenCount;

    protected Double moreCottonPercent;

    protected Double lessCottonPercent;

    protected Double equalCottonPercent;
}
