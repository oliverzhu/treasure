package com.home.patterns.eight_one.improved;

import java.util.Iterator;

import com.home.patterns.seven_two.Payment;


public class CapitalStrategyTermLoan extends CapitalStrategy {
	public double capital(Loan loan)
	{
		return riskAmountFor(loan) * duration(loan) * riskFactor(loan);
	}
	
	public double duration(Loan loan)
	{
		return weightedAverageDuration(loan);
	}
	
	protected double weightedAverageDuration(Loan loan)
	{
		double duration = 0.0;
		double weightedAverage = 0.0;
		double sumOfPayments = 0.0;
		Iterator loadPayments =  loan.getPayments().iterator();
		while(loadPayments.hasNext())
		{
			Payment payment = (Payment) loadPayments.next();
			sumOfPayments += payment.amount();
			weightedAverage += yearsTo(payment.date(),loan) * payment.amount();
		}
		if(loan.getCommitment() != 0.0)
		{
			duration = weightedAverage/sumOfPayments;
		}
		return duration;
	}
	
	public double riskAmountFor(Loan loan)
	{
		return loan.getCommitment();
	}
}
