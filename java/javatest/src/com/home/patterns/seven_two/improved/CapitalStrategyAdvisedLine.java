package com.home.patterns.seven_two.improved;

public class CapitalStrategyAdvisedLine extends CapitalStrategy {
	public double capital(Loan loan)
	{
		return (loan.outstandingRiskAmount() * duration(loan) * riskFactor(loan))
				+ (loan.unusedRiskAmount() * duration(loan)* unusedRiskFactor(loan));
	}
}
