package com.home.patterns.eight_one.improved;


public class CapitalStrategyAdvisedLine extends CapitalStrategy {
//	public double capital(Loan loan)
//	{
//		return riskAmountFor(loan) * duration(loan) * riskFactor(loan);
//	}
	
	public double riskAmountFor(Loan loan)
	{
		return loan.getCommitment() * loan.getUnusedPercentage();
	}
}
