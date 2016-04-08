package at.metalab.m68k.schurli_grinder;

import java.io.StringReader;
import java.util.SortedSet;

import net.sf.ofx4j.domain.data.ResponseEnvelope;
import net.sf.ofx4j.domain.data.ResponseMessage;
import net.sf.ofx4j.domain.data.ResponseMessageSet;
import net.sf.ofx4j.domain.data.banking.BankStatementResponse;
import net.sf.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import net.sf.ofx4j.domain.data.banking.BankingResponseMessageSet;
import net.sf.ofx4j.domain.data.common.Transaction;
import net.sf.ofx4j.domain.data.signon.SignonResponse;
import net.sf.ofx4j.domain.data.signon.SignonResponseMessageSet;
import net.sf.ofx4j.io.AggregateUnmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class HelloOFX {

	public static void main(String[] args) throws Exception {
		AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<ResponseEnvelope>(
				ResponseEnvelope.class);

		String ofx = IOUtils.toString(Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("82821260400_2016001.ofx"));

		System.out.println("OFX message:");
		System.out.println(ofx);
		System.out.println();

		System.out.println("Unmarshalled OFX:");
		System.out.println();

		ResponseEnvelope responseEnvelope = unmarshaller
				.unmarshal(new StringReader(ofx));

		SortedSet<ResponseMessageSet> responseMessageSets = responseEnvelope
				.getMessageSets();
		for (ResponseMessageSet responseMessageSet : responseMessageSets) {
			if (responseMessageSet instanceof SignonResponseMessageSet) {
				SignonResponse signonResponse = ((SignonResponseMessageSet) responseMessageSet)
						.getSignonResponse();

				System.out.println("SignonResponse:");
				System.out.println(ToStringBuilder.reflectionToString(
						signonResponse, ToStringStyle.MULTI_LINE_STYLE));
				System.out.println();
			} else if (responseMessageSet instanceof BankingResponseMessageSet) {
				BankingResponseMessageSet bankingResponseMessageSet = (BankingResponseMessageSet) responseMessageSet;

				System.out.println("BankingResponseMessageSet:");
				System.out.println(ToStringBuilder.reflectionToString(
						bankingResponseMessageSet,
						ToStringStyle.MULTI_LINE_STYLE));
				System.out.println();

				for (ResponseMessage responseMessage : bankingResponseMessageSet
						.getStatementResponses()) {
					if (responseMessage instanceof BankStatementResponseTransaction) {
						BankStatementResponseTransaction bankStatementResponseTransaction = ((BankStatementResponseTransaction) responseMessage);

						System.out.println("BankStatementResponseTransaction:");
						System.out.println(ToStringBuilder.reflectionToString(
								bankStatementResponseTransaction,
								ToStringStyle.MULTI_LINE_STYLE));
						System.out.println();

						BankStatementResponse bankStatementResponse = bankStatementResponseTransaction
								.getMessage();

						System.out.println("BankStatementResponse:");
						System.out.println(ToStringBuilder.reflectionToString(
								bankStatementResponse,
								ToStringStyle.MULTI_LINE_STYLE));
						System.out.println();

						System.out.println("BankStatementResponse.account:");
						System.out.println(ToStringBuilder.reflectionToString(
								bankStatementResponse.getAccount(),
								ToStringStyle.MULTI_LINE_STYLE));
						System.out.println();

						System.out.println("BankStatementResponse.ledgerBalance:");
						System.out.println(ToStringBuilder.reflectionToString(
								bankStatementResponse.getLedgerBalance(),
								ToStringStyle.MULTI_LINE_STYLE));
						System.out.println();

						for (Transaction transaction : bankStatementResponse
								.getTransactionList().getTransactions()) {
							System.out.println("BankStatementResponse.Transaction:");
							System.out.println(ToStringBuilder
									.reflectionToString(transaction,
											ToStringStyle.MULTI_LINE_STYLE));
							System.out.println();
						}
					} else {
						System.out.println("unknown rresponseMessage skipped: "
								+ responseMessage);
					}
				}
			} else {
				System.out.println("unknown responseMessageSet skipped: "
						+ responseMessageSet);
			}
		}
	}
}
