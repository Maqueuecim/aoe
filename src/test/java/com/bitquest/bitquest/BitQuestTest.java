package com.bitquest.bitquest;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class BitQuestTest {

  @Test
  public void testWallet()
      throws Exception {
    // generate new mnemonic code
    MnemonicCode mnemonicCode = new MnemonicCode();
    // Create some random entropy.
    SecureRandom random = new SecureRandom();
    byte[] entropy = random.generateSeed(20);

    List<String> mnemonicWords = mnemonicCode.toMnemonic(entropy);
    System.out.println(mnemonicWords);
    DeterministicSeed seed = new DeterministicSeed(entropy, "", 0);
    System.out.println(seed.toHexString());
    System.out.println(seed.getMnemonicCode());
    // Recovering a wallet with Mnemonic code
    List<String> backupWords = Arrays
        .asList("enroll", "cover", "practice", "bullet", "dad", "surround", "install", "match",
            "fault", "dragon", "innocent", "blame", "brown", "bind", "alcohol");
    seed = new DeterministicSeed(backupWords, null, "", 0);
    System.out.println(seed.toHexString());
    System.out.println(seed.getMnemonicCode());
    NetworkParameters network = TestNet3Params.get();
    DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
    String xpub = masterKey.serializePubB58(network);
    System.out.println(xpub);
    Node node = new Node();
    // Test Node
    System.out.println(node.getBlockchainInfo());
    // Test wallet
    Wallet alice = new Wallet(node, "alice");
    int minimumConfirmations = 3;
    System.out.println(alice.balance(minimumConfirmations));
    System.out.println(alice.address());
    System.out.println(alice.addressUrl());
    Wallet bob = new Wallet(node, "bob");
    System.out.println(bob.balance(minimumConfirmations));
    System.out.println(bob.address());
    System.out.println(bob.addressUrl());
    Double amount = 10.0;
    if (alice.balance(minimumConfirmations) > amount) {
      alice.send(bob.address(), amount);
    } else if (bob.balance(minimumConfirmations) > amount) {
      bob.send(alice.address(), amount);
    }
  }
  
  @Test
  public void testLandOwnership() throws Exception {
    Land land = new Land();
    land.runMigrations();
    String uuid = "63d9719e-571a-4963-ac11-2f1233393580";
    int x = 0;
    int z = 0;
    LandChunk chunk = land.chunk(x, z);
    if (chunk == null) {
      land.claim(x, z, uuid, "Land");
    } else {
      System.out.println(chunk.name);
      System.out.println(chunk.permission);
      System.out.println(chunk.owner);
      if (chunk.permission == ChunkPermission.PRIVATE) {
        land.changePermission(x, z, ChunkPermission.CLAN);
      } else if (chunk.permission == ChunkPermission.CLAN) {
        land.changePermission(x, z, ChunkPermission.PUBLIC);
      } else {
        land.changePermission(x, z, ChunkPermission.PRIVATE);
      }
      if (chunk.name.equals("Good Land")) {
        land.rename(x, z, "Bad Land");
      } else {
        land.rename(x, z, "Good Land");
      }
    }
  }
}
