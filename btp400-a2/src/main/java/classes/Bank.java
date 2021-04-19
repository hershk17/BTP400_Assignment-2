package classes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.rpc.request.node.RequestAccountHistory;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccountHistory.BlockInfo;
import uk.oczadly.karl.jnano.rpc.util.wallet.LocalRpcWalletAccount;
import uk.oczadly.karl.jnano.rpc.util.wallet.WalletActionException;
import uk.oczadly.karl.jnano.util.NetworkConstants;
import uk.oczadly.karl.jnano.util.WalletUtil;
import uk.oczadly.karl.jnano.util.blockproducer.BlockProducer;
import uk.oczadly.karl.jnano.util.blockproducer.BlockProducerSpecification;
import uk.oczadly.karl.jnano.util.blockproducer.StateBlockProducer;
import uk.oczadly.karl.jnano.util.workgen.CPUWorkGenerator;

public class Bank {

	public static final NanoAmount.Denomination UNIT_BAN = new NanoAmount.DenominationImpl("Banano", 29);
	private static HexData seed = new HexData("42C3E0A4A28315819313E5AFF56EAB61C654491771634548B02CD8091BEEEBB7");
	private static final double BAN_NAN_MULT = 10;
	private final String node = "https://kaliumapi.appditto.com/api";
//	private final String node = "174.93.72.172";


	private RpcQueryNode rpc;
	private final String rep = "ban_1fomoz167m7o38gw4rzt7hz67oq6itejpt4yocrfywujbpatd711cjew8gjj";
	private final String prefix = "ban";
	private BlockProducer blockProducer;

	public Bank() {
					try {
						rpc = new RpcQueryNode(new URL(node));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					rpc = new RpcQueryNode(node,7072);
		
					blockProducer = new StateBlockProducer(BlockProducerSpecification.builder().defaultRepresentative(rep)
							// Local work on cpu
							.workGenerator(new CPUWorkGenerator(NetworkConstants.BANANO.getWorkDifficulties())) 
							// Local work on gpu
							// .workGenerator(new OpenCLWorkGenerator(0,0,NetworkConstants.BANANO.getWorkDifficulties())) 
							.addressPrefix(prefix).build());

	}

	// amount you want to send and the account number you want to send to
	public boolean send(int fromAccountNo, int toAccountNo, double amount) {

		HexData skFrom = WalletUtil.deriveKeyFromSeed(seed, fromAccountNo);
		HexData skTo = WalletUtil.deriveKeyFromSeed(seed, toAccountNo);

		// Create account from private key
		LocalRpcWalletAccount account = new LocalRpcWalletAccount(skFrom, // Secret key
				rpc, // Kalium RPC
				blockProducer); // Using our BlockProducer defined above

		// Send funds
		HexData hash = null;

		// Send funds to another account
		System.out.printf("Send block hash: %s%n", hash);
		try {
			hash = account.send(NanoAccount.fromPrivateKey(skTo, prefix),
					NanoAmount.valueOfNano(String.valueOf(amount / BAN_NAN_MULT))).getHash();
		} catch (WalletActionException e) {
			e.printStackTrace();
		}

		return false;
	}

	public List<BlockInfo> getAccountHistory(int accountNo) {
		List<BlockInfo> returnList = null;
		RequestAccountHistory history = new RequestAccountHistory(getPubAddress(accountNo), 17);

		try {
			returnList = this.rpc.processRequest(history).getHistory();
		} catch (IOException | RpcException e) {
			return null;
		}

		return returnList;
	}

	public void updateBalance(int accountNo) {
		HexData privateKey = WalletUtil.deriveKeyFromSeed(seed, accountNo);

		LocalRpcWalletAccount account = new LocalRpcWalletAccount(privateKey, // Private key
				rpc, // Kalium RPC
				blockProducer); // Using our BlockProducer defined above

		try {
			account.receiveAll();
		} catch (WalletActionException e) {
			e.printStackTrace();
		}
	}

	public BigDecimal getBalance(int accountNo) {
		try {
			HexData skFrom = WalletUtil.deriveKeyFromSeed(seed, accountNo);
			LocalRpcWalletAccount wallet = new LocalRpcWalletAccount(skFrom, rpc, blockProducer);

			wallet.receiveAll();
			return wallet.getBalance().getAs(Bank.UNIT_BAN);
		} catch (WalletActionException e) {
			e.printStackTrace();
		}

		return new BigDecimal(-1);
	}

	public String getPubAddress(int accountNo) {
		return NanoAccount.fromPrivateKey(WalletUtil.deriveKeyFromSeed(seed, accountNo), prefix).toString();
	}

}
