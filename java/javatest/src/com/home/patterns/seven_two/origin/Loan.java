package com.home.patterns.seven_two.origin;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.home.patterns.seven_two.Payment;

/**
 * 计算贷款类型
 * @author jianwen.zhu
 *
 */
public class Loan {
	private Date expiry;
	private Date maturity;
	private Date today;
	private Date start;
	private double outstanding;
	private double commitment;
	private static final long MILLIS_PER_DAY = 0; 
	private static final long DAYS_PER_YEAR = 0; 
	private List<Payment> payments;
	
	public Loan(double commitment,double outstanding,
			Date start,Date expiry,Date maturity,
			int riskRating)
	{
		this.commitment = commitment;
		this.outstanding = outstanding;
		this.start = start;
		this.expiry = expiry;
		this.maturity = maturity;
	}
	
	public double capital()
	{
		if(expiry == null && maturity != null)
		{
			return commitment * duration() * riskFactor();
		}
		if(expiry != null && maturity == null)
		{
			if(getUnusedPercentage() != 0)
			{
				return commitment * getUnusedPercentage() * duration() * riskFactor();
			}else
			{
				return (outstandingRiskAmount() * duration() * riskFactor())
						+ (unusedRiskAmount() * duration() * unusedRiskFactor());
			}
		}
		return 0.0;
	}
	
	private double outstandingRiskAmount()
	{
		return outstanding;
	}
	
	private double unusedRiskAmount()
	{
		return (commitment - outstanding);
	}
	
	public double duration()
	{
		if(expiry == null && maturity == null)
		{
			return weightedAverageDuration();
		}else if(expiry != null && maturity == null)
		{
			return yearsTo(expiry);
		}
		return 0.0;
	}
	
	private double weightedAverageDuration()
	{
		double duration = 0.0;
		double weightedAverage = 0.0;
		double sumOfPayments = 0.0;
		Iterator loadPayments =  payments.iterator();
		while(loadPayments.hasNext())
		{
			Payment payment = (Payment) loadPayments.next();
			sumOfPayments += payment.amount();
			weightedAverage += yearsTo(payment.date()) * payment.amount();
		}
		if(commitment != 0.0)
		{
			duration = weightedAverage/sumOfPayments;
		}
		return duration;
	}
	
	private double yearsTo(Date endDate)
	{
		Date beginDate = (today == null ? start : today);
		return ((endDate.getTime() - beginDate.getTime()) / MILLIS_PER_DAY) / DAYS_PER_YEAR;
	}
	
	private double riskFactor()
	{
		return 0.0;
	}
	
	private double unusedRiskFactor()
	{
		return 0.0;
	}
	
	private double getUnusedPercentage()
	{
		return 0.0;
	}

}
