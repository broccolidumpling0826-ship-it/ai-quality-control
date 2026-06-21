package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcStandardDocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcStandardDocumentChunkMapper extends BaseMapper<QcStandardDocumentChunk> {

    @Select("<script>"
            + "SELECT * FROM qc_standard_document_chunk "
            + "WHERE MATCH(chunk_text, keyword_tags) AGAINST(#{query} IN NATURAL LANGUAGE MODE) "
            + "<if test='standardId != null and standardId != \"\"'> AND standard_id = #{standardId} </if>"
            + " LIMIT #{limit}"
            + "</script>")
    List<QcStandardDocumentChunk> fulltextSearch(@Param("query") String query,
                                                 @Param("standardId") String standardId,
                                                 @Param("limit") int limit);

    @Select("<script>"
            + "SELECT * FROM qc_standard_document_chunk "
            + "WHERE (chunk_text LIKE CONCAT('%', #{query}, '%') OR keyword_tags LIKE CONCAT('%', #{query}, '%')) "
            + "<if test='standardId != null and standardId != \"\"'> AND standard_id = #{standardId} </if>"
            + " LIMIT #{limit}"
            + "</script>")
    List<QcStandardDocumentChunk> keywordFallbackSearch(@Param("query") String query,
                                                        @Param("standardId") String standardId,
                                                        @Param("limit") int limit);
}
