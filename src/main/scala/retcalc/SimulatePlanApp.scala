package retcalc

import cats.data.{NonEmptyList, Validated}
import cats.implicits._
import retcalc.RetCalcError.{InvalidArgument, InvalidNumber, RetCalcResult}

object SimulatePlanApp extends App {
  println(strMain(args))

  def parseInt(name: String, value: String): RetCalcResult[Int] =
    Validated
      .catchOnly[NumberFormatException](value.toInt)
      .leftMap(_ => NonEmptyList.of(InvalidNumber(name, value)))

  def parseFromUntil(fromUntil: String): RetCalcResult[(String, String)] = {
    val array = fromUntil.split(",")
    if (array.length != 2)
      InvalidArgument(
        name = "fromUntil", value = fromUntil, expectedFormat = "from,until"
      ).invalidNel
    else (array(0), array(1)).validNel
  }

  def parseParams(args: Array[String]): RetCalcResult[RetCalcParams] =
    (
      parseInt("nbOfYearsRetired", args(2)),
      parseInt("netIncome", args(3)),
      parseInt("currentExpenses", args(4)),
      parseInt("initialCapital", args(5))
    ).mapN { case (nbOfYearsRetired, netIncome, currentExpenses, initialCapital) =>
      RetCalcParams(
        nbOfMonthsInRetirement = nbOfYearsRetired * 12,
        netIncome = netIncome,
        currentExpenses = currentExpenses,
        initialCapital = initialCapital)
    }

  def strSimulatePlan(returns: Returns, nbOfYearsSaving: Int, params: RetCalcParams)
  : RetCalcResult[String] = {
    RetCalc.simulatePlan(
      returns = returns,
      params = params,
      nbOfMonthsSavings = nbOfYearsSaving * 12
    ).map {
      case (capitalAtRetirement, capitalAfterDeath) =>
        val nbOfYearsInRetirement = params.nbOfMonthsInRetirement / 12
        s"""
           |Capital after $nbOfYearsSaving years of savings:    ${capitalAtRetirement.round}
           |Capital after $nbOfYearsInRetirement years in retirement: ${capitalAfterDeath.round}
           |""".stripMargin
    }.toValidatedNel
  }
  
  def strMain(args: Array[String]): String = {
    val (from +: until +: Nil) = args(0).split(",").toList
    val nbOfYearsSaving = args(1).toInt
    val nbOfYearsInRetirement = args(2).toInt

    val allReturns = Returns.fromEquityAndInflationData(
      equities = EquityData.fromResource("sp500.tsv"),
      inflations = InflationData.fromResource("cpi.tsv"))
    RetCalc.simulatePlan(
      returns = allReturns.fromUntil(from, until),
      params = RetCalcParams(
        nbOfMonthsInRetirement = nbOfYearsInRetirement * 12,
        netIncome = args(3).toInt,
        currentExpenses = args(4).toInt,
        initialCapital = args(5).toInt),
      nbOfMonthsSavings = nbOfYearsSaving * 12
    ) match {
      case Right((capitalAtRetirement, capitalAfterDeath)) =>
        s"""
           |Capital after $nbOfYearsSaving years of savings: ${capitalAtRetirement.round}
           |Capital after $nbOfYearsInRetirement years in retirement: ${capitalAfterDeath.round}
        """.stripMargin
      case Left(err) => err.message
    }
  }
}