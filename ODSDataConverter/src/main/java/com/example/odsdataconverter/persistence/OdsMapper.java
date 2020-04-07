package com.example.odsdataconverter.persistence;

import com.example.odsdataconverter.model.ODS;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OdsMapper {

    @Select("select * from convertedOds")
    List<ODS> getAllOdsFiles();

    @Select("select * from convertedOds where id = #{id}")
    ODS getOdsById(Integer id);

    @Insert("insert into convertedOds (id, name, type) values(#{id}, #{name}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertOdsFile(ODS ods);

    @Delete("delete from convertedOds where id = #{id}")
    void deleteOdsById(Integer id);
}
