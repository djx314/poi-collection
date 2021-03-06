package net.scalax.cpoi.content

import net.scalax.cpoi.rw.{CellReader, CellWriter}

import org.apache.poi.ss.usermodel.{Cell, CellStyle, CellType}

import scala.util.Try

trait CellContentAbs {

  val poiCell: Option[Cell]

  def isBlank: Boolean =
    poiCell.map(_.getCellType == CellType.BLANK).getOrElse(true)

  def cellType: Option[CellType] =
    Try(poiCell.map(_.getCellType)).toOption.flatten

  def cellStyle: Option[CellStyle] = poiCell.map(_.getCellStyle)

  lazy val rowIndex: Option[Int]    = poiCell.map(_.getRowIndex)
  lazy val columnIndex: Option[Int] = poiCell.map(_.getColumnIndex)

  def genData[T: CellWriter: CellReader]: CellReader.CellReadResult[CellData[T]] = {
    val valueEt = CellReader[T].get(poiCell)
    valueEt.map(s => CellDataImpl(s, List.empty))
  }

  def tryValue[T: CellReader]: CellReader.CellReadResult[T] = {
    CellReader[T].get(poiCell)
  }

}

object CellContentAbs {

  implicit class CellContentOptExtensionMethon(cellOpt: Option[CellContentAbs]) {

    def openAlways: CellContentAbs = {
      cellOpt match {
        case Some(s) => s
        case _ =>
          new CellContentAbs {
            override val poiCell = Option.empty
          }
      }
    }

  }

}
