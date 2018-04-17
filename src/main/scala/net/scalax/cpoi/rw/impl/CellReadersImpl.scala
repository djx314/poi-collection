package net.scalax.cpoi.rw

import java.util.Date

import cats.ApplicativeError
import net.scalax.cpoi.exception._
import net.scalax.cpoi._
import org.apache.poi.ss.usermodel.{Cell, CellType}
import cats.implicits._

trait CellReadersImpl {

  def nonBlankStringReader: CellReader[String] = {

    val m = ApplicativeError[CellReader, CellReaderException]

    stringReader.flatMap { str =>
      val trimStr = str.trim

      val either = if (trimStr.isEmpty) {
        Left(new CellNotExistsException())
      } else {
        Right(trimStr)
      }

      m.fromEither(either)
    }

  }

  def nonEmptyStringReader: CellReader[String] = {

    val m = ApplicativeError[CellReader, CellReaderException]

    stringReader.flatMap { str =>
      val trimStr = str

      val either = if (trimStr.isEmpty) {
        Left(new CellNotExistsException())
      } else {
        Right(trimStr)
      }

      m.fromEither(either)
    }

  }

  def stringReader: CellReader[String] = new CellReader[String] {
    override def get(cell: Option[Cell]): CellReadResult[String] = {
      cell match {
        case Some(c) =>
          c.getCellTypeEnum match {
            case CellType.BLANK =>
              Right(c.getStringCellValue)
            case CellType.STRING =>
              Right(c.getStringCellValue)
            case CellType.NUMERIC =>
              c.setCellType(CellType.STRING)
              Right(c.getStringCellValue)
            //convert boolean to string is meaningless.
            //case CellType.BOOLEAN =>
            //c.setCellType(CellType.STRING)
            //Right(c.getStringCellValue)
            case _ =>
              Left(new ExpectStringCellException())
          }
        case _ =>
          //read null as empty cell
          Right("")
      }
    }
  }

  def doubleReader: CellReader[Double] = new CellReader[Double] {
    override def get(cell: Option[Cell]): CellReadResult[Double] = {
      cell match {
        case Some(c) =>
          c.getCellTypeEnum match {
            case CellType.BLANK =>
              Left(new CellNotExistsException())
            case CellType.NUMERIC =>
              Right(c.getNumericCellValue)
            case _ =>
              Left(new ExpectNumericCellException())
          }
        case _ =>
          Left(new CellNotExistsException())
      }
    }
  }

  def booleanReader: CellReader[Boolean] = new CellReader[Boolean] {
    override def get(cell: Option[Cell]): CellReadResult[Boolean] = {
      cell match {
        case Some(c) =>
          c.getCellTypeEnum match {
            case CellType.BLANK =>
              Left(new CellNotExistsException())
            case CellType.BOOLEAN =>
              Right(c.getBooleanCellValue)
            case _ =>
              Left(new ExpectBooleanCellException())
          }
        case _ =>
          Left(new CellNotExistsException())
      }
    }
  }

  def dateReader: CellReader[Date] = new CellReader[Date] {
    override def get(cell: Option[Cell]): CellReadResult[Date] = {
      cell match {
        case Some(c) =>
          c.getCellTypeEnum match {
            case CellType.BLANK =>
              Left(new CellNotExistsException())
            case CellType.NUMERIC =>
              Option(c.getDateCellValue) match {
                case Some(s) =>
                  Right(s)
                case _ =>
                  Left(new ExpectDateException())
              }
            case _ =>
              Left(new ExpectDateException())
          }
        case _ =>
          Left(new CellNotExistsException())
      }
    }
  }

}
