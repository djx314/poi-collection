package org.xarcher.cpoi

import org.apache.poi.ss.usermodel.{CellStyle, Workbook, RichTextString, Cell}
import scala.language.implicitConversions
import scala.language.existentials
import java.util.Date

abstract class CCellData[T](val data: T)
case class CFormulaData(override val data: String) extends CCellData(data)
case class CNumericData(override val data: BigDecimal) extends CCellData(data)
case class CDateData(override val data: Date) extends CCellData(data)
case class CRichTextStringData(override val data: Date) extends CCellData(data)
case class CStringData(override val data: String) extends CCellData(data)
case class CBooleanData(override val data: Boolean) extends CCellData(data)
case class CErrorData(override val data: Byte) extends CCellData(data)

trait AbsCellConvert[T] {

  val convert: CCellAbs => Option[T]

}

trait DefaultCellConvert[T] extends AbsCellConvert[T]
trait CustomCellConvert[T] extends AbsCellConvert[T]

trait CCellAbs {

  val poiCell: Option[Cell]

  import scala.util.control.Exception._

  lazy val cellType: Option[Int] = allCatch.opt(poiCell.map(_.getCellType)).flatten

  lazy val formulaValue: Option[String] = allCatch.opt(poiCell.map(_.getCellFormula)).flatten

  lazy val numericValue: Option[BigDecimal] = allCatch.opt(poiCell.map(s => BigDecimal(s.getNumericCellValue))).flatten

  lazy val dateValue: Option[Date] = allCatch.opt(poiCell.map(_.getDateCellValue)).flatten

  lazy val richTextStringValue: Option[RichTextString] = allCatch.opt(poiCell.map(_.getRichStringCellValue)).flatten

  lazy val stringValue: Option[String] = allCatch.opt(poiCell.map(_.getStringCellValue)).flatten

  lazy val booleanValue: Option[Boolean] = allCatch.opt(poiCell.map(_.getBooleanCellValue)).flatten

  lazy val errorValue: Option[Byte] = allCatch.opt(poiCell.map(_.getErrorCellValue)).flatten

  lazy val actualValue: Option[CCellData[_]] = cellType.flatMap {
    case Cell.CELL_TYPE_FORMULA => formulaValue.map(CFormulaData(_))
    case Cell.CELL_TYPE_NUMERIC => numericValue.map(CNumericData(_))
    case Cell.CELL_TYPE_STRING => stringValue.map(CStringData(_))
    case Cell.CELL_TYPE_BOOLEAN => booleanValue.map(CBooleanData(_))
    case Cell.CELL_TYPE_ERROR => errorValue.map(CErrorData(_))
    case Cell.CELL_TYPE_BLANK => None
    case _ => None
  }

  lazy val cellStyle: Option[CellStyle] = poiCell.map(_.getCellStyle)
  lazy val rowIndex: Option[Int] = poiCell.map(_.getRowIndex)
  lazy val columnIndex: Option[Int] = poiCell.map(_.getColumnIndex)

  //use default convert to get the cell value
  def tryValue[T](implicit defaultCellConvert: DefaultCellConvert[T]): Option[T] = defaultCellConvert.convert(this)
  //use custom convert to get the cell value
  def tryCustomValue[T](implicit customCellConvert: CustomCellConvert[T]): Option[T] = customCellConvert.convert(this)
  //use all convert to get the cell value
  def tryAllValue[T](implicit allCellConvert: AbsCellConvert[T]): Option[T] = allCellConvert.convert(this)

}

class CCell(override val poiCell: Option[Cell]) extends CCellAbs {
}

object CCell {

  def apply(poiCell: Option[Cell]): CCellAbs = new CCell(poiCell)
  def apply(poiCell: Cell): CCellAbs = new CCell(Option(poiCell))

}

case class CWorkbook(sheets: Set[CSheet])
case class CSheet(index: Int, name: String, rows: Set[CRow])
case class CRow(index: Int, cells: Set[CCellAbs])

object CPoi {

  def load(workbook: Workbook) = {

    val sheets = for {
      i <- (0 until workbook.getNumberOfSheets).toSet[Int]
      sheet = workbook.getSheetAt(i) if (sheet != null)
    } yield {

        val rows = for {
          k <- (0 to sheet.getLastRowNum).toSet[Int]
          row = sheet.getRow(k) if (row != null)
        } yield {

          val cells = for {
            j <- (0 until row.getLastCellNum).toSet[Int]
            cell = row.getCell(j) if (cell != null)
          } yield {
            CCell(cell)
          }
          CRow(k, cells)

        }
        CSheet(i, sheet.getSheetName, rows)

    }

    CWorkbook(sheets)

  }

}