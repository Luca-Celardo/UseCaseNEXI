package com.example.odsfilearchiver.persistence;

import com.example.odsfilearchiver.model.ODS;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OdsMapper {

    @Select("select * from readyOds")
    List<ODS> getAllReadyOdsFiles();

    @Select("select * from archivedOds")
    List<ODS> getAllArchivedOdsFiles();

    @Select("select * from archivedOds where id = #{id}")
    ODS getOdsById(Integer id);

    @Insert("insert into archivedOds (id, name, type) values(#{id}, #{name}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertOdsFile(ODS ods);

    @Delete("delete from archivedOds where id = #{id}")
    void deleteOdsById(Integer id);
}
