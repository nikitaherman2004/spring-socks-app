package com.socks.backend.dto;

import com.socks.backend.enums.SocksSortField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
public class SocksSortFiler extends SocksFilter {

    private Sort.Direction orderBy = Sort.Direction.ASC;

    private SocksSortField sortBy = SocksSortField.COLOR;
}
