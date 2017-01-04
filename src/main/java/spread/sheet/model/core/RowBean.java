package spread.sheet.model.core;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hanwen on 15-12-16.
 */
public class RowBean implements Row {

  private int index;

  private List<Cell> cells = new ArrayList<>();

  private Sheet sheet;

  public RowBean(int index) {
    this.index = index;
  }

  public RowBean(org.apache.poi.ss.usermodel.Row row) {
    this.index = row.getRowNum() + 1;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public List<Cell> getCells() {
    Collections.sort(cells);
    return cells;
  }

  @Override
  public int sizeOfCells() {
    return cells.size();
  }

  @Override
  public Cell getCell(int index) {
    if (index < 1 || index > sizeOfCells()) {
      throw new IllegalArgumentException("index out of bounds");
    }
    Collections.sort(cells);
    return cells.get(index - 1);
  }

  @Override
  public boolean addCell(Cell cell) {
    ((CellBean) cell).setRow(this);
    return cells.add(cell);
  }

  @Override
  public Cell getFirstCell() {
    if (sizeOfCells() == 0) {
      return null;
    }
    return getCell(1);
  }

  @Override
  public Cell getLastCell() {
    if (sizeOfCells() == 0) {
      return null;
    }
    return getCell(sizeOfCells());
  }

  @Override
  public Sheet getSheet() {
    return sheet;
  }

  void setSheet(Sheet sheet) {
    this.sheet = sheet;
  }

  @Override
  public int compareTo(Row o) {
    return new CompareToBuilder().append(index, o.getIndex()).toComparison();
  }
}
