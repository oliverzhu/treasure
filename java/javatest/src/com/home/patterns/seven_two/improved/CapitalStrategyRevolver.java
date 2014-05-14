package com.home.patterns.seven_two.improved;

public class CapitalStrategyRevolver extends CapitalStrategy {
	public double capital(Loan loan)
	{
		return loan.getCommitment() * loan.getUnusedPercentage() * duration(loan) * riskFactor(loan);
	}
}
