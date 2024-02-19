package  retcalc

sealed abstract class RetCalcError(val message: String)

object RetCalcError {
  case class MoreExpensesThanIncome(income: Double, expenses: Double)
    extends RetCalcError(s"Expenses: $expenses >= $income. You will never be able to save enough to retire!")
}
