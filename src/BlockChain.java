import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class BlockChain {

	private ArrayList<Block> chain = new ArrayList<Block>();

	public static void main(String[] args) {

		String filename;
		Scanner reader = new Scanner(System.in);
		System.out.println("Specify a blockchain file name (without file extension): ");
		filename = reader.nextLine();
		BlockChain blockchain = fromFile(filename + ".txt");
		

		if (!blockchain.validateBlockChain()) {
			System.out.println("Invalid blockchain. Terminating program.");
			System.exit(0);
		}
		System.out.println("Valid blockchain. Proceeding.");

		blockchain.initiateTransaction();

		blockchain.toFile(filename + "_abaet090.txt");

		reader.close();

	}

	private void initiateTransaction() {
		Scanner reader = new Scanner(System.in);

		System.out.println("Do you wish to make a transaction? Y/N ");
		String confirm = reader.nextLine();
		while (!confirm.toUpperCase().equals("Y") && !confirm.toUpperCase().equals("N")) {
			System.out.println("Invalid input. Do you wish to make a transaction? Y/N");
			System.out.println();
			confirm = reader.nextLine();
		}
		if (confirm.toUpperCase().equals("Y")) {
			System.out.println("Enter your username: ");
			String sender = reader.nextLine();
			System.out.println(sender + " has balance of : " + getBalance(sender));

			System.out.println("Enter recipient's username: ");
			String receiver = reader.nextLine();

			int amount;
			while (true) {
				try {
					System.out.println("Enter amount to send.");
					amount = reader.nextInt();
					break;
				} catch (InputMismatchException e) {
					System.out.println("Invalid input. Enter an amount in integers.");
					reader.next();
				}
			}
			boolean success = true;
			try {
				makeTransaction(sender, receiver, amount);
			} catch (IllegalArgumentException e) {
				System.out.println("Transaction failed. You do not have enough funds to process the transaction.");
				success = false;
			}
			if (success) {
				System.out.println("Transaction complete.");
			} 

			initiateTransaction();

			reader.close();
		} else {
			reader.close();
			return;
		}
	}

	public void makeTransaction(String sender, String receiver, int amount) {
		if (amount > getBalance(sender)) {
			throw new IllegalArgumentException();
		}

		int index = chain.size();
		Long timestamp = System.currentTimeMillis();
		Transaction transaction = new Transaction(sender, receiver, amount);
		String nonce = "";
		String previousHash = chain.get(chain.size() - 1).getHash();

		Block block = new Block(index, timestamp, transaction, nonce, previousHash);
		
		//nonce generation
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=[]\\;',./!@#$%^&*()_+{}|:\"<>?";
		int nonceLength;
		Random r = new Random();
		String hash = block.getHash();
		int counter = 0;
		while (!hash.startsWith("00000")) {
			nonce = "";
			nonceLength = r.nextInt(20);
			for (int i = 0; i < nonceLength; i++) {
				nonce = nonce.concat(Character.toString(alphabet.charAt(r.nextInt(alphabet.length()))));
			}
			block.setNonce(nonce);
			block.generateHash();
			hash = block.getHash();
			counter ++;
		}
		System.out.println("Hash trials: " + counter);
		add(block);
		boolean isValid = validateBlockChain();
		if (isValid) {
			System.out.println("Valid blockchain. Proceeding.");
		} else {
			System.out.println("Invalid blockchain. Terminating program.");
		}
	}

	public int getBalance(String username) {
		int balance = 0;
		for (Block block : chain) {
			if (block.getTransaction().getSender().equals(username)) {
				balance -= block.getTransaction().getAmount();
			}
			if (block.getTransaction().getReceiver().equals(username)) {
				balance += block.getTransaction().getAmount();
			}
		}
		return balance;
	}

	public boolean validateBlockChain() {
		String previousHash = "00000";
		int index = 0;
		if (chain.isEmpty()) {
			return false;
		}
		for (Block block : chain) {
			if (!block.getPreviousHash().equals(previousHash)) {
				return false;
			}
			if (block.getIndex() != index) {
				return false;
			}

			// regenerating hash to compare with existing
			String hashInput = block.getTimestamp().toString() + ":" + block.getTransaction().toString() + "."
					+ block.getNonce() + block.getPreviousHash();
			try {
				String hash = Sha1.hash(hashInput);
				if (!block.getHash().equals(hash)) {
					return false;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			previousHash = block.getHash();
			index++;
		}
		return true;
	}

	public void add(Block block) {
		chain.add(block);
	}

	public static BlockChain fromFile(String fileName) {
		BlockChain blockchain = new BlockChain();

		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			String line;
			String previousHash = "00000";

			while ((line = br.readLine()) != null) {
				int index = Integer.parseInt(line);
				Long timestamp = Long.parseLong(br.readLine());
				String sender = br.readLine();
				String receiver = br.readLine();
				int amount = Integer.parseInt(br.readLine());
				String nonce = br.readLine();
				String hash = br.readLine();

				Transaction transaction = new Transaction(sender, receiver, amount);
				Block block = new Block(index, timestamp, transaction, nonce, previousHash);
				block.setHash(hash);

				blockchain.add(block);
				previousHash = block.getHash();
			}
			br.close();
			return blockchain;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return blockchain;
	}

	public void toFile(String filename) {
		try {
			PrintWriter pr = new PrintWriter(filename);

			for (Block block : chain) {
				pr.println(block.getIndex());
				pr.println(block.getTimestamp().getTime());
				pr.println(block.getTransaction().getSender());
				pr.println(block.getTransaction().getReceiver());
				pr.println(block.getTransaction().getAmount());
				pr.println(block.getNonce());
				pr.println(block.getHash());
			}

			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
