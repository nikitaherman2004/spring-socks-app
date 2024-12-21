package com.socks.backend.mapper;

import com.socks.backend.dto.SocksDto;
import com.socks.backend.entity.Socks;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SocksMapper {

    SocksMapper INSTANCE = Mappers.getMapper(SocksMapper.class);

    SocksDto toDto(Socks socks);

    Socks toEntity(SocksDto socksDto);
}
