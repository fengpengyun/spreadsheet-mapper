package spread.sheet.o2w.composer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import spread.sheet.model.core.SheetList;
import spread.sheet.model.core.*;
import spread.sheet.model.meta.FieldMeta;
import spread.sheet.model.meta.HeaderMeta;
import spread.sheet.model.meta.SheetMeta;
import spread.sheet.o2w.extractor.BeanUtilValueExtractor;
import spread.sheet.o2w.extractor.FieldValueExtractor;
import spread.sheet.o2w.extractor.ValueExtractor;
import spread.sheet.w2o.processor.WorkbookProcessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hanwen on 15-12-16.
 */
public class DefaultSheetComposer implements SheetComposer {

  private SheetMeta sheetMeta;

  private SheetList<Object> data;

  private Map<String, FieldValueExtractor> key2fieldValueExtractor = new HashMap<>();

  private ValueExtractor defaultValueExtractor = new BeanUtilValueExtractor();

  @Override
  public SheetComposer fieldValueExtractor(FieldValueExtractor... fieldValueExtractors) {
    if (fieldValueExtractors == null) {
      return this;
    }
    for (FieldValueExtractor extractor : fieldValueExtractors) {
      key2fieldValueExtractor.put(extractor.getMatchField(), extractor);
    }
    return this;
  }

  @Override
  public SheetComposer sheetMeta(SheetMeta sheetMeta) {
    this.sheetMeta = sheetMeta;
    return this;
  }

  @Override
  public SheetComposer data(SheetList<Object> data) {
    this.data = data;
    return this;
  }

  @Override
  public Sheet compose() {
    if (sheetMeta == null) {
      throw new WorkbookComposeException("set sheet meta first");
    }

    Sheet sheet = createSheet(sheetMeta);

    int dataStartRowIndex = sheetMeta.getDataStartRowIndex();
    for (int i = 1; i < dataStartRowIndex; i++) {

      Row row = createRow(i);
      sheet.addRow(row);
      createHeaderCellsIfNecessary(row, sheetMeta);
    }

    if (CollectionUtils.isEmpty(data)) {
      return sheet;
    }

    if (data.getSheetIndex() != sheetMeta.getSheetIndex()) {
      throw new WorkbookProcessException("data[sheet index:" + data.getSheetIndex() + "] not corresponding the sheet meta[sheet index:" + sheetMeta.getSheetIndex() + "]");
    }

    for (int i = 0; i < data.size(); i++) {

      Row row = createRow(i + dataStartRowIndex);
      sheet.addRow(row);
      createDataCells(row, data.get(i), sheetMeta);
    }

    return sheet;
  }

  private Sheet createSheet(SheetMeta sheetMeta) {
    int sheetIndex = sheetMeta.getSheetIndex();
    String sheetName = sheetMeta.getSheetName();

    if (StringUtils.isBlank(sheetName)) {
      return new SheetBean(sheetIndex);
    }
    return new SheetBean(sheetIndex, sheetName);
  }

  private Row createRow(int rowIndex) {
    return new RowBean(rowIndex);
  }

  private void createHeaderCellsIfNecessary(Row row, SheetMeta sheetMeta) {
    List<FieldMeta> fieldMetas = sheetMeta.getFieldMetas();

    int lastColumnNum = getLastColumnNum(fieldMetas);
    Map<Integer, FieldMeta> columnIndex2fieldMeta = buildFieldMetaMap(fieldMetas);

    for (int i = 1; i <= lastColumnNum; i++) {
      Cell cell;
      FieldMeta fieldMeta = columnIndex2fieldMeta.get(i);

      if (fieldMeta == null) {

        cell = CellBean.EMPTY_CELL(i);
        row.addCell(cell);
        continue;
      }

      HeaderMeta headerMeta = fieldMeta.getHeaderMeta(row.getIndex());
      if (headerMeta == null) {

        cell = CellBean.EMPTY_CELL(i);
        row.addCell(cell);
        continue;
      }

      cell = new CellBean(i, headerMeta.getValue());
      row.addCell(cell);
    }

  }

  private void createDataCells(Row row, Object object, SheetMeta sheetMeta) {

    List<FieldMeta> fieldMetas = sheetMeta.getFieldMetas();
    int lastColumnNum = getLastColumnNum(fieldMetas);
    Map<Integer, FieldMeta> columnIndex2fieldMeta = buildFieldMetaMap(fieldMetas);

    for (int i = 1; i <= lastColumnNum; i++) {
      Cell cell;
      FieldMeta fieldMeta = columnIndex2fieldMeta.get(i);

      if (fieldMeta == null) {

        cell = CellBean.EMPTY_CELL(i);
        row.addCell(cell);
        continue;
      }

      String value = getFieldStringValue(object, fieldMeta);
      cell = new CellBean(i, value);
      row.addCell(cell);
    }
  }

  private String getFieldStringValue(Object object, FieldMeta fieldMeta) {
    FieldValueExtractor extractor = key2fieldValueExtractor.get(fieldMeta.getName());

    if (extractor != null) {
      return extractor.getStringValue(object, fieldMeta);
    }

    return defaultValueExtractor.getStringValue(object, fieldMeta);
  }

  private int getLastColumnNum(List<FieldMeta> fieldMetas) {
    FieldMeta lastFieldMeta = fieldMetas.get(fieldMetas.size() - 1);
    return lastFieldMeta.getColumnIndex();
  }

  private Map<Integer, FieldMeta> buildFieldMetaMap(List<FieldMeta> fieldMetas) {
    Map<Integer, FieldMeta> columnIndex2fieldMeta = new HashMap<>();
    for (FieldMeta fieldMeta : fieldMetas) {
      columnIndex2fieldMeta.put(fieldMeta.getColumnIndex(), fieldMeta);
    }
    return columnIndex2fieldMeta;
  }
}
