package com.und.model

class ComparisonOperator {
    enum class OperatorType {
        greaterThan,
        lessThan,
        greaterThanEqualTo,
        lessThanEqualTo,
        notEqualTo,
        between,
        notBetween,
        containsAValue,
        doesNotContainAValue
    }

    companion object {

        fun operate(operatorType: OperatorType, operant1: Int, operant2: Array<Int> = arrayOf()): Boolean = operateGeneric(operatorType, operant1, operant2)

        fun operate(operatorType: OperatorType, operant1: String, operant2: Array<String> = arrayOf()): Boolean = operateGeneric(operatorType, operant1, operant2)

        private fun <T : Comparable<T>> operateGeneric(operatorType: OperatorType, operant1: T, operant2: Array<T>): Boolean {
            return when (operatorType) {
                OperatorType.greaterThan -> operant1 > operant2[0]

                OperatorType.lessThan -> operant1 < operant2[0]

                OperatorType.greaterThanEqualTo -> operant1 >= operant2[0]

                OperatorType.lessThanEqualTo -> operant1 <= operant2[0]

                OperatorType.notEqualTo -> operant1 != operant2[0]

                OperatorType.between -> operant1 >= operant2[0] && operant1 <= operant2[1]

                OperatorType.notBetween -> operant1 < operant2[0] || operant1 > operant2[1]

                OperatorType.containsAValue -> operant2.contains(operant1)

                OperatorType.doesNotContainAValue -> !operant2.contains(operant1)
            }
        }
    }
}



