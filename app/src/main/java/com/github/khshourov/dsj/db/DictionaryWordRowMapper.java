package com.github.khshourov.dsj.db;

import com.github.khshourov.dsj.models.DictionaryWord;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class DictionaryWordRowMapper implements RowMapper<DictionaryWord> {
  private static final String COL_ID = "id";
  private static final String COL_SOURCE = "source";
  private static final String COL_WORD = "word";
  private static final String COL_LEXICAL_ENTRY = "lexical_entry";
  private static final String COL_STATUS = "status";

  @Override
  public DictionaryWord mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new DictionaryWord(
        rs.getLong(COL_ID),
        rs.getString(COL_SOURCE),
        rs.getString(COL_WORD),
        rs.getString(COL_LEXICAL_ENTRY),
        // We enforce status check in database
        StatusType.valueOf(rs.getString(COL_STATUS)));
  }
}
