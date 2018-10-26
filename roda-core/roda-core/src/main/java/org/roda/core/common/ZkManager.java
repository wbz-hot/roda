package org.roda.core.common;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZkManager {
  private static ZooKeeper zookeeper;
  private static ZkConnection zookeeperConnection;

  public ZkManager(String connectString) {
    initialize(connectString);
  }

  private void initialize(String connectString) {
    zookeeperConnection = new ZkConnection();
    try {
      zookeeper = zookeeperConnection.connect(connectString);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void closeConnection() {
    try {
      zookeeperConnection.close();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public ZooKeeper getZookeeper() {
    return zookeeper;
  }

  public void create(String path, byte[] data) throws KeeperException, InterruptedException {
    zookeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
  }

  public String getZNodeData(String path) throws KeeperException, InterruptedException {

    byte[] b = null;
    b = zookeeper.getData(path, null, null);
    return new String(b);
  }

  public void update(String path, byte[] data) throws KeeperException, InterruptedException {
    Stat exists = zookeeper.exists(path, true);
    int version = exists.getVersion();
    zookeeper.setData(path, data, version);
  }

  public boolean exists(String path) {
    try {
      return zookeeper.exists(path, false) != null;
    } catch (KeeperException | InterruptedException e) {
      return false;
    }
  }
}
