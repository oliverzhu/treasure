package com.home.patterns.eight_one.improved;

public class CapitalStrategyRevolver extends CapitalStrategy {
	/**
	 * 用到了父类的方法，但是不完全用到父类中的方法
	 */
	public double capital(Loan loan)
	{
		return super.capital(loan)
				+ (loan.unusedRiskAmount() * duration(loan)* unusedRiskFactor(loan));
	}

	@Override
	public double riskAmountFor(Loan loan) {
		return loan.outstandingRiskAmount();
	}
}
