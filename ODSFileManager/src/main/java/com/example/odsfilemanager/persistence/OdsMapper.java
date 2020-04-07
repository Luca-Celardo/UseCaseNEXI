package com.example.odsfilemanager.persistence;

import com.example.odsfilemanager.model.ODS;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OdsMapper {

    @Select("select * from convertedOds")
    List<ODS> getAllConvertedOdsFiles();

    @Select("select * from readyOds")
    List<ODS> getAllReadyOdsFiles();

    @Select("select * from readyOds where id = #{id}")
    ODS getOdsById(Integer id);

    @Insert("insert into readyOds (id, name, type) values(#{id}, #{name}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertOdsFile(ODS ods);

    @Delete("delete from readyOds where id = #{id}")
    void deleteOdsById(Integer id);
}
