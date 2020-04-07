package com.example.odsdatamartextractor.persistence;

import com.example.odsdatamartextractor.model.DataMart;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DataMartMapper {

    @Select("select * from datamarts")
    List<DataMart> getAllDataMarts();

    @Select("select * from datamarts where id = #{id}")
    DataMart getDataMartById(Integer id);

    @Insert("insert into datamarts (id, interfaceId, type) values(#{id}, #{interfaceId}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertDataMart(DataMart dataMart);

    @Delete("delete from datamarts where id = #{id}")
    void deleteDataMartById(Integer id);
}
