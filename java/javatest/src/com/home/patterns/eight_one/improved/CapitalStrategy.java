package com.home.patterns.eight_one.improved;

import java.util.Date;


public abstract class CapitalStrategy {
	private static final long MILLIS_PER_DAY = 0; 
	private static final long DAYS_PER_YEAR = 0; 
	
	public double capital(Loan loan)
	{
		return riskAmountFor(loan) * duration(loan) * riskFactor(loan);
	}
	
	public abstract double riskAmountFor(Loan loan);
	
	public double duration(Loan loan)
	{
		return yearsTo(loan.getExpiry(),loan);
	}
	
	double yearsTo(Date endDate,Loan loan)
	{
		Date beginDate = (loan.getToday() == null ? loan.getStart() : loan.getToday());
		return ((endDate.getTime() - beginDate.getTime()) / MILLIS_PER_DAY) / DAYS_PER_YEAR;
	}
	
	protected double riskFactor(Loan loan)
	{
		return 0.0;
	}
	
	protected double unusedRiskFactor(Loan loan)
	{
		return 0.0;
	}

}
