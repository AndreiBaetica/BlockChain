import java.io.UnsupportedEncodingException;

public class Block {

	private int index;
	private java.sql.Timestamp timestamp;
	private Transaction transaction;
	private String nonce;
	private String previousHash;
	private String hash;
	
	public Block(int index, Long timestamp, Transaction transaction, String nonce, String previousHash) {
		this.index = index;
		this.timestamp = new java.sql.Timestamp(timestamp);
		this.transaction = transaction;
		this.nonce = nonce;
		this.previousHash = previousHash;
		this.generateHash();
	}
	
	public void generateHash() {
		try {
			hash = Sha1.hash(this.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public int getIndex() {
		return index;
	}

	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public String getNonce() {
		return nonce;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getHash() {
		return hash;
	}
	
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}

	public String toString() {
		return timestamp.toString() + ":" + transaction.toString() + "." + nonce + previousHash;
	}
}
