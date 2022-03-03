package finance.omm.score.core.reward.db;

import static finance.omm.utils.math.MathUtils.ICX;
import static finance.omm.utils.math.MathUtils.exaMultiply;

import finance.omm.libs.structs.WeightStruct;
import finance.omm.score.core.reward.exception.RewardException;
import finance.omm.score.core.reward.model.Asset;
import finance.omm.utils.constants.TimeConstants;
import java.math.BigInteger;
import java.util.Map;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import scorex.util.HashMap;

public class AssetWeightDB {

    private final static String TAG = "Asset Weight DB";

    //type => checkPointCounter => AssetAddress => value;
    private final BranchDB<String, BranchDB<Integer, DictDB<Address, BigInteger>>> wCheckpoint;
    //type => checkPointCounter => value;
    private final BranchDB<String, DictDB<Integer, BigInteger>> totalCheckpoint;
    //type => checkPointCounter => timestamp;
    private final BranchDB<String, DictDB<Integer, BigInteger>> tCheckpoint;

    private final DictDB<String, Integer> checkpointCounter;

    private final DictDB<Address, Asset> assets;

    private final BranchDB<String, ArrayDB<Address>> assetMap;


    public AssetWeightDB(String key) {
        this.checkpointCounter = Context.newDictDB(key + "CheckpointCounter", Integer.class);
        this.totalCheckpoint = Context.newBranchDB(key + "Total", BigInteger.class);
        this.wCheckpoint = Context.newBranchDB(key + "WeightCheckpoint", BigInteger.class);
        this.tCheckpoint = Context.newBranchDB(key + "TimestampCheckpoint", BigInteger.class);

        this.assets = Context.newDictDB(key + "Assets", Asset.class);
        this.assetMap = Context.newBranchDB(key + "TypeAssetMap", Address.class);
    }


    public void addAsset(String type, Address address, String name) {
        if (this.assets.get(address) != null) {
            throw RewardException.invalidAsset(address + " already exists");
        }

        Asset asset = new Asset(address, type);
        asset.name = name;
        assets.set(address, asset);
        assetMap.at(type).add(address);
    }


    public void setWeights(String type, WeightStruct[] weights, BigInteger timestamp) {
        Integer checkpointCounter = this.checkpointCounter.getOrDefault(type, 0);
        BigInteger latestCheckpoint = this.tCheckpoint.at(type).getOrDefault(checkpointCounter, BigInteger.ZERO);
        int compareValue = latestCheckpoint.compareTo(timestamp);
        if (compareValue > 0) {
            throw RewardException.unknown("latest " + latestCheckpoint + " checkpoint exists than " + timestamp);
        }

        BigInteger total = this.totalCheckpoint.at(type).getOrDefault(checkpointCounter, BigInteger.ZERO);
        if (compareValue == 0) {
            setWeights(type, total, weights, checkpointCounter);
        } else {
            DictDB<Address, BigInteger> dictDB = this.wCheckpoint.at(type).at(checkpointCounter);
            Integer counter = checkpointCounter + 1;
            DictDB<Address, BigInteger> newCheckpoint = this.wCheckpoint.at(type).at(counter);

            ArrayDB<Address> addresses = this.assetMap.at(type);

            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                BigInteger value = dictDB.getOrDefault(address, BigInteger.ZERO);
                newCheckpoint.set(address, value);
            }
            setWeights(type, total, weights, counter);
            this.tCheckpoint.at(type).set(counter, timestamp);
            this.checkpointCounter.set(type, counter);
        }
    }

    private void setWeights(String type, BigInteger total, WeightStruct[] weights, Integer counter) {
        DictDB<Address, BigInteger> dictDB = this.wCheckpoint.at(type).at(counter);
        for (WeightStruct tw : weights) {
            Asset asset = this.assets.get(tw.address);
            if (asset == null) {
                throw RewardException.unknown(msg("Invalid asset :: " + tw.address));
            }
            BigInteger prevWeight = dictDB.getOrDefault(tw.address, BigInteger.ZERO);
            total = total.subtract(prevWeight).add(tw.weight);
            dictDB.set(tw.address, tw.weight);
        }
        if (!total.equals(ICX)) {
            System.out.println("total = " + total);
            throw RewardException.invalidTotalPercentage();
        }
        this.totalCheckpoint.at(type).set(counter, total);
    }


    private int searchCheckpoint(String type, int checkpoint, BigInteger timestamp) {
        int lower = 0, upper = checkpoint;
        while (upper > lower) {
            int mid = (upper + lower + 1) / 2;
            BigInteger midTimestamp = this.tCheckpoint.at(type).get(mid);
            int value = midTimestamp.compareTo(timestamp);
            if (value < 0) {
                lower = mid;
            } else if (value > 0) {
                upper = mid - 1;
            } else {
                return mid;
            }
        }
        return lower;
    }

    public Map<String, BigInteger> getWeight(Address assetAddress) {
        BigInteger timestamp = TimeConstants.getBlockTimestamp();
        return getWeight(assetAddress, timestamp);
    }

    public Map<String, BigInteger> getWeight(Address assetAddress, BigInteger timestamp) {
        Asset asset = getAsset(assetAddress);
        return getWeight(asset, timestamp);
    }

    public Map<String, BigInteger> getWeight(Asset asset, BigInteger timestamp) {
        String typeId = asset.type;
        Integer checkpointCounter = this.checkpointCounter.getOrDefault(typeId, 0);
        int index = searchCheckpoint(typeId, checkpointCounter, timestamp);
        return Map.of(
                "index", BigInteger.valueOf(index),
                "value", this.wCheckpoint.at(typeId)
                        .at(index)
                        .getOrDefault(asset.address,
                                BigInteger.ZERO),
                "timestamp", this.tCheckpoint.at(typeId)
                        .get(index)
        );
    }

    public BigInteger getTotal(String typeId) {
        BigInteger timestamp = TimeConstants.getBlockTimestamp();
        return getTotal(typeId, timestamp);
    }

    public BigInteger getTotal(String typeId, BigInteger timestamp) {
        int index = searchCheckpoint(typeId, this.checkpointCounter.get(typeId), timestamp);
        return this.totalCheckpoint.at(typeId).getOrDefault(index, BigInteger.ZERO);
    }

    public static String msg(String message) {
        return TAG + " :: " + message;
    }

    public Asset getAsset(Address assetAddress) {
        return this.assets.get(assetAddress);
    }

    public BigInteger getTimestamp(String typeId, int index) {
        return this.tCheckpoint.at(typeId).get(index);
    }

    public Integer getCheckpointCount(String typeId) {
        return checkpointCounter.getOrDefault(typeId, 0);
    }

    public Map<Address, BigInteger> getWeightByTimestamp(String type, BigInteger timestamp) {
        DictDB<Address, BigInteger> dictDB = getCheckpoint(type, timestamp);
        Map<Address, BigInteger> result = new HashMap<>();

        ArrayDB<Address> addresses = this.assetMap.at(type);

        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            BigInteger value = dictDB.getOrDefault(address, BigInteger.ZERO);
            result.put(address, value);
        }

        return result;
    }

    private DictDB<Address, BigInteger> getCheckpoint(String type, BigInteger timestamp) {
        int index = searchCheckpoint(type, this.checkpointCounter.get(type), timestamp);
        return this.wCheckpoint.at(type).at(index);
    }

    public Map<String, BigInteger> getAggregatedWeight(BigInteger weight, String type, BigInteger timestamp) {
        DictDB<Address, BigInteger> dictDB = getCheckpoint(type, timestamp);
        BigInteger total = BigInteger.ZERO;
        Map<String, BigInteger> result = new HashMap<>();

        ArrayDB<Address> addresses = this.assetMap.at(type);

        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            Asset asset = this.assets.get(address);
            if (asset != null) {
                String name = asset.name;
                BigInteger value = exaMultiply(dictDB.getOrDefault(asset.address, BigInteger.ZERO), weight);
                result.put(name, value);
                total = total.add(value);
            }
        }
        result.put("total", total);
        return result;
    }

}
