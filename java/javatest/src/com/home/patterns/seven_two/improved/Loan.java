package com.home.patterns.seven_two.improved;

import java.util.Date;
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
	private List<Payment> payments;
	
	private CapitalStrategy capitalStrategy;
	
	private Loan(double commitment,double outstanding,
			Date start,Date expiry,Date maturity,
			int riskRating,CapitalStrategy capitalStrategy)
	{
		this.commitment = commitment;
		this.outstanding = outstanding;
		this.start = start;
		this.expiry = expiry;
		this.maturity = maturity;
		this.capitalStrategy = capitalStrategy;
	}
	
	public static Loan newTermLoan(double commitment,double outstanding,
			Date start,Date expiry,Date maturity,
			int riskRating)
	{
		return new Loan(commitment, commitment, 
				null, expiry, maturity, 
				riskRating,new CapitalStrategyTermLoan());
	}
	
	public static Loan newRevolver(double commitment,double outstanding,
			Date start,Date expiry,Date maturity,
			int riskRating)
	{
		return new Loan(commitment, 0, 
				start, expiry, null, 
				riskRating,new CapitalStrategyRevolver());
	}
	
	public static Loan newAdvisedLine(double commitment,double outstanding,
			Date start,Date expiry,Date maturity,
			int riskRating)
	{
		if(riskRating > 3) return null;
		Loan advisedLine = 
				new Loan(commitment, 0, 
						start, expiry, null, 
						riskRating, new CapitalStrategyAdvisedLine());
		advisedLine.setUnusedPercentage(0.1);
		return advisedLine;
	}
	
	private void setUnusedPercentage(double d) {
		
	}

	public double capital()
	{
		return capitalStrategy.capital(this);
	}
	
	double outstandingRiskAmount()
	{
		return outstanding;
	}
	
	double unusedRiskAmount()
	{
		return (commitment - outstanding);
	}
	
	 double riskFactor()
	{
		return 0.0;
	}
	
	 double unusedRiskFactor()
	{
		return 0.0;
	}
	
	 double getUnusedPercentage()
	{
		return 0.0;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public Date getMaturity() {
		return maturity;
	}

	public void setMaturity(Date maturity) {
		this.maturity = maturity;
	}

	public double getCommitment() {
		return commitment;
	}

	public void setCommitment(double commitment) {
		this.commitment = commitment;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public Date getToday() {
		return today;
	}

	public void setToday(Date today) {
		this.today = today;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	

}
