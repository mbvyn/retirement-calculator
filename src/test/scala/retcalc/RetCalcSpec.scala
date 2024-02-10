package retcalc

import org.scalactic.{Equality, TolerantNumerics, TypeCheckedTripleEquals}
import org.scalatest.{Matchers, WordSpec}

class RetCalcSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {
  implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.0001)

  "RetCalc.futureCapital" should {
    "calculate the amount of savings I will have in n months" in {
      // Excel =-FV(0.04/12,25*12,1000,10000,0)
      val actual = RetCalc.futureCapital(FixedReturns(0.04), nbOfMonths = 25 * 12,
        netIncome = 3000, currentExpenses = 2000, initialCapital = 10000)
      val expected = 541267.1990
      actual should ===(expected)
    }

    "calculate how much savings will be left after having taken a pension for n months" in {
      val actual = RetCalc.futureCapital(FixedReturns(0.04), nbOfMonths = 40 * 12,
        netIncome = 0, currentExpenses = 2000, initialCapital = 541267.198962)
      val expected = 309867.5316
      actual should ===(expected)
    }
  }

  val params = RetCalcParams(
    nbOfMonthsInRetirement = 40 * 12,
    netIncome = 3000,
    currentExpenses = 2000,
    initialCapital = 10000)

  "RetCalc.simulatePlan" should {
    "calculate the capital at retirement and the capital after death" in {
      val (capitalAtRetirement, capitalAfterDeath) = RetCalc.simulatePlan(
        returns = FixedReturns(0.04), params, nbOfMonthsSavings = 25 * 12)

      capitalAtRetirement should ===(541267.1990)
      capitalAfterDeath should ===(309867.5316)
    }

    "use different returns for capitalisation and drawdown" in {
      val nbOfMonthsSavings = 25 * 12
      val returns = VariableReturns(
        Vector.tabulate(nbOfMonthsSavings + params.nbOfMonthsInRetirement)(i =>
          if (i < nbOfMonthsSavings)
            VariableReturn(i.toString, 0.04 / 12)
          else
            VariableReturn(i.toString, 0.03 / 12)))
      val (capitalAtRetirement, capitalAfterDeath) =
        RetCalc.simulatePlan(returns, params, nbOfMonthsSavings)
      // Excel: =-FV(0.04/12, 25*12, 1000, 10000)
      capitalAtRetirement should ===(541267.1990)
      // Excel: =-FV(0.03/12, 40*12, -2000, 541267.20)
      capitalAfterDeath should ===(-57737.7227)
    }

  }

  "RetCalc.nbOfMonthsSaving" should {
    "calculate how long I need to save before I can retire" in {
      val actual = RetCalc.nbOfMonthsSaving(params, FixedReturns(0.04))
      val expected = 23 * 12 + 1
      actual should ===(Some(expected))
    }

    "not crash if the resulting nbOfMonths is very high" in {
      val actual = RetCalc.nbOfMonthsSaving(
        params = RetCalcParams(
          nbOfMonthsInRetirement = 40 * 12,
          netIncome = 3000, currentExpenses = 2999, initialCapital = 0),
        returns = FixedReturns(0.01))
      val expected = 8280
      actual should ===(Some(expected))
    }

    "not loop forever if I enter bad parameters" in {
      val actual = RetCalc.nbOfMonthsSaving(params.copy(netIncome = 1000), FixedReturns(0.04))
      actual should ===(None)
    }
  }

  "VariableReturns.fromUntil" should {
    "keep only a window of the returns" in {
      val variableReturns = VariableReturns(Vector.tabulate(12) { i =>
        val d = (i + 1).toDouble
        VariableReturn(f"2017.$d%02.0f", d)
      })

      variableReturns.fromUntil("2017.07", "2017.09").returns should ===(
        Vector(
          VariableReturn("2017.07", 7.0),
          VariableReturn("2017.08", 8.0),
        )
      )

      variableReturns.fromUntil("2017.10", "2018.01").returns should ===(
        Vector(
          VariableReturn("2017.10", 10.0),
          VariableReturn("2017.11", 11.0),
          VariableReturn("2017.12", 12.0)
        )
      )
    }
  }

}
